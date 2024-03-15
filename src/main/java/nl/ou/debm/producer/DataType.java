package nl.ou.debm.producer;

/*
    Data type class, is used to store datatypes
 */

import javax.xml.crypto.Data;
import java.util.Map;

public class DataType {
    // data type name (such as "char", "int" or "SThisIsAStruct"
    protected String name;
    private boolean primitive;
    private String default_value; //explicitly allowed to remain null

    // often used types. Unfortunately the real type names can't be used because they also mean something in java. Therefore there is a suffix _t .
    public static DataType void_t = new DataType("void", false, null);
    public static DataType char_t = DataType.make_primitive("char", "0");
    public static DataType short_t = DataType.make_primitive("short", "0");
    public static DataType long_t = DataType.make_primitive("long", "0");
    public static DataType long_long_t = DataType.make_primitive("long long", "0");
    public static DataType unsigned_char_t = DataType.make_primitive("unsigned char", "0");
    public static DataType unsigned_short_t = DataType.make_primitive("unsigned short", "0");
    public static DataType unsigned_long_t = DataType.make_primitive("unsigned long", "0");
    public static DataType unsigned_long_long_t = DataType.make_primitive("unsigned long long", "0");
    public static DataType float_t = DataType.make_primitive("float", "0.0");
    public static DataType double_t = DataType.make_primitive("double", "0.0");

    public static DataType ptrTypeOf(DataType T) {
        return new DataType(T.getNameForUse() + "*", true, "0");
    }
    public static DataType ptrType(String name) {
        return new DataType(name + "*", true, "0");
    }

    public DataType toPtrType() {
        return new DataType(name + "*", true, "0");
    }

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
