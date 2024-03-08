package nl.ou.debm.common.feature2;

import nl.ou.debm.common.BaseCodeMarker;
import nl.ou.debm.common.EFeaturePrefix;

public class DataStructureCodeMarker extends BaseCodeMarker {
    public final String category_key = "category";

    public DataStructureCodeMarker(ETypeCategory category) {
        super(EFeaturePrefix.DATASTRUCTUREFEATURE);
        setProperty(category_key, category.toString());
    }

    public ETypeCategory getTypeCategory() {
        return ETypeCategory.valueOf(strPropertyValue(category_key));
    }
}
