package nl.ou.debm.common;

import nl.ou.debm.common.feature1.LoopCodeMarker;
import nl.ou.debm.common.feature3.FunctionCodeMarker;

/**
 *     enumerate feature prefixes
 *     this enables testing for duplicates and reversing a code to a constant
 */
public enum EFeaturePrefix {
    FUNCTIONFEATURE         { public String toString() { return "FF";} },
    DATASTRUCTUREFEATURE    { public String toString() { return "DS";} },
    CONTROLFLOWFEATURE      { public String toString() { return "CF";} };

    /**
     * Convert string to constant
     * @param strIn   String containing prefix or its full name, Matching prefix is case-insensitive.
     * @return        constant matching the string, or null if not found
     */
    public static EFeaturePrefix fromString(String strIn){
        try{
            // the easy way, used for all full names
            return valueOf(strIn);
        }
        catch (Exception e) {
            // the hard way: search all the short codes manually
            for (var fp : EFeaturePrefix.values()) {
                if (fp.toString().equalsIgnoreCase(strIn)) {
                    return fp;
                }
            }
        }
        return null;
    }

    /**
     * Create a CodeMarker or one of its child classes, depending on the prefix passed. If no
     * specific child is found, a general CodeMarker is returned
     * @param prefix     prefix indicating the feature using the code marker
     * @return           an object of CodeMarker type or one of its children
     */
    public static CodeMarker createNewFeaturedCodeMarker(EFeaturePrefix prefix, String strCodedProperties){
        if (prefix == EFeaturePrefix.CONTROLFLOWFEATURE){
            return new LoopCodeMarker(strCodedProperties);
        }
        if (prefix == EFeaturePrefix.FUNCTIONFEATURE){
            return new FunctionCodeMarker(strCodedProperties);
        }
        return new CodeMarker(strCodedProperties);
    }
}
