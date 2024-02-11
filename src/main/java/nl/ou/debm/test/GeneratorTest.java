package nl.ou.debm.test;

import nl.ou.debm.common.*;
import nl.ou.debm.common.antlr.*;
import nl.ou.debm.common.feature1.LoopProducer;
import nl.ou.debm.producer.CGenerator;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class GeneratorTest {

    @Test
    void GetOneBinaryDone() throws Exception
    {
        final int containerIndex = 0;
        final int testIndex = 0;

        LoopProducer.setSatisfactionFractionManually(.5);  // make sure that this part of all the loops are included


        //1. Initialize folder structure
        var containersFolder = new File(Environment.containerBasePath);
        if (!containersFolder.exists() && !containersFolder.mkdirs())
            throw new Exception("Unable to create containers folder");

//        for (var containerIndex = 0; containerIndex < amountOfContainers; containerIndex++) {
        {
            //2. Make package folder structure
            var containerFolderPath = IOElements.strContainerFullPath(containerIndex);
            var containerFolder = new File(containerFolderPath);
            if (!containerFolder.exists() && !containerFolder.mkdirs())
                throw new Exception("Unable to create package folder" + containerIndex);

            //3. Generate C-sources
            int hardwareThreads = Runtime.getRuntime().availableProcessors();
            var EXEC = Executors.newFixedThreadPool(hardwareThreads);
            var tasks = new ArrayList<Callable<String>>();
//            for (var testIndex = 0; testIndex < amountOfSources; testIndex++) {
            {
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
                    var binaryPath = IOElements.strBinaryFullFileName(containerIndex, testIndex, config.architecture, config.compiler, config.optimization);
                    var llvmPath = IOElements.strLLVMFullFileName(containerIndex, testIndex, config.architecture, config.compiler, config.optimization);

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
                //todo after this the compilation processes have all ended, yet the program still hangs for ~1 minute

                /////////// check c-code in parser

                var lexer = new CLexer(CharStreams.fromFileName(sourceFilePath));
                var parser = new CParser(new CommonTokenStream(lexer));

                var walker = new ParseTreeWalker();
                var listener = new CBaseListener();

                System.out.println("Walking...");

                assertDoesNotThrow(() -> walker.walk(listener, parser.compilationUnit()));

                //break;
            }
        }
    }
}
