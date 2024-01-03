package nl.ou.debm.common;

import nl.ou.debm.producer.IFeature;

public class BaseCodeMarker extends CodeMarker{
    public BaseCodeMarker(IFeature feature) {
        super(feature);
    }

    public BaseCodeMarker(EFeaturePrefix prefix) {
        super(prefix);
    }

    public BaseCodeMarker(String strCodedProperties) {
        super(strCodedProperties);
    }

    public BaseCodeMarker(CodeMarker codeMarker) {
        super(codeMarker);
    }

    @Override
    public void setProperty(String strPropertyName, String strPropertyValue) {
        super.setProperty(strPropertyName, strPropertyValue);
    }

    @Override
    public String strPropertyValue(String strPropertyName) {
        return super.strPropertyValue(strPropertyName);
    }

    @Override
    public void removeProperty(String strPropertyName) {
        super.removeProperty(strPropertyName);
    }
}
