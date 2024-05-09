package nl.ou.debm.common.feature2;

import nl.ou.debm.common.EArchitecture;

import java.util.ArrayList;

// Namespace class that defines an internal representation of C data types, to be used by the assessor
//
// The idea of a type's "normal form" is that type names are replaced by their definition until no names remain. Eventually, all types are composed of the builtin types such as int, float, void* etc. The desired effect is that two different notations for what is essentially the same type in C, are represented the same way in the normal form. Consider, for example, the type specifiers "struct { name n1; }" and "struct { int i; }". These two specifiers denote the same type (in terms of contents) if name refers to int, which is the case if there is a typedef in scope like "typedef int name;". Both struct notations are represented in normal form as just "a struct that contains an int". Note that in C, the language considers all struct specifiers to be unique types, even if the contents are the same. For the purpose of testing decompilers, I'm only interested in the contents.
//
// There is one problematic case which cannot be represented in this normal form, which is pointers to an incomplete type. A type is considered "incomplete" (both in the C language and in this class) if it is used before it is defined. An example is "struct S { int x,y; S* next; }". Such a struct, that contain a pointer to an instance of its own type, is useful to represent something like a node in a graph or an element of a linked list. Replacing the name S inside the struct with its normal form would lead to infinite recursion, so this situation cannot be perfectly represented in the normal form. As a workaround I replace all pointers to an incomplete type with "void*" (this is done by Parsing.parseCompletely). A struct that refers to itself is just one example of that. Consider also:
// struct S1 {
//     struct S2 {
//         S1* ptr; //does not refer to itself, but S1 is still incomplete here
//     } s2;
// };
//
// struct Incomplete; //struct is declared but not defined
// struct S1 {
//     Incomplete* x;
// }
// Note that C does not allow creating instances of incomplete type (which also wouldn't made sense), only pointers, so those pointers can always be converted to "void*".
//
// In addition to the type representation classes there is also an "Unprocessed" class to store type specifier code that is not (yet) processed. I use this to avoid having to interpret literally every data type in the decompiled code, which is unnecessary. The DataStructureCVisitor uses this possibility to parse lazily. Only when it is really needed is a type parsed completely.
//
public class NormalForm
{
    // Like NameInfo, this is supposed to be a union like type.
    public static sealed class Type
        permits Unprocessed, Builtin, Struct, Union, Array, VariableLengthArray, Pointer
    {
        public String toString()
        {
            var sb = new StringBuilder();

            if (this instanceof Unprocessed casted) {
                sb.append("[[Unprocessed "+casted.specifier+"]]");
            }
            else if (this instanceof Builtin casted) {
                sb.append((casted.unsigned ? "unsigned " : ""));
                sb.append(casted.baseSpecifier);
            }
            else if (this instanceof Struct casted) {
                sb.append("struct{ ");
                var memberStrings = new ArrayList<String>();
                for (var member : casted.members) {
                    memberStrings.add(member.toString());
                }
                sb.append(String.join(", ", memberStrings));
                if (memberStrings.size() > 0) sb.append(' ');
                sb.append("}");
            }
            else if (this instanceof Array casted) {
                sb.append(casted.T + "[" + casted.elements + "]");
            }
            else if (this instanceof VariableLengthArray casted) {
                sb.append(casted.T + "[]");
            }
            else if (this instanceof Pointer casted) {
                sb.append(casted.T + "*");
            }

            return sb.toString();
        }

        // size in bits
        public int size(EArchitecture arch)
        {
            if (this instanceof Builtin x) {
                if (x.isIntegral()) {
                    if (x.baseSpecifier.equals("char"))       return 8;
                    else if (x.baseSpecifier.equals("short")) return arch.shortBits();
                    else if (x.long_long)                     return arch.longlongBits();
                    else if (x.long_)                         return arch.longBits();
                    else                                      return arch.intBits();
                }
                else if (x.baseSpecifier.equals("float"))     return 32;
                else if (x.baseSpecifier.equals("double"))    return 64;
                // todo there are also long doubles, and are float and double sizes standard?
            }
            else if (this instanceof Pointer x) {
                return arch.pointerBits();
            }
            else if (this instanceof Array x) {
                return x.elements * x.T.size(arch);
            }
            else if (this instanceof Struct x) {
                int total = 0;
                for (var memberType : x.members) {
                    total += memberType.size(arch);
                }
                return total;
            }
            else if (this instanceof Union x) {
                int max = 0;
                for (var memberType : x.members) {
                    var size = memberType.size(arch);
                    if (size > max) max = size;
                }
                return max;
            }
            else assert false : "no size implemented";
            return 0;
        }
    }

    // holds code that specifies a base type in normal form
    // Base type means no arrays or pointers. That's because I don't support abstract declarators.
    public static final class Unprocessed extends Type
    {
        String specifier;

        public Unprocessed(String code) {
            // bring the code to normal form. This also checks if the code can be parsed.
            var parser = Parsing.makeParser(code);
            var declarationSpecifiers = parser.declarationSpecifiers();
            Parsing.assertNoErrors(parser);
            specifier = Parsing.normalizedCode(declarationSpecifiers);
        }
    }

    public static final class Builtin extends Type
    {
        public String baseSpecifier;
        public boolean unsigned;
        public boolean long_;
        public boolean long_long;

        public Builtin(String code) {
            this(code, new NameInfo());
        }

        public Builtin(String code, NameInfo nameInfo)
        {
            var specifiers = code.split(" ");
            // I also check if there are 0 specifiers because something like " ".split(" ") returns 0 elements!
            if (specifiers.length == 0)
                throw new RuntimeException("invalid specifier for builtin type: " + code);

            baseSpecifier = "int";
            unsigned = false;
            long_ = false;
            long_long = false;
            for (var specifier : specifiers)
            {
                if (specifier.equals("signed")) {
                    //ignored
                }
                else if (specifier.equals("unsigned")) {
                    unsigned = true;
                }
                else if (specifier.equals("long")) {
                    if (long_) long_long = true;
                    long_ = true;
                }
                else {
                    if (Parsing.builtinTypeSpecifiers.contains(specifier)) {
                        baseSpecifier = specifier;
                    }
                    else {
                        var typeInfo = nameInfo.getTypeInfo(specifier);
                        var T = DataStructureCVisitor.parseCompletely(typeInfo.T, nameInfo);
                        if (T instanceof Builtin asBuiltin) {
                            baseSpecifier = asBuiltin.baseSpecifier;
                        }
                        else {
                            throw new RuntimeException("invalid specifier for builtin type: " + code);
                        }
                    }
                }
            }
            if (baseSpecifier.equals("short")) {
                assert ! long_long;
                assert ! long_;
            }
        }

        public boolean isIntegral() {
            return ! (
                baseSpecifier.equals("float")
                || baseSpecifier.equals("double"));
        }
    }

    public static final class Struct extends Type
    {
        ArrayList<Type> members = new ArrayList<>();
        Type atOffset(int bits, EArchitecture arch) {
            for (var member : members) {
                if (bits == 0)
                    return member;
                else
                    bits -= member.size(arch);
            }
            return null;
        }
    }

    public static final class Union extends Type
    {
        ArrayList<Type> members = new ArrayList<>();
    }

    public static final class Array extends Type
    {
        Type T;
        int elements;
        public Array(Type T_, int elements_) {T=T_; elements=elements_;}
    }

    // I already implemented this but it turns out that VLA's are discouraged by many people, are not in C++ and have even been made optional in the 2011 C-standard. Therefore I will not test VLA recognition by decompilers.
    public static final class VariableLengthArray extends Type
    {
        Type T;
        public VariableLengthArray(Type T_) {T=T_;}
    }

    public static final class Pointer extends Type
    {
        Type T;
        public Pointer(Type T_) {T=T_;}
    }
}
