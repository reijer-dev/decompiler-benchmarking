package nl.ou.debm.common.feature1;

public enum ELoopUnrollTypes {
    NO_ATTEMPT,
    ATTEMPT_PRINT_LOOP_VAR,
    ATTEMPT_DO_NOT_PRINT_LOOP_VAR;

    public String strPropertyValue(){
        String out;
        switch (this){
            case ATTEMPT_PRINT_LOOP_VAR ->          out =  "U+" ;
            case ATTEMPT_DO_NOT_PRINT_LOOP_VAR ->   out = "U-" ;
            default ->                              out =  "NU" ;
        }
        return out;
    }

    public static ELoopUnrollTypes stringToType(String strIn){
        ELoopUnrollTypes out = NO_ATTEMPT;
        try {
            out = ELoopUnrollTypes.valueOf(strIn);
        }
        catch (Exception e){
            for (var item : ELoopUnrollTypes.values()){
                if (strIn.equals(item.toString())){
                    out = item;
                    break;
                }
            }
        }
        return out;
    }
}
