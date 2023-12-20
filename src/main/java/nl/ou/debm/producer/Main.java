package nl.ou.debm.producer;

import nl.ou.debm.common.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {

        final var amountOfContainers = 1;
        final var amountOfSources = 1;

        //1. Initialize folder structure
        var containersFolder = new File(Environment.containerBasePath);
        if (!containersFolder.exists() && !containersFolder.mkdirs())
            throw new Exception("Unable to create containers folder");

        for (var containerIndex = 0; containerIndex < amountOfContainers; containerIndex++) {
            //2. Make package folder structure
            var containerFolderPath = IOElements.strContainerFullPath(containerIndex);
            var containerFolder = new File(containerFolderPath);
            if (!containerFolder.exists() && !containerFolder.mkdirs())
                throw new Exception("Unable to create package folder" + containerIndex);

            //3. Generate C-sources
            var EXEC = Executors.newCachedThreadPool(); //todo leidt tot problemen als er heel veel taken zijn. Zie https://www.baeldung.com/java-executors-cached-fixed-threadpool "The cached pool starts with zero threads and can potentially grow to have Integer.MAX_VALUE threads". Alternatief: newFixedThreadPool(n), alleen je moet dan zelf opgeven hoe veel threads (n) hij maximaal mag aanmaken, bijv. het aantal cpus in de computer (hoe krijg je dat aantal te pakken in java?)
            var tasks = new ArrayList<Callable<String>>();
            for (var testIndex = 0; testIndex < amountOfSources; testIndex++) {
                var testFolderPath = IOElements.strTestFullPath(containerIndex, testIndex);
                var testFolder = new File(testFolderPath);
                if (!testFolder.exists() && !testFolder.mkdirs())
                    throw new Exception("Unable to create test folder" + testIndex);

                var sourceFilePath = IOElements.strCSourceFullFilename(containerIndex, testIndex);

                System.out.print("generating C source file");
                String program = new CGenerator().generateSourceFile();
                System.out.println(" done");
                System.out.print("writing C source file");
                IOElements.writeToFile(program, sourceFilePath);
                System.out.println(" done");

                //4. call compiler(s)
                for (CompilerConfig config : CompilerConfig.configs)
                {
                    var binaryPath = IOElements.strBinaryFullFileName(containerIndex, testIndex, config.arch, config.compiler, config.optimization);
                    var llvmPath = IOElements.strLLVMFullFileName(containerIndex, testIndex, config.arch, config.compiler, config.optimization);

                    //task definition for either binary or LLVM IR generation
                    final var mode_LLVM_IR = true;
                    final var mode_binary = false;
                    java.util.function.Function<Boolean, Callable<String>> compilationTask = (mode) -> {
                        return () -> {
                            List<String> parameters;
                            String targetFilePath;
                            boolean generate_LLVM_IR;

                            if (mode == mode_binary) {
                                targetFilePath = binaryPath;
                                generate_LLVM_IR = false;
                            }
                            else {
                                targetFilePath = llvmPath;
                                generate_LLVM_IR = true;
                            }
                            //todo parameters bepalen
                            parameters = config.compileCommandParameters(sourceFilePath, targetFilePath, generate_LLVM_IR);

                            var ps = new ProcessBuilder(parameters);
                            System.out.println(ps.command().toString());
                            ps.redirectErrorStream(true);
                            var compilationProcess = ps.start();
                            var reader = new BufferedReader(new InputStreamReader(compilationProcess.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println(line);
                            }
                            int returncode = compilationProcess.waitFor();
                            if (returncode != 0) {
                                String errorMessage = "Compiler exit code " + returncode + ", parameters: " + parameters;
                                System.out.println(errorMessage);
                                throw new Exception(errorMessage);
                            }
                            return targetFilePath;
                        };
                    };

                    tasks.add( compilationTask.apply(mode_binary) );
                    tasks.add( compilationTask.apply(mode_LLVM_IR) );
                }

                System.out.print("compiling ");
                var results = EXEC.invokeAll(tasks);
                System.out.println(" done");
            }
        }
    }
}