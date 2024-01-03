package nl.ou.debm.common;

import nl.ou.debm.producer.IFeature;

/**
 * BaseCodeMarker is a CodeMarker child class, making it possible to manipulate the properties by
 * string access <br>
 * - setProperty("FIRSTNAME", "Harry")<br>
 * rather than by method access, such as<br>
 * - setFirstName("Harry").<br>
 * In the latter case, the CodeMarker child class would invoke its parents setProperty routine with a constant string
 * for the field name.
 * <br>
 * All constructors are derived from CodeMarker, setProperty/strGetProperty/RemoveProperty are overwritten to make them
 * public.
 */
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
