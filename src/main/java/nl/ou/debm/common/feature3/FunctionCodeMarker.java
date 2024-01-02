package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.producer.Function;

public class FunctionCodeMarker extends CodeMarker {

    public FunctionCodeMarker(String strCodedProperties){
        super(strCodedProperties);
    }

    public int functionId;
    public int positionInFunction;

    public String getFunctionName(){ return strPropertyValue("functionName"); }
}
