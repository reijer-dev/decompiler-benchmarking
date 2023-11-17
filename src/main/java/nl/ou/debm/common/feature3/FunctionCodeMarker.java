package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;

public class FunctionCodeMarker extends CodeMarker {

    private int _functionId;
    public int _positionInFunction;

    public FunctionCodeMarker(String strCodedProperties, int functionId, int positionInFunction){
        super(strCodedProperties);
        _functionId = functionId;
        _positionInFunction = positionInFunction;
    }


    public String getFunctionName(){ return strPropertyValue("functionName"); }
    public int getPositionInFunction(){ return _positionInFunction; }
    public int getFunctionId(){ return _functionId; }
}
