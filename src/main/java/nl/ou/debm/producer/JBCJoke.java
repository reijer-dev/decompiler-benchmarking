package nl.ou.debm.producer;

import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.ECompiler;
import nl.ou.debm.common.IOElements;
import nl.ou.debm.common.task.ProcessTask;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class JBCJoke implements IBuildExecutable{
    @Override
    public List<ProcessTask.ProcessResult> build_executable(String source_location, Collection<String> source_filenames, CompilerConfig config, ExecutorService workerThreadPool) {
        System.out.println("JBC's compiler called - producing...");
        final String WHATEVER = "HI ALL!";

        for (var item : source_filenames) {
            IOElements.writeToFile(WHATEVER, Path.of(source_location, IOElements.strGeneralFilename(item + "_bitcode", config, ".bc")).toString());
        }

        IOElements.writeToFile(WHATEVER, Path.of(source_location,IOElements.strBinaryFilename(config)).toString());
        IOElements.writeToFile(WHATEVER, Path.of(source_location,IOElements.strLLVMFilename(config)).toString());
        IOElements.writeToFile(WHATEVER, Path.of(source_location,IOElements.strASMFilename(config)).toString());
        IOElements.writeToFile(WHATEVER, Path.of(source_location,IOElements.strGeneralFilename("merged_bitcode_", config, ".bc")).toString());


        return null;
    }

    @Override
    public ECompiler buildUsesThisCompiler() {
        return null;
    }

    @Override
    public boolean bAreAllCompilerComponentsAvailable(boolean bShowErrorOnStdError) {
        return true;
    }
}
