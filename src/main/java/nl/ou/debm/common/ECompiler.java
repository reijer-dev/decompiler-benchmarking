package nl.ou.debm.common;

public enum ECompiler {
    CLANG {
        private String strCompilerLocation = "";
        public String strFileCode()             { return "cln"; }
        public String strCommand()              { return "clang"; }
        public String strCommandLocation() throws Exception {
            if (strCompilerLocation.isEmpty()){
                strCompilerLocation = Misc.strGetExternalSoftwareLocation(strCommand());
            }
            return strCompilerLocation;
        }
        public String strOutputSwitch()         { return "-o";}
        public String strArchitectureFlag(EArchitecture architecture){
            return switch (architecture){
                case X64ARCH -> "-march=x86-64";
                case X86ARCH -> "-march=native";
            };
        }
        public String strOptFlag(EOptimize optimize){
            return switch (optimize){
                case OPTIMIZE -> "-O3";
                case NO_OPTIMIZE -> "-O0";
            };
        }
    }/*,
    GCC {
        public String strFileCode() { return "gcc"; }
        public String strCommand() { return "gcc"; }
    }*/;

    public String strFileCode(){
        return "";
    }
    public String strCommand(){
        return "";
    }
    public String strCommandLocation() throws Exception {
        return "";
    }

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
