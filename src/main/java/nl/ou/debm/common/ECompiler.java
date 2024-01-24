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
    public String strCommandLocation() throws Exception {
        return "";
    }
    public String strTableCode() {return ""; }

    //todo waar is dit voor? alleen strFileCod en strProgramName worden nog gebruikt.
    public String strArchitectureFlag(EArchitecture architecture){
        return "";
    }
    public String strOutputSwitch(){
        return "";
    }
    public String strOptFlag(EOptimize optimize){
        return "";
    }
}
