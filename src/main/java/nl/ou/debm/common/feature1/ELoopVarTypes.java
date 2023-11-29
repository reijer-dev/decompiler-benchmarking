package nl.ou.debm.common.feature1;

public enum ELoopVarTypes {
    UNUSED, INT, FLOAT;

    public String strGetCKeyword(){
        switch (this){
            case INT -> {
                return "int";
            }
            case FLOAT -> {
                return "float";
            }
        }
        return "";
    }
}
