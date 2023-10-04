package nl.ou.debm.common;

public enum EOptimize {
    NO_OPTIMIZE {
        public String strFileCode() { return "nop"; }
    },
    OPTIMIZE {
        public String strFileCode() { return "opt"; }
    };

    public String strFileCode(){
        return "";
    }
}
