package nl.ou.debm.test;

import nl.ou.debm.assessor.Assessor;
import nl.ou.debm.assessor.EAssessorWorkModes;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.*;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.LLVMIRLexer;
import nl.ou.debm.common.antlr.LLVMIRParser;
import nl.ou.debm.common.assembly.AssemblyAnalyzer;
import nl.ou.debm.common.feature5.IndirectionsAssessor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static nl.ou.debm.common.IOElements.cAmalgamationFilename;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        var firstTest = Files.list(firstContainer.get()).filter(x -> x.getFileName().toString().startsWith("test_")).findFirst();
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
        var llvmFileName = binaryFileName.replace("binary_", "llvm_").replace(".exe", ".ll");
        var llvmFile = new File(Paths.get(firstTest.get().toString(), llvmFileName).toString());
        if(!llvmFile.exists())
            fail("No LLVM found in " + testDirPath);

        //Look up corresponding assembly file
        var asmFileName = binaryFileName.replace("binary_", "assembly_").replace(".exe", ".s");
        var asmFile = new File(Paths.get(firstTest.get().toString(), asmFileName).toString());
        if(!asmFile.exists())
            fail("No assembly found in " + testDirPath);

        //Look up the C source file
        var sourceFile = Files.list(firstTest.get()).filter(x -> x.getFileName().toString().equals(cAmalgamationFilename)).findFirst();
        if(!sourceFile.isPresent())
            fail("No amalgamation.c found in " + testDirPath);

        //Copy all needed files to our temp container file
        Files.copy(asmFile.toPath(), Paths.get(testDirPath.toString(), asmFileName), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(llvmFile.toPath(), Paths.get(testDirPath.toString(), llvmFileName), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(firstBinary.get(), Paths.get(testDirPath.toString(), binaryFileName), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(sourceFile.get(), Paths.get(testDirPath.toString(), sourceFile.get().getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);

        //Run assessor with our temp container file and our perfect decompiler
        var ass = new Assessor();
        var results = ass.RunTheTests(tempDir.toString(), strDecompileScript, 0,true,
                EAssessorWorkModes.DECOMPILE_AND_ASSESS, true, -1);

        // Remove temp dir
        IOElements.bFolderAndAllContentsDeletedOK(tempDir);

        //Check for full score
        for (var testResult : results) {
            if(testResult.dblGetActualValue() != null && !Objects.equals(testResult.dblGetTarget(), testResult.dblGetActualValue())){
                System.out.println("Test " + testResult.getWhichTest());
            }
            assertTrue(testResult.dblGetActualValue() == null || Objects.equals(testResult.dblGetActualValue(), testResult.dblGetTarget()));
        }
    }

    @Test
    public void SwitchAssessor() throws Exception{
//        Map<Long, SwitchInfo> map = new HashMap<>();
//        var lexer = new LLVMIRLexer(CharStreams.fromFileName("/home/jaap/VAF/containers/container_000/test_000/llvm_x64_cln_nop.ll"));
//        var parser = new LLVMIRParser(new CommonTokenStream(lexer));
//        var tree = parser.compilationUnit();
//        var walker = new ParseTreeWalker();
//        var listener = new IndirectionLLVMListener(map, parser);
//        walker.walk(listener, tree);

        final String strPath = "/home/jaap/VAF/containers/container_000/test_007/";
        final String strOpt = "opt";
        final String strArch = "x86";


        final String strCode = strArch + "_cln_" + strOpt;
        IAssessor.CodeInfo ci = new IAssessor.CodeInfo();
        ci.llexer_org = new LLVMIRLexer(CharStreams.fromFileName(strPath + "llvm_" + strCode + ".ll"));
        ci.lparser_org = new LLVMIRParser((new CommonTokenStream(ci.llexer_org)));
        ci.clexer_dec = new CLexer(CharStreams.fromFileName(strPath + "binary_" + strCode + "-run-RetDec.c"));
        ci.cparser_dec = new CParser(new CommonTokenStream(ci.clexer_dec));
        ci.strAssemblyFilename = strPath + "assembly_" + strCode + ".s";
        ci.compilerConfig.architecture= EArchitecture.X64ARCH;
        ci.compilerConfig.compiler= ECompiler.CLANG;
        ci.compilerConfig.optimization= EOptimize.NO_OPTIMIZE;
        ci.assemblyInfo = AssemblyAnalyzer.getInfo(ci);
        var a = new IndirectionsAssessor();
        a.GetTestResultsForSingleBinary(ci);
    }
}
