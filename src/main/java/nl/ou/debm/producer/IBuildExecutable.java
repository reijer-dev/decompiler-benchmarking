package nl.ou.debm.producer;

import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.ECompiler;
import nl.ou.debm.common.task.ProcessTask;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * This interface is used for compiling a set of source files, resulting in a binary with the correct options set
 */
public interface IBuildExecutable {

    /**
     * Build an executable and build intermediate files (the llvm's and assembly files)
     * @param source_location   path to the source folder, all the product must be put here as well
     * @param source_filenames  all the source files
     * @param config            configuration to be used
     * @param workerThreadPool  worker threads available
     */
    List<ProcessTask.ProcessResult> build_executable(String source_location, Collection<String> source_filenames, CompilerConfig config, ExecutorService workerThreadPool);

    /**
     * determine the compiler associated with this class
     * @return  the enum of the compiler that this class uses
     */
    ECompiler buildUsesThisCompiler();

    /**
     * determine if this class uses this compiler
     * @param compiler the compiler to test against
     * @return true if, and only if, this class uses this compiler
     */
    default boolean bUsesThisCompiler(ECompiler compiler){
        return buildUsesThisCompiler() == compiler;
    }

    /**
     * determine if all compiler components are available on the system
     * @param bShowErrorOnStdError if true, missing components will be shown on StdErr
     * @return true if all is ok
     */
    boolean bAreAllCompilerComponentsAvailable(boolean bShowErrorOnStdError);
}
