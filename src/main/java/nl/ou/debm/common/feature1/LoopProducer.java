package nl.ou.debm.common.feature1;

import nl.ou.debm.common.BaseCodeMarker;
import nl.ou.debm.common.CodeMarker;
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
 * <br>
 */


public class LoopProducer implements IFeature, IStatementGenerator, IFunctionGenerator {

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
    private static final StatementPrefs s_dummyPrefs = new StatementPrefs(null);
    private static final StatementPrefs s_defaultEmptyPrefs = new StatementPrefs(null);
    private static final String STRINDENT = "\t";
    private static final ArrayList<LoopInfo> s_loopRepo = new ArrayList<>();// repo of all possible loops
    private static int s_iLoopRepoPointer = 0;                             // pointer to /next/ element to be used from the repo
    private static double s_dblManuallySetLoopRepoFraction = -1.0;           // may be set seperately for test purposes
    private static final Object s_syncObj = new Object();    // synchronize object

    // class init
    // ----------
    static {
        // copy loop info repo
        LoopInfo.FillLoopRepo(s_loopRepo, true);
        // set values for dummy statements, meaning everything is ok...
        s_dummyPrefs.loop = EStatementPref.NOT_WANTED;                      // ... but disallow loops
        s_dummyPrefs.compoundStatement = EStatementPref.NOT_WANTED;         // ... and disallow compounds
        s_dummyPrefs.expression = EStatementPref.NOT_WANTED;                // ... and disallow expressions
    }

    // object attributes
    ////////////////////
    private final List<LoopPatternNode> m_patternRepo;              // repository of loop patterns
    private int m_iLoopPatternIndex = -1;                           // keep track of used patterns
    private final CGenerator m_cgenerator;                          // generator object that uses this producer
    private boolean m_bSatisfied = false;                           // satisfied flag, is set when the entire repo is processed
    private int m_iSatisfactionCutOff = 0;                          // minimum number of loops to be produced
    private int m_iNLoopsProduced = 0;                              // count number of loops produced

    // construction

    /**
     * Construction, setting generator object
     * @param generator CGenerator that uses this producer class
     */
    public LoopProducer(CGenerator generator){
        // set pointer to generator
        this.m_cgenerator = generator;
        // get loop pattern repo
        m_patternRepo = LoopPatternNode.getPatternRepo();
        // set satisfaction cut off
        m_iSatisfactionCutOff = (int)((double)s_loopRepo.size() * dblGetSatisfactionFraction());
    }

    /**
     * method for test purposes. This makes it possible to set the percentage (0.0 - 1.0) of the loops
     * to be included in the source.
     * @param dblFraction A value between 0.01 and 1.0 (any value is forced within these borders)
     */
    public static void setSatisfactionFractionManually(double dblFraction){
        // set satisfaction cut off
        if (dblFraction<.01) { dblFraction = .01;}
        if (dblFraction>1.0) { dblFraction = 1.0;}
        s_dblManuallySetLoopRepoFraction = dblFraction;
    }

    /**
     * Get the fraction of loops to be used in this run.
     * This fraction may be set manually class-wide, for test purposes, using
     * setLoopRepoFraction()
     * @return a value between 0.1 and 1.0 (including both)
     */
    private static double dblGetSatisfactionFraction(){
        if (s_dblManuallySetLoopRepoFraction >=0){
            return s_dblManuallySetLoopRepoFraction;
        }
        // calculate the percentage for loops according to a random value
        // this function assures that the vast majority of c-sources will have between
        // 25% and 35% of all the loops in the repo. Considering that each container
        // will contain some 200 sources, we are quite sure to use every loop type
        // at least once in the container, while also having a substantial number of loops
        // per c-source /and/ not having too many loops per source (as the size grows
        // substantially if there are).
        //
        // random value        number of loops
        // 0                         10%
        // .15                       25%
        // .7                        35%
        // .995                     100%
        // between these dots, the number of loops grows linear
        //
        double randomValue = Misc.rnd.nextDouble();
        if (randomValue<.15){
            return dblInterpolate(0,.1, .15, .25, randomValue);
        }
        if (randomValue<.7){
            return dblInterpolate(.15,.25, .7, .35, randomValue);
        }
        if (randomValue<.995){
            return dblInterpolate(.7, .35, 1, 1, randomValue);
        }
        return 1;
    }

    /**
     * calculate y value corresponding to an x value, with a linear relation between (x1,y1) and (x2,y2)
     * @param x1 left point of imaginary line (x1<x2)
     * @param y1 y belonging to x1
     * @param x2 right point of imaginary line (x1<x2)
     * @param y2 y belonging to x2
     * @param x x to be converted  x1<=x<=x2
     * @return y value on the line
     */
    private static double dblInterpolate(double x1, double y1, double x2, double y2, double x){
        return ((x-x1)/(x2-x1)) * (y2-y1);
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
        assert m_cgenerator != null : "No C-generator object";
        // internalize prefs
        var internalPrefs = prefs;
        // check prefs object
        if (internalPrefs == null) {
            // no prefs are wanted, use defaults
            internalPrefs = s_defaultEmptyPrefs;
        }

        // create string list for statements
        var list = new ArrayList<String>();

        // can we oblige to the preferences set?
        if (bStatementPrefsAreMetForALoop(internalPrefs)) {
            // select loop pattern & fill it with loops
            var pattern = getNextFilledLoopPattern();
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

        // get parent ID, if present
        var loopParent = pattern.getParent();
        long lngParentLoopID = -1;
        if (loopParent!=null){
            lngParentLoopID = loopParent.getLoopInfo().lngGetLoopID();
        }

        // use correct variable prefix
        loopInfo.setVariablePrefix(getPrefix());

        /////////////
        // get labels
        /////////////
        String strEndOfBodyLabel = m_cgenerator.getLabel();
        String strDirectlyAfterLoopLabel = m_cgenerator.getLabel();
        String strFurtherAfterLoopLabel = m_cgenerator.getLabel();

        ///////////////////////////////////////
        // make loop conditional if it is a TIL
        ///////////////////////////////////////
        // if we don't do this, all code after the loop will be thrown out for being unreachable
        String strInfIntend = "";
        if (loopInfo.bMakeConditional()) {
            list.add("if (getchar()==79) {");
            strInfIntend = STRINDENT;
        }

        /////////////
        // loop setup
        /////////////
        list.add(strInfIntend + "/* " + LoopInfo.strToStringHeader() + " */"); // useful debugging info
        list.add(strInfIntend + "/* " + loopInfo + " */");                     // useful debugging info
        list.add(strInfIntend + loopInfo.getStartMarker(pattern.iGetNumParents(), lngParentLoopID).strPrintf());        // mark code
        list.add(strInfIntend + loopInfo.strGetLoopInit());                    // put init statement (this may be only a comment, when using for)
        list.add(strInfIntend + loopInfo.strGetLoopCommand());                 // put loop command (for/do/while)

        ////////////
        // loop body
        ////////////

        // add loop body header
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
                list.add(strInfIntend + STRINDENT + loopInfo.getBodyMarker().strPrintfInteger(loopInfo.strGetLoopVarName()));
            } else {
                list.add(strInfIntend + STRINDENT + loopInfo.getBodyMarker().strPrintfFloat(loopInfo.strGetLoopVarName()));
            }
        }
        else {
            // but if no loop var is used, one cannot print it ;-)
            list.add(strInfIntend + STRINDENT + loopInfo.getBodyMarker().strPrintf());
        }

        // get some dummy commands (but only in non-unrolling loops)
        if (loopInfo.getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT) {
            addDummies(currentDepth, f, list, strInfIntend + STRINDENT, loopInfo.lngGetLoopID());
        }
        else {
            // add a getchar() statement to prevent the body code markers to be merged into one big printf
            list.add((strInfIntend + STRINDENT + "getchar();"));
        }

        // control flow statements that transfer control out of this loop
        if (loopInfo.bGetELC_UseBreak()){                       // add break if needed
            list.add(strInfIntend + STRINDENT + "if (getchar()==23) {break;}");
        }
        if (loopInfo.bGetELC_UseExit()){                        // add exit it needed
            list.add(strInfIntend + STRINDENT + "if (getchar()==97) {exit(1923);}");
        }
        if (loopInfo.bGetELC_UseReturn()){                      // add return if needed
            if (f.getType().bIsPrimitive()) {
                // for any primitive, we can rely on the default value based on the type
                list.add(strInfIntend + STRINDENT + "if (getchar()==31) {return " + f.getType().strDefaultValue(m_cgenerator.structsByName) + ";}");
            }
            else{
                // for any non-primitive, we must instantiate a return struct
                list.add(strInfIntend + STRINDENT + "if (getchar()==31) {struct " + f.getType().getName() + " out; return out;}");
            }
        }
        if (loopInfo.bGetELC_UseGotoDirectlyAfterThisLoop()){   // add goto outside of loop, if needed
            list.add(strInfIntend + STRINDENT + "if (getchar()==19) {goto " + strGotoLabel(strDirectlyAfterLoopLabel) + ";} // goto directly after");
        }
        if (loopInfo.bGetELC_UseGotoFurtherFromThisLoop()){     // add goto further outside of loop, if needed
            var lcm = new LoopCodeMarker(ELoopMarkerLocationTypes.BEFORE_GOTO_FURTHER_AFTER);
            lcm.setLoopID(loopInfo.lngGetLoopID());
            list.add(strInfIntend + STRINDENT + "if (getchar()==83) {");
            list.add(strInfIntend + STRINDENT + STRINDENT + lcm.strPrintf());
            list.add(strInfIntend + STRINDENT + STRINDENT + "goto " + strGotoLabel(strFurtherAfterLoopLabel) + "; // goto further after");
            list.add(strInfIntend + STRINDENT + "}");
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
                var lcm = new LoopCodeMarker(ELoopMarkerLocationTypes.BEFORE_GOTO_BREAK_MULTIPLE);
                lcm.setLoopID(loopInfo.lngGetLoopID());
                list.add(strInfIntend + STRINDENT + "if (getchar()==73) {");
                list.add(strInfIntend + STRINDENT + STRINDENT + lcm.strPrintf());
                list.add(strInfIntend + STRINDENT + STRINDENT + "goto " + STRPLACEHOLDER + "; // goto end of root loop");
                list.add(strInfIntend + STRINDENT + "}");
                // if the top loop is closed, all placeholders are substituted with the appropriate label
            }
        }

        // get some dummy commands
        if (loopInfo.getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT) {
            addDummies(currentDepth, f, list, strInfIntend + STRINDENT, loopInfo.lngGetLoopID());
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
                list.add(strInfIntend + STRINDENT + item);
            }
        }

        // control flow statements that transfer control within this loop
        if (loopInfo.bGetILC_UseContinue()){                    // add continue if needed
            list.add(strInfIntend + STRINDENT + "if (getchar()==67) {continue;}");
        }
        if (loopInfo.bGetILC_UseGotoEnd()){                     // add goto <end-of-loop> if needed (loop var will be updated)
            list.add(strInfIntend + STRINDENT + "if (getchar()==17) {goto " + strGotoLabel(strEndOfBodyLabel) + ";} // goto end of loop body");
        }

        // get some dummy commands
        if (loopInfo.getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT) {
            addDummies(currentDepth, f, list, strInfIntend + STRINDENT, loopInfo.lngGetLoopID());
        }

        // finish up body with update command if needed and the closing statements
        list.add(strInfIntend + STRINDENT + strEndOfBodyLabel);
        if ((loopInfo.getLoopCommand() != ELoopCommands.FOR) &&
                (loopInfo.getLoopExpressions().bUpdateAvailable())){
            list.add(strInfIntend + STRINDENT + loopInfo.strGetCompleteLoopUpdateExpression() + ";");
        }
        else{
            list.add(strInfIntend + STRINDENT + ";");
        }
        list.add(strInfIntend + loopInfo.strGetLoopTrailer());

        ///////////////////
        // close the TIL-if
        ///////////////////
        if (loopInfo.bMakeConditional()) {
            list.add("}");
        }

        /////////////////
        // after the loop
        /////////////////
        // add code
        list.add(strDirectlyAfterLoopLabel);                    // label
        list.add(loopInfo.getEndMarker().strPrintf());          // after-body-marker
        addDummies(currentDepth, f, list, "", loopInfo.lngGetLoopID());  // get dummy statements
        list.add(strFurtherAfterLoopLabel);                     // and put end-of-all-label
        list.add(";");                                          // add skip (or be cursed upon by the C compiler grammar)

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
                            strGotoLabel(strDirectlyAfterLoopLabel) +
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
    private synchronized LoopInfo getNextLoopInfo(){
        // the loop_repo is shuffled when created in the class "constructor"
        // so, we can simply pick one item each time
        // every time the entire repo is completely used, it is reshuffled
        //
        // in case multiple generators do their jobs in multiple threads, we expect
        // race conditions, because they all use the same class wide repo
        // therefore, this method is synchronized

        LoopInfo loopInfo = null;
        synchronized (s_syncObj) {
            // get current loop info
            loopInfo = s_loopRepo.get(s_iLoopRepoPointer);

            // increase number of loops produced
            m_iNLoopsProduced++;

            // increase loop info object pointer
            s_iLoopRepoPointer++;
            if (s_iLoopRepoPointer == s_loopRepo.size()) {
                // start again
                s_iLoopRepoPointer = 0;
                // re-shuffle repo
                Collections.shuffle(s_loopRepo);
                // no longer automatically mark the work as done,
                // for the repo is now static, so an instance of the LoopProducer
                // doesn't necessarily start at the beginning of the repo
            }
            if (m_iNLoopsProduced >= m_iSatisfactionCutOff) {
                // also stop after earlier cut off
                m_bSatisfied = true;
            }
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
                var function = m_cgenerator.getFunction(currentDepth, m_cgenerator.getRawDataType());
                if (function.getParameters().isEmpty() && !function.hasVarArgs()) {
                    list.add(function.getName() + "();");
                }
                else {
                    var arguments = new ArrayList<String>();
                    for(var parameter : function.getParameters()) {
                        arguments.add(m_cgenerator.getNewExpression(currentDepth + 1, parameter.getType()));
                    }
                    // in case of a varargs function, we could add extra arguments, but we decide not to, at least not for
                    // now; in future versions this might change.
                    list.add(function.getName() + "(" + String.join(", ", arguments) + ");");
                }
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
    private void addDummies(int currentDepth, Function f, List<String> list, String strIndent, long lngLoopID) {
        for (var item : m_cgenerator.getNewStatements(currentDepth + 1, f, s_dummyPrefs)) {
            if (!item.isBlank()) {
                LoopCodeMarker lcm = (LoopCodeMarker)CodeMarker.findInStatement(EFeaturePrefix.CONTROLFLOWFEATURE, item);
                if (lcm != null){
                    // adapt any loop code markers found, to include loopID and dummy-status
                    lcm.setLoopID(lngLoopID);
                    lcm.setAsDummyStatement();
                    item=lcm.strPrintf();
                }
                list.add(strIndent + item); // get dummy statements and indent them
            }
        }
    }

    /**
     * Get the next loop pattern from the list and fill it with loop definitions
     * @return another (filled) loop pattern
     */
    public LoopPatternNode getNextFilledLoopPattern(){
        return AttachLoops(getNextLoopPattern());
    }

    /**
     * get the next loop pattern from the list
     * @return another loop pattern
     */
    private LoopPatternNode getNextLoopPattern(){
        m_iLoopPatternIndex++;
        if (m_iLoopPatternIndex >= m_patternRepo.size()) {
            // processed the entire collection: shuffle the lot and start again
            Collections.shuffle(m_patternRepo, Misc.rnd);
            m_iLoopPatternIndex = 0;
        }
        return new LoopPatternNode(m_patternRepo.get(m_iLoopPatternIndex));
    }

    /**
     * attach LoopInfo objects to a loop pattern and then check for multiple breaks
     * if the deepest loop has no multiple-break-statement, but another loop has,
     * than these two are switched, in order to make sure that multiple-loop-breaks
     * are really used
     * @param patternNode  root node of the tree to be processed
     */
    private LoopPatternNode AttachLoops(LoopPatternNode patternNode){
        // keep track of all the nodes, sorted by level
        List<List<LoopPatternNode>> levellist = new ArrayList<>();
        // keep track of leaves and other nodes
        List<LoopPatternNode> leaflist = new ArrayList<>();
        List<LoopPatternNode> nodelist = new ArrayList<>();

        // attach all loop recursively
        recurseAttach(patternNode, levellist, leaflist, nodelist);

        // make sure that a multi-breakout-goto is put in the loop with the highest nesting level
        switchBreakOuts(levellist);
        // make sure no unroll-able loop has children
        switchUnrollables(leaflist, nodelist, levellist.size());

        // return the value
        return patternNode;
    }

    private void switchUnrollables(List<LoopPatternNode> leaflist, List<LoopPatternNode> nodelist, int iNumberOfLevels){
        // make a list of nodes with unrollables
        List<LoopPatternNode> nodesToBeSwitched = new ArrayList<>();
        for (var item : nodelist){
            if (item.getLoopInfo().getUnrolling() != ELoopUnrollTypes.NO_ATTEMPT){
                nodesToBeSwitched.add(item);
            }
        }
        // if there are none, be done
        if (nodesToBeSwitched.isEmpty()){
            return;
        }

        // there are nodes to be switched with leaves
        // a leaf that contains a multiple-loop-break and has the highest nesting level, is not suitable, so such
        // a leaf must be removed from the leaf list
        // only max one leaf must be removed. In case multiple leaves have a multiple-loop-break and also the
        // highest nesting level, the others may remain. The one removed ensures the property that, in case multiple
        // loop breaking is present, it is at least present in a loop with the highest nesting level.
        //
        // leaves that already contain unrollables are also not welcome, as switching would not solve the problem
        //
        // copy all the leaves that do not contain unrollables
        List<LoopPatternNode> cleanedLeaflist = new ArrayList<>();
        for (var item : leaflist){
            if (item.getLoopInfo().getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT){
                cleanedLeaflist.add(item);
            }
        }
        // check for the multiple breakout and remove is necessary
        for (var item : cleanedLeaflist){
            if (item.getLoopInfo().bGetELC_BreakOutNestedLoops() && item.iGetNumParents()==(iNumberOfLevels-1)){
                // found a leaf to be removed, so remove and done searching
                cleanedLeaflist.remove(item);
                break;
            }
        }

        // add leaves if (and while) necessary
        while (cleanedLeaflist.size() < nodesToBeSwitched.size()){
            // select random node, but make sure it is one level down from the highest nesting level,
            // because a multi-break-loop might be added
            //
            // start randomly
            int iNodeIndex = Misc.rnd.nextInt(0, nodelist.size());
            LoopPatternNode patternNode = null;
            while (true) {
                // check whether the node has a nesting level one below max
                patternNode = nodelist.get(iNodeIndex);
                if (patternNode.iGetNumParents() == (iNumberOfLevels-2)){
                    // yes, so we're happy
                    break;
                }
                // no, so we try the next node
                iNodeIndex++;
                iNodeIndex%=nodelist.size();
                // the loop will terminate, as there is always a node that satisfies the condition.
                // in case the tree only contains a root node, that root node is a leaf. A single leaf is
                // never a problem, so the execution won't reach this point. Whenever the root node has
                // child nodes, it can itself satisfy the property that it has a nesting level
                // one-below-deepest-nesting-level and otherwise one of its descendants will satisfy the
                // property
            }
            // keep adding leaves to this node, as long a necessary
            var li = new LoopInfo(getNextLoopInfo());           // next loop info
            var lpn = new LoopPatternNode(li);                  // new loop pattern node
            patternNode.addChild(lpn);                          // add loop pattern node as a child
            // add this node to the cleaned leaf list, if it is a suitable spot for switching
            if (li.getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT && !li.bGetELC_BreakOutNestedLoops()){
                cleanedLeaflist.add(lpn);
            }
        }

        // so, now we finally have two lists:
        // one list of nodes that contain unrollables, these need to be switched to suitable leaves
        // the list of suitable leaves is at least as long as the list of nodes to be switched
        // this makes switching an easy job in the end
        //
        // randomize targets
        Collections.shuffle(cleanedLeaflist, Misc.rnd);
        // perform the switches
        int iDestIndex = 0;
        for (var node : nodesToBeSwitched){
            var tmp = node.getLoopInfo();
            node.setLoopInfo(cleanedLeaflist.get(iDestIndex).getLoopInfo());
            cleanedLeaflist.get(iDestIndex).setLoopInfo(tmp);
            iDestIndex++;
        }
    }

    private void recurseAttach(LoopPatternNode node, List<List<LoopPatternNode>> levellist,
                               List<LoopPatternNode> leaflist, List<LoopPatternNode> nodelist){
        // attach LoopInfo to this node
        //
        // make copy from repo, because it is possible that repo items are used more than
        // once and that would mean that loop ID's would be re-used as well.
        node.setLoopInfo(new LoopInfo(getNextLoopInfo()));
        // keep node in level list
        int lev = node.iGetNumParents();
        while (levellist.size()<=lev){
            levellist.add(new ArrayList<>());
        }
        levellist.get(lev).add(node);
        // keep node in either node list or leaf list
        if (node.iGetNumChildren()>0){
            nodelist.add(node);
        }
        else{
            leaflist.add(node);
        }
        // attach LoopInfo to all the children
        for (int c=0; c<node.iGetNumChildren(); ++c) {
            recurseAttach(node.getChild(c), levellist, leaflist, nodelist);
        }
    }

    private void switchBreakOuts(List<List<LoopPatternNode>> levlist){
        // look for breakouts
        for (int lev = levlist.size()-1; lev>=0 ; lev--){
            for (var item : levlist.get(lev)){
                if (item.getLoopInfo().bGetELC_BreakOutNestedLoops()){
                    // if breakout is found in deepest level -- do nothing and done
                    // if otherwise, switch and done
                    if (lev != levlist.size()-1) {
                        // not found in highest level, so switch needed
                        var list_up = levlist.get(levlist.size() - 1);
                        var tmp = item.getLoopInfo();
                        item.setLoopInfo(list_up.get(0).getLoopInfo());
                        list_up.get(0).setLoopInfo(tmp);
                    }
                    // in any case: done
                    return;
                }
            }
        }
    }

    @Override
    public Function getNewFunction(int currentDepth, DataType type, EWithParameters withParameters) {
        assert m_cgenerator != null : "No C-generator object";

        // basics: data type and empty function object
        if (type == null) {
            type = m_cgenerator.getRawDataType();
        }
        var function = new Function(type);    // use auto-name constructor

        // add a parameter, when requested
        if(withParameters != EWithParameters.NO){
            function.addParameter(new FunctionParameter("p" + 1, m_cgenerator.getRawDataType()));
        }

        // add loop statements
        var pattern = getNextFilledLoopPattern();
        var list = new ArrayList<String>();
        getLoopStatements(currentDepth, function, list, pattern);
        function.addStatements(list);

        // add return statement
        if(type.getName().equals("void")) {
            function.addStatement("return;");
        }
        else {
            function.addStatement("return " + type.strDefaultValue(m_cgenerator.structsByName) + ";");
        }

        // and done ;-)
        return function;
    }
}
