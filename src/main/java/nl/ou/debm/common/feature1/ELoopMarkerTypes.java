package nl.ou.debm.common.feature1;

public enum ELoopMarkerTypes {
    BEFORE, BODY, AFTER;

    public static final String STRPROPERTYNAME = "Location";

    public String strPropertyValue(){
        return this.toString();
    }
}
