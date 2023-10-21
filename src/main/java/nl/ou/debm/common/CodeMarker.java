package nl.ou.debm.common;

import java.util.HashMap;

public class CodeMarker {

    /**
     *  Property is a simple struct to hold both the name of a property and its value
     */
    private final String STRPROPERTYSEPARATOR="|";
    private final String STRVALUESEPARATOR="=";

    private final HashMap<String, String> propMap = new HashMap<String, String>();

    public void ClearProperties(){
        propMap.clear();
    }
    public void setProperty(String strPropertyName, String strPropertyValue){
        propMap.put(strPropertyName, strPropertyValue);
    }
    public String strPropertyValue(String strPropertyName){
        String out = propMap.get(strPropertyName);
        if (out == null){
            return "";
        }
        return out;
    }
    public String toString(){
        var sb = new StringBuilder();
        for (var s : propMap.entrySet()){
            sb.append(s.getKey());
            sb.append(STRVALUESEPARATOR);
            sb.append(s.getValue());
            sb.append(STRPROPERTYSEPARATOR);
        }
        return sb.toString();
    }
}
