package nl.ou.debm.common;

public enum ECompiler {
    CLANG {
        public String strFileCode()             { return "cln"; }
        public String strProgramName()              { return "clang"; }
        public String strTableCode() {return "clang";}
    }/*,
    GCC {
        public String strFileCode() { return "gcc"; }
        public String strProgramName() { return "gcc"; }
    }*/;

    public String strFileCode(){
        return "";
    }
    public String strProgramName(){
        return "";
    }
    public String strTableCode() {return ""; }
}
