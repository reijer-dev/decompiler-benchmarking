package nl.ou.debm.test;

import nl.ou.debm.common.*;
import nl.ou.debm.producer.CGenerator;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class GeneratorTest {

    @Test
    void GetOneBinaryDone() throws Exception
    {
        final var amountOfContainers = 1;
        final var amountOfSources = 1;

        //1. Initialize folder structure
        // set base path for all container operations, use a hack to differentiate between Reijer & Jaap & Kesava
        IOElements.setBasePath(Misc.strGetContainersBaseFolder());
        var containersFolder = new File(IOElements.getBasePath());
        if (!containersFolder.exists() && !containersFolder.mkdirs())
            throw new Exception("Unable to create containers folder");

        for (var containerIndex = 0; containerIndex < amountOfContainers; containerIndex++) {
            //2. Make package folder structure
            var containerFolderPath = IOElements.strContainerFullPath(containerIndex);
            var containerFolder = new File(containerFolderPath);
            if (!containerFolder.exists() && !containerFolder.mkdirs())
                throw new Exception("Unable to create package folder" + containerIndex);

            //3. Generate C-sources
            var EXEC = Executors.newCachedThreadPool();
            var tasks = new ArrayList<Callable<String>>();
            for (var testIndex = 0; testIndex < amountOfSources; testIndex++) {
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
                break;
            }

        }
    }
}
