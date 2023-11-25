package nl.ou.debm.common.feature1;

public enum ELoopVarUpdateTypes {
    UNUSED,                                 // unused
    INCREASE_BY_ONE,                        // ++
    DECREASE_BY_ONE,                        // --
    INCREASE_OTHER,                         // += <?>
    DECREASE_OTHER,                         // -= <?>
    MULTIPLY,                               // *= <?>
    DIVIDE,                                 // /= <?>
    INCREASE_BY_INPUT,                      // +=getchar()
    DECREASE_BY_INPUT;                      // -=getchar()

    public String strShortCode(){
        String out = "xx";
        switch (this){
            case INCREASE_BY_ONE -> {   out = "++";}
            case DECREASE_BY_ONE -> {   out = "--";}
            case INCREASE_OTHER -> {    out = "+=";}
            case DECREASE_OTHER -> {    out = "-=";}
            case MULTIPLY -> {          out = "*=";}
            case DIVIDE -> {            out = "/=";}
            case INCREASE_BY_INPUT -> { out = "+?";}
            case DECREASE_BY_INPUT -> { out = "-?";}
        }
        return out;
    }

    public boolean bIsIncreasing(){
        return switch (this) {
            case INCREASE_BY_ONE, INCREASE_OTHER, MULTIPLY, INCREASE_BY_INPUT -> true;
            default -> false;
        };
    }
    public boolean bIsDecreasing(){
        return switch (this) {
            case DECREASE_BY_ONE, DECREASE_OTHER, DIVIDE, DECREASE_BY_INPUT -> true;
            default -> false;
        };
    }
}
