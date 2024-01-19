package nl.ou.debm.common.feature3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FoundFunction {
    private String _name;
    private boolean _isVariadic;
    private List<FunctionCodeMarker> _containingMarkers = new ArrayList<>();
    private HashMap<String, Integer> _calledFromFunctions = new HashMap<>();
    private int _numberOfStatements;
    public void addMarker(FunctionCodeMarker marker){
        _containingMarkers.add(marker);
    }
    public List<FunctionCodeMarker> getMarkers(){ return _containingMarkers; }
    public void addCalledFromFunction(String functionName){

        if(!_calledFromFunctions.containsKey(functionName))
            _calledFromFunctions.put(functionName, 1);
        else
            _calledFromFunctions.put(functionName, _calledFromFunctions.get(functionName)+1);
    }

    public HashMap<String, Integer> getCalledFromFunctions() {
        return _calledFromFunctions;
    }

    public String getName(){ return _name; }
    public void setName(String name) { _name = name; }

    public boolean isVariadic() {
        return _isVariadic;
    }

    public void setIsVariadic(boolean _isVariadic) {
        this._isVariadic = _isVariadic;
    }
}
