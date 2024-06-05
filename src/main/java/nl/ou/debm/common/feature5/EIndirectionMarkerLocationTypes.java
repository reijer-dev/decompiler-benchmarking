package nl.ou.debm.common.feature5;

public enum EIndirectionMarkerLocationTypes {
    BEFORE, CASEBEGIN, CASEEND, AFTER, UNDEFINED;

    public static final String STRPROPERTYNAME = "location";

    public String strPropertyValue(){
        return this.toString();
    }
}
