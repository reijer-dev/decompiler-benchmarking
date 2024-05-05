package nl.ou.debm.common;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static nl.ou.debm.common.Misc.strSafeToString;

public class CompilerConfig implements Comparable<CompilerConfig> {

    /** list of all compiler configurations */ private static final List<CompilerConfig> s_configs=new ArrayList<>();
    static {
        // loop over all compiler configs
        for (var compiler : ECompiler.values()){
            for (var arch : EArchitecture.values()){
                for (var optimization : EOptimize.values()) {
                    var config = new CompilerConfig(arch, compiler, optimization);
                    s_configs.add(config);
                }
            }
        }
    }
    public static synchronized List<CompilerConfig> getAllCompilerConfigurations(){
        return s_configs;
    }

    // struct-like access
    public ECompiler compiler;
    public EArchitecture architecture;
    public EOptimize optimization;

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
