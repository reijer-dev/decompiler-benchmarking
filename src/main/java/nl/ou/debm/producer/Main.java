package nl.ou.debm.producer;

import nl.ou.debm.common.*;
import nl.ou.debm.common.task.ProcessTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.System.exit;
import static nl.ou.debm.common.CommandLineUtils.strGetParameterValue;
import static nl.ou.debm.common.ProjectSettings.IDEFAULTNUMBEROFCONTAINERS;
import static nl.ou.debm.common.ProjectSettings.IDEFAULTTESTSPERCONTAINER;

public class Main {

    private static final List<IBuildExecutable> s_exeBuilders = new ArrayList<>();

    static {
        s_exeBuilders.add(new ExeBuildUsingClang());
    }

    private static final List<ProcessTask.ProcessResult> s_processErrorList = new ArrayList<>();

    // Creates files and returns a list of their names
    public static List<String> generate_source_code(String destination) {
        System.out.println("generating C source files for destination " + destination + " ...");

        var cFileContents = new CGenerator().generateSourceFiles();
        System.out.println("generating C source files for destination " + destination + " done");
        if(cFileContents.keySet().isEmpty()) throw new RuntimeException("no source files returned");

        System.out.print("writing C source files to " + destination + "... ");
        var filenames_to_compile = new ArrayList<String>();
        for (var filename : cFileContents.keySet()) {
            String fullPath = destination + filename;
            String content = cFileContents.get(filename);
            IOElements.writeToFile(content, fullPath);
            if (!filename.equals(IOElements.cAmalgamationFilename)) {
                filenames_to_compile.add(filename);
            }
        }
        System.out.println("done");
        return filenames_to_compile;
    }

    public static void build_executable(String source_location, Collection<String> source_filenames, CompilerConfig config, ExecutorService workerThreadPool) {
        IBuildExecutable exe = config.compiler.exeBuilder();
        if (exe == null) {
            throw new RuntimeException("Requested compiler (" + config.compiler.strCompilerDescription() + ") is not supported.");
        }
        else {
            final List<ProcessTask.ProcessResult> processErrorList = exe.build_executable(source_location, source_filenames, config, workerThreadPool);
            assert processErrorList != null;
            synchronized (s_processErrorList) {
                s_processErrorList.addAll(processErrorList);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // handle command line parameters --> handleCLIParameters exits on help or errors
        var cli = new ProducerCLIParameters();
        handleCLIParameters(args, cli);

        // show program name & parameters
        System.out.println("Containers root folder: " + cli.strContainerDestinationLocation);
        System.out.println("Number of containers:   " + cli.iNumberOfContainers);
        System.out.println("Tests per container:    " + cli.iNumberOfTestsPerContainer);

        // check compiler availability
        boolean bAllOK = true;
        for (var config : CompilerConfig.configs) {
            if (!config.bAreAllCompilerComponentsAvailable(true)){
                bAllOK = false;
                break;
            }
        }
        if (!bAllOK){
            exit(1);
        }

        // set the number of containers and sources
        final var amountOfContainers = cli.iNumberOfContainers;
        final var amountOfSources = cli.iNumberOfTestsPerContainer;

        //1. Initialize folder structure
        var containersFolder = new File(cli.strContainerDestinationLocation);
        if (!containersFolder.exists() && !containersFolder.mkdirs())
            throw new Exception("Unable to create containers folder");
        Environment.containerBasePath = containersFolder.toString();

        // Two threadpools are used, for two kinds of tasks. workerThreadPool is used for the actual work.
        // The workCreatorThreadPool is used for tasks that are kind of "orchestrator" tasks.
        // They define and schedule work on the workerThreadpool, but do not do anything intensive themselves.
        // To be clear: even the worker threads delegate most of the work to another process such as a compiler,
        // so they will also wait most of the time, so there are multiple layers of waiting.
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

            //For each container, create test folders.
            // Each test is a unique piece of source code that will be built with all compiler configurations.
            for (var testIndex = 0; testIndex < amountOfSources; testIndex++) {
                // testFolderPath is the directory of this particular c source. All executables and related files will be placed in this directory.
                var testFolderPath = IOElements.strTestFullPath(containerIndex, testIndex);
                var testFolder = new File(testFolderPath);
                if (!testFolder.exists() && !testFolder.mkdirs())
                    throw new Exception("Unable to create test folder" + testIndex);

                testFolderPaths.add(testFolderPath);
            }
        }

        // A single thread is used to generate all C code because it doesn't work well otherwise.
        // Producer code may not always be thread safe.
        // Easy solution: don't use multiple threads for this.
        // The main task needs to keep generating C code as fast as possible, to keep all threads busy.
        // Therefore, it doesn't wait for the followup tasks it created.
        // Instead, their futures are added to a list, so that they can be waited for later.
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

        // report errors
        // (keep access synchronized, as there is some stray thread running)
        synchronized (s_processErrorList){
            if (!s_processErrorList.isEmpty()) {
                var output = new StringBuilder();
                int cnt=0;
                output.append("***************\n");
                output.append("*** WARNING ***\n");
                output.append("***************\n");
                output.append("\n");
                output.append("The compilation process led to errors coming from the compilation tools.\n");
                output.append("These errors are shown below.\n");
                output.append("\n");
                output.append("Total number of errors: ").append(s_processErrorList.size()).append("\n");
                output.append("\n");
                for (var item : s_processErrorList) {
                    output.append("----------------------------------------------------------------------------------------------- ");
                    output.append(++cnt).append("\n");
                    output.append(item.toString()).append("\n");
                    output.append("\n");
                }
                output.append("\n");        // intentionally added number of errors twice, because one usually starts looking at the end
                                            // of the output, and it should be at the start as well.
                output.append("Total number of errors: ").append(s_processErrorList.size()).append("\n");
                System.err.println(output);
            }
        }

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