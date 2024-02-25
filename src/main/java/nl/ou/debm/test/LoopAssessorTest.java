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
import java.util.ArrayList;

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
    public void TestCodeMarkerInfoFromLLVM() throws Exception{
        final String STR_C_DECOMPILED = strTestSetPath() + "binary_x64_cln_nop.exe---retdec.c";
        final String STR_LLVM_COMPILED = strTestSetPath() +  "llvm_x64_cln_nop.llvm";

        var clexer = new CLexer(CharStreams.fromFileName(STR_C_DECOMPILED));
        var cparser = new CParser(new CommonTokenStream(clexer));

        var llexer = new LLVMIRLexer(CharStreams.fromFileName(STR_LLVM_COMPILED));
        var lparser = new LLVMIRParser(new CommonTokenStream(llexer));

        var info = CodeMarker.getCodeMarkerInfoFromLLVM(lparser);
        System.out.println(info.size());
        for (var item : info.entrySet()){
            System.out.println(item.getKey() + "--" + item.getValue().iNOccurrencesInLLVM + ", " + item.getValue().strLLVMFunctionNames +
                    ", " + item.getValue().codeMarker.toString());
        }

    }


    @Test
    public void BasicFindLoops () throws Exception{
        final String STR_ARCH = "x64";
//        final String STR_OPT="opt";
        final String STR_OPT="nop";
//        final String STR_DECOMPILER="hexrays-online";
        final String STR_DECOMPILER="retdec";


        final String STR_C_DECOMPILED = strTestSetPath() + "binary_" + STR_ARCH + "_cln_" + STR_OPT + ".exe---" + STR_DECOMPILER + ".c";
//        final String STR_C_DECOMPILED = strTestSetPath() + "source.c";
        final String STR_LLVM_COMPILED = strTestSetPath() +  "llvm_" + STR_ARCH + "_cln_" + STR_OPT + ".llvm";

        var ci = new IAssessor.CodeInfo();
        ci.clexer_dec = new CLexer(CharStreams.fromFileName(STR_C_DECOMPILED));
        ci.cparser_dec = new CParser(new CommonTokenStream(ci.clexer_dec));

        ci.llexer_org = new LLVMIRLexer(CharStreams.fromFileName(STR_LLVM_COMPILED));
        ci.lparser_org = new LLVMIRParser(new CommonTokenStream(ci.llexer_org));

        ci.compilerConfig.architecture= EArchitecture.X64ARCH;
        ci.compilerConfig.compiler= ECompiler.CLANG;
        ci.compilerConfig.optimization= EOptimize.NO_OPTIMIZE;

        var a = new LoopAssessor();
        System.out.println("Infile: " + STR_C_DECOMPILED);
        var q = a.GetTestResultsForSingleBinary(ci);

        var strFilename = "/home/jaap/VAF/containers/output.html";
        Assessor.generateReport(q, strFilename);

        Desktop.getDesktop().open(new File(strFilename));
    }
}
