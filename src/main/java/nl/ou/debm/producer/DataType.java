package nl.ou.debm.producer;

/*
    Data type class, is used to store datatypes
 */

import java.util.Map;

public class DataType {
    // data type name (such as "char", "int" or "SThisIsAStruct"
    protected String name;
    private boolean primitive;
    private String default_value; //explicitly allowed to remain null

    /**
     * default constructor, takes a data type name as parameter
     * @param name  name of the datatype, such as "int", "CThisIsAClass" or "ptr*"
     * @param primitive  should be true if it is a built-in type such as int
     * @param default_value  null or a valid literal that can be used as default value. If primitive then it may not be null.
     */
    public DataType(String name, boolean primitive, String default_value) {
        this.name = name;
        this.primitive = primitive;
        if (primitive) {
            assert default_value != null;
        }
        this.default_value = default_value;
    }

    //factory function for primitive types
    public static DataType make_primitive(String name, String default_value) {
        return new DataType(name, true, default_value);
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

    public boolean bIsPrimitive(){
        return primitive;
    }

    /**
     * Get a default value for this datatype. Highly usable when writing return statements, so
     * they can match a function's type
     * @param structMap  Map of all the structs in use by the generator, with struct name as key. This will be used to create default values for any struct datatypes.
     * @return  default value as a string, for example "0", "'a'", "0.0"<br>
     *          any primitive will return some value, any non-primitive will return NULL,
     *          a void will return an empty string ("")
     */
    public String strDefaultValue(Map<String, Struct> structMap){
        // If a default value exists, use it. Otherwise create one.
        if (default_value != null) {
            return default_value;
        }
        if (primitive) {
            return "0";
        }

        // since type is no primitive, assume type to be a struct and build init string recursively
        var out = new StringBuilder();
        // add struct keyword and struct name
        out.append("(").append(getNameForUse()).append("){");
        // add default values for each attribute by recursion
        var struct = structMap.get(this.name);
        if (struct!=null){
            for (var member : struct.getProperties()){
                out.append(member.getType().strDefaultValue(structMap));
                out.append(", ");
            }
            // as each member (even the last) will add ", ", the last ", " must be removed again
            out.setLength(out.length()-2);
        }
        // add closing character
        out.append("}");

        return out.toString();
    }


}
