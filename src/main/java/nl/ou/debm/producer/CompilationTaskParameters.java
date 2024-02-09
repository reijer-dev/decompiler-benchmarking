package nl.ou.debm.producer;

import nl.ou.debm.common.CompilerConfig;

public class CompilationTaskParameters {
    public CompilationTaskParameters(boolean mode, CompilerConfig compilerConfig, int containerIndex, int testIndex){
        this.compilerConfig = compilerConfig;
        this.containerIndex = containerIndex;
        this.testIndex = testIndex;
    }
    public boolean mode;
    public CompilerConfig compilerConfig;
    public int containerIndex;
    public int testIndex;
}
