package nl.ou.debm.common.feature1;

public enum ELoopMarkerLocationTypes {
    BEFORE, BODY, AFTER, UNDEFINED;

    public static final String STRPROPERTYNAME = "location";

    public String strPropertyValue(){
        return this.toString();
    }
}
