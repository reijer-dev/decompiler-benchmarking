package nl.ou.debm.common.feature2;

import nl.ou.debm.common.BaseCodeMarker;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;

public class DataStructureCodeMarker extends BaseCodeMarker {
    public static final String characteristic = CodeMarker.STRCODEMARKERGUID + EFeaturePrefix.DATASTRUCTUREFEATURE;

    public String variableName;

    public DataStructureCodeMarker(String variableName_) {
        super(EFeaturePrefix.DATASTRUCTUREFEATURE);
        variableName = variableName_;
    }

    public String toCode() {
        return super.strPrintfPtr(variableName);
    }
}
