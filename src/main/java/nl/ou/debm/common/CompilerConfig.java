package nl.ou.debm.common;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static nl.ou.debm.common.Misc.strSafeToString;

public class CompilerConfig implements Comparable<CompilerConfig> {
    public ECompiler compiler;
    public EArchitecture architecture;
    public EOptimize optimization;
    private String path = "";

    public String getPath() throws Exception {
        if (Objects.equals(path, "")) {
            path = Misc.strGetExternalSoftwareLocation(compiler.strProgramName());
        }
        return path;
    }

    public List<String> compileCommandParameters(
            String sourceFilePath,
            String targetFilePath,
            boolean generate_LLVM_IR
    ) throws Exception {
        List<String> ret = new ArrayList<>();

        if (compiler == ECompiler.CLANG) {
            ret.add(getPath());
            ret.add(sourceFilePath);
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

            if (generate_LLVM_IR) {
                ret.add("-S");
                ret.add("-emit-llvm");
            }
        }

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
                            config.path = "C:\\winlibs-i686-posix-dwarf-gcc-13.2.0-llvm-17.0.6-mingw-w64msvcrt-11.0.1-r3\\mingw32\\bin\\clang.exe";
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
