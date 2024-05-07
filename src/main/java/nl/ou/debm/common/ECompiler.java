package nl.ou.debm.common;

import nl.ou.debm.producer.ExeBuildUsingClang;
import nl.ou.debm.producer.IBuildExecutable;

public enum ECompiler {
    CLANG;

    public String strFileCode() {
        switch (this) {
            case CLANG ->               { return "cln"; }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public String strTableCode() {
        switch (this){
            case CLANG -> { return "clang"; }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public IBuildExecutable exeBuilder(){
        switch (this){
            case CLANG -> { return new ExeBuildUsingClang(); }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        }
    }
}
