package nl.ou.debm.producer;

public enum EFeaturePrefix {
    // enumerate feature prefixes
    // this enables testing for duplicates and reversing a code to a constant
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
}
