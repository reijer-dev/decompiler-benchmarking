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
    public List<String> getNewStatements() {
        return getNewStatements(null);
    }

    @Override
    public List<String> getNewStatements(StatementPrefs prefs) {
        // check prefs object
        if (prefs == null){
            prefs = new StatementPrefs(null);
        }

        // create list
        var list = new ArrayList<String>();

        // can we oblige?
        if (prefs.loop == EStatementPref.NOT_WANTED){
            // no, as we only produce loops
            return list;
        }
        if (prefs.numberOfStatements == ENumberOfStatementsPref.SINGLE){
            // no, as we only produce multiple statements
            return list;
        }
        if (prefs.expression == EStatementPref.REQUIRED){
            // no, as we do not do expressions
            return list;
        }
        if (prefs.assignment == EStatementPref.REQUIRED){
            // no, as we do not do assignments
            return list;
        }
        if (prefs.compoundStatement == EStatementPref.NOT_WANTED){
            // no, as we use a compound statement as loop body
            return list;
        }

        // we have now asserted that:
        // - loops, multiple statements and compound statements are  allowed or required
        // - expressions and assignments are not required


        // still a stub but makesomething of it
        var forloop = new ForLoop("int c=0", "c<10", "++c");

        list.add("printf(\"" + forloop.toCodeMarker() + "\");");
        list.add(forloop.strGetForStatement(true));
        list.add("   printf(\"loopvar = %d;\", c);");
        list.add("}");

        m_iNForLoops++;
        return list;
    }
}
