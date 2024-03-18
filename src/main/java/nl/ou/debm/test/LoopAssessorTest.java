package nl.ou.debm.test;

import nl.ou.debm.assessor.Assessor;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.*;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.LLVMIRLexer;
import nl.ou.debm.common.antlr.LLVMIRParser;
import nl.ou.debm.common.feature1.LoopAssessor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LoopAssessorTest {

    private String strTestSetPath(){
        String strOutput;
        for (int x=999; x>=0 ; --x){
            strOutput = Environment.containerBasePath + "testset_" + Misc.strGetNumberWithPrefixZeros(x, 3) + "/";
            if (IOElements.bFolderExists(strOutput)){
                return strOutput;
            }
        }
        return Environment.containerBasePath + "testset_000/";
    }

    @Test
    public void BasicFindLoops () {
        /*

            This function is not a test in the sense that it asserts a lot.
            It is written to easily determine what happens when certain binaries are tested.

            Use the script 'make-single-testfolder.sh' first. That will create a folder containing
            - a single c source
            - the corresponding binaries and LLVM's
            - decompiled versions, invoking all decompilation scripts in the script folder

            This saves a lot of time when trying and testing - you don't need to decompile with
            every test.

            There are no assertions in this test, which means it is one big assertion: the test
            should not throw any exception.

         */

        final String[] STR_ARCH = {"x64", "x86"};
        final String[] STR_OPT = {"opt", "nop"};
        final String[] STR_DECOMPILER = {"binaryninja-online",  // 0
                                        "hexrays-online",       // 1
                                        "recstudio-online",     // 2
                                        "reko-online",          // 3
                                        "retdec",               // 4
                                        "snowman-online"};      // 5

        final int[] deci = {4};

        StringBuilder h = new StringBuilder(), c = new StringBuilder(), f = new StringBuilder();
        Assessor.getHTMLHeaderAndFooter(h, f);
        for (int opt = 0; opt<1; opt++){
            for (var i : deci){
                c = AssessOneSingleBinary(STR_ARCH[0], STR_OPT[opt], STR_DECOMPILER[i]);
                h.append(c).append("<br><br>");
            }
        }
        h.append(f);

        var strFilename = "/home/jaap/VAF/containers/output.html";
        IOElements.writeToFile(h, strFilename);

        try {
            Desktop.getDesktop().open(new File(strFilename));
        }
        catch (Exception ignore){}
    }

    private StringBuilder AssessOneSingleBinary(final String STR_ARCH, final String STR_OPT, final String STR_DECOMPILER){

        final String STR_C_DECOMPILED = strTestSetPath() + "binary_" + STR_ARCH + "_cln_" + STR_OPT + ".exe---" + STR_DECOMPILER + ".c";
        final String STR_LLVM_COMPILED = strTestSetPath() +  "llvm_" + STR_ARCH + "_cln_" + STR_OPT + ".ll";

        var ci = new IAssessor.CodeInfo();
        try {
            ci.clexer_dec = new CLexer(CharStreams.fromFileName(STR_C_DECOMPILED));
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        ci.cparser_dec = new CParser(new CommonTokenStream(ci.clexer_dec));

        try {
            ci.llexer_org = new LLVMIRLexer(CharStreams.fromFileName(STR_LLVM_COMPILED));
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
        ci.lparser_org = new LLVMIRParser(new CommonTokenStream(ci.llexer_org));

        ci.compilerConfig.architecture= EArchitecture.X64ARCH;
        ci.compilerConfig.compiler= ECompiler.CLANG;
        ci.compilerConfig.optimization= EOptimize.NO_OPTIMIZE;

        var a = new LoopAssessor();
        System.out.println("Infile: " + STR_C_DECOMPILED);
        var q = a.GetTestResultsForSingleBinary(ci);
        Map<String, String> pars = new HashMap<>();
        pars.put("C decompiled", STR_C_DECOMPILED);
        pars.put("LLVM compiled", STR_LLVM_COMPILED);

        return Assessor.generateReport(pars, q);
    }
}
