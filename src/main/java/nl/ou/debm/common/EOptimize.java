package nl.ou.debm.common;

public enum EOptimize {
    NO_OPTIMIZE {
        public String strFileCode() { return "nop"; }
        public String strTableCode() { return "N" ; }
    },
    OPTIMIZE {
        public String strFileCode() { return "opt"; }
        public String strTableCode() { return "Y" ; }
    };

    public String strFileCode(){
        return "";
    }
    public String strTableCode() { return "" ; }
}
