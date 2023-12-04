package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.Misc;
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
    public static final int ILOWESTNUMBEROFDUMMYSTATEMENTS=1;
    public static final int IHIGHESTNUMBEROFDUMMYSTATEMENTS=23;
    public static final double DBLCHANCEOFFUNCTIONCALLASDUMMY=.3;



    private int m_iNumberOfEnclosingLoops = -1;

    private final StatementPrefs m_dummyprefs = new StatementPrefs(null);
    private final String STRINDENT = "  ";

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
        // set values for dummy statements
        m_dummyprefs.loop = EStatementPref.NOT_WANTED;                    // but disallow loops
        m_dummyprefs.compoundStatement = EStatementPref.NOT_WANTED;       // and disallow compounds

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
              add("<stdbool.h>");
              add("<stdlib.h");
            }
        };
    }

    private boolean bStatementPrefsAreMetForALoop(StatementPrefs prefs){
        // check preferences against what this implementation does, implementing loops
        if (prefs.loop == EStatementPref.NOT_WANTED){
            // no, as we check to produce loops
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

    private boolean bStatementPrefsAreMetForSimpleDummies(StatementPrefs prefs){
        // check against another implemtentation: dummy statements
        // can we oblige?
        if (prefs.loop == EStatementPref.REQUIRED){
            // no, as dummies do not contain loops
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
        if (prefs.compoundStatement == EStatementPref.REQUIRED){
            // no, as we do not use compound statements
            return false;
        }

        // we have now asserted that:
        // - loops, expressions and assignments and compound statements are not required
        // so we can safely go on with dummies
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
    public List<String> getNewStatements(Function f) {
        return getNewStatements(f, null);
    }

    @Override
    public List<String> getNewStatements(Function f, StatementPrefs prefs) {
        // check prefs object
        if (prefs == null) {
            prefs = new StatementPrefs(null);
        }

        // create list
        var list = new ArrayList<String>();

        // can we oblige?
        if (bStatementPrefsAreMetForALoop(prefs)) {
            // get loop to be implemented
            var loopInfo = getNextLoopInfo();

            // get new statements
            getLoopStatements(f, list, loopInfo, prefs.iAllowHowManyLevelsOfNestedLoops);
        }
        if (bStatementPrefsAreMetForSimpleDummies(prefs)){
            // get dummies
            getDummyStatements(prefs, list);
        }

        // return the lot
        return list;
    }

    private void getDummyStatements(StatementPrefs prefs, List<String> list){
        // make one or more dummy-statements
        // that is: a random function call or a random code marker (which in essence is
        // also a random function call, except that it doesn't involve the generator mechanism

        // how many?
        int max=ILOWESTNUMBEROFDUMMYSTATEMENTS;
        if (prefs.numberOfStatements!=ENumberOfStatementsPref.SINGLE){
            max = Misc.rnd.nextInt(ILOWESTNUMBEROFDUMMYSTATEMENTS, IHIGHESTNUMBEROFDUMMYSTATEMENTS);
        }

        // make them
        for (int i=0; i<max; ++i){
            // function call or dummy?
            if (Misc.rnd.nextDouble() < DBLCHANCEOFFUNCTIONCALLASDUMMY){
                // function call
                generator.getFunction();
            }
            else {
                // code marker, make it and add it
                var cm = new CodeMarker();
                list.add(cm.strPrintf());
            }
        }
    }


    private String strGotoLabel(String strLabelWithColon){
        int p = strLabelWithColon.indexOf(":");
        if (p>-1){
            return strLabelWithColon.substring(0, p);
        }
        return strLabelWithColon;
    }

    private void addDummies(Function f, List<String> list) {
        for (var item : generator.getNewStatements(f, m_dummyprefs)) {
            list.add(STRINDENT + item);// get dummy statements and indent them
        }
    }

    public void getLoopStatements(Function f, List<String> list, LoopInfo loopInfo, int iMaxNestingLevel){

        // set current nesting level
        loopInfo.setCurrentNestingLevel(m_iNumberOfEnclosingLoops+1);

        /////////////
        // get labels
        /////////////
        String strBeginOfBodyLabel = generator.getLabel();
        String strEndOfBodyLabel = generator.getLabel();
        String strDirectlyAfterLoopLabel = generator.getLabel();
        String strFurtherAfterLoopLabel = generator.getLabel();


        /////////////
        // loop setup
        /////////////
        list.add(loopInfo.getStartMarker().strPrintf());        // mark code
        list.add(loopInfo.strGetLoopInit());                    // put init statement (may be only a comment, when using for)
        list.add(loopInfo.strGetLoopCommand());                 // put loop command (for/do/while)

        ////////////
        // loop body
        ////////////
        //
        // keep track of the number of loops that enclose the current code
        m_iNumberOfEnclosingLoops++;
        // add code:
        list.add(STRINDENT + strBeginOfBodyLabel);              // add start of body label
        if (loopInfo.getLoopVar().bUseLoopVariable) {           // add start of body marker
            // if loop var is used, put it in the marker, so it cannot be optimized out for not being used in the loop
            list.add(STRINDENT + loopInfo.getBodyMarker().strPrintfDecimal(loopInfo.strGetLoopVarName()));
        }
        else {
            // but if no loop var is used, one cannot print it ;-)
            list.add(STRINDENT + loopInfo.getBodyMarker().strPrintf());
        }

        // get some dummy commands
        addDummies(f, list);

        // control flow statements that transfer control out of this loop
        if (loopInfo.bGetELC_UseBreak()){                       // add break if needed
            list.add(STRINDENT + "if (getchar()==23) {break;}");
        }
        if (loopInfo.bGetELC_UseExit()){                        // add exit it needed
            list.add(STRINDENT + "if (getchar()==97) {exit(1923);}");
        }
        if (loopInfo.bGetELC_UseReturn()){                      // add return if needed
            list.add(STRINDENT + "if (getchar()==31) {return " + f.getType().strDefaultValue() + ";}");
        }
        if (loopInfo.bGetELC_UseGotoDirectlyAfterThisLoop()){   // add goto outside of loop, if needed
            list.add(STRINDENT + "if (getchar()==19) {goto " + strGotoLabel(strDirectlyAfterLoopLabel) + ";}");
        }
        if (loopInfo.bGetELC_UseGotoFurtherFromThisLoop()){     // add goto further outside of loop, if needed
            list.add(STRINDENT + "if (getchar()==83) {goto " + strGotoLabel(strFurtherAfterLoopLabel) + ";}");
        }

        // get some dummy commands
        addDummies(f, list);

        // nested loop wanted?
        if (m_iNumberOfEnclosingLoops<iMaxNestingLevel){
            // get loop to be implemented
            var loopInfo2 = getNextLoopInfo();
            // and implement it
            var list2 = new ArrayList<String>();
            getLoopStatements(f, list2, loopInfo2, iMaxNestingLevel-1);
            for (var item : list2){
                list.add(STRINDENT + item);
            }
        }

        // control flow statements that transfer control within this loop
        if (loopInfo.bGetILC_UseContinue()){                    // add continue if needed
            list.add(STRINDENT + "if (getchar()==67) {continue;}");
        }
        if (loopInfo.bGetILC_UseGotoBegin()){                   // add goto <begin-of-loop> if needed (no loop var update in this jump)
            list.add(STRINDENT + "if (getchar()==11) {goto " + strGotoLabel(strBeginOfBodyLabel) + ";}");
        }
        if (loopInfo.bGetILC_UseGotoEnd()){                     // add goto <end-of-loop> if needed (loop var will be updated)
            list.add(STRINDENT + "if (getchar()==17) {goto " + strGotoLabel(strEndOfBodyLabel) + ";}");
        }

        // get some dummy commands
        addDummies(f, list);

        // finish up body with update command if needed and the closing statements
        list.add(STRINDENT + strEndOfBodyLabel + " ;");
        if ((loopInfo.getLoopCommand() != ELoopCommands.FOR) &&
            (loopInfo.getLoopVar().bUseLoopVariable)){
            list.add(STRINDENT + loopInfo.strGetCompleteLoopUpdateExpression());
        }
        list.add(loopInfo.strGetLoopTrailer());

        /////////////////
        // after the loop
        /////////////////
        // keep track of the number of loops that enclose the current code
        m_iNumberOfEnclosingLoops--;
        // add code
        list.add(strDirectlyAfterLoopLabel);                    // label
        list.add(loopInfo.getEndMarker().strPrintf());          // after-body-marker
        addDummies(f, list);                                    // get dummy statements
        list.add(strFurtherAfterLoopLabel);                     // and put end-of-all-label
    }
}
