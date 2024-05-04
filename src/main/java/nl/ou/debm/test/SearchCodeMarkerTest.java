package nl.ou.debm.test;

import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.Environment;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.LLVMIRLexer;
import nl.ou.debm.common.antlr.LLVMIRParser;
import nl.ou.debm.common.feature1.LoopCListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

public class SearchCodeMarkerTest {

    @Test
    public void ProcessAssignments() throws Exception{
        IAssessor.CodeInfo ci = new IAssessor.CodeInfo();
        ci.clexer_dec = new CLexer(CharStreams.fromFileName(Environment.containerBasePath + "test.c"));
        ci.cparser_dec = new CParser(new CommonTokenStream(ci.clexer_dec));
        ci.llexer_org = new LLVMIRLexer(CharStreams.fromFileName(Environment.containerBasePath + "empty.ll"));
        ci.lparser_org = new LLVMIRParser(new CommonTokenStream(ci.llexer_org));

        var tree = ci.cparser_dec.compilationUnit();
        var walker = new ParseTreeWalker();
        var listener = new LoopCListener(ci);
        walker.walk(listener, tree);

    }
}
