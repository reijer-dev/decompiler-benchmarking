package nl.ou.debm.common.feature1;

import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.List;

public class ControlFlowProducer implements IFeature, IStatementGenerator  {

    // attributes
    // ----------

    final CGenerator generator;                 // see note in IFeature.java

    // repo of all possible loops
    private final ArrayList<LoopInfo> loop_repo = new ArrayList<>();
    // pointer to /next/ element to be used from the repo
    private int iRepoPointer = 0;
    // satisfied flag, is set when the entire repo is processed
    private boolean bSatisfied = false;

    // construction
    public ControlFlowProducer(CGenerator generator){
        // set pointer to generator
        this.generator = generator;
        // initialise repo
        // TODO: replace false with true, to ensure a shuffled loop
        LoopInfo.FillLoopRepo(loop_repo, false);
    }

    @Override
    public boolean isSatisfied() {
        return bSatisfied ;
    }

    @Override
    public String getPrefix() {
        return EFeaturePrefix.CONTROLFLOWFEATURE.toString();
    }

    @Override
    public List<String> getIncludes() {
        return new ArrayList<>(){
            { add("<stdio.h>");
              add("<stdbool.h>"); }
        };
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
        // - loops, multiple statements and compound statements are allowed or required
        // - expressions and assignments are not required

        // the loop_repo is shuffled when created in the constructor
        // so, we can simply pick one item each time

        // loop info
        var loopinfo = loop_repo.get(iRepoPointer);

        // increase pointer
        iRepoPointer++;
        if (iRepoPointer == loop_repo.size()){
            // start again
            iRepoPointer = 0;
            // and mark the work as done (well, at least for now)
            bSatisfied = true;
        }

        // mark this specific loop as used
        loopinfo.iNumberOfImplementations++;

        // ****************** continue here for real implementation :-)


        // still a stub but make something of it
        var forloop = new ForLoop("int c=0", "c<10", "++c");

        list.add("printf(\"" + forloop.toCodeMarker() + "\");");
        list.add(forloop.strGetForStatement(true));
        list.add("   printf(\"loopVar = %d;\", c);");
        list.add("}");

        return list;
    }
}
