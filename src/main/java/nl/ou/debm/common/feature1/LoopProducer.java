package nl.ou.debm.common.feature1;

import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.List;

public class LoopProducer implements IFeature, IStatementGenerator  {

    // general settings for loops
    public static final int ILOOPVARLOWVALUELOWBOUND=0;
    public static final int ILOOPVARLOWVALUEHIGHBOUND=10;
    public static final int ILOOPVARHIGHVALUELOWBOUND=2000;
    public static final int ILOOPVARHIGHVALUEHIGHBOUND=10000;
    public static final int ILOOPUPDATEIFNOTONELOWBOUND=3;
    public static final int ILOOPUPDATEIFNOTONEHIGHBOUND=15;
    public static final int IMULTIPLYLOWBOUND=2;
    public static final int IMULTIPLYHIGHBOUND=17;
    public static final int IDIVIDELOWBOUND=7;
    public static final int IDIVIDEHIGHBOUND=23;




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
    public LoopProducer(CGenerator generator){
        // set pointer to generator
        this.generator = generator;
        // initialise repo
        // TODO: replace false with true, to ensure a shuffled loop set
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

        // get labels
        String beginOfBodyLabel = generator.getLabel();
        String endOfBodyLabel = generator.getLabel();
        String afterLoopLabel = generator.getLabel();

        final String strIntend = "  ";

        // create loop
        list.add(loopInfo.getStartMarker().strPrintf());
        list.add(loopInfo.strGetLoopInit());
        list.add(loopInfo.strGetLoopCommand());


            list.add(strIntend + beginOfBodyLabel);
            if (loopInfo.getLoopVar().bUseLoopVariable) {
                list.add(strIntend + loopInfo.getBodyMarker().strPrintfDecimal(loopInfo.strGetLoopVarName()));
            }
            else {
                list.add(strIntend + loopInfo.getBodyMarker().strPrintf());
            }
            if (loopInfo.bGetELC_UseBreak()){
                list.add(strIntend + "if (getchar()==23) {break;}");
            }
            if (loopInfo.bGetELC_UseExit()){
                list.add(strIntend + "if (getchar()==97) {exit(1923);}");
            }
            if (loopInfo.bGetELC_UseReturn()){
                list.add(strIntend + "if (getchar()==31) {return ***;}");
            }

            if (loopInfo.bGetILC_UseContinue()){
                list.add(strIntend + "if (getchar()==69) {continue;}");
            }


            list.add(strIntend + endOfBodyLabel);
            if ((loopInfo.getLoopCommand() != ELoopCommands.FOR) &&
                (loopInfo.getLoopVar().bUseLoopVariable)){
                list.add(strIntend + loopInfo.strGetCompleteLoopUpdateExpression());
            }
        list.add(loopInfo.strGetLoopTrailer());
        list.add(afterLoopLabel);
        list.add(loopInfo.getEndMarker().strPrintf());
    }
}
