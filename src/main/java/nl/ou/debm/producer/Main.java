package nl.ou.debm.producer;

import nl.ou.debm.common.*;
import nl.ou.debm.common.task.ProcessTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.System.exit;
import static nl.ou.debm.common.CommandLineUtils.strGetParameterValue;
import static nl.ou.debm.common.ProjectSettings.IDEFAULTNUMBEROFCONTAINERS;
import static nl.ou.debm.common.ProjectSettings.IDEFAULTTESTSPERCONTAINER;

public class Main {


    // Creates files and returns a list of their names
    public static List<String> generate_source_code(String destination) {
        System.out.println("generating C source files for destination " + destination);
        var cFileContents = new CGenerator().generateSourceFiles();
        System.out.println("generating C source files for destination " + destination + " done");
        if(cFileContents.keySet().isEmpty()) throw new RuntimeException("no source files returned");

        System.out.print("writing C source files to " + destination);
        var filenames_to_compile = new ArrayList<String>();
        for (var filename : cFileContents.keySet()) {
            String fullPath = destination + filename;
            String content = cFileContents.get(filename);
            IOElements.writeToFile(content, fullPath);
            if (filename != IOElements.cAmalgamationFilename) {
                filenames_to_compile.add(filename);
            }
        }
        System.out.println(" done");
        return filenames_to_compile;
    }

    // This is the whole build process, including generation of LLVM IR. It is the logical next step after generate_source_code. All created files will be placed in the source_location as well.
    public static void build_executable(String source_location, Collection<String> source_filenames, CompilerConfig config, ExecutorService workerThreadPool)
    {
        if (config.compiler != ECompiler.CLANG) {
            throw new RuntimeException("Only clang is supported");
        }
        // Everything here is clang specific.
        // For reference, this is the kind of command sequence that build_executable does:
        // clang .\source_1.c -c -emit-llvm
        // clang .\source_2.c -c -emit-llvm
        // llvm-link .\source_1.bc .\source_2.bc -o merged.bc
        // llvm-dis merged.bc -o human_readable.ll
        // clang merged.bc -o exe.exe

        var clangPath = config.getPath(ECompiler.CLANG.strProgramName());
        var llvmLinkPath = config.getPath("llvm-link");
        var llvmDisPath = config.getPath("llvm-dis");
        var llcPath = config.getPath("llc");

        var binaryFilename = IOElements.strBinaryFilename(config);
        var llvmFilename = IOElements.strLLVMFilename(config);
        var asmFilename = IOElements.strASMFilename(config);
        // llvmFilename is a human-readable version of this:
        var llvmMergedBitcodeFilename = IOElements.strGeneralFilename("merged_bitcode_", config, ".bc");

        // The compilation process is divided into a few steps. First, all the c files are compiled to LLVM IR bitcode. The resulting files are then merged into one LLVM IR file with llvm-link. That file is then converted to human-readable LLVM IR and further compiled and linked, without linker optimization, to create an executable.

        // first, define all tasks. They will be executed later.
        var compilationTasks = new ArrayList<ProcessTask>();
        var bitcodeFilenames = new ArrayList<String>();

        for (var source_filename : source_filenames) {
            compilationTasks.add(new ProcessTask(() -> {
                String targetFilename = IOElements.strGeneralFilename(source_filename + "_bitcode_", config, ".bc");
                bitcodeFilenames.add(targetFilename);
                var parameters = config.compileCommandParameters(source_filename, targetFilename);

                var pb = new ProcessBuilder(parameters);
                pb.directory(new File(source_location));
                pb.redirectErrorStream(true);
                return pb;
            }, (result) -> {
                System.out.println("bitcodeMergeTask done in " + source_location);
                if(result.exitCode != 0) throw new RuntimeException("pid " + result.procId + " exited with code " + result.exitCode);
            }));
        }

        var bitcodeMergeTask = new ProcessTask(() -> {
            var parameters = new ArrayList<String>();
            parameters.add(llvmLinkPath);
            parameters.addAll(bitcodeFilenames);
            parameters.add("-o"); parameters.add(llvmMergedBitcodeFilename);

            var pb = new ProcessBuilder(parameters);
            pb.directory(new File(source_location));
            pb.redirectErrorStream(true);
            return pb;
        }, (result) -> {
            System.out.println("bitcodeMergeTask done in " + source_location);
            if(result.exitCode != 0) throw new RuntimeException("pid " + result.procId + " exited with code " + result.exitCode);
        });

        //creates the human readable merged LLVM IR file
        var bitcodeToLLVMTask = new ProcessTask(() -> {
            var parameters = new ArrayList<String>();
            parameters.add(llvmDisPath);
            parameters.add(llvmMergedBitcodeFilename);
            parameters.add("-o"); parameters.add(llvmFilename);

            var pb = new ProcessBuilder(parameters);
            pb.directory(new File(source_location));
            pb.redirectErrorStream(true);
            return pb;
        }, (result) -> {
            System.out.println("bitcodeToLLVMTask done in " + source_location);
            if(result.exitCode != 0) throw new RuntimeException("pid " + result.procId + " exited with code " + result.exitCode);
        });

        //creates the human readable merged LLVM IR file
        var bitcodeToASMTask = new ProcessTask(() -> {
            var parameters = new ArrayList<String>();
            parameters.add(llcPath);
            parameters.add(llvmMergedBitcodeFilename);
            parameters.add("-o"); parameters.add(asmFilename);

            var pb = new ProcessBuilder(parameters);
            pb.directory(new File(source_location));
            pb.redirectErrorStream(true);
            return pb;
        }, (result) -> {
            System.out.println("bitcodeToASMask done in " + source_location);
            if(result.exitCode != 0) throw new RuntimeException("pid " + result.procId + " exited with code " + result.exitCode);
        });

        var createExecutableTask = new ProcessTask(() -> {
            var parameters = new ArrayList<String>();
            parameters.add(clangPath);
            parameters.add(llvmMergedBitcodeFilename);
            parameters.add("-o"); parameters.add(binaryFilename);

            var pb = new ProcessBuilder(parameters);
            pb.directory(new File(source_location));
            pb.redirectErrorStream(true);
            return pb;
        }, (result) -> {
            System.out.println("createExecutableTask done in " + source_location);
            if(result.exitCode != 0) throw new RuntimeException("pid " + result.procId + " exited with code " + result.exitCode);
        });

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
        }
        catch (Exception e) { throw new RuntimeException(e); }
    }



    public static void main(String[] args) throws Exception {
        // handle command line parameters --> handleCLIParameters exits on help or errors
        var cli = new ProducerCLIParameters();
        handleCLIParameters(args, cli);

        // show program name & parameters
        System.out.println("Containers root folder: " + cli.strContainerDestinationLocation);
        System.out.println("Number of containers:   " + cli.iNumberOfContainers);
        System.out.println("Tests per container:    " + cli.iNumberOfTestsPerContainer);

        final var amountOfContainers = cli.iNumberOfContainers;
        final var amountOfSources = cli.iNumberOfTestsPerContainer;

        //1. Initialize folder structure
        var containersFolder = new File(cli.strContainerDestinationLocation);
        if (!containersFolder.exists() && !containersFolder.mkdirs())
            throw new Exception("Unable to create containers folder");

        //Two threadpools are used, for two kinds of tasks. workerThreadPool is used for the actual work. The workCreatorThreadPool is used for tasks that are kind of "orchestrator" tasks. They define and schedule work on the workerThreadpool, but do not do anything intensive themselves. To be clear: even the worker threads delegate most of the work to another process such as a compiler, so they will also wait most of the time, so there are multiple layers of waiting.
        var testFolderPaths = new ArrayList<String>();
        int hardwareThreads = Runtime.getRuntime().availableProcessors();
        var workerThreadPool = Executors.newFixedThreadPool(hardwareThreads);
        var workCreatorThreadPool = Executors.newFixedThreadPool(hardwareThreads);

        // These nested loops create the folder structure
        // Create containers
        for (var containerIndex = 0; containerIndex < amountOfContainers; containerIndex++) {
            // Make package folder structure
            var containerFolderPath = IOElements.strContainerFullPath(containerIndex);
            var containerFolder = new File(containerFolderPath);
            if (!containerFolder.exists() && !containerFolder.mkdirs())
                throw new Exception("Unable to create package folder" + containerIndex);

            //For each container, create tests. Each test is a unique piece of source code that will be built with all compiler configurations.
            for (var testIndex = 0; testIndex < amountOfSources; testIndex++) {
                // testFolderPath is the directory of this particular c source. All executables and related files will be placed in this directory.
                var testFolderPath = IOElements.strTestFullPath(containerIndex, testIndex);
                var testFolder = new File(testFolderPath);
                if (!testFolder.exists() && !testFolder.mkdirs())
                    throw new Exception("Unable to create test folder" + testIndex);

                testFolderPaths.add(testFolderPath);
            }
        }

        // A single thread is used to generate all C code because it doesn't work well otherwise. There is at least one reason why that may be the case: the CodeMarker class has a global variable to keep track of the IDs it has generated, which is not accessed in a thread safe way. Easy solution: don't use multiple threads for this.
        // The main task needs to keep generating C code as fast as possible, to keep all threads busy. Therefore it doesn't wait for the followup tasks it created. Instead, their futures are added to a list, so that they can be waited for later.
        var futures = new ArrayList<Future>();
        Runnable main_task =  () -> {
            for (var testFolderPath : testFolderPaths)
            {
                var source_filenames = generate_source_code(testFolderPath);

                //  Start the build processes
                for (var config : CompilerConfig.configs) {
                    var future = workCreatorThreadPool.submit(() -> {
                        build_executable(testFolderPath, source_filenames, config, workerThreadPool);
                    });
                    futures.add(future);
                }
            }
        };

        //start and wait for the main task
        workerThreadPool.submit(main_task).get();
        System.out.println("all C sources generated");

        //wait for all the futures it has created
        for (var future : futures) {
            future.get();
        }
        System.out.println("all tasks finished");

        //The JVM keeps running forever. It is not clear which thread causes this, but a workaround for now is a hard exit.
        exit(0);
    }

    /**
     * Class to exchange command line data
     */
    private static class ProducerCLIParameters{
        public String strContainerDestinationLocation = "";
        public int iNumberOfContainers = 0;
        public int iNumberOfTestsPerContainer = 0;
    }

    /**
     * Handle command line arguments. When completely empty: do defaults. Show help is requested.
     * Exits program when errors are encountered or help is requested.
     * @param args command line argument array as provided to main()-function
     * @param cli output
     */
    private static void handleCLIParameters(String[] args, ProducerCLIParameters cli){
        // options
        final String STRROOTCONTAINEROPTION = "-c=";
        final String STRNCONTAINERS = "-nc=";
        final String STRNTESTSPERCONTAINER = "-ntc=";

        // set up basic interpretation parameters
        List<CommandLineUtils.ParameterDefinition> pmd = new ArrayList<>();
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "root_containers_folder",
                "location of the root folder where all the test containers will be located. " +
                        "If omitted, subfolder 'containers' of current folder is used.\n" +
                        "If you use *, the default setting from the class Environment is used.",
                new String[]{STRROOTCONTAINEROPTION, "/c="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "number_of_containers",
                "number of containers to be produced. If omitted, " + IDEFAULTNUMBEROFCONTAINERS +
                        " containers will be made. Integer value expected, decimal number.",
                new String[]{STRNCONTAINERS, "/nc="}, '?', IDEFAULTNUMBEROFCONTAINERS + ""
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "number_of_tests_per_containers",
                "number of tests per container. If omitted, " + IDEFAULTTESTSPERCONTAINER + "" +
                        " tests per container will be made.",
                new String[]{STRNTESTSPERCONTAINER, "/ntc="}, '?', IDEFAULTTESTSPERCONTAINER + ""
        ));
        // set up info
        var me = new CommandLineUtils("deb'm producer",
                "(c) 2023/2024 Jaap van den Bos, Kesava van Gelder, Reijer Klaasse",
                pmd);
        me.setGeneralHelp("This program produces C source codes and has these compiled to LLVM-IR and binaries.");
        // parse command line parameters (errors or requested help will stop the program by using exit(), so
        // it only returns if all is well)
        var a = me.parseCommandLineInput(args);

        // and use parsed data
        String strValue;

        // container base folder
        ////////////////////////
        strValue = strGetParameterValue(STRROOTCONTAINEROPTION, a);
        if (strValue==null){
            // omitted, use current folder with default subfolder
            cli.strContainerDestinationLocation = IOElements.strAdaptPathToMatchFileSystemAndAddSeparator(Environment.STRDEFAULTCONTAINERSROOTFOLDER);
        }
        else {
            if (strValue.equals("*")) {
                cli.strContainerDestinationLocation = Environment.containerBasePath;
            } else {
                // use argument
                cli.strContainerDestinationLocation = IOElements.strAdaptPathToMatchFileSystemAndAddSeparator(strValue);
            }
        }

        // check the number of containers
        /////////////////////////////////
        strValue = strGetParameterValue(STRNCONTAINERS, a);
        assert strValue!=null;          // safe, as this has a default value
        // try to interpret
        long val = Misc.lngRobustStringToLong(strValue, Long.MIN_VALUE);
        if (val == Long.MIN_VALUE){
            me.printError("Conversion to number failed for number of containers (" + strValue + ")" );
        }
        if (val <= 0 ){
            me.printError("Number of containers must at least be 1 (" + strValue + ")");
        }
        cli.iNumberOfContainers = (int)val;

        // check the number of tests per container
        //////////////////////////////////////////
        strValue = strGetParameterValue(STRNTESTSPERCONTAINER, a);
        assert strValue!=null;          // safe, as this has a default value
        // try to interpret
        val = Misc.lngRobustStringToLong(strValue, Long.MIN_VALUE);
        if (val == Long.MIN_VALUE){
            me.printError("Conversion to number failed for number of tests per container (" + strValue + ")" );
        }
        if (val <= 0 ){
            me.printError("Number of tests per containers must at least be 1 (" + strValue + ")");
        }
        cli.iNumberOfTestsPerContainer = (int)val;

        // program header
        me.printProgramHeader();
    }
}