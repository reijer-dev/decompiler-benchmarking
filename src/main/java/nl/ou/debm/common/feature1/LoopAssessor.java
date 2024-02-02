package nl.ou.debm.common.feature1;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;


public class LoopAssessor implements IAssessor {

    public LoopAssessor() {

    }

    @Override
    public List<SingleTestResult> GetTestResultsForSingleBinary(CodeInfo ci) {
        var tr = new SingleTestResult(ETestCategories.FEATURE1_AGGREGATED, ci.compilerConfig);
        tr.dblLowBound = 0;
        tr.dblActualValue = 15;
        tr.dblHighBound = 15;

        useWalker(ci);
        //useVisitor(ci);

        final List<SingleTestResult> out = new ArrayList<>();
        out.add(tr);
        return out;
    }

    private void useWalker(CodeInfo ci) {
        var tree = ci.cparser_dec.compilationUnit();
        var walker = new ParseTreeWalker();
        var listener = new LoopCListener(ci);

        walker.walk(listener, tree);

    }

    public List<SingleTestResult> BasicLoopTesting(CodeInfo ci) {
        var tree = ci.cparser_dec.compilationUnit();
        var walker = new ParseTreeWalker();
        var listener = new LoopCListener(ci);

        walker.walk(listener, tree);

        return listener.getTestResults();
    }
}