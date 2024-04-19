package nl.ou.debm.common.feature2;

import java.util.ArrayList;

public class NormalForm
{
    // Like NameInfo, this is supposed to be a union like type.
    public static sealed class Type
        permits Unparsed, Builtin, Struct, Array, VariableLengthArray, Pointer
    {
        public String toString()
        {
            var sb = new StringBuilder();

            if (this instanceof Unparsed casted) {
                sb.append("[[Unparsed "+casted.specifier+"]]");
            }
            else if (this instanceof Builtin casted) {
                sb.append(casted.specifier);
            }
            else if (this instanceof Struct casted) {
                sb.append("struct{ ");
                var memberStrings = new ArrayList<String>();
                for (var member : casted.members) {
                    memberStrings.add(member.toString());
                }
                sb.append(String.join(", ", memberStrings));
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

    public static final class Unparsed extends Type
    {
        String specifier; //can be anything that specifies a type
        public Unparsed(String specifier_){specifier=specifier_;}
    }

    public static final class Builtin extends Type
    {
        String specifier; //for example "int", "void", "unsigned int"
        public Builtin(String specifier_){specifier=specifier_;}
    }

    public static final class Struct extends Type
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

    // unparsed should be a valid type specifier
    public static Type parse(String unparsed, NameInfo nameInfo)
    {
        unparsed = DataStructureCVisitor.normalizeCode(unparsed); //todo move normalizeCode out of the visitor
        if (unparsed.startsWith("struct")) {

        }
        /*
        //insert unsigned first to make typenames uniform (C allows both "int unsigned" and "unsigned int".)
        if (typeSpecifiers.contains("unsigned")) {
                sb.append("unsigned");
            }
            for (var typeSpec : typeSpecifiers) {
                if (typeSpec.equals("unsigned")) continue;
                if ( ! sb.isEmpty()) sb.append(" ");
                sb.append(typeSpec);
            }
        */
        return new Type(); //todo unfinished
    }
}
