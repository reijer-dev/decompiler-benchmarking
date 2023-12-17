package nl.ou.debm.common.feature1;

public enum ELoopVarTypes {
    INT, FLOAT;

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

    public String strShortCode(){
        switch (this){
            case INT -> {
                return "I";
            }
            case FLOAT -> {
                return "F";
            }
        }
        return "";
    }
}
