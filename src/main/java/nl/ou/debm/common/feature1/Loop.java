package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.ICodeMarker;

public abstract class Loop implements ICodeMarker {
    public CodeMarker toCodeMarker(){
        return new CodeMarker();
    }
    public void fromCodeMarker(CodeMarker cm){

    }
}
