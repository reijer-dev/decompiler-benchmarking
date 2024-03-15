package nl.ou.debm.common.feature2;

import nl.ou.debm.common.BaseCodeMarker;
import nl.ou.debm.common.EFeaturePrefix;

public class DataStructureCodeMarker extends BaseCodeMarker {
    private String variable;

    public DataStructureCodeMarker(ETypeCategory category, String expected_type, String variable) {
        super(EFeaturePrefix.DATASTRUCTUREFEATURE);
        setProperty("category", category.toString());
        setProperty("expected", expected_type);
        this.variable = variable;
    }

    @Override
    public String strPrintf() {
        return "custom_printf(" + '\"' + this + '\"' + ", " + variable + ");";
    }

    public ETypeCategory getTypeCategory() {
        return ETypeCategory.valueOf(strPropertyValue("category"));
    }
}
