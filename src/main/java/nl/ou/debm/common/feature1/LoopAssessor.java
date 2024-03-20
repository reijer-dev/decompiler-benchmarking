package nl.ou.debm.common.feature1;

import nl.ou.debm.assessor.Assessor;
import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * This class assesses C code and produces results for loops
 * <br>
 * The hard work is done by the LoopCListener class
 */
public class LoopAssessor implements IAssessor {

    @Override
    public List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci){
        // the real work is done by the listener...
        var tree = ci.cparser_dec.compilationUnit();
        var walker = new ParseTreeWalker();
        var listener = new LoopCListener(ci);
        walker.walk(listener, tree);
        return listener.getTestResults();
    }
}