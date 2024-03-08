package nl.ou.debm.common;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import static nl.ou.debm.common.Misc.strSafeToString;

// dontdo: This class was designed with the possibility of using various compilers in mind. Currently that's not possible because several key parts of the project rely on clang specific features. We use LLVM IR to determine what really ends up in the program. For that, we use a clang specific compilation process in the producer.
public class CompilerConfig implements Comparable<CompilerConfig> {
    public ECompiler compiler;
    public EArchitecture architecture;
    public EOptimize optimization;
    private Map<String, String> programPaths = new HashMap<>();

    public String getPath(String programName) {
        if ( ! programPaths.containsKey(programName)) {
            try {
                var path = Misc.strGetExternalSoftwareLocation(programName);
                programPaths.put(programName, path);
                System.out.println("map met programmas : " + programPaths);
            } catch (Exception e) { throw new RuntimeException("program " + programName + " not found"); }
        }
        return programPaths.get(programName);
    }

    public List<String> compileCommandParameters(
            String sourceFilePath,
            String targetFilePath
    ) {
        List<String> ret = new ArrayList<>();

        if (compiler == ECompiler.CLANG) {
            ret.add(getPath(compiler.strProgramName()));
            ret.add(sourceFilePath);
            ret.add("-c");
            ret.add("-o"); ret.add(targetFilePath);

            if (optimization == EOptimize.OPTIMIZE)
                ret.add("-O3");
            else
                ret.add("-O0"); //this is also the default

            switch (architecture) {
                case X64ARCH -> {
                    ret.add("-march=x86-64");
                    ret.add("-m64");
                }
                case X86ARCH -> {
                    ret.add("-march=i686");
                    ret.add("-m32");
                }
            }

            //always compile to LLVM IR bitcode
            ret.add("-emit-llvm");
        }
        else { throw new RuntimeException("no other compilers than clang supported"); }

        return ret;
    }

    public static final List<CompilerConfig> configs;
    static {
        configs = new ArrayList<>();

        for (var compiler : ECompiler.values()){
            for (var arch : EArchitecture.values()){
                for (var optimization : EOptimize.values()) {
                    var config = new CompilerConfig();
                    config.compiler = compiler;
                    config.architecture = arch;
                    config.optimization = optimization;

                    //determine path based on the environment
                    if (Environment.actual == Environment.EEnv.KESAVA) {
                        //use a different path for the 32-bit compiler
                        if (arch == EArchitecture.X86ARCH && compiler == ECompiler.CLANG) {
                            config.programPaths.put("clang", "C:\\winlibs-i686-posix-dwarf-gcc-13.2.0-llvm-17.0.6-mingw-w64msvcrt-11.0.1-r3\\mingw32\\bin\\clang.exe");
                            config.programPaths.put("llvm-link", "C:\\winlibs-i686-posix-dwarf-gcc-13.2.0-llvm-17.0.6-mingw-w64msvcrt-11.0.1-r3\\mingw32\\bin\\llvm-link.exe");
                            config.programPaths.put("llvm-dis", "C:\\winlibs-i686-posix-dwarf-gcc-13.2.0-llvm-17.0.6-mingw-w64msvcrt-11.0.1-r3\\mingw32\\bin\\llvm-dis.exe");
                        }
                    }
                    configs.add(config);
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompilerConfig other)){
            return false;
        }
        return ((this.compiler     == other.compiler) &&
                (this.architecture == other.architecture) &&
                (this.optimization == other.optimization));
    }

    /**
     * Copy values from another compiler config. Only applies to the values used during testing: arch/comp/opt
     * @param rhs  source
     */
    public void copyFrom(CompilerConfig rhs){
        this.optimization=rhs.optimization;
        this.compiler=rhs.compiler;
        this.architecture=rhs.architecture;
    }

    public CompilerConfig(){};
    public CompilerConfig(EArchitecture architecture, ECompiler compiler, EOptimize optimization){
        this.architecture=architecture;
        this.compiler=    compiler;
        this.optimization=optimization;
    }

    @Override
    public int compareTo(@NotNull CompilerConfig o) {
        int v = Misc.iSafeCompare(this.architecture, o.architecture);
        if (v==0){
            v = Misc.iSafeCompare(this.compiler, o.compiler);
            if (v==0){
                v = Misc.iSafeCompare(this.optimization, o.optimization);
            }
        }
        return v;
    }

    @Override
    public String toString() {
        return strSafeToString(architecture) + "|" + strSafeToString(compiler) + "|" + strSafeToString(optimization) + "|";
    }

}
