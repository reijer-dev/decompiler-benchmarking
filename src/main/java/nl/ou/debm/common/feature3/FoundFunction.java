package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class FoundFunction {
    public String name;
    public String type;
    public String id;
    private List<String> _parameterTypes = new ArrayList<>();
    private List<CodeMarker> _containingMarkers = new ArrayList<>();
    public static Map<String,String> _typesMap = Map.ofEntries(
            entry("i8", "byte"),
            entry("i32", "int"),
            entry("i64", "long")
    );

    public String getSignature(){
        return type + "|" + String.join(",", _parameterTypes);
    }

    public void addParameter(String type){
        _parameterTypes.add(_typesMap.getOrDefault(type, type));
    }
    public void addMarker(CodeMarker marker){
        _containingMarkers.add(marker);
    }
}
