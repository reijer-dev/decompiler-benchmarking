package nl.ou.debm.common.feature1;

import nl.ou.debm.assessor.CountTestResult;
import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.antlr.CBaseListener;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.*;

import static nl.ou.debm.common.CodeMarker.findInPostFixExpression;
import static nl.ou.debm.common.Misc.dblSafeDiv;

/*

    Loop beauty score - school mark 0...10.

    A total of 10 points can be allocated to every loop. The average is the test score.

    A loop present in any form in decompiled code:      +1
    B loop present as any loop in decompiled code:      +1
    C loop command correct:                             +1
    D1 no loop leaking                                  +1  \
    D2 no after doubling                                +1   check occurences in LLVM
    D3 no before doubling                               +1  /
    E loop variable test equation correct:              +1
    F lack of goto's except goto further/multiple break +1
    G loop body control flow correct:                   +1
    H first body statement is body marker               +1
                                                       --- +
                                                        10

    ad A.
    check loop start code markers in LLVM, assure that each start code marker shows up in DC (decompiled C code)

    ad B.
    between loop start marker and latest loop body start marker, a DO/FOR/WHILE must be present

    ad C.
    do/for/while present according to expectations: ok
    for expected, do present: ok (and vice versa)
    while (true), for (;;) and do {} while (true): ok

    ad D.
    loop body command not found exactly twice. once means perfect loop,
    more than twice means unrolled loop added unrolled, two means a strange
    combination of one 'pre loop execution' and a loop.
    strange body doubling -- 0-1, after doubling 0-1, before doubling


    ad E.
    When no loop var test is present: score
    When loop var test is present: only score when it is in the loop command
                                   +.5 if test is other than getchar()-test
                                   +.5 if test is completely correct

    ad F.
    All goto's should be eliminated, except two
    - goto <end of loop body> --> continue
    - goto <directly after loop> --> break
    - goto <further after loop> --> may be present (even advisable)
    - goto <break multiple loops> --> may be present (even advisable)
    any non-wanted goto's found will set score to 0

    ad G.
    All markers in the loop must be in ascending order (marker ID is used)

    ad H.
    First (or possibly: only) loop statement must be start of body loop marker

    if A == 0, total score will always be 0 (regardless of other scores)
    if A == 1 and B ==0, total socre will always be 1 (regardless of other scores)

 */

/**
 * class to assess decompiled c code
 */
public class LoopCListener extends CBaseListener {
    /** beauty scores per part */
    private final static double DBL_MAX_A_SCORE = 1,
                                DBL_MAX_B_SCORE = 1,
                                DBL_MAX_C_SCORE = 1,
                                DBL_MAX_D1_SCORE = 1,
                                DBL_MAX_D2_SCORE = 1,
                                DBL_MAX_D3_SCORE = 1,
                                DBL_E_SCORE_ONLY_NOT_GETCHAR = .5,
                                DBL_MAX_E_SCORE = 1,
                                DBL_MAX_F_SCORE = 1,
                                DBL_MAX_G_SCORE = 1,
                                DBL_MAX_H_SCORE = 1;


    /**
     * Info on loops that are found in de decompiled C code
     */
    private static class FoundLoopInfo{
        /** function name where the loop was found */                                                           public String m_strInFunction = "";
        /** a list of loop commands that are found belonging to this loop */                                    public final List<ELoopCommands> m_loopCommandsInCode = new ArrayList<>();
        /**  the number of times a 'before' (=defining) code marker is found */                                 public int m_iNBeforeCodeMarkers = 0;
        /** loop code marker that defines the loop (before code marker), containing all loop info the producer made */public LoopCodeMarker m_DefiningLCM;
        /** loop code marker that marks the end of the loop */                                                  public LoopCodeMarker m_AfterLCM;
        /** number of body code markers found for this loop */                                                  public int m_iNBodyCodeMarkers = 0;
        /** found a body code marker outside a loop */                                                          public boolean m_bFoundAnyOutsideTheLoopBody = false;
        /** number of after code markers found for this loop */                                                 public int m_iNAfterCodeMarkers = 0;
        /** the exit-test; while (test), do {} while (test), for (...; test ; ...) */                           public String m_strLoopVarTest = "";
        /** the parse tree for the exit test */                                                                 public ParseTree m_LoopVarTestParseTree = null;
        /** array of all loop code markers for this loop */                                                     public final List<CodeMarker> m_lcm = new ArrayList<>();
        /** true if the first statement in a loop body is the loop body code marker */                          public boolean m_bFirstBodyStatementIsBodyCodeMarker = false;
    }

    /** beauty score per loop */
    private static class LoopBeautyScore {
        /** 1 if the loop found in the LLVM is also found (in any form) in de decompiled C code */  public double m_dblLoopProgramCodeFound = 0;
        /** 2 if the loop is found as *a* loop, regardless of the do/while/for command */           public double m_dblLoopCommandFound = 0;
        /** 1 if the correct loop command is found */                                               public double m_dblCorrectLoopCommand = 0;
        /** 1 if no body statements are outside the loop body */                                    public double m_dblNoLoopLeaking = 0;
        /** 1 if exactly one loop defining code marker is found */                                  public double m_dblNoLoopDoubleDefining = 0;
        /** 1 if exactly one loop ending code marker is found */                                    public double m_dblNoLoopDoubleEnding = 0;
        /** 1 if the equation to break the loop is correct (or when no equation is expected (TIL loops)) */public double m_dblEquationScore = 0;
        /** 2 if the loop contains no goto's, other than a goto-further-from-body or a goto-break-multiple-loops */public double m_dblGotoScore = DBL_MAX_F_SCORE;
        /** 1 if all the code markers in the body are in correct order */                           public double m_dblBodyFlow = 0;
        /** 1 if the first body statement is the body code marker */                                public double m_dblNoCommandsBeforeBodyMarker = 0;
        /**
         * calculate the total for this loop
         * @return sum of all the parts, but only returns a score ig the loop is found in any way in the DC;
         *         range is 0...10
         */
        public double dblGetTotal(){
            double sum = m_dblLoopProgramCodeFound +
                         m_dblLoopCommandFound +
                         m_dblCorrectLoopCommand +
                         m_dblNoLoopLeaking +
                         m_dblNoLoopDoubleDefining +
                         m_dblNoLoopDoubleEnding +
                         m_dblEquationScore +
                         m_dblGotoScore +
                         m_dblBodyFlow +
                         m_dblNoCommandsBeforeBodyMarker;
            return m_dblLoopProgramCodeFound == 0 ? 0 :
                   m_dblLoopCommandFound == 0 ? 1 :
                   sum;
        }

        @Override
        public String toString(){
            return m_dblLoopProgramCodeFound + ", " +
                    m_dblLoopCommandFound + ", " +
                    m_dblCorrectLoopCommand + ",  " +

                    m_dblNoLoopLeaking + ", " +
                    m_dblNoLoopDoubleDefining + ", " +
                    m_dblNoLoopDoubleEnding + ",  " +

                    m_dblEquationScore + ", " +
                    m_dblGotoScore + ", " +
                    m_dblBodyFlow + ", " +
                    m_dblNoCommandsBeforeBodyMarker + "--> " +
                    dblGetTotal();
        }
    }
    /** map of beauty scores, key = loopID */   private final Map<Long, LoopBeautyScore> m_beautyMap = new HashMap<>();
    /** list of all code markers encountered in code, in the sequence that they are encountered */  private final List<LoopCodeMarker> m_loopcodemarkerList = new ArrayList<>();

    /** list of all test results */                             private final List<IAssessor.TestResult> m_testResult = new ArrayList<>();
    /** info on all loops found, key = loopID */                private final Map<Long, FoundLoopInfo> m_fli = new HashMap<>();
    /** info on code markers in LLVM, key = code marker ID */   private final Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_llvmInfo = new HashMap<>();
    /** map loop ID (key) to start code markerID (value) */     private final Map<Long, Long> m_LoopIDToStartMarkerCMID = new HashMap<>();
    /** list of all loopID's from loops that are unrolled in the LLVM*/ private final List<Long> m_loopIDsUnrolledInLLVM = new ArrayList<>();
    /** list of the ordinals of the tests performed, serves as index*/  private final List<Integer> m_testOridnalsList = new ArrayList<>();
    /** loop code marker immediately before the current statement, null if statement before this statement is not a code marker, made for goto analysis */ private LoopCodeMarker m_precedingCodeMarkerForGotos = null;
    /** loop code marker immediately before the current statement, null if statement before this statement is not a code marker, made for loop analysis */ private LoopCodeMarker m_precedingCodeMarkerForLoops = null;

    /** current function name */                                private String m_strCurrentFunctionName;
    /** keep track of loop start code markers, remove when loop end code marker is found*/  private final Stack<Long> m_LngCurrentLoopID = new Stack<>();
    /** try to find a loop body statement as a first statement in a compound statement, null if nothing is searched */  private Long m_lngLookForThisLoopIDInCompoundStatement = null;
    /** current nesting level of compound statements, function compound statement = level 0*/ private int m_iCurrentCompoundStatementNestingLevel = 0;
    /** are we currently within a declaration?*/ private int m_iInDeclarationCount = 0;
    /** CodeInfo input */                           private IAssessor.CodeInfo m_ci;
    /** level counter for postfix expressions */    private int m_iPostFixExpressionLevel = 0;

    private enum EIfBranches{
        NOIF, TRUEBRANCH, ELSEBRANCH
    }
    private static class AssignmentInfo{
        public String strVarName = "";
        public String strVarValue = "";
        public int iIfLevel = 0;
        public EIfBranches eIfBranch;

        public AssignmentInfo(){}
        public AssignmentInfo(String strVarName, String strVarValue, int iIfLevel, EIfBranches eIfBranch){
            this.strVarName=strVarName;
            this.strVarValue=strVarValue;
            this.iIfLevel =iIfLevel;
            this.eIfBranch = eIfBranch;
        }
        public String toString(){
            return strVarName + " = \"..." + Misc.strSafeRightString(strVarValue,5) + " (" + iIfLevel + ", " + eIfBranch + ")";
        }
    }

    /** map variable name to variable info*/            private Map<String, AssignmentInfo> m_CMAssignmentsMap = new HashMap<>();
    private int m_iCurrentConditionalLevel = 0;

    /**
     * constructor
     * @param ci code info passing the code to be analysed
     */
    public LoopCListener(final IAssessor.CodeInfo ci) {
        // set list to appropriate size
        while (m_testResult.size()<ETestCategories.values().length){
            m_testResult.add(null);
        }
        // setup test class objects
        addTestClass(new CountTestResult(), ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL);
        addTestClass(new CountTestResult(), ETestCategories.FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED);
        addTestClass(new CountTestResult(), ETestCategories.FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP);
        addTestClass(new SchoolTestResult(), ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_OVERALL);
        addTestClass(new SchoolTestResult(), ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_NORMAL);
        addTestClass(new SchoolTestResult(), ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED);
        addTestClass(new CountTestResult(), ETestCategories.FEATURE1_NUMBER_OF_UNWANTED_GOTOS);
        countTest(ETestCategories.FEATURE1_NUMBER_OF_UNWANTED_GOTOS).setTargetMode(CountTestResult.ETargetMode.LOWBOUND);

        // set configs
        for (var item : m_testResult) {
            if (item != null) {
                item.setCompilerConfig(ci.compilerConfig);
            }
        }

        // process LLVM info
        ProcessLLVM(ci);

        // remember input object
        m_ci = ci;
    }

    /**
     * add test class to the appropriate structs
     * @param tr TestResult class to be processed
     * @param whichTest sets which test the TestResultClass represents
     */
    private void addTestClass(IAssessor.TestResult tr, ETestCategories whichTest){
        // store which test is reported in the class
        tr.setWhichTest(whichTest);
        // put it in the list
        m_testResult.set(whichTest.ordinal(), tr);
        // store ordinal
        m_testOridnalsList.add(whichTest.ordinal());
    }

    /**
     * retrieve a CountTestResult-object corresponding to a test
     * @param whichTest which test to access
     * @return the found object
     */
    private CountTestResult countTest(ETestCategories whichTest){
        return testGeneral(whichTest);
    }

    /**
     * retrieve a SchoolTestResult-object corresponding to a test
     * @param whichTest which test to access
     * @return the found object
     */
    private SchoolTestResult schoolTest(ETestCategories whichTest){
        return testGeneral(whichTest);
    }

    /**
     * retrieve a specified testResult object and test it for class correctness
     * @param whichTest which test to access
     * @return the specified test object
     * @param <T> the wanted test object type
     */
    private <T> T testGeneral(ETestCategories whichTest){
        Object objOut = m_testResult.get(whichTest.ordinal());
        T defOut = null;
        assert objOut!=null : "test is not in array";
        try {
            //noinspection unchecked
            defOut = (T) objOut;
        }
        catch (ClassCastException e){
            assert false : "wrong type of test result class";
        }
        catch (Throwable e){
            throw new RuntimeException(e);
        }
        return defOut;
    }

    /**
     * This callback is called when all the code has been processed. It makes sure that this code's score
     * is calculated
     * @param ctx the parse tree
     */
    @Override
    public void exitCompilationUnit(CParser.CompilationUnitContext ctx) {
        super.exitCompilationUnit(ctx);
        // determine loop count statistics
        loopCountStatistics();
        // compile beauty scores
        compileBeautyScores();
        // goto scores set on the fly
    }

    void loopCountStatistics(){
        for (var item : m_fli.entrySet()){
            var v = item.getValue();
            if (!v.m_loopCommandsInCode.isEmpty()){
                countTest(ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL).increaseActualValue();
                var lcm = v.m_DefiningLCM;
                var eWhichTest = ETestCategories.FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED;
                if (lcm!=null){
                    if (m_loopIDsUnrolledInLLVM.contains(lcm.lngGetLoopID())) {
                        eWhichTest=ETestCategories.FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP;
                    }
                }
                countTest(eWhichTest).increaseActualValue();
            }
        }
    }

    @Override
    public void enterDeclaration(CParser.DeclarationContext ctx) {
        super.enterDeclaration(ctx);
        //
        // a declaration is only comparable to a statement if it contains an initializer
        // we have to check this in the enterInitDeclarator function
        //
        m_iInDeclarationCount++;
    }

    @Override
    public void exitDeclaration(CParser.DeclarationContext ctx) {
        super.exitDeclaration(ctx);
        m_iInDeclarationCount--;
    }

    @Override
    public void enterInitDeclarator(CParser.InitDeclaratorContext ctx) {
        super.enterInitDeclarator(ctx);
        if (m_iInDeclarationCount>0){
            if (ctx.getChildCount()>1){
                // more than one child means some kind of init expression, which really is an expression,
                // so reset the just-before-variables
                m_precedingCodeMarkerForGotos = null;
                m_lngLookForThisLoopIDInCompoundStatement = null;
            }
        }
    }

    /**
     * retrieve LLVM info and set up its use in this class
     * @param ci code to be analysed
     */
    private void ProcessLLVM(final IAssessor.CodeInfo ci){
        synchronized (ci) {
            // get llvm info from file
            Map<String, Long> mapLLVMIDtoCodeMarkerID = new HashMap<>();
            CodeMarker.getCodeMarkerInfoFromLLVM(ci.lparser_org, m_llvmInfo, mapLLVMIDtoCodeMarkerID);

            // remove all info on non-control-flow-features
            StrikeNonLoopCodeMarkers();

            // fill the map with loopID's to codeMarkerID's
            for (var item : m_llvmInfo.entrySet()) {
                assert item.getValue().codeMarker instanceof LoopCodeMarker;// safe, since we selected before
                // map loop ID to the ID of the defining code marker
                var lcm = (LoopCodeMarker) item.getValue().codeMarker;
                if (lcm.getLoopCodeMarkerLocation() == ELoopMarkerLocationTypes.BEFORE) {
                    m_LoopIDToStartMarkerCMID.put(lcm.lngGetLoopID(), lcm.lngGetID());
                }
            }

            // check unrolled loops, more comment in the visitor class
            LoopLLVMVisitor visitor = new LoopLLVMVisitor(m_llvmInfo, mapLLVMIDtoCodeMarkerID);
            visitor.visit(ci.lparser_org.compilationUnit());
            m_loopIDsUnrolledInLLVM.clear();
            m_loopIDsUnrolledInLLVM.addAll(visitor.getIDsOfUnrolledLoops());

            // determine upper limits
            countTest(ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL).setHighBound(m_LoopIDToStartMarkerCMID.size());
            countTest(ETestCategories.FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP).setHighBound(m_loopIDsUnrolledInLLVM.size());
            countTest(ETestCategories.FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED).setHighBound(m_LoopIDToStartMarkerCMID.size() - m_loopIDsUnrolledInLLVM.size());

            // fill beauty score hashmap with all the LLVM-loops
            for (var item : m_llvmInfo.entrySet()) {
                var lcm = (LoopCodeMarker) item.getValue().codeMarker;  // safe cast, as we've eliminated non-loop code markers from the list
                // create a new score form for every possible loop
                long lngLoopID = lcm.lngGetLoopID();
                if (lngLoopID > 0) {
                    if (!m_beautyMap.containsKey(lngLoopID)) {
                        // only add when necessary (otherwise a lot of useless LoopBeautyScores would be made
                        m_beautyMap.put(lngLoopID, new LoopBeautyScore());
                    }
                }
            }
        }
    }

    /**
     * Only retain loop code markers in m_llvmInfo
     */
    private void StrikeNonLoopCodeMarkers(){
        List<Long> removeList = new ArrayList<>();  // list with all ID's to be removed
        for (var item : m_llvmInfo.entrySet()){
            if (!(item.getValue().codeMarker instanceof LoopCodeMarker)){
                removeList.add(item.getKey());
            }
        }
        for (var item : removeList){    // remove all items to be removed
            m_llvmInfo.remove(item);
        }
    }

    /**
     * Compile list of test results
     * @return test results
     */
    public List<IAssessor.TestResult> getTestResults(){
        // only copy non-nulls
        // and only copy normal loop results when normal loops are present,
        // and only copy unrolled loop results when unrolled loops are present
        boolean bNormalPresent = countTest(ETestCategories.FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED).dblGetHighBound()>0;
        boolean bUnrolledPresent = countTest(ETestCategories.FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP).dblGetHighBound()>0;
        List<IAssessor.TestResult> out = new ArrayList<>();
        for (var item : m_testOridnalsList){
            var tr = m_testResult.get(item);
            switch (tr.getWhichTest()){
                case FEATURE1_NUMBER_OF_LOOPS_NOT_UNROLLED, FEATURE1_LOOP_BEAUTY_SCORE_NORMAL -> {
                    if (bNormalPresent)    { out.add(tr); }
                }
                case FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP, FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED -> {
                    if (bUnrolledPresent)  { out.add(tr); }
                }
                default -> out.add(tr);
            }
        }
        return out;
    }

    /**
     * calculate aggregate beauty score for the analyzed code, being the
     * average of all loop beauty scores
     */
    private void compileBeautyScores(){
        processScores();
        double sumAll = 0, sumNormal = 0, sumUnrolled = 0;
        int cntAll = 0, cntNormal = 0, cntUnrolled = 0;
        // general score
        for (var item : m_beautyMap.entrySet()){
            // cumulative score over all loops
            sumAll += item.getValue().dblGetTotal();
            cntAll ++;
            if (m_loopIDsUnrolledInLLVM.contains(item.getKey())){
                // unrolled loop
                sumUnrolled += item.getValue().dblGetTotal();
                cntUnrolled ++;
            }
            else {
                // normal loop
                sumNormal += item.getValue().dblGetTotal();
                cntNormal ++;
            }
        }
        schoolTest(ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_OVERALL).setScore(dblSafeDiv(sumAll, (double)cntAll));
        schoolTest(ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_NORMAL).setScore(dblSafeDiv(sumNormal, (double)cntNormal));
        schoolTest(ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED).setScore(dblSafeDiv(sumUnrolled, (double)cntUnrolled));
    }

    /**
     * score the loops, based on all loop info acquired
     */
    private void processScores(){
        // determine A-E, H
        for (var item : m_fli.entrySet()){
            var score = m_beautyMap.get(item.getKey());
            if (score!=null) {
                var fli = item.getValue();
                // A-score: loop code marker was found in DC, so it was found, in some form, by the decompiler
                score.m_dblLoopProgramCodeFound = DBL_MAX_A_SCORE;
                // B-score: loop present as any loop
                score.m_dblLoopCommandFound = fli.m_loopCommandsInCode.isEmpty() ? 0 : DBL_MAX_B_SCORE;
                // C-score: correct loop command
                score.m_dblCorrectLoopCommand = dblScoreCorrectCommand(fli);
                // D-scores:
                processLoopCodeMarkersForDoublingIssues(score, fli);
                // E-score: correct loop continuation check
                score.m_dblEquationScore = dblScoreEquation(fli);
                // H-score: first body command is code marker
                score.m_dblNoCommandsBeforeBodyMarker = fli.m_bFirstBodyStatementIsBodyCodeMarker ? DBL_MAX_H_SCORE : 0;
            }
        }
        // calculate G
        processBodyCodeMarkersForCorrectOrder();
        // F-score is done while processing the code
    }

    /**
     * determine the D-scores
     */
    private void processLoopCodeMarkersForDoublingIssues(LoopBeautyScore score, FoundLoopInfo fli){
        // D2: no loop leaking
        score.m_dblNoLoopLeaking = fli.m_bFoundAnyOutsideTheLoopBody ? 0 : DBL_MAX_D2_SCORE;

        // D1+D3: take the number of occurrences in LLVM into account!

        // D1: at least one defined and not more than in the LLVM
        if (fli.m_iNBeforeCodeMarkers > 0) {
            if (fli.m_DefiningLCM==null) {
                score.m_dblNoLoopDoubleDefining = 0;
            }
            else{
                long lngNOC_before = m_llvmInfo.get(fli.m_DefiningLCM.lngGetID()).iNOccurrencesInLLVM;
                score.m_dblNoLoopDoubleDefining = fli.m_iNBeforeCodeMarkers <= lngNOC_before ? DBL_MAX_D1_SCORE : 0;
            }
        }

        // D3: at least one defined and not more than in the LLVM
        if (fli.m_iNAfterCodeMarkers > 0 ) {
            if (fli.m_AfterLCM==null) {
                score.m_dblNoLoopDoubleEnding = 0;
            }
            else {
                long lngNOC_after = m_llvmInfo.get(fli.m_AfterLCM.lngGetID()).iNOccurrencesInLLVM;
                score.m_dblNoLoopDoubleEnding = fli.m_iNAfterCodeMarkers <= lngNOC_after ? DBL_MAX_D3_SCORE : 0;
            }
        }
    }


    /**
     * determine if all loop code markers occurring in a loop body are in correct order
     */
    private void processBodyCodeMarkersForCorrectOrder(){
        // check if any code markers were found
        if (m_loopcodemarkerList.isEmpty()){
            // if none were found: no score -- at least a body code marker must be found
            return;
        }

        // make sub lists of code markers
        // - iFirstElement = 0 (start at the beginning)
        // - loop:
        //    - look for the first element after iFirstElement that has a different loopID from iFirstElement
        //      (when end of list is reached, the virtual element after the list is used) --> iLastPlusOneElement
        //    - add the set of elements to the list indicated by the loop ID
        //    - add trailing null
        //    - set iFirstElement to iLastPlusOneElement, so next series can be processed
        //    (repeat)
        //
        //    thus
        //        1 1 1 2 2 2 3 3 3 4 4 4 5 5 4 3 2 1 1 1 2 2 1 3 1
        //    becomes
        //        1 1 1 null 1 1 1 null 1 null 1 mull
        //        2 2 2 null 2 null 2 2 null
        //        3 3 3 null 3 null 3 null
        //        4 4 4 null 4 null
        //        5 5 null

        // first, strike all markers without loopID's (these markers are not in a loop, but dummy code after the loop)
        List<LoopCodeMarker> purgedLoopCodeMarkerList = new ArrayList<>(m_loopcodemarkerList.size());
        for (var item: m_loopcodemarkerList){
            if (item.lngGetLoopID()>=0){
                purgedLoopCodeMarkerList.add(item);
            }
        }
        if (purgedLoopCodeMarkerList.isEmpty()){
            return;
        }

        // try to find series of code markers that share the same loop ID and copy them to sub lists
        int iFirstElement = 0;
        while (iFirstElement<purgedLoopCodeMarkerList.size()){
            int iLastPlusOneElement = iFirstElement + 1;
            long lngCurrentLoopID = purgedLoopCodeMarkerList.get(iFirstElement).lngGetLoopID();
            while (iLastPlusOneElement < purgedLoopCodeMarkerList.size()){
                if (purgedLoopCodeMarkerList.get(iLastPlusOneElement).lngGetLoopID()!=lngCurrentLoopID){
                    break;
                }
                ++iLastPlusOneElement;
            }
            // copy
            var fli = m_fli.get(lngCurrentLoopID);
            if (fli!=null) {
                // it is possible that a loop code marker is found containing a loop ID that is not
                // in the m_fli-array. This happens when the start code marker is not in the decompiled C code
                for (int ptr = iFirstElement; ptr < iLastPlusOneElement; ++ptr) {
                    fli.m_lcm.add(purgedLoopCodeMarkerList.get(ptr));
                }
                fli.m_lcm.add(null);
            }
            // next set
            iFirstElement = iLastPlusOneElement;
        }

        // process sub lists
        for (var fliSet : m_fli.entrySet()){
            var fli = fliSet.getValue();
            if (!fli.m_lcm.isEmpty()){
                // loop code marker set should never be empty, but better be safe than sorry
                //
                // count number of nulls, may be 1 (trailing) or 2 (trailing + nested loop(s))
                int iNullCount = 0;
                for (var item : fli.m_lcm){
                    if (item == null){
                        ++iNullCount;
                        if (iNullCount>2){
                            break;  // break the count loop
                        }
                    }
                }
                if (iNullCount>2){
                    continue;   // more than 2 found, break the loopInfo loop
                }
                // test code marker ID sequence
                long lngLastID = -1;
                for (var item: fli.m_lcm){
                    if (item != null){
                        if (item.lngGetID()<=lngLastID){
                            lngLastID=-2;   // mark as error
                            break;
                        }
                        else {
                            lngLastID = item.lngGetID();
                        }
                    }
                }
                if (lngLastID!=-2){
                    // no error found, so score
                    var score = m_beautyMap.get(fliSet.getKey());
                    if (score!=null) {
                        score.m_dblBodyFlow = DBL_MAX_G_SCORE;
                    }
                }
            }
        }
    }

    /**
     * score loop test continuation equation
     * @param fli loop to be tested
     * @return score
     */
    private double dblScoreEquation(FoundLoopInfo fli){
        // scoring depends on the type op loop in question
        //
        // for TIL's, we test whether the expression is constant and true
        // for PFL's, we first distinguish between 'normal' loops and reconstructed unrolled loops
        // normal loops we can test easily, but reconstructed unrolled loops must be treated with care
        // - there may be loops without a loop variable, in which case the de compiler basically only
        //   needed to determine the number of iterations. The original expressions will be lost, only
        //   a counting expression is used -- we assume the decompiler to be able to count correctly
        //   and thus we score. It is, for the moment, too much work to test whether the
        //   correct number of iterations is achieved.

        // only score if exactly 1 loop command is found
        if (fli.m_loopCommandsInCode.size() != 1) {
            return 0;
        }

        // only score when defining LCM is found
        if (fli.m_DefiningLCM==null){
            return 0;
        }

        // remove all whitespace from test expression in code, to make matching easier
        String strCondensedLoopVarTest = fli.m_strLoopVarTest.replaceAll("\\s", "");

        // test for different situations
        if (fli.m_DefiningLCM.getLoopFinitude() == ELoopFinitude.TIL){
            // truly infinite loops
            // - in a do or while, we expect a 'true' or '1'
            //   strictly speaking, any constant non-zero number will do, but it is custom to use these
            //   values in code.
            // - in a for loop, it is custom to leave the expression empty, but not wrong to
            //   use a constant true expression, so we accept all three
            if (strCondensedLoopVarTest.equalsIgnoreCase("true")){
                return DBL_MAX_E_SCORE;
            }
            if (strCondensedLoopVarTest.equals("1")){
                return DBL_MAX_E_SCORE;
            }
            if (fli.m_DefiningLCM.getLoopCommand() == ELoopCommands.FOR){
                return strCondensedLoopVarTest.isEmpty() ? DBL_MAX_E_SCORE : 0;
            }
            return 0;
        }
        else if ((m_loopIDsUnrolledInLLVM.contains(fli.m_DefiningLCM.lngGetLoopID())) &&
                 (fli.m_DefiningLCM.getLoopUnrolling() ==  ELoopUnrollTypes.ATTEMPT_DO_NOT_PRINT_LOOP_VAR)){
            // PFL, unrolled in LLVM and no loop variable used in the printing statement
            // we cannot test the decompiler for the loop expression, as it is impossible to determine:
            // for (x=0;  x<10; ++x) yields the same 10 iterations as
            // for (x=10; x<20; ++x) (and an infinite other number of constructs)
            //
            return DBL_MAX_E_SCORE;
        }
        else {
            // PFL, either not unrolled or unrolled with a loop variable
            //
            // if loop had no loop var test expression in the source, just
            // score it. (NB: (true) doesn't count as test expression, as
            // no loop variable is used).
            // decompilers may move a conditional break to the while-
            // statement, which is in itself not a problem. It may lead to a
            // different sequence of the body code (-markers), but that's
            // a different point of assessing.
            if (fli.m_DefiningLCM.strGetTestExpression().isEmpty()) {
                return DBL_MAX_E_SCORE;
            }

            // it may be that no test expression is available, due to
            // ill-formed code that could not be processed by ANTLR
            // in which case we do not score
            if (fli.m_strLoopVarTest.isEmpty()){
                return 0;
            }

            // parse the test expression found
            var tree = fli.m_LoopVarTestParseTree;
            var walker = new ParseTreeWalker();
            var listener = new IterationTextExpressionListener();
            walker.walk(listener, tree);
            var expList = listener.getVTI();

            // we are looking for expressions of the pattern: [numeral] [comparator] [some variable expression]
            // expressions with this pattern will always produce exactly 1 element in expList, so we
            // ignore all other sizes
            if (expList.size()!=1){
                // no score
                return 0;
            }
            var exp = expList.get(0);

            // check that no getchar() is used
            // getc(<anything>) is considered to be equal to getchar(), as
            // getchar() is short for getc(stdin) and we do not use file I/O in our project
            if (exp.bContainsGetChar()) {
                return 0;
            }

            // test the test expression (pun intended ;-))
            if (exp.equalsTestExpression(fli.m_DefiningLCM.strGetTestExpression())){
                return DBL_MAX_E_SCORE;
            }

            // expression/equation different, so only half a score
            return DBL_E_SCORE_ONLY_NOT_GETCHAR;
        }
    }

    private double dblScoreCorrectCommand(FoundLoopInfo fli){
        if (fli.m_loopCommandsInCode.size() == 1) {
            // assess correct command
            //
            // 0. if there's nothing to compare, there is nothing to score
            if (fli.m_DefiningLCM==null){
                return 0;
            }
            // 1. found command is expected command
            if (fli.m_loopCommandsInCode.get(0) == fli.m_DefiningLCM.getLoopCommand()) {
                return DBL_MAX_C_SCORE;
            }
            // 2. interchangeability for and do
            else if (
                    ((fli.m_loopCommandsInCode.get(0) == ELoopCommands.FOR) && (fli.m_DefiningLCM.getLoopCommand() == ELoopCommands.WHILE)) ||
                    ((fli.m_loopCommandsInCode.get(0) == ELoopCommands.WHILE) && (fli.m_DefiningLCM.getLoopCommand() == ELoopCommands.FOR))
            ) {
                return DBL_MAX_C_SCORE;
            }
            // 3. when a TIL is found, the loop command is irrelevant
            else if (fli.m_DefiningLCM.getLoopFinitude()==ELoopFinitude.TIL) {
                return DBL_MAX_C_SCORE;
            }
        }
        return 0;
    }

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        super.enterFunctionDefinition(ctx);

        assert m_iCurrentCompoundStatementNestingLevel == 0 : "every function must start with compound statement nesting level 0" ;

        m_strCurrentFunctionName = "";
        if (ctx.declarator() != null){
            if (ctx.declarator().directDeclarator()!=null){
                if (ctx.declarator().directDeclarator().children!=null) {
                    m_strCurrentFunctionName = ctx.declarator().directDeclarator().children.get(0).getText();
                    m_LngCurrentLoopID.clear();
                    m_precedingCodeMarkerForGotos = null;
                    m_precedingCodeMarkerForLoops = null;
                }
            }
        }
    }

    @Override
    public void enterJumpStatement(CParser.JumpStatementContext ctx) {
        super.enterJumpStatement(ctx);

        // if we were looking for a body marker, it was unsuccessful
        // we weren't, we can keep it at null, saves an if
        m_lngLookForThisLoopIDInCompoundStatement = null;

        // only check goto's (a jump statement can also be a continue statement, break or return)
        if (ctx.Goto() != null) {
            // count goto's in general
            countTest(ETestCategories.FEATURE1_NUMBER_OF_UNWANTED_GOTOS).increaseHighBound();

            // assess the un-wanted-ness of the goto
            // if a goto is preceded immediately by a code marker, marking one of the two
            // jumps that we do allow, we do not count the goto as unwanted, otherwise, we do
            //
            // by default: unwanted
            boolean bUnwantedGoto = true;
            if (m_precedingCodeMarkerForGotos !=null) {
                // there was a code marker immediately before the code, so check if it's one that
                // allows the goto
                if ((m_precedingCodeMarkerForGotos.getLoopCodeMarkerLocation() == ELoopMarkerLocationTypes.BEFORE_GOTO_BREAK_MULTIPLE) ||
                    (m_precedingCodeMarkerForGotos.getLoopCodeMarkerLocation() == ELoopMarkerLocationTypes.BEFORE_GOTO_FURTHER_AFTER)) {
                    bUnwantedGoto = false;    // not unwanted, hurray!
                }
            }
            if (bUnwantedGoto){
                // This goto is unwanted.
                // We need to do two things:
                // (1) we mark the goto unwanted in general
                // (2) if the goto occurs in one of our loops, we mark the loop as having unwanted goto's
                //
                // (1) -- really simple
                countTest(ETestCategories.FEATURE1_NUMBER_OF_UNWANTED_GOTOS).increaseActualValue();
                // (2) -- not so difficult either
                if (!m_LngCurrentLoopID.empty()) {
                    // we are currently in a loop, get the ID
                    Long LngLoopID = m_LngCurrentLoopID.peek();
                    if (LngLoopID!=null) {
                        // get the loop beauty score
                        var sc = m_beautyMap.get(LngLoopID);
                        if (sc != null) {
                            // and set the score to 0...
                            sc.m_dblGotoScore = 0;
                        }
                    }
                }
                // no else:
                // we only score loops we created ourselves, so if the goto occurs somewhere else,
                // it does not affect any loop score
            }
        }

        // the jump may have been accepted because of the preceding code marker, but in any case
        // there should be a next code marker before the next goto, so we reset the previously found code marker
        m_precedingCodeMarkerForGotos = null;
        m_precedingCodeMarkerForLoops = null;
    }

    @Override
    public void enterPostfixExpression(CParser.PostfixExpressionContext ctx) {
        super.enterPostfixExpression(ctx);

        // a code marker has the form: call("....")
        // this is a postfix expression.
        // unfortunately, the "..." is also a postfix expression
        // if we do nothing, the code marker found when we find call("  ") will be
        // overwritten by the next enterPostFixExpression, because we don't recognize
        // code markers that are only string literals.
        //
        // to prevent this, we keep track of a postfix level. Normally, it is set to 0 -- we check for
        // code markers. When found, it is increased. Every subsequent call to enterPostFixExpression
        // will increase the level **BUT NOT TEST**. This basically means we ignore all other postfix
        // expressions until we're really done with this one
        //
        // the level is always lowered when the exit-routine is called (see below)

        if (m_iPostFixExpressionLevel==0) {
            m_precedingCodeMarkerForGotos = null;
            m_precedingCodeMarkerForLoops = null;

            // is this a loop code marker?
            var nodes = Misc.getAllTerminalNodes(ctx, true);
            // try to substitute vars for code markers
            if (nodes.size()>=4){
                var item=nodes.get(2);
                if (item.iTokenID == CLexer.Identifier){
                    var data = m_CMAssignmentsMap.get(item.strText);
                    if (data != null){
                        EIfBranches tf = inTrueOrElseBranch(ctx);
                        boolean bOK = false;
                        if (tf==EIfBranches.NOIF) {
                            bOK = true;
                        }
                        else if (m_iCurrentConditionalLevel > data.iIfLevel) {
                            bOK = true;
                        }
                        else {
                            bOK = (data.eIfBranch == tf);
                        }
                        if (bOK){
                            System.out.println(item.strText + "--->" + data.strVarValue);
                            item.strText = data.strVarValue;
                            item.iTokenID = CLexer.StringLiteral;
                        }
                    }
                }
            }
            LoopCodeMarker lcm = (LoopCodeMarker) CodeMarker.findInListOfTerminalNodes(nodes, EFeaturePrefix.CONTROLFLOWFEATURE);
            if (lcm != null) {
                ProcessLoopCodeMarker(lcm);
//                System.out.println("---> LCM = " + lcm);
                m_iPostFixExpressionLevel++;
            }
        }
        else{
            m_iPostFixExpressionLevel++;
        }
    }

    @Override
    public void exitPostfixExpression(CParser.PostfixExpressionContext ctx) {
        super.exitPostfixExpression(ctx);
        // lower the postfix expression level
        if (m_iPostFixExpressionLevel>0){
            --m_iPostFixExpressionLevel;
        }
    }

    private void ProcessLoopCodeMarker(LoopCodeMarker lcm){
        // loop code marker, find loop ID
        long lngLoopID = lcm.lngGetLoopID();
        // store code marker for use in goto-code
        m_precedingCodeMarkerForGotos = lcm;
        // store code marker for use in loop analysis
        m_precedingCodeMarkerForLoops = lcm;
        // add marker to the sequential list of all loop code markers
        m_loopcodemarkerList.add(lcm);
        // get fli-object
        var fli=safeGetFli(lngLoopID);
        // process code marker info
        switch (lcm.getLoopCodeMarkerLocation()) {

            case BEFORE -> {
                // count the number of before-markers
                fli.m_iNBeforeCodeMarkers++;
                // store code marker & function name (but only first time)
                if (fli.m_iNBeforeCodeMarkers ==1){
                    fli.m_DefiningLCM = lcm;
                    fli.m_strInFunction = m_strCurrentFunctionName;
                }
            }

            case BODY -> {
                // count the number of body markers per loop
                fli.m_iNBodyCodeMarkers++;
                // check if it's inside or outside the loop's body
                if (m_LngCurrentLoopID.empty()) {
                    // no current loop body
                    fli.m_bFoundAnyOutsideTheLoopBody = true;
                }
                else if (m_LngCurrentLoopID.peek()==null){
                    // current loop body is not recognized as one of ours
                    fli.m_bFoundAnyOutsideTheLoopBody = true;
                }
                else if (m_LngCurrentLoopID.peek() != lngLoopID) {
                    // found in another loop's body
                    fli.m_bFoundAnyOutsideTheLoopBody = true;
                }
            }

            case AFTER -> {
                // count the number of after markers per loop
                fli.m_iNAfterCodeMarkers++;
                // store code marker
                if (fli.m_iNAfterCodeMarkers ==1){
                    fli.m_AfterLCM = lcm;
                }
            }
        }
    }

    @Override
    public void enterAssignmentExpression(CParser.AssignmentExpressionContext ctx) {
        super.enterAssignmentExpression(ctx);

        if (ctx.assignmentOperator()!=null) {

            // get the assigned value
            var exp = Misc.getAllTerminalNodes(ctx.assignmentExpression(), true);
            // only continue on single argument
            if (exp.size()==1){
                // only continue on string
                if (exp.get(0).iTokenID == CLexer.StringLiteral){
                    // only continue on code marker
                    LoopCodeMarker lcm = (LoopCodeMarker) CodeMarker.MatchCodeMarkerStringLiteral(exp.get(0).strText, EFeaturePrefix.CONTROLFLOWFEATURE);
                    if (lcm!=null) {

//                        System.out.println("ASE (" + m_iCurrentConditionalLevel + "): " + ctx.getChild(0).getText() + " ==== " + exp.get(0).strText);

                        // determine true or false branch
                        EIfBranches tf = inTrueOrElseBranch(ctx);

                        // store assignment
                        String strVarName = ctx.getChild(0).getText();
                        m_CMAssignmentsMap.put(strVarName, new AssignmentInfo(strVarName, exp.get(0).strText, m_iCurrentConditionalLevel, tf));
//                        System.out.println(m_CMAssignmentsMap);
                    }
                }

            }
        }
    }

    private EIfBranches inTrueOrElseBranch(ParserRuleContext ctx){
        if (m_iCurrentConditionalLevel==0){
            return EIfBranches.NOIF;
        }
        ParserRuleContext ifCtx= ctx;
        ParserRuleContext statCtx = null;
        do {
            statCtx = ifCtx;
            ifCtx = ifCtx.getParent();
        } while (! (ifCtx instanceof CParser.SelectionStatementContext));
        if (((CParser.SelectionStatementContext) ifCtx).statement().get(0).equals(statCtx)){
            return EIfBranches.TRUEBRANCH;
        }
        return EIfBranches.ELSEBRANCH;
    }

    @Override
    public void enterExpressionStatement(CParser.ExpressionStatementContext ctx) {
        super.enterExpressionStatement(ctx);

        // are we looking for a body marker?
        if (m_lngLookForThisLoopIDInCompoundStatement !=null){
            // yes, we are! So process it
            LoopCodeMarker lcm = (LoopCodeMarker) CodeMarker.findInStatement(EFeaturePrefix.CONTROLFLOWFEATURE, ctx.getText());
            if (lcm != null) {
                if ((lcm.lngGetLoopID() == m_lngLookForThisLoopIDInCompoundStatement) &&
                    (lcm.getLoopCodeMarkerLocation() == ELoopMarkerLocationTypes.BODY)) {
                    m_fli.get(m_lngLookForThisLoopIDInCompoundStatement).m_bFirstBodyStatementIsBodyCodeMarker = true;
                }
            }
            // search is done, regardless of the result
            m_lngLookForThisLoopIDInCompoundStatement =null;
        }

        // reset previous code marker (needed for assessing goto's)
        // after the previous code marker something else is found
        // this 'something else' may a another code marker, but that will be processed later
        m_precedingCodeMarkerForGotos = null;
        // allow expressions between code marker and a loop, as expressions may be used to initialize the loop
    }

    /*@Override
    public void enterLabeledStatement(CParser.LabeledStatementContext ctx) {
        super.enterLabeledStatement(ctx);

        // if the walker is looking for a first statement, we don't have to stop now
        // if we do nothing, the search continues; expressions may be labeled

        // We don't do anything with m_precedingCodeMarker either, as a label as such is
        // no problem and the parser will continue into a statement.

        // In the end, we do nothing with this enter-function, so we've commented it out.
        // However, we kept the comment to be aware that we checked it and, if we ever were
        // to have to write real code in this callback, we know we don't have to worry
        // about the above
    }*/

    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.enterSelectionStatement(ctx);

        // if the walker is looking for a first statement and encounters a selection
        // statement, the loop is in trouble - the body marker should be the first
        // - and unconditional - statement

        m_lngLookForThisLoopIDInCompoundStatement = null;

        // no selection statement may be between the marker and the goto, so we
        // reset the previously found code marker
        m_precedingCodeMarkerForGotos = null;
        m_precedingCodeMarkerForLoops = null;

//        System.out.println("SEL: " + ctx.getText());
        m_iCurrentConditionalLevel++;
    }

    @Override
    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.exitSelectionStatement(ctx);

//        System.out.println("---S: " + ctx.getText());
//
        List<String> keysToDelete = new ArrayList<>(m_CMAssignmentsMap.size()+2);
        for (var item : m_CMAssignmentsMap.values()){
            if (item.iIfLevel == m_iCurrentConditionalLevel) {
                keysToDelete.add(item.strVarName);
            }
        }
        for (var item : keysToDelete){
            m_CMAssignmentsMap.remove(item);
        }

        m_iCurrentConditionalLevel--;
    }

    @Override
    public void enterCompoundStatement(CParser.CompoundStatementContext ctx) {
        super.enterCompoundStatement(ctx);

        // if the walker is looking for a first element, we don't have to stop now
        // compound statements may be nested, as long as the first non-compound-statement
        // is a body marker, it's ok
        // so... we do nothing

        // we don't have anything to do in our search for a code marker immediately
        // preceding a goto, as this would be perfectly fine: <code marker> { goto _LAB }

        // keep track of current compound statement nesting level
        m_iCurrentCompoundStatementNestingLevel++;
    }

    @Override
    public void exitCompoundStatement(CParser.CompoundStatementContext ctx) {
        super.exitCompoundStatement(ctx);

        // keep track of current compound statement nesting level
        m_iCurrentCompoundStatementNestingLevel--;
        assert m_iCurrentCompoundStatementNestingLevel >= 0 : "negative compound statement nesting level";
    }

    /**
     * return the fli (found-loop-information) object for a loop ID, create a new one when necessary
     * @param lngLoopID the loop's ID
     * @return the fli object
     */
    private FoundLoopInfo safeGetFli(long lngLoopID){
        // return a fli-object, make a new one when necessary
        var fli = m_fli.get(lngLoopID);
        if (fli==null){
            // make new instance
            fli = new FoundLoopInfo();
            m_fli.put(lngLoopID, fli);
        }
        return fli;
    }

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        super.enterIterationStatement(ctx);

        // if the walker is busy finding the first statement in a body loop, it can
        // stop looking, as it is an iteration statement. If it wasn't looking at all
        // it doesn't change a thing (but saves the trouble of an if)
        m_lngLookForThisLoopIDInCompoundStatement =null;

        // determine which loop this command belongs to
        Long LngCurrentLoopID = LngGetThisLoopsID(ctx);

        // remember info (maybe a null, but we're ok with that)
        m_LngCurrentLoopID.push(LngCurrentLoopID);

        // if it is one of our loops
        // - store the loop command and, when present, loop test expression
        // - start searching for the first loop code marker in the statement
        //
        if (LngCurrentLoopID!=null){
            // get loop info object (will never return null)
            var fli = safeGetFli(LngCurrentLoopID);
            // process command: store which it is and extract expression
            switch (ctx.children.get(0).getText()) {
                case "for" -> {
                    fli.m_loopCommandsInCode.add(ELoopCommands.FOR);
                    if (!ctx.forCondition().forExpression().isEmpty()){
                        int iFirstSemiColonIndex = -1;
                        for (int c=0;c<ctx.forCondition().getChildCount();c++) {
                            if (ctx.forCondition().getChild(c).getText().equals(";")){
                                if (iFirstSemiColonIndex == -1){
                                    iFirstSemiColonIndex = c;
                                }
                                else{
                                    if (c>(iFirstSemiColonIndex+1)) {
                                        // we need to test that there is something between the semicolons, as
                                        // this may be empty (infinite loops)
                                        fli.m_strLoopVarTest = ctx.forCondition().getChild(iFirstSemiColonIndex+1).getText();
                                        fli.m_LoopVarTestParseTree = ctx.forCondition().getChild(iFirstSemiColonIndex+1);
                                    }
                                }
                            }
                        }
                    }
                }
                case "do" -> {
                    fli.m_loopCommandsInCode.add(ELoopCommands.DOWHILE);
                    if (ctx.expression()!=null) {
                        // ill-formed do-commands may occur in the code and would otherwise cause trouble
                        fli.m_strLoopVarTest = ctx.expression().getText();
                    }
                    fli.m_LoopVarTestParseTree = ctx.expression();
                }
                case "while" -> {
                    fli.m_loopCommandsInCode.add(ELoopCommands.WHILE);
                    if (ctx.expression()!=null) {
                        // ill-formed while-commands may occur in the code and would otherwise cause trouble
                        fli.m_strLoopVarTest = ctx.expression().getText();
                    }
                    fli.m_LoopVarTestParseTree = ctx.expression();
                }
            }

            // determine the first body statement
            // ----------------------------------
            //
            // this is done, in practice, by the enter-statement-calls
            // we only set it up here, by setting the loopID of the body marker we are looking for
            // we ignore compounds and labeled statements (as they contain statements themselves, which we check)
            // we end the search when finding a selection/iteration/jump-statement, as these rule out a first
            // expression-statement
            // if we find an expression statement while the search is still going on, it must be the first
            // statement, and thus we check it for being a body code marker. If it is, the loop is deemed ok.
            m_lngLookForThisLoopIDInCompoundStatement = LngCurrentLoopID;
        }

        // no iteration statement may be between a marker and a goto, so we
        // reset the previously found code marker
        m_precedingCodeMarkerForGotos = null;
        m_precedingCodeMarkerForLoops = null;
    }

    private Long LngGetThisLoopsID(CParser.IterationStatementContext ctx){
        /*
            This function is about determining the loopID for a certain loop. This is not always easy.

            Main rule: we expect our loops to be preceded by a before-loop code marker, without any
            code between the code marker and the for/do/while. If this is the case, the for/do/while is
            assumed to be the loop defined by the before-loop code marker.

            In many cases, the main rule is inconclusive. Loops that carry a loop variable, for instance,
            may initialize this between the code marker and the loop (do/while). But sometimes things are
            messed up for other reasons, such as a loop leak between the before-loop code marker and the
            loop itself.
            In those cases, we plough on.

            We use a loop body listener to extract all code markers in the loop's body and assess them.
            More comment on the selection process is found in the listener code. In many cases, we succeed,
            but if we don't, we just return null.

         */

        // is there a defining loop code marker just before the loop command?
        if (m_precedingCodeMarkerForLoops !=null){
            if (m_precedingCodeMarkerForLoops.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.BEFORE){
                // loop command is preceded by loop code marker -- definite answer
                return m_precedingCodeMarkerForLoops.lngGetLoopID();
            }
        }

        // no preceding loop code marker -- try to find a loop code marker within the body,
        // containing a body code marker.
        //
        int iStatementChildIndex=0;
        // get statement parser tree
        switch (ctx.getChild(0).getText()) {
            case "while", "for" -> {
                iStatementChildIndex = ctx.getChildCount()-1;
            }
            case "do" -> {
                iStatementChildIndex = 1;
            }
        }

        // walk the loop statement, looking for code markers
        var tree = ctx.getChild(iStatementChildIndex);
        var walker = new ParseTreeWalker();
        var listener = new IterationBodyListener();
        walker.walk(listener, tree);

        // return the listener's result
        return listener.getLngLoopID();
    }

    @Override
    public void exitIterationStatement(CParser.IterationStatementContext ctx) {
        super.exitIterationStatement(ctx);
        // remove from stack
        m_LngCurrentLoopID.pop();
    }

    /**
     * Listener designed specifically to extract loop code markers to try to determine a loop's ID
     */
    private class IterationBodyListener extends CBaseListener{
        /** current nesting level of compound statements */             private int m_iCompoundLevel = 0;
        /** max compound level encountered */                           private int m_iMaxCompoundLevel = -1;
        /**
         * struct class for loop code marker info
         */
        private static class LoopAndLevelInfo{
            /** the code marker found*/                                 public LoopCodeMarker lcm;
            /** the compound level for this code marker */              public int iCompoundLevel = 0;
            LoopAndLevelInfo(LoopCodeMarker l, int lev){
                lcm=l;
                iCompoundLevel=lev;
            }
        }
        /** array of all the loop code markers and compound levels*/    private final List<LoopAndLevelInfo> m_lcm = new ArrayList<>();

        /**
         * retrieve the loop's ID from the found code markers
         * @return this loop's ID, null if it could not be found out
         */
        public Long getLngLoopID() {
            // analyze loop code markers

            // compound level should be zero, meaning we've exited every compound statement we've entered
            assert m_iCompoundLevel == 0 : "compound level error";

            // sub list per level
            final List<LoopAndLevelInfo> curLev = new ArrayList<>(m_lcm.size());
            // before-marker loop ID's per level
            final List<Long> beforeMarker = new ArrayList<>(m_lcm.size());
            // work per compound level
            for (int compoundLevel = 0; compoundLevel<=m_iMaxCompoundLevel ; compoundLevel++){
                // extract only this level's code markers + remember all before marker ID's
                curLev.clear();
                beforeMarker.clear();
                for (var cmi : m_lcm){
                    if (cmi.iCompoundLevel==compoundLevel){
                        // keep code marker
                        curLev.add(cmi);
                        if (cmi.lcm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.BEFORE){
                            beforeMarker.add(cmi.lcm.lngGetLoopID());
                        }
                    }
                }

                // unrolled loops will have before, body and after markers on the same level
                // these should be removed, as they are a nested loop in this loop instead of the loop
                // itself
                for (int i =0; i<curLev.size(); i++){
                    var cmi = curLev.get(i);
                    if (beforeMarker.contains(cmi.lcm.lngGetLoopID())){
                        // remove this code marker from the list...
                        curLev.remove(i);
                        // ... and make sure we don't skip markers
                        i--;
                    }
                }

                // 1. try to find a body code marker
                for (var cmi : curLev){
                    var lcm = cmi.lcm;
                    if (lcm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.BODY){
                        Long loopID = LngTravelUp(lcm.lngGetLoopID(), compoundLevel);
                        if (loopID!=null) {
                            return loopID;
                        }
                    }
                }
                // 2. try to find a before code marker (nested loop)
                for (var cmi : curLev) {
                    var lcm = cmi.lcm;
                    if ((lcm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.BEFORE) ||
                            (lcm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.AFTER)) {
                        Long loopID = LngTravelUp(lcm.lngGetLoopID(), compoundLevel+1);
                        if (loopID!=null) {
                            return loopID;
                        }
                    }
                }
                // 3. try to find a dummy code marker
                for (var cmi : curLev){
                    var lcm = cmi.lcm;
                    if (lcm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.UNDEFINED){
                        Long loopID = LngTravelUp(lcm.lngGetLoopID(), compoundLevel);
                        if (loopID!=null) {
                            return loopID;
                        }
                    }
                }
            }
            return null;
        }

        private Long LngTravelUp(Long LngCurrentLoopID, int iNLevels){
            // done recursing?
            if (iNLevels==0){
                return LngCurrentLoopID;
            }

            // try to find a parent
            ///////////////////////
            // get a code marker ID for the defining code marker of this loop
            Long codeMarkerID = m_LoopIDToStartMarkerCMID.get(LngCurrentLoopID);
            if (codeMarkerID==null){
                return null;
            }

            // get the defining code marker
            var cmInfo = m_llvmInfo.get(codeMarkerID);
            if (cmInfo==null){
                return null;
            }
            LoopCodeMarker definingLoopCodemarker = (LoopCodeMarker) cmInfo.codeMarker;

            // does this one have a parent?
            if (definingLoopCodemarker.iGetNestingLevel()==0){
                return null;    // no
            }

            // yes, so recurse
            return LngTravelUp(definingLoopCodemarker.lngGetParentLoopID(), iNLevels-1);
        }

        @Override
        public void enterExpressionStatement(CParser.ExpressionStatementContext ctx) {
            super.enterExpressionStatement(ctx);

            LoopCodeMarker lcm = (LoopCodeMarker) CodeMarker.findInStatement(EFeaturePrefix.CONTROLFLOWFEATURE, ctx.getText());
            if (lcm!=null) {
                // if a loop code marker is found, save it
                m_lcm.add(new LoopAndLevelInfo(lcm,m_iCompoundLevel));
                // keep track of the deepest compound level
                if (m_iMaxCompoundLevel<m_iCompoundLevel){
                    m_iMaxCompoundLevel=m_iCompoundLevel;
                }
            }
        }

        @Override
        public void enterIterationStatement(CParser.IterationStatementContext ctx) {
            super.enterIterationStatement(ctx);
            m_iCompoundLevel++;
        }

        @Override
        public void exitIterationStatement(CParser.IterationStatementContext ctx) {
            super.exitIterationStatement(ctx);
            m_iCompoundLevel--;
        }
    }

    /**
     * private class to search loop test expressions, works together with a further
     * listener class
     */
    private static class IterationTextExpressionListener extends CBaseListener{

        private final List<LoopTestInfo> m_vti = new ArrayList<>();
        public List<LoopTestInfo> getVTI(){
            return m_vti;
        }

        @Override
        public void enterRelationalExpression(CParser.RelationalExpressionContext ctx) {
            super.enterRelationalExpression(ctx);
            processInput(ctx);
        }

        @Override
        public void enterEqualityExpression(CParser.EqualityExpressionContext ctx) {
            super.enterEqualityExpression(ctx);
            processInput(ctx);
        }

        private void processInput(ParserRuleContext pcx){
            String [] args = {"", "", ""};
            if (pcx.children.size()>=3) {
                for (int chp=0;chp<3;chp+=2) {
                    var tree = pcx.getChild(chp);
                    var walker = new ParseTreeWalker();
                    var listener = new NumericConstantExpressionListener();
                    walker.walk(listener, tree);
                    args[chp] = listener.strGetValue();
                }
                m_vti.add(new LoopTestInfo(args[0],
                                          pcx.getChild(1).getText(),
                                          args[2]));
            }
        }
    }

    private static class NumericConstantExpressionListener extends CBaseListener {
        private final List<String> numberList = new ArrayList<>();
        private String m_strSign = "";
        private boolean m_bBlockIt = false;
        private boolean m_bContainsGetChar = false;
        public String strGetValue(){
            if (!m_bBlockIt) {
                if (numberList.size() == 1) {
                    return numberList.get(0);
                }
            }
            if (m_bContainsGetChar) {
                return "getchar()";
            }
            return "";
        }
        @Override
        public void enterPrimaryExpression(CParser.PrimaryExpressionContext ctx) {
            super.enterPrimaryExpression(ctx);
            String strWhat = ctx.getText();
            if (!strWhat.isEmpty()) {
                char f = strWhat.charAt(0);
                if ((f>='0') && (f<='9')) {
                    var x = new Misc.ConvertCNumeral(ctx.getText());
                    if (x.bIsFloat()) {
                        numberList.add(m_strSign + x.DblGetFloatLikeValue());
                    } else if (x.bIsInteger()) {
                        numberList.add(m_strSign + x.LngGetIntegerLikeValue());
                    }
                }
                else {
                    // non-number, so must be an identifier (for variable)
                    m_bBlockIt = true;
                }
                if (strWhat.startsWith("getc")) {
                    m_bContainsGetChar = true;
                }
            }
        }

        @Override
        public void exitUnaryOperator(CParser.UnaryOperatorContext ctx) {
            super.exitUnaryOperator(ctx);
            if (ctx.Minus()!=null){
                m_strSign ="-";
            }
        }

        @Override
        public void enterPostfixExpression(CParser.PostfixExpressionContext ctx) {
            super.enterPostfixExpression(ctx);
            if (ctx.children.size()>1){
                if (ctx.getChild(1).getText().equals("(")){
                    // no function calls!
                    m_bBlockIt = true;
                }
            }
        }
    }
}