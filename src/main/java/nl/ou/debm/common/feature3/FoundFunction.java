package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FoundFunction {
    private String _name;
    private List<FunctionCodeMarker> _containingMarkers = new ArrayList<>();
    private int _numberOfStatements;
    public void addMarker(FunctionCodeMarker marker){
        _containingMarkers.add(marker);
    }
    public List<FunctionCodeMarker> getMarkers(){ return _containingMarkers; }
    public void setNumberOfStatements(int size) { _numberOfStatements = size; }
    public int getNumberOfStatements() { return _numberOfStatements; }
    public String getName(){ return _name; }
    public void setName(String name) { _name = name; }

    public boolean isMarkerAtStart(FunctionCodeMarker marker){
        return marker.getPositionInFunction() == 0;
    }

    public boolean isMarkerAtEnd(FunctionCodeMarker marker){
        //Marker can occur in the return statement or just before it
        return getMarkers().get(1).getPositionInFunction() == getNumberOfStatements() - 1;
    }
}
