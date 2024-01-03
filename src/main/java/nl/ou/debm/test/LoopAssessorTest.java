package nl.ou.debm.test;

import nl.ou.debm.common.Environment;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.LLVMIRLexer;
import nl.ou.debm.common.antlr.LLVMIRParser;
import nl.ou.debm.common.feature1.LoopCListener;
import nl.ou.debm.common.feature1.LoopLLVMListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

public class LoopAssessorTest {

    @Test
    public void BasicLoopAssessorTesting() throws Exception{
        final String STR_C_DECOMPILED = Environment.containerBasePath + "decoded-c.c";
        final String STR_LLVM_COMPILED = Environment.containerBasePath + "original_llvm.ll";

        var clexer = new CLexer(CharStreams.fromFileName(STR_C_DECOMPILED));
        var cparser = new CParser(new CommonTokenStream(clexer));

        var llexer = new LLVMIRLexer(CharStreams.fromFileName(STR_LLVM_COMPILED));
        var lparser = new LLVMIRParser(new CommonTokenStream(llexer));

        var ltree = lparser.compilationUnit();
        var lwalker = new ParseTreeWalker();
        var llistener = new LoopLLVMListener();

        lwalker.walk(llistener, ltree);


        var ctree = cparser.compilationUnit();
        var cwalker = new ParseTreeWalker();
        var clistener = new LoopCListener();
        //cwalker.walk(clistener, ctree);

    }
}
