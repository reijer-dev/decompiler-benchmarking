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

    private boolean bStatementPrefsAreMet(StatementPrefs prefs){
        // check preferences against what this implementation does
        if (prefs.loop == EStatementPref.NOT_WANTED){
            // no, as we only produce loops
            return false;
        }
        if (prefs.numberOfStatements == ENumberOfStatementsPref.SINGLE){
            // no, as we only produce multiple statements
            return false;
        }
        if (prefs.expression == EStatementPref.REQUIRED){
            // no, as we do not do expressions
            return false;
        }
        if (prefs.assignment == EStatementPref.REQUIRED){
            // no, as we do not do assignments
            return false;
        }
        if (prefs.compoundStatement == EStatementPref.NOT_WANTED){
            // no, as we use a compound statement as loop body
            return false;
        }

        // we have now asserted that:
        // - loops, multiple statements and compound statements are allowed or required
        // - expressions and assignments are not required
        return true;
    }

    private LoopInfo getNextLoopInfo(){
        // the loop_repo is shuffled when created in the constructor
        // so, we can simply pick one item each time

        // get current loop info
        var loopInfo = loop_repo.get(iRepoPointer);

        // increase loop info object pointer
        iRepoPointer++;
        if (iRepoPointer == loop_repo.size()){
            // start again
            iRepoPointer = 0;
            // and mark the work as done (well, at least for now)
            bSatisfied = true;
        }

        // mark this specific loop as used
        loopInfo.IncreaseImplementations();

        // return loop details
        return loopInfo;
    }

    @Override
    public List<String> getNewStatements() {
        return getNewStatements(null);
    }

    @Override
    public List<String> getNewStatements(StatementPrefs prefs) {
        // check prefs object
        if (prefs == null) {
            prefs = new StatementPrefs(null);
        }

        // create list
        var list = new ArrayList<String>();

        // can we oblige?
        if (!bStatementPrefsAreMet(prefs)) {
            return list;
        }

        // get loop to be implemented
        var loopInfo = getNextLoopInfo();

        // get new statements
        getLoopStatements(list, loopInfo);

        return list;
    }

    public void getLoopStatements(List<String> list, LoopInfo loopInfo){

        // printf(startmarker)
        // loop init
        // loop command
        // {
        // label:
        //    loop body
        // label:
        // }
        // label:
        // printf(endmarker)

        String startlooplabel = generator.

        // create loop
        list.add(loopInfo.getStartMarker().strPrintf());
        list.add(loopInfo.strGetLoopInit());
        list.add(loopInfo.strGetLoopCommand());
            list.add("  // loop body");
        list.add(loopInfo.strGetLoopTrailer());
        list.add(loopInfo.getEndMarker().strPrintf());
    }
}
