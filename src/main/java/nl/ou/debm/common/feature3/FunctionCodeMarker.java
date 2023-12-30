package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;

public class FunctionCodeMarker {

    public CodeMarker rawMarker;

    public FunctionCodeMarker(CodeMarker codeMarker){
        this.rawMarker = codeMarker;
    }
    public int functionId;
    public int positionInFunction;

    public String getFunctionName(){ return rawMarker.strPropertyValue("functionName"); }
}
