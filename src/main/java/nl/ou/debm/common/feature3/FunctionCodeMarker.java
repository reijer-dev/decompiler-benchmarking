package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;

public class FunctionCodeMarker extends CodeMarker {

    public FunctionCodeMarker(String strCodedProperties){
        super(strCodedProperties);
    }

    public int functionId;
    public boolean isStartMarker(){ return strPropertyValue("position").equals("start"); }
    public boolean isEndMarker(){ return strPropertyValue("position").equals("end"); }

    public String getFunctionName(){ return strPropertyValue("functionName"); }
}
