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


/*
    //determine path based on the environment
                    if (Environment.actual == Environment.EEnv.KESAVA) {
        //use a different path for the 32-bit compiler
        if (arch == EArchitecture.X86ARCH && compiler == ECompiler.CLANG) {
            config.programPaths.put("clang", "C:\\winlibs-i686-posix-dwarf-gcc-13.2.0-llvm-17.0.6-mingw-w64msvcrt-11.0.1-r3\\mingw32\\bin\\clang.exe");
            config.programPaths.put("llvm-link", "C:\\winlibs-i686-posix-dwarf-gcc-13.2.0-llvm-17.0.6-mingw-w64msvcrt-11.0.1-r3\\mingw32\\bin\\llvm-link.exe");
            config.programPaths.put("llvm-dis", "C:\\winlibs-i686-posix-dwarf-gcc-13.2.0-llvm-17.0.6-mingw-w64msvcrt-11.0.1-r3\\mingw32\\bin\\llvm-dis.exe");
        }
    }
                    else if (Environment.actual == Environment.EEnv.JAAP) {
        if (compiler == ECompiler.CLANG) {
            config.programPaths.put("clang",     "/usr/bin/clang");
            config.programPaths.put("llvm-link", "/usr/bin/llvm-link");
            config.programPaths.put("llvm-dis",  "/usr/bin/llvm-dis");
        }
    }*/

}
