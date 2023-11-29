package nl.ou.debm.common.feature1;

public enum ELoopCommands {
    FOR, DO, WHILE;

    public static final String STRPROPERTYNAME = "loopcom";
    public String strPropertyValue(){
        return this.toString();
    }
}
