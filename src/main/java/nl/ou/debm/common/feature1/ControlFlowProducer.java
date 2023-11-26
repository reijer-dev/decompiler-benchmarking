package nl.ou.debm.common.feature1;

import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ControlFlowProducer implements IFeature, IStatementGenerator  {

    // attributes
    // ----------

    final CGenerator generator;                 // see note in IFeature.java

    // repo of all possible loops
    private final ArrayList<LoopInfo> loop_repo = new ArrayList<>();
    // done flag
    private boolean bSatisfied = false;


    // construction
    public ControlFlowProducer(CGenerator generator){
        // set pointer to generator
        this.generator = generator;
        // initialise repo
        LoopInfo.FillLoopRepo(loop_repo);
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

        // select loop at random
        // try 10 "completely" random pointers
        // if this fails, look for first unused loop
        // if this fails, take any loop and mark feature as satisfied
        int loop_ptr=0;
        for (int try1 = 0; try1 < 10; try1++) {
            loop_ptr = ThreadLocalRandom.current().nextInt(0, loop_repo.size());
            if (loop_repo.get(loop_ptr).iNumberOfImplementations == 0) {
                break;
            }
        }
        if (loop_repo.get(loop_ptr).iNumberOfImplementations != 0) {
            for (int try2 = 0; try2 < loop_repo.size() ; try2++){
                if (loop_repo.get((loop_ptr + try2) % loop_repo.size()).iNumberOfImplementations == 0) {
                    loop_ptr += try2;
                    loop_ptr %= loop_repo.size();
                    break;
                }
            }
        }
        if (loop_repo.get(loop_ptr).iNumberOfImplementations != 0) {
            // feature satisfied!
            bSatisfied = true;
        }

        // loop info
        var loopinfo = loop_repo.get(loop_ptr);

        // mark loop as used
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
