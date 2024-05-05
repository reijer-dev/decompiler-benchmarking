package nl.ou.debm.producer;

import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.ECompiler;
import nl.ou.debm.common.task.ProcessTask;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

public interface IBuildExecutable {

    /**
     * Build an executable, su
     * @param source_location
     * @param source_filenames
     * @param config
     * @param workerThreadPool
     */
    List<ProcessTask.ProcessResult> build_executable(String source_location, Collection<String> source_filenames, CompilerConfig config, ExecutorService workerThreadPool);

    ECompiler buildUsingCompiler();
    default boolean bUsesThisCompiler(ECompiler compiler){
        return buildUsingCompiler() == compiler;
    }

    boolean bAreAllCompilerComponentsAvailable(boolean bShowErrorOnStdError);
}
