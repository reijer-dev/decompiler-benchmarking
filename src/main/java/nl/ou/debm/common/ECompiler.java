package nl.ou.debm.common;

import nl.ou.debm.producer.ExeBuildUsingClang;
import nl.ou.debm.producer.IBuildExecutable;
import nl.ou.debm.producer.JBCJoke;

public enum ECompiler {
    CLANG,JBCJOKE;

    public String strFileCode() {
        switch (this) {
            case CLANG ->               { return "cln"; }
            case JBCJOKE ->             { return "jbc"; }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public String strTableCode() {
        switch (this){
            case CLANG -> { return "clang"; }
            case JBCJOKE -> { return "JBC's joke"; }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public IBuildExecutable exeBuilder(){
        switch (this){
            case CLANG -> { return new ExeBuildUsingClang(); }
            case JBCJOKE -> {return new JBCJoke(); }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
