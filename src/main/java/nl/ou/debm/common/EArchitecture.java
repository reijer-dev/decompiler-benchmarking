package nl.ou.debm.common;

/**
 * enumarate CPU-architectures
 */
public enum EArchitecture {
    X64ARCH {
        public String strFileCode() { return "x64"; }
        public int pointerBits() { return 64; }
        public int shortBits() { return 16; }
        public int intBits() { return 32; }
        public int longBits() { return 32; }
        public int longlongBits() { return 64; }
    },
    X86ARCH {
        public String strFileCode() { return "x86"; }
        public int pointerBits() { return 32; }
        public int shortBits() { return 16; }
        public int intBits() { return 32; }
        public int longBits() { return 32; }
        public int longlongBits() { return 64; } //yes, 64. It's emulated by the compiler.
    };

    public String strFileCode(){
        return "";
    }
    public String strTableCode() { return strFileCode(); }
    public int pointerBits() { return 0; }
    public int shortBits() { return 0; }
    public int intBits() { return 0; }
    public int longBits() { return 0; }
    public int longlongBits() { return 0; }
}
