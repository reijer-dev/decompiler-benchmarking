package nl.ou.debm.common;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static nl.ou.debm.common.Misc.strSafeToString;

// dontdo: This class was designed with the possibility of using various compilers in mind.
// Currently that's not possible because several key parts of the project rely on clang specific features.
// We use LLVM IR to determine what really ends up in the program.
// For that, we use a clang specific compilation process in the producer.
public class CompilerConfig implements Comparable<CompilerConfig> {
    public ECompiler compiler;
    public EArchitecture architecture;
    public EOptimize optimization;

//    public String getCompilerPath(String programName) {
//        return compiler == null ? null : compiler.getPath(programName);
//    }

    public static final List<CompilerConfig> configs;
    static {
        configs = new ArrayList<>();

        for (var compiler : ECompiler.values()){
            for (var arch : EArchitecture.values()){
                for (var optimization : EOptimize.values()) {
                    var config = new CompilerConfig(arch, compiler, optimization);
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

    /** total number of possible compiler configurations */
    private static final int s_iNPCC;
    static {
        // calculate constant only once
        s_iNPCC = EArchitecture.values().length * ECompiler.values().length * EOptimize.values().length;
    }

    /**
     * get the total number of possible compiler configs
     * @return number of architectures * number of compilers * number of optimization options
     */
    public static int iNumberOfPossibleCompilerConfigs(){
        return s_iNPCC;
    }

}
