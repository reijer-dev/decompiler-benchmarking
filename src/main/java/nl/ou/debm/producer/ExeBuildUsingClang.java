package nl.ou.debm.producer;

import nl.ou.debm.common.*;
import nl.ou.debm.common.task.ProcessTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class ExeBuildUsingClang implements IBuildExecutable {

    private final List<ProcessTask.ProcessResult> m_processErrorList = new ArrayList<>();

    @Override
    public ECompiler buildUsesThisCompiler() {
        return ECompiler.CLANG;
    }

    @Override
    public boolean bAreAllCompilerComponentsAvailable(boolean bShowErrorOnStdError) {
        boolean out=true;
        var lst = new String[]{"clang", "llvm-link", "llvm-dis", "llc"};
        for (var item : lst) {
            try {
                var ignore = Misc.strGetExternalSoftwareLocation(item);
            }
            catch (Exception ignore) {
                out = false;
                if (bShowErrorOnStdError) {
                    System.err.println("Compiler software component missing: " + item);
                }
            }
        }
        return out;
    }

    @Override
    public List<ProcessTask.ProcessResult> build_executable(String source_location, Collection<String> source_filenames, CompilerConfig config, ExecutorService workerThreadPool) {
        // This is the whole build process, including generation of LLVM IR.
        // It is the logical next step after generate_source_code. All created files will be placed in the source_location as well.

        // Everything here is clang specific.
        // For reference, this is the kind of command sequence that build_executable does:
        // clang .\source_1.c -c -emit-llvm
        // clang .\source_2.c -c -emit-llvm
        // llvm-link .\source_1.bc .\source_2.bc -o merged.bc
        // llvm-dis merged.bc -o human_readable.ll
        // clang merged.bc -o exe.exe

        // get compiler component locations
        boolean bNormal = !(Environment.actual == Environment.EEnv.KESAVA && config.architecture == EArchitecture.X86ARCH && config.compiler == ECompiler.CLANG);
        var clangPath =    bNormal ? Misc.strGetExternalSoftwareLocation("clang") :     "C:\\winlibs-i686-posix-dwarf-gcc-13.2.0-llvm-17.0.6-mingw-w64msvcrt-11.0.1-r3\\mingw32\\bin\\clang.exe";;
        var llvmLinkPath = bNormal ? Misc.strGetExternalSoftwareLocation("llvm-link") : "C:\\winlibs-i686-posix-dwarf-gcc-13.2.0-llvm-17.0.6-mingw-w64msvcrt-11.0.1-r3\\mingw32\\bin\\llvm-link.exe";
        var llvmDisPath =  bNormal ? Misc.strGetExternalSoftwareLocation("llvm-dis") :  "C:\\winlibs-i686-posix-dwarf-gcc-13.2.0-llvm-17.0.6-mingw-w64msvcrt-11.0.1-r3\\mingw32\\bin\\llvm-dis.exe";
        var llcPath =                Misc.strGetExternalSoftwareLocation("llc");

        // filenames
        var binaryFilename = IOElements.strBinaryFilename(config);
        var llvmFilename = IOElements.strLLVMFilename(config);
        var asmFilename = IOElements.strASMFilename(config);
        // llvmFilename is a human-readable version of this:
        var llvmMergedBitcodeFilename = IOElements.strGeneralFilename("merged_bitcode_", config, ".bc");

        // The compilation process is divided into a few steps. First, all the c files are compiled to LLVM IR bitcode.
        // The resulting files are then merged into one LLVM IR file with llvm-link.
        // That file is then converted to human-readable LLVM IR and further compiled and linked, without linker optimization, to create an executable.

        // first, define all tasks. They will be executed later.
        var compilationTasks = new ArrayList<ProcessTask>();
        var bitcodeFilenames = new ArrayList<String>();

        for (var source_filename : source_filenames) {
            compilationTasks.add(new ProcessTask(() -> {
                String targetFilename = IOElements.strGeneralFilename(source_filename + "_bitcode_", config, ".bc");
                bitcodeFilenames.add(targetFilename);
                var parameters = compileCommandParameters(source_filename, targetFilename, config.architecture, config.optimization);

                var pb = new ProcessBuilder(parameters);
                pb.directory(new File(source_location));
                pb.redirectErrorStream(true);
                return pb;
            }, (result) -> {
                System.out.println(Misc.strGetHexNumberWithPrefixZeros(result.procId, 8) + ": bitcodeMergeTask done in " + source_location);
                if (result.exitCode != 0)
                    throw new RuntimeException(Misc.strGetHexNumberWithPrefixZeros(result.procId, 8) + ":  exited with code " + result.exitCode);
            }, m_processErrorList));
        }

        var bitcodeMergeTask = new ProcessTask(() -> {
            var parameters = new ArrayList<String>();
            parameters.add(llvmLinkPath);
            parameters.addAll(bitcodeFilenames);
            parameters.add("-o");
            parameters.add(llvmMergedBitcodeFilename);

            var pb = new ProcessBuilder(parameters);
            pb.directory(new File(source_location));
            pb.redirectErrorStream(true);
            return pb;
        }, (result) -> {
            System.out.println(Misc.strGetHexNumberWithPrefixZeros(result.procId, 8) + ": bitcodeMergeTask done in " + source_location);
            if (result.exitCode != 0)
                throw new RuntimeException(Misc.strGetHexNumberWithPrefixZeros(result.procId, 8) + ": exited with code " + result.exitCode);
        }, m_processErrorList);

        //creates the human readable merged LLVM IR file
        var bitcodeToLLVMTask = new ProcessTask(() -> {
            var parameters = new ArrayList<String>();
            parameters.add(llvmDisPath);
            parameters.add(llvmMergedBitcodeFilename);
            parameters.add("-o");
            parameters.add(llvmFilename);

            var pb = new ProcessBuilder(parameters);
            pb.directory(new File(source_location));
            pb.redirectErrorStream(true);
            return pb;
        }, (result) -> {
            System.out.println(Misc.strGetHexNumberWithPrefixZeros(result.procId, 8) + ": bitcodeToLLVMTask done in " + source_location);
            if (result.exitCode != 0)
                throw new RuntimeException(Misc.strGetHexNumberWithPrefixZeros(result.procId, 8) + ": exited with code " + result.exitCode);
        }, m_processErrorList);

        //creates the human readable merged LLVM IR file
        var bitcodeToASMTask = new ProcessTask(() -> {
            var parameters = new ArrayList<String>();
            parameters.add(llcPath);
            parameters.add(llvmMergedBitcodeFilename);
            parameters.add("-o");
            parameters.add(asmFilename);

            var pb = new ProcessBuilder(parameters);
            pb.directory(new File(source_location));
            pb.redirectErrorStream(true);
            return pb;
        }, (result) -> {
            System.out.println(Misc.strGetHexNumberWithPrefixZeros(result.procId, 8) + ": bitcodeToASMask done in " + source_location);
            if (result.exitCode != 0)
                throw new RuntimeException(Misc.strGetHexNumberWithPrefixZeros(result.procId, 8) + ": exited with code " + result.exitCode);
        }, m_processErrorList);

        var createExecutableTask = new ProcessTask(() -> {
            var parameters = new ArrayList<String>();
            parameters.add(clangPath);
            parameters.add(llvmMergedBitcodeFilename);
            parameters.add("-o");
            parameters.add(binaryFilename);

            var pb = new ProcessBuilder(parameters);
            pb.directory(new File(source_location));
            pb.redirectErrorStream(true);
            return pb;
        }, (result) -> {
            System.out.println(Misc.strGetHexNumberWithPrefixZeros(result.procId, 8) + ": createExecutableTask done in " + source_location);
            if (result.exitCode != 0)
                throw new RuntimeException(Misc.strGetHexNumberWithPrefixZeros(result.procId, 8) + ": exited with code " + result.exitCode);
        }, m_processErrorList);

        //  Execute the tasks in the right order.
        try {
            // list used to group tasks that may be executed in parallel
            // Due to how the threadpool works, the tasks need to be Callables when grouped. invokeAll accepts a collection of callables and waits for all tasks to finish. submit accepts a Runnable and doesn't wait.
            var bundled_tasks = new ArrayList<Callable<Void>>();

            // compilation tasks
            for (var compilationTask : compilationTasks) {
                bundled_tasks.add(() -> {
                    compilationTask.run();
                    compilationTask.await();
                    return null;
                });
            }
            workerThreadPool.invokeAll(bundled_tasks);
            bundled_tasks.clear();

            // merging bitcode
            workerThreadPool.submit(() -> {
                bitcodeMergeTask.run();
                bitcodeMergeTask.await();
            }).get();

            // create executable, assembly code and human-readable LLVM IR. Both tasks require the merged bitcode and so can be done in parallel.
            bundled_tasks.add(() -> {
                bitcodeToLLVMTask.run();
                bitcodeToLLVMTask.await();
                return null;
            });
            bundled_tasks.add(() -> {
                bitcodeToASMTask.run();
                bitcodeToASMTask.await();
                return null;
            });
            bundled_tasks.add(() -> {
                createExecutableTask.run();
                createExecutableTask.await();
                return null;
            });
            workerThreadPool.invokeAll(bundled_tasks);
            bundled_tasks.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return m_processErrorList;
    }

    /**
     * Get all command line components for a given set of parameters
     * @param sourceFilePath source to be compiled
     * @param targetFilePath target to write to
     * @param architecture architecture to be used
     * @param optimization optimization to be used
     * @return all the command line components
     */
    private List<String> compileCommandParameters(String sourceFilePath, String targetFilePath,
                                                  EArchitecture architecture, EOptimize optimization) {
        final List<String> ret = new ArrayList<>();

        ret.add("clang");
        ret.add(sourceFilePath);
        ret.add("-c");
        ret.add("-o");
        ret.add(targetFilePath);

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

        // return the lot
        return ret;
    }
}