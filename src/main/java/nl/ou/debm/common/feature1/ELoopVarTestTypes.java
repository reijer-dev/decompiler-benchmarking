package nl.ou.debm.common.feature1;

public enum ELoopVarTestTypes {
    UNUSED,
    SMALLER_THAN,
    GREATER_THAN,
    SMALLER_OR_EQUAL,
    GREATER_OR_EQUAL,
    NON_EQUAL;


    public String strCOperator(){
        String out = "xx";
        switch (this){
            case SMALLER_THAN ->     { out = "<";}
            case GREATER_THAN ->     { out = ">";}
            case SMALLER_OR_EQUAL -> { out = "<=";}
            case GREATER_OR_EQUAL -> { out = ">=";}
            case NON_EQUAL ->        { out = "!=";}
        }
        return out;
    }
    // EQUAL is not implemented, as this will not be used in loops
    // for (...; x == ; ...) {...}  will not be a loop at all, because it either doesn't loop at all
    // (if the condition is false from the start) or only loop once (as the update will change
    // the loopVar immediately and render the expression false)

    public static final String STRPROPERTYNAME = "test_op";

    public String strPropertyValue(){
        return strCOperator();
    }
}
