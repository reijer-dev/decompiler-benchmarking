package nl.ou.debm.common.feature2;

import java.util.ArrayList;
import java.util.Arrays;

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
                sb.append(casted.T + "[" + casted.size + "]");
            }
            else if (this instanceof VariableLengthArray casted) {
                sb.append(casted.T + "[]");
            }
            else if (this instanceof Pointer casted) {
                sb.append(casted.T + "*");
            }

            return sb.toString();
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

    static ArrayList<String> builtins = new ArrayList<>(Arrays.asList(
        "void",
        "char",
        "short",
        "int",
        "long",
        "float",
        "double",
        "signed",
        "unsigned",
        "_Bool"
    ));
    public static final class Builtin extends Type
    {
        public String baseSpecifier;
        public boolean unsigned;
        public boolean long_;
        public boolean long_long;

        public Builtin(String code)
        {
            var specifiers = code.split(" ");
            // I also check if there are 0 specifiers because something like " ".split(" ") returns 0 elements!
            if (specifiers.length == 0)
                throw new RuntimeException("invalid specifier for builtin type: " + code);
            // default values that are changed if needed:
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
                    if ( ! builtins.contains(specifier)) {
                        throw new RuntimeException("invalid specifier for builtin type: " + code);
                    }
                    baseSpecifier = specifier;
                }
            }
        }
    }

    public static final class Struct extends Type
    {
        ArrayList<Type> members = new ArrayList<>();
    }

    public static final class Union extends Type
    {
        ArrayList<Type> members = new ArrayList<>();
    }

    public static final class Array extends Type
    {
        Type T;
        int size;
        public Array(Type T_, int size_) {T=T_;size=size_;}
    }

    // Variable length array
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
