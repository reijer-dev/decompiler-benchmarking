package nl.ou.debm.common;

/**
 * enumarate CPU-architectures
 */
public enum EArchitecture {
    X64ARCH {
        public String strFileCode() { return "x64"; }
    },
    X86ARCH {
        public String strFileCode() { return "x86"; }
    };

    public String strFileCode(){
        return "";
    }
    public String strTableCode() {return strFileCode();}
}
