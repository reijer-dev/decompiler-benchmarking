package nl.ou.debm.common.feature1;

public enum ELoopVarTestTypes {
    UNUSED,
    SMALLER_OR_EQUAL,
    GREATER_OR_EQUAL,
    SMALLER_THAN,
    GREATER_THAN,
    NON_EQUAL,
    EQUAL;


    public String strCOperator(){
        String out = "xx";
        switch (this){
            case SMALLER_OR_EQUAL -> { out = "<=";}
            case GREATER_OR_EQUAL -> { out = ">=";}
            case SMALLER_THAN ->     { out = "<";}
            case GREATER_THAN ->     { out = ">";}
            case NON_EQUAL ->        { out = "!=";}
            case EQUAL ->            { out = "==";}
        }
        return out;
    }

    public String strPropertyValue(){
        return strCOperator();
    }

    public static ELoopVarTestTypes OAIntToType(int i, ELoopVarUpdateTypes update){
        if ((i<1) || (i>3)) {
            return UNUSED;
        }
        if (update.bIsDecreasing()){
            return switch (i){
                case 2 -> GREATER_OR_EQUAL;
                case 3 -> GREATER_THAN;
                default -> NON_EQUAL;
            };
        }
        return switch (i) {
            case 2 -> SMALLER_OR_EQUAL;
            case 3 -> SMALLER_THAN;
            default -> NON_EQUAL;
        };
    }

    public static ELoopVarTestTypes stringStartToType(String strInput){
        for (var q : values()){
            if (q!=UNUSED) {
                if (strInput.startsWith(q.strCOperator())) {
                    return q;
                }
            }
        }
        return UNUSED;
    }

    /**
     * get the mirror operator.
     * @return the mirror operator (in case one switches the left-hand side argument with the right-hand side argument)
     */
    public ELoopVarTestTypes mirrorOperator(){
        ELoopVarTestTypes out = UNUSED;
        switch (this){
            case SMALLER_OR_EQUAL ->    out = GREATER_OR_EQUAL;
            case GREATER_OR_EQUAL ->    out = SMALLER_OR_EQUAL;
            case SMALLER_THAN ->        out = GREATER_THAN;
            case GREATER_THAN ->        out = SMALLER_THAN;
            case NON_EQUAL ->           out = NON_EQUAL;
            case EQUAL ->               out = EQUAL;
        }
        return out;
    }
}
