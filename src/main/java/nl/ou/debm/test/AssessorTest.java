package nl.ou.debm.test;

import nl.ou.debm.assessor.Assessor;
import nl.ou.debm.common.Environment;
import nl.ou.debm.common.IOElements;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class AssessorTest {
    @Test
    public void doesAPerfectDecompilerGetFullScore() throws Exception {
        //We copy one existing container with one test
        var strDecompileScript = Paths.get(new File("").getAbsolutePath(), "scripts", Environment.strGetPerfectDecompilerScript()).toString();

        // get name of the decompiler-script and test its existence & executableness
        if (!Files.isExecutable(Paths.get(strDecompileScript))) {
            fail("Decompilation script (" + strDecompileScript + ") does not exist or is not executable.");
        }
        // setup temporary folder to copy the test files to
        var tempDir = Files.createTempDirectory("debm");

        //Look up first container dir
        var firstContainer = Files.list(Path.of(Environment.containerBasePath)).findFirst();
        if(!firstContainer.isPresent())
            fail("No containers found!");

        //Look up first test dir
        var firstTest = Files.list(firstContainer.get()).findFirst();
        if(!firstTest.isPresent())
            fail("No tests found!");

        //Create temp container + test dir
        var containerDirPath = Paths.get(tempDir.toString(), firstContainer.get().getFileName().toString());
        var testDirPath = Paths.get(containerDirPath.toString(), firstTest.get().getFileName().toString());
        if(!new File(testDirPath.toString()).mkdirs())
            fail("Failed to create test container directory");

        //Look up first binary file
        var firstBinary = Files.list(firstTest.get()).filter(x -> x.toString().endsWith(".exe") || !x.toString().contains(".")).findFirst();
        if(!firstBinary.isPresent())
            fail("No binary found in " + testDirPath);

        //Look up corresponding LLVM file
        var binaryFileName = firstBinary.get().getFileName().toString();
        var llvmFileName = binaryFileName.replace("binary_", "llvm_").replace(".exe", ".llvm");
        var llvmFile = new File(Paths.get(firstTest.get().toString(), llvmFileName).toString());
        if(!llvmFile.exists())
            fail("No LLVM found in " + testDirPath);

        //Look up the C source file
        var sourceFile = Files.list(firstTest.get()).filter(x -> x.getFileName().toString().equals("source.c")).findFirst();
        if(!sourceFile.isPresent())
            fail("No source.c found in " + testDirPath);

        //Copy all needed files to our temp container file
        Files.copy(llvmFile.toPath(), Paths.get(testDirPath.toString(), llvmFileName), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(firstBinary.get(), Paths.get(testDirPath.toString(), binaryFileName), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile.get(), Paths.get(testDirPath.toString(), sourceFile.get().getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);

        //Run assessor with our temp container file and our perfect decompiler
        var ass = new Assessor();
        var results = ass.RunTheTests(tempDir.toString(), strDecompileScript, -1,true);

        // Remove temp dir
        IOElements.bFolderAndAllContentsDeletedOK(tempDir);

        //Check for full score
        for (var testResult : results) {
            assertEquals(testResult.dblGetActualValue(), testResult.dblGetTarget());
        }
    }
}
