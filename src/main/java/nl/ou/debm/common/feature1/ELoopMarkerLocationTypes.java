package nl.ou.debm.common.feature1;

public enum ELoopMarkerLocationTypes {
    BEFORE, BODY, AFTER,
    BEFORE_GOTO_DIRECTLY_AFTER,
    BEFORE_GOTO_FURTHER_AFTER,
    BEFORE_GOTO_BREAK_MULTIPLE,
    UNDEFINED;

    public static final String STRPROPERTYNAME = "location";

    public String strPropertyValue(){
        return this.toString();
    }
}
