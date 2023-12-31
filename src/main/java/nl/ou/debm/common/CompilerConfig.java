package nl.ou.debm.common;

import java.util.ArrayList;
import java.util.List;

public class CompilerConfig {
    public ECompiler compiler;
    public EArchitecture arch;
    public EOptimize optimization;
    private String path = "";

    public String getPath() throws Exception {
        if (path == "") {
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

            switch (arch) {
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

        for (var compiler : ECompiler.values())
        for (var arch : EArchitecture.values())
        for (var optimization : EOptimize.values())
        {
            var config = new CompilerConfig();
            config.compiler = compiler;
            config.arch = arch;
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
