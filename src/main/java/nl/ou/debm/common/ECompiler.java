package nl.ou.debm.common;

import nl.ou.debm.producer.ExeBuildUsingClang;
import nl.ou.debm.producer.IBuildExecutable;

public enum ECompiler {
    CLANG {
        @Override
        public String strFileCode()             { return "cln"; }
        @Override
        public String strProgramName()              { return "clang"; }
        @Override
        public String strTableCode() {return "clang";}
        @Override
        public IBuildExecutable exeBuilder(){ return new ExeBuildUsingClang(); }

        @Override
        public String strCompilerDescription() {    return "clang";  }
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

    public String strCompilerDescription () { return ""; }

    public IBuildExecutable exeBuilder(){
        return null;
    }
}
