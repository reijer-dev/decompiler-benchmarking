package org.example;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        var amountOfContainers = 2;
        var amountOfSources = 3;

        //1. Initialize folder structure
        var containersFolder = new File("C:\\Users\\reije\\OneDrive\\Documenten\\Development\\c-program\\containers");
        if (!containersFolder.exists() && !containersFolder.mkdirs())
            throw new Exception("Unable to create containers folder");

        for (var containerIndex = 0; containerIndex < amountOfContainers; containerIndex++) {
            //2. Make package folder structure
            var containerFolderPath = Paths.get(containersFolder.getAbsolutePath(), "container_"+containerIndex).toString();
            var containerFolder = new File(containerFolderPath);
            if (!containerFolder.exists() && !containerFolder.mkdirs())
                throw new Exception("Unable to create package folder" + containerIndex);

            //3. Generate C-sources
            var EXEC = Executors.newCachedThreadPool();
            var tasks = new ArrayList<Callable<String>>();
            for (var i = 0; i < amountOfSources; i++) {
                var testFolderPath = Paths.get(containerFolderPath, "test_"+i).toString();
                var testFolder = new File(testFolderPath);
                if (!testFolder.exists() && !testFolder.mkdirs())
                    throw new Exception("Unable to create test folder" + i);

                var sourceFilePath = Paths.get(testFolderPath, "source.c").toString();
                new CGenerator().generateSourceFile(sourceFilePath);

                //4. Call GCC compiler
                var marchs = new String[]{"x86-64", "native"};
                var oFlags = new String[]{"-O0", "-O3"};
                for (var march : marchs) {
                    for (var oFlag : oFlags) {
                        var binaryPath = Paths.get(testFolderPath,"gcc_" + march + oFlag + ".exe").toString();
                        tasks.add(() -> {
                            var ps = new ProcessBuilder("C:\\MinGW\\bin\\gcc.exe", sourceFilePath, "-o", binaryPath, "-march="+march, oFlag);
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
                    }
                }
                var results = EXEC.invokeAll(tasks);
            }

        }
    }
}