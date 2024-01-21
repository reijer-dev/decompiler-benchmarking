package nl.ou.debm.common.feature1;

import nl.ou.debm.common.BaseCodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.Misc;
import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The loop producer produces loops.
 * Any loop has this basic pattern:
 * - loop start marker
 * - loop init
 * - loop command
 * - loop body
 * - loop trailer
 * - loop end marker
 * - some dummy commands
 * The body has this basic pattern:
 * - loop body start marker
 * - dummy commands
 * - internal control flow
 * - dummy commands
 * - nested loop(s) [optional]
 * - external control flow
 * - dummy commands
 */


public class LoopProducer implements IFeature, IStatementGenerator  {

    // general settings for loops
    /////////////////////////////
    public static final int ILOOPVARLOWVALUELOWBOUND=0;             // lower bound for loops is between 0...20
    public static final int ILOOPVARLOWVALUEHIGHBOUND=10;
    public static final int ILOOPVARHIGHVALUELOWBOUND=2000;         // higher bound for loops is between 2000...10000
    public static final int ILOOPVARHIGHVALUEHIGHBOUND=10000;
    public static final int ILOOPUPDATEIFNOTONELOWBOUND=3;          // +=? and -=? --> ? lies between 3...15
    public static final int ILOOPUPDATEIFNOTONEHIGHBOUND=15;
    public static final int IMULTIPLYLOWBOUND=2;                    // *=?  --> ? lies between 2...7
    public static final int IMULTIPLYHIGHBOUND=7;
    public static final int IDIVIDELOWBOUND=3;                      // /=? --> ? lies between 3...7
    public static final int IDIVIDEHIGHBOUND=7;
    public static final int ILOOPMINNUMBEROFITERATIONSFORUNROLLING = 5;    // minimum number of iterations when seducing for loop unrolling
    public static final int ILOOPMAXNUMBEROFITERATIONSFORUNROLLING = 23;    // maximum number of iterations when seducing for loop unrolling
    public static final int ILOOPSTARTMINIMUMFORUNROLLING = 2000;           // minimum init value when seducing for loop unrolling
    public static final int ILOOPSTARTMMAXMUMFORUNROLLING = 3000;           // minimum init value when seducing for loop unrolling
    public static final int ILOWESTNUMBEROFDUMMYSTATEMENTS=1;       // minimum number of dummy statements
    public static final int IHIGHESTNUMBEROFDUMMYSTATEMENTS=23;     // maximum number of dummy statements
    public static final double DBLCHANCEOFFUNCTIONCALLASDUMMY=.3;   // chance of a function call inserted as dummy statement

    // class attributes
    ///////////////////
    private final StatementPrefs m_dummyPrefs = new StatementPrefs(null);
    private final StatementPrefs m_defaultEmptyPrefs = new StatementPrefs(null);
    private final static String STRINDENT = "\t";

    // object attributes
    ////////////////////
    private final List<LoopPatternNode> m_patternRepo;              // repository of loop patterns
    private int m_iLoopPatternIndex = -1;                           // keep track of used patterns
    private final CGenerator m_cgenerator;                          // generator object that uses this producer
    private final ArrayList<LoopInfo> m_loopRepo = new ArrayList<>();// repo of all possible loops
    private int m_iLoopRepoPointer = 0;                             // pointer to /next/ element to be used from the repo
    private boolean m_bSatisfied = false;                           // satisfied flag, is set when the entire repo is processed

    // construction

    /**
     * Construction, setting generator object
     * @param generator CGenerator that uses this producer class
     */
    public LoopProducer(CGenerator generator){
        // set pointer to generator
        this.m_cgenerator = generator;
        // initialise loop repo
        LoopInfo.FillLoopRepo(m_loopRepo, true);
        // set values for dummy statements, meaning everything is ok...
        m_dummyPrefs.loop = EStatementPref.NOT_WANTED;                      // ... but disallow loops
        m_dummyPrefs.compoundStatement = EStatementPref.NOT_WANTED;         // ... and disallow compounds
        m_dummyPrefs.expression = EStatementPref.NOT_WANTED;                // ... and disallow expressions
        // get loop pattern repo
        m_patternRepo = LoopPatternNode.getPatternRepo();
    }

    @Override
    public boolean isSatisfied() {
        return m_bSatisfied ;
    }

    @Override
    public String getPrefix() {
        return EFeaturePrefix.CONTROLFLOWFEATURE.toString();
    }

    @Override
    public List<String> getIncludes() {
        return new ArrayList<>(){
            { add("<stdio.h>");             // needed for printf
              add("<stdbool.h>");           // needed for true/false
              add("<stdlib.h>");            // needed for exit()
            }
        };
    }

    @Override
    public List<String> getNewStatements(int currentDepth, Function f) {
        return getNewStatements(currentDepth, f, null);
    }

    @Override
    public List<String> getNewStatements(int currentDepth, Function f, StatementPrefs prefs) {
        // internalize prefs
        var internalPrefs = prefs;
        // check prefs object
        if (internalPrefs == null) {
            // no prefs are wanted, use defaults
            internalPrefs = m_defaultEmptyPrefs;
        }

        // create string list for statements
        var list = new ArrayList<String>();

        // can we oblige to the preferences set?
        if (bStatementPrefsAreMetForALoop(internalPrefs)) {
            // select loop pattern
            var pattern = getNextLoopPattern();
            // select loops for pattern
            AttachLoops(pattern);
            // get new statements
            getLoopStatements(currentDepth, f, list, pattern);
        }
        else if (bStatementPrefsAreMetForSimpleDummies(internalPrefs)){
            // get dummies
            getDummyStatements(currentDepth, internalPrefs, list);
        }

        // return the lot
        return list;
    }

    /**
     * Add statements to the list object, based on the loopInfo and the pattern
     * @param f         function that will contain the loop. Necessary, because loops may use return statements
     *                  and must therefore know the functions return type.
     * @param list      the list of strings to which new statements must be added
     * @param pattern   the pattern being used
     */
    public void getLoopStatements(int currentDepth, Function f, List<String> list, LoopPatternNode pattern){
        //////////////
        // basic setup
        //////////////

        // remember list size, so we can check the placeholders
        int iStartPostProcessAt = list.size();
        final String STRPLACEHOLDER = "$$$$$PleaseJumpOutOfMultipleLoops$$$$$";

        // get loop info
        var loopInfo = pattern.getLoopInfo();

        // use correct variable prefix
        loopInfo.setVariablePrefix(getPrefix());

        /////////////
        // get labels
        /////////////
        String strBeginOfBodyLabel = m_cgenerator.getLabel();
        String strEndOfBodyLabel = m_cgenerator.getLabel();
        String strDirectlyAfterLoopLabel = m_cgenerator.getLabel();
        String strFurtherAfterLoopLabel = m_cgenerator.getLabel();

        /////////////
        // loop setup
        /////////////
        list.add("// " + LoopInfo.strToStringHeader());         // useful debugging info
        list.add("// " + loopInfo);                             // useful debugging info
        list.add(loopInfo.getStartMarker(pattern.iGetNumParents()).strPrintf());        // mark code
        list.add(loopInfo.strGetLoopInit());                    // put init statement (this may be only a comment, when using for)
        list.add(loopInfo.strGetLoopCommand());                 // put loop command (for/do/while)

        ////////////
        // loop body
        ////////////

        // add loop body header
        list.add(STRINDENT + strBeginOfBodyLabel);              // add start of body label
        boolean bPrintVar = false;
        if (loopInfo.getLoopVar() != null) {                    // add start of body marker
            // use loop var in print?
            if (loopInfo.getUnrolling() != ELoopUnrollTypes.ATTEMPT_DO_NOT_PRINT_LOOP_VAR) {
                bPrintVar = true;
            }
        }
        if (bPrintVar) {
            // print var
            if (loopInfo.getLoopVar().eVarType == ELoopVarTypes.INT) {
                list.add(STRINDENT + loopInfo.getBodyMarker().strPrintfInteger(loopInfo.strGetLoopVarName()));
            } else {
                list.add(STRINDENT + loopInfo.getBodyMarker().strPrintfFloat(loopInfo.strGetLoopVarName()));
            }
        }
        else {
            // but if no loop var is used, one cannot print it ;-)
            list.add(STRINDENT + loopInfo.getBodyMarker().strPrintf());
        }

        // get some dummy commands (but only in non-unrolling loops)
        if (loopInfo.getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT) {
            addDummies(currentDepth, f, list, STRINDENT);
        }

        // control flow statements that transfer control out of this loop
        if (loopInfo.bGetELC_UseBreak()){                       // add break if needed
            list.add(STRINDENT + "if (getchar()==23) {break;}");
        }
        if (loopInfo.bGetELC_UseExit()){                        // add exit it needed
            list.add(STRINDENT + "if (getchar()==97) {exit(1923);}");
        }
        if (loopInfo.bGetELC_UseReturn()){                      // add return if needed
            if (f.getType().bIsPrimitive()) {
                // for any primitive, we can rely on the default value based on the type
                list.add(STRINDENT + "if (getchar()==31) {return " + f.getType().strDefaultValue(m_cgenerator.structsByName) + ";}");
            }
            else{
                // for any non-primitive, we must instantiate a return struct
                list.add(STRINDENT + "if (getchar()==31) {struct " + f.getType().getName() + " out; return out;}");
            }
        }
        if (loopInfo.bGetELC_UseGotoDirectlyAfterThisLoop()){   // add goto outside of loop, if needed
            list.add(STRINDENT + "if (getchar()==19) {goto " + strGotoLabel(strDirectlyAfterLoopLabel) + ";} // goto directly after");
        }
        if (loopInfo.bGetELC_UseGotoFurtherFromThisLoop()){     // add goto further outside of loop, if needed
            list.add(STRINDENT + "if (getchar()==83) {goto " + strGotoLabel(strFurtherAfterLoopLabel) + ";} // goto further after");
        }

        // breaking out of nested loops?
        if (loopInfo.bGetELC_BreakOutNestedLoops()) {
            // breaking out of nested loops is somewhat problematic, as the loop only knows itself and not
            // its parents, nor its parents' labels
            //
            // what we do know however, is whether or not there are parent loops, so we first test
            // for that, as it is no use to only break out this loop
            if (pattern.bHasParent()) {
                // there are parent loops, so we need to do something.
                // we add a goto with a placeholder
                list.add(STRINDENT + "if (getchar()==73) {goto " + STRPLACEHOLDER + ";} // goto end of root loop");
                // if the top loop is closed, all placeholders are substituted with the appropriate label
            }
        }

        // get some dummy commands
        if (loopInfo.getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT) {
            addDummies(currentDepth, f, list, STRINDENT);
        }

        // nested loop or loops wanted?
        for (int ch=0; ch<pattern.iGetNumChildren(); ++ch){
            // assert no children in unroll-able loops
            assert loopInfo.getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT;
            // yes, so create new list
            var list2 = new ArrayList<String>();
            // add inner loop to that list
            getLoopStatements(currentDepth, f, list2, pattern.getChild(ch));
            // copy list with indention
            for (var item : list2){
                list.add(STRINDENT + item);
            }
        }

        // control flow statements that transfer control within this loop
        if (loopInfo.bGetILC_UseContinue()){                    // add continue if needed
            list.add(STRINDENT + "if (getchar()==67) {continue;}");
        }
        if (loopInfo.bGetILC_UseGotoBegin()){                   // add goto <begin-of-loop> if needed (no loop var update in this jump)
            list.add(STRINDENT + "if (getchar()==11) {goto " + strGotoLabel(strBeginOfBodyLabel) + ";} // goto begin of loop body");
        }
        if (loopInfo.bGetILC_UseGotoEnd()){                     // add goto <end-of-loop> if needed (loop var will be updated)
            list.add(STRINDENT + "if (getchar()==17) {goto " + strGotoLabel(strEndOfBodyLabel) + ";} // goto end of loop body");
        }

        // get some dummy commands
        if (loopInfo.getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT) {
            addDummies(currentDepth, f, list, STRINDENT);
        }

        // finish up body with update command if needed and the closing statements
        list.add(STRINDENT + strEndOfBodyLabel);
        if ((loopInfo.getLoopCommand() != ELoopCommands.FOR) &&
                (loopInfo.getLoopExpressions().bUpdateAvailable())){
            list.add(STRINDENT + loopInfo.strGetCompleteLoopUpdateExpression() + ";");
        }
        else{
            list.add(STRINDENT + ";");
        }
        list.add(loopInfo.strGetLoopTrailer());

        /////////////////
        // after the loop
        /////////////////
        // add code
        list.add(strDirectlyAfterLoopLabel);                    // label
        list.add(loopInfo.getEndMarker().strPrintf());          // after-body-marker
        addDummies(currentDepth, f, list, "");         // get dummy statements
        list.add(strFurtherAfterLoopLabel);                     // and put end-of-all-label
        list.add(";");

        ////////////////////////////
        // post-process placeholders
        ////////////////////////////
        if (!pattern.bHasParent()){
            // we have just closed a top level loop
            // search for placeholders
            for (int ptr=iStartPostProcessAt; ptr<list.size(); ++ptr){
                int p = list.get(ptr).indexOf(STRPLACEHOLDER);
                if (p>-1){
                    var strOld = list.get(ptr);
                    var strNew = strOld.substring(0,p) +
                            strGotoLabel(strFurtherAfterLoopLabel) +
                            strOld.substring(p + STRPLACEHOLDER.length());
                    list.set(ptr, strNew);
                }
            }
        }
    }

    // internal affairs
    ///////////////////

    /**
     * check preferences to see if this implementation can oblige
     * @param prefs  statement preferences
     * @return   true if loop can be returned
     */
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

    /**
     * check preferences to see if this implementation can oblige
     * @param prefs  statement preferences
     * @return      true if requested dummies can be returned
     */
    private boolean bStatementPrefsAreMetForSimpleDummies(StatementPrefs prefs){
        // check against another implementation: dummy statements
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
        // - loops, expressions and assignments and compound statements are not required,
        // so we can safely go on with dummies
        return true;
    }

    /**
     * Get next loop to be implemented and mark the work done
     * @return  all info for the next loop
     */
    private LoopInfo getNextLoopInfo(){
        // the loop_repo is shuffled when created in the constructor
        // so, we can simply pick one item each time

        // get current loop info
        var loopInfo = m_loopRepo.get(m_iLoopRepoPointer);

        // increase loop info object pointer
        m_iLoopRepoPointer++;
        if (m_iLoopRepoPointer == m_loopRepo.size()){
            // start again
            m_iLoopRepoPointer = 0;
            // and mark the work as done
            m_bSatisfied = true;
        }

        // return loop details
        return loopInfo;
    }

    /**
     * make one or more dummy-statements
     * that is: a random function call or a random code marker (which in essence is
     * also a random function call, except that it doesn't involve the generator mechanism)
     *
     * @param prefs preferences for the statements
     * @param list  dummy statements
     */
    private void getDummyStatements(int currentDepth, StatementPrefs prefs, List<String> list){

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
                m_cgenerator.getFunction(currentDepth);
            }
            else {
                // code marker, make it and add it
                list.add(new BaseCodeMarker(this).strPrintf());
            }
        }
    }


    /**
     * strip colon from a label
     * @param strLabelWithColon   label name, may end with colon
     * @return  label name, guaranteed without colon
     */
    private String strGotoLabel(String strLabelWithColon){
        int p = strLabelWithColon.indexOf(":");
        if (p>-1){
            return strLabelWithColon.substring(0, p);
        }
        return strLabelWithColon;
    }

    /**
     * add dummy statements to list
     * @param f  function in which we are working
     * @param list  list to be expanded
     */
    private void addDummies(int currentDepth, Function f, List<String> list, String strIndent) {
        for (var item : m_cgenerator.getNewStatements(currentDepth + 1, f, m_dummyPrefs)) {
            if (!item.isBlank()) {
                list.add(strIndent + item); // get dummy statements and indent them
            }
        }
    }

    /**
     * get the next loop pattern from the list
     * @return another loop pattern
     */
    public LoopPatternNode getNextLoopPattern(){
        m_iLoopPatternIndex++;
        if (m_iLoopPatternIndex >= m_patternRepo.size()){
            // processed the entire collection: shuffle the lot and start again
            Collections.shuffle(m_patternRepo, Misc.rnd);
            m_iLoopPatternIndex=0;
        }
        return new LoopPatternNode(m_patternRepo.get(m_iLoopPatternIndex));
    }

    /**
     * struct like class as a helper to filling loop patterns properly
     */
    static private class LoopPatternStats{
        public LoopPatternNode deepestNode = null;
        public LoopPatternNode multipleLoopBreakingNode = null;
    }

    /**
     * attach LoopInfo objects to a loop pattern and then check for multiple breaks
     * if the deepest loop has no multiple-break-statement, but another loop has,
     * than these two are switched, in order to make sure that multiple-loop-breaks
     * are really used
     * @param patternNode  root node of the tree to be processed
     */
    public void AttachLoops(LoopPatternNode patternNode){
        // setup pattern stats object
        var psi= new LoopPatternStats();
        psi.deepestNode = patternNode;
        // add a node to all pattern nodes (recursively)
        recurseAttach(patternNode, psi);
        // now make sure that multiple-loop-breaking loops put in the right place,
        // which is: in a (deep) nested loop
        if (!psi.deepestNode.getLoopInfo().bGetELC_BreakOutNestedLoops()){
            // the deepest loop has no multiple-loop-break statement
            if (psi.multipleLoopBreakingNode!=null){
                // but we do have a loop with a multiple loop break, so switch them
                var tmp = psi.deepestNode.getLoopInfo();
                psi.deepestNode.setLoopInfo(psi.multipleLoopBreakingNode.getLoopInfo());
                psi.multipleLoopBreakingNode.setLoopInfo(tmp);
            }
        }
        // make sure no unroll-able loop has children
        recurseCheckUnRollableLoops(patternNode);
    }

    /**
     * attach new loopInfo objects to this patternNode and process all its children
     * @param patternNode  the root pattern node
     * @param psi  stats to be able to reshuffle
     */
    private void recurseAttach(LoopPatternNode patternNode, LoopPatternStats psi){
        // attach LoopInfo to this node
        //
        // make copy from repo, because it is possible that repo items are used more than
        // once and that would mean that loop ID's would be re-used as well.
        patternNode.setLoopInfo(new LoopInfo(getNextLoopInfo()));
        // attach LoopInfo to all the children
        for (int c=0; c<patternNode.iGetNumChildren(); ++c){
            recurseAttach(patternNode.getChild(c), psi);
        }
        // keep track of deepest node
        if (patternNode.iGetNumParents()>psi.deepestNode.iGetNumParents()){
            psi.deepestNode = patternNode;
        }
        // keep track of a node that has multiple breaks
        if (patternNode.getLoopInfo().bGetELC_BreakOutNestedLoops()){
            psi.multipleLoopBreakingNode = patternNode;
        }
    }

    private void recurseCheckUnRollableLoops(LoopPatternNode patternNode){
        // check if this node has an unroll-able loop
        if (patternNode.getLoopInfo().getUnrolling() != ELoopUnrollTypes.NO_ATTEMPT){
            // this loop is potentially unroll-able, that's ok, as long as there are no kids
            if (patternNode.iGetNumChildren()>0){
                // ... but there are children, so try to find a child that is not unrollable and switch
                while (true) {
                    for (int c = 0; c < patternNode.iGetNumChildren(); ++c) {
                        if ((patternNode.getChild(c).getLoopInfo().getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT) &&
                            (!patternNode.getChild(c).getLoopInfo().bGetELC_BreakOutNestedLoops())) {
                            // found a child that is not unroll-able, so switch them
                            var tmp = patternNode.getLoopInfo();
                            patternNode.setLoopInfo(patternNode.getChild(c).getLoopInfo());
                            patternNode.getChild(c).setLoopInfo(tmp);
                            break;
                        }
                    }
                    // success?
                    if (patternNode.getLoopInfo().getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT) {
                        break;
                    }
                    // ... no joy. Apparently all children are also unroll-able. Too bad.
                    // So, plan B: we keep attaching children to this node, until we've added an unroll-able one
                    // which will be switched with this node in the upper part of the do-loop
                    LoopPatternNode lpn;
                    do {
                        lpn = new LoopPatternNode();
                        lpn.setLoopInfo(getNextLoopInfo());
                        patternNode.addChild(lpn);
                    } while ((lpn.getLoopInfo().getUnrolling() != ELoopUnrollTypes.NO_ATTEMPT) ||
                             (lpn.getLoopInfo().bGetELC_BreakOutNestedLoops()));
                }
            }
        }

        // check all the children (do so recursively)
        for (int c=0; c<patternNode.iGetNumChildren(); ++c){
            recurseCheckUnRollableLoops(patternNode.getChild(c));
        }
    }
}
