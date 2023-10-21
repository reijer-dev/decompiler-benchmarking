package nl.ou.debm.common.feature1;

import nl.ou.debm.common.IAssessor;
import nl.ou.debm.common.antlr.MyCListener;
import nl.ou.debm.producer.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.List;


public class ControlFlowFeature implements  IFeature, IExpressionGenerator, IAssessor {

    // attributes
    // ----------

    // what work has been done jet?
    private int m_iNForLoops = 0;               // count number of for loops introduced
    private int m_iDeepestNestingLevel = 0;     // deepest number of nested for loops

    final CGenerator generator;     // see note in IFeature.java

    // construction
    public ControlFlowFeature(CGenerator generator){
        this.generator = generator;
    }
    public ControlFlowFeature(){
        this.generator = null;
    }

    @Override
    public String getNewExpression(int currentDepth, DataType type, boolean terminating) {
        ++m_iNForLoops; // for now: just make sure that the main generator is not jammed
        return null;
    }

    @Override
    public boolean isSatisfied() {
        return (m_iNForLoops >= 10) ;
    }

    @Override
    public String getPrefix() {
        return EFeaturePrefix.CONTROLFLOWFEATURE.toString();
    }

    @Override
    public List<String> getIncludes() {
        return null;
    }

    @Override
    public SingleTestResult GetSingleTestResult(Codeinfo ci) {

        var tree = ci.cparser_dec.compilationUnit();

        var listener = new MyCListener();
        var walker = new ParseTreeWalker();
        walker.walk(listener, tree);


        return null;
    }
}
