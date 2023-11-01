package nl.ou.debm.common.feature1;

import nl.ou.debm.common.IAssessor;
import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.List;


public class ControlFlowFeature implements  IFeature, IAssessor, IStatementGenerator  {

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
    public boolean isSatisfied() {
        return (m_iNForLoops >= 10) ;
    }

    @Override
    public String getPrefix() {
        return EFeaturePrefix.CONTROLFLOWFEATURE.toString();
    }

    @Override
    public List<String> getIncludes() {
        return new ArrayList<>(){
            { add("<stdio.h>"); }
        };
    }

    @Override
    public SingleTestResult GetSingleTestResult(Codeinfo ci) {
/*

        var tree = ci.cparser_dec.compilationUnit();

        var listener = new CVisitor().visit(ci.cparser_dec.compilationUnit());
        var walker = new ParseTreeWalker();
        walker.walk(listener, tree);

*/

        return null;
    }

    @Override
    public List<String> getNewStatement() {
        return getNewStatement(null);
    }

    @Override
    public List<String> getNewStatement(StatementPrefs prefs) {
        // check prefs object
        if (prefs == null){
            prefs = new StatementPrefs(null);
        }

        // **** as this is a stub, forget about preferences and just return something *****


        var forloop = new ForLoop("int c=0", "c<10", "++c");
        m_iNForLoops++;
        var list = new ArrayList<String>();
        list.add(forloop.strGetForStatement() + "{ printf(\"" + forloop.toCodeMarker() + "\"); }");
        return list;
    }
}
