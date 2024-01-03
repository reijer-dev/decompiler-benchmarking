package nl.ou.debm.test;

import nl.ou.debm.common.*;
import nl.ou.debm.common.antlr.*;
import nl.ou.debm.producer.CGenerator;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class GeneratorTest {

    @Test
    void GetOneBinaryDone() throws Exception
    {
        //1. Initialize folder structure
        var containersFolder = new File(Environment.containerBasePath);
        if (!containersFolder.exists() && !containersFolder.mkdirs())
            throw new Exception("Unable to create containers folder");

        //for (var containerIndex = 0; containerIndex < amountOfContainers; containerIndex++) {
        { int containerIndex = 0;
            //2. Make package folder structure
            var containerFolderPath = IOElements.strContainerFullPath(containerIndex);
            var containerFolder = new File(containerFolderPath);
            if (!containerFolder.exists() && !containerFolder.mkdirs())
                throw new Exception("Unable to create package folder" + containerIndex);

            //3. Generate C-sources
            var EXEC = Executors.newCachedThreadPool();
            var tasks = new ArrayList<Callable<String>>();
            //for (var testIndex = 0; testIndex < amountOfSources; testIndex++) {
            { int testIndex = 0;
                var testFolderPath = IOElements.strTestFullPath(containerIndex, testIndex);
                var testFolder = new File(testFolderPath);
                if (!testFolder.exists() && !testFolder.mkdirs())
                    throw new Exception("Unable to create test folder" + testIndex);

                var sourceFilePath = IOElements.strCSourceFullFilename(containerIndex, testIndex);
                new CGenerator().generateSourceFile(sourceFilePath);

                System.out.println("Source = " + sourceFilePath);

                //4. call compiler(s)
                for (var compiler : ECompiler.values()) {
                    var cLangLocation = compiler.strCommandLocation();
                    for (var architecture : EArchitecture.values()) {
                        for (var optimize : EOptimize.values()) {
                            var binaryPath = IOElements.strBinaryFullFileName(containerIndex, testIndex, architecture, compiler, optimize);
                            var llvmPath = IOElements.strLLVMFullFileName(containerIndex, testIndex, architecture, compiler, optimize);
                            //Generate binary
                            tasks.add(() -> {
                                var ps = new ProcessBuilder(cLangLocation,
                                        sourceFilePath,
                                        compiler.strOutputSwitch(),
                                        binaryPath,
                                        compiler.strArchitectureFlag(architecture),
                                        compiler.strOptFlag(optimize));
                                System.out.println(ps.command().toString());
                                ps.redirectErrorStream(true);
                                var compilationProcess = ps.start();
                                var reader = new BufferedReader(new InputStreamReader(compilationProcess.getInputStream()));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    System.out.println(line);
                                }
                                compilationProcess.waitFor();
                                return binaryPath;
                            });
                            //Generate LLVM
//                            tasks.add(() -> {
//                                var ps = new ProcessBuilder(cLangLocation,
//                                        sourceFilePath, "-S", "-emit-llvm",
//                                        compiler.strOutputSwitch(),
//                                        llvmPath,
//                                        compiler.strArchitectureFlag(architecture),
//                                        compiler.strOptFlag(optimize));
//                                System.out.println(ps.command().toString());
//                                ps.redirectErrorStream(true);
//                                var compilationProcess = ps.start();
//                                var reader = new BufferedReader(new InputStreamReader(compilationProcess.getInputStream()));
//                                String line;
//                                while ((line = reader.readLine()) != null) {
//                                    System.out.println(line);
//                                }
//                                compilationProcess.waitFor();
//                                return llvmPath;
//                            });
                            break;
                        }
                        break;
                    }
                    break;
                }
                var results = EXEC.invokeAll(tasks);

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
