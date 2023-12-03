package nl.ou.debm.producer;

/*
    Data type class, is used to store datatypes
 */

public class DataType {
    // data type name (such as "char", "int" or "SThisIsAStruct"
    protected String name;

    /**
     * default constructor, takes a data type name as parameter
     * @param name  name of the datatype, such as "int", "CThisIsAClass" or "ptr*"
     */
    public DataType(String name){
        this.name = name;
    }

    /**
     * returns the name of the datatype
     * @return  name of datatype, such as "int", "CThisIsAClass" or "ptr*"
     */
    public String getName(){
        return name;
    }

    public String getNameForUse(){
        return name;
    }

    /**
     * set prefix for datatype
     * @param prefix  such as "JBC", "Reijer", "Dinky". An underscore is automatically added
     */
    public void prefixName(String prefix){
        name = prefix + "_" + name;
    }

    /**
     * determine whether datatype is "char"
     * @return  true when datatype is char
     */
    public boolean bIsChar(){
        return name.equals("char");
    }
    /**
     * determine whether datatype is "int"
     * @return  true when datatype is int
     */
    public boolean bIsInt(){
        return name.equals("int");
    }
    /**
     * determine whether datatype is "float"
     * @return  true when datatype is float
     */
    public boolean bIsFloat(){
        return name.equals("float");
    }
    /**
     * determine whether datatype is "double"
     * @return  true when datatype is double
     */
    public boolean bIsDouble(){
        return name.equals("double");
    }
    /**
     * determine whether datatype is "short"
     * @return  true when datatype is short int/short
     */
    public boolean bIsShortInt(){
        return (name.equals("short") || name.equals("short int"));
    }
    /**
     * determine whether datatype is "long"
     * @return  true when datatype is long
     */
    public boolean bIsLongInt(){
        return name.equals("long");
    }
    /**
     * determine whether datatype is any of the primitives
     * @return  true when datatype is any of the primitives
     */
    public boolean bIsPrimitive(){
        return bIsChar() || bIsDouble() || bIsFloat() || bIsInt() || bIsShortInt() || bIsLongInt();
    }
    /**
     * determine whether datatype is a numeric datatype (primitives -/- char)
     * @return  true when datatype is numeric (primitive -/- char)
     */
    public boolean bIsNumeric(){
        return bIsPrimitive() && !bIsChar();
    }
    public boolean bIsNonFixedNumeric(){
        return bIsDouble() || bIsFloat();
    }

    /**
     * Get a default value for this datatype. Highly usable when writing return statements, so
     * they can match a function's type
     * @return  default value as a string, for example "0", "\"a\"", "0.0", "NULL"<br>
     *          any primitive will return some value, any non-primitive will return NULL,
     *          a void will return an empty string ("")
     */
    public String strDefaultValue(){
        if (bIsChar()){
            return "\"a\"";
        }
        if (bIsNonFixedNumeric()){
            return "0.0";
        }
        if (bIsPrimitive()){
            return "0";
        }
        if (name.equals("void")){
            return "";
        }
        return "NULL";
    }
}
