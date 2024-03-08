package nl.ou.debm.common.feature1;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.antlr.CBaseListener;
import nl.ou.debm.common.antlr.CParser;

import java.util.*;

/*

    Loop beauty score - school mark 0...10.

    A total of 10 points can be allocated to every loop. The average is the test score.

    A loop present in any form in decompiled code:      +1
    B loop present as any loop in decompiled code:      +2
    C loop command correct:                             +1
    D no loop doubling                                  +1
    E loop variable test equation correct:              +1
    F lack of goto's except goto further/multiple break +2
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

    if A == 0, total score will always be 0.

 */


public class LoopCListener extends CBaseListener {
    private final static double DBL_MAX_A_SCORE = 1,
                                DBL_MAX_B_SCORE = 2,
                                DBL_MAX_C_SCORE = 1,
                                DBL_MAX_D_SCORE = 1,
                                DBL_E_SCORE_ONLY_NOT_GETCHAR = .5,
                                DBL_MAX_E_SCORE = 1,
                                DBL_MAX_F_SCORE = 1,
                                DBL_MAX_G_SCORE = 1,
                                DBL_MAX_H_SCORE = 1;


    /**
     * Info on loops that are found in de decompiled C code
     */
    private static class FoundLoopInfo{
        /** function name where the loop was found */
        public String m_strInFunction = "";
        /** a list of loop commands that are found belonging to this loop */
        public final List<ELoopCommands> m_loopCommandsInCode = new ArrayList<>();
        /**  true if the code marker "before" is found more than once */
        public boolean m_bLoopBeforeCodeMarkerDuplicated = false;
        /** loop code marker that defines the loop (before code marker), containing all loop info the producer made */
        public LoopCodeMarker m_DefiningLCM;
        /** number of body code markers found for this loop */
        public int m_iNBodyCodeMarkers = 0;
        /** the exit-test; while (test), do {} while (test), for (...; test ; ...) */
        public String m_strLoopVarTest = "";
        /** array of all loop code markers for this loop */
        public final List<CodeMarker> m_lcm = new ArrayList<>();
        /** true if the first statement in a loop body is the loop body code marker */
        public boolean m_bFirstBodyStatementIsBodyCodeMarker = false;
    }

    private static class LoopBeautyScore {
        /** 1 if the loop found in the LLVM is also found (in any form) in de decompiled C code */
        public double m_dblLoopProgramCodeFound = 0;
        /** 2 if the loop is found as *a* loop, regardless of the do/while/for command */
        public double m_dblLoopCommandFound = 0;
        /** 1 if the correct loop command is found */
        public double m_dblCorrectLoopCommand = 0;
        /** 1 if the loop is a clean loop, without any (partial) unrolling */
        public double m_dblNoLoopDoubling = 0;
        /** 1 if the equation to break the loop is correct (or when no equation is expected (TIL loops)) */
        public double m_dblEquationScore = 0;
        /** 2 if the loop contains no goto's, other than a goto-further-from-body or a goto-break-multiple-loops */
        public double m_dblGotoScore = DBL_MAX_F_SCORE;
        /** 1 if all the code markers in the body are in correct order */
        public double m_dblBodyFlow = 0;
        /** 1 if the first body statement is the body code marker */
        public double m_dblNoCommandsBeforeBodyMarker = 0;
        /**
         * calculate the total for this loop
         * @return sum of all the parts, but only returns a score ig the loop is found in any way in the DC;
         *         range is 0...10
         */
        public double dblGetTotal(){
            double sum = m_dblLoopProgramCodeFound +
                         m_dblLoopCommandFound +
                         m_dblCorrectLoopCommand +
                         m_dblNoLoopDoubling +
                         m_dblEquationScore +
                         m_dblGotoScore +
                         m_dblBodyFlow +
                         m_dblNoCommandsBeforeBodyMarker;
            return m_dblLoopProgramCodeFound == 0 ? 0 : sum;
        }
    }
    /** map of beauty scores, key = loopID */
    private final Map<Long, LoopBeautyScore> m_beautyMap = new HashMap<>();
    /** list of all code markers encountered in code, in the sequence that they are encountered */
    private final List<LoopCodeMarker> m_loopcodemarkerList = new ArrayList<>();


    /** list of all test results */
    private final List<IAssessor.TestResult> m_testResult = new ArrayList<>();
    /** info on all loops found, key = loopID */
    private final Map<Long, FoundLoopInfo> m_fli = new HashMap<>();
    /** info on code markers in LLVM, key = code marker ID */
    private Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_llvmInfo;
    /** map loop ID (key) to start code markerID (value) */
    private final Map<Long, Long> m_LoopIDToStartMarkerCMID = new HashMap<>();
    /** list of all loopID's from loops that are unrolled in the LLVM*/
    private final List<Long> m_loopIDsUnrolledInLLVM = new ArrayList<>();
    /** list of the ordinals of the tests performed, seerves as index*/
    private final List<Integer> m_testOridnalsList = new ArrayList<>();
    /** most recent code marker encountered */
    private LoopCodeMarker m_lastCodeMarker;

    /** current function name */
    private String m_strCurrentFunctionName;
    private final Stack<Long> m_currentLoopID = new Stack<>();
    private Long m_lngLookForThisLoopIDInCompoundStatement = null;

    public LoopCListener(final IAssessor.CodeInfo ci) {
        // set list to appropriate size
        while (m_testResult.size()<ETestCategories.values().length){
            m_testResult.add(null);
        }
        // setup test class objects
        addTestClass(new IAssessor.CountTestResult(), ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL);
        addTestClass(new IAssessor.CountTestResult(), ETestCategories.FEATURE1_NUMBER_OF_CORRECT_LOOP_COMMANDS);
        addTestClass(new IAssessor.CountTestResult(), ETestCategories.FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP);
        addTestClass(new SchoolTestResult(), ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_OVERALL);
        addTestClass(new SchoolTestResult(), ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_NORMAL);
        addTestClass(new SchoolTestResult(), ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED);

        // set configs
        for (var item : m_testResult) {
            if (item != null) {
                item.setCompilerConfig(ci.compilerConfig);
            }
        }

        // process LLVM info
        ProcessLLVM(ci);
    }

    private void addTestClass(IAssessor.TestResult tr, ETestCategories whichTest){
        // store which test is reported in the class
        tr.setWhichTest(whichTest);
        // put it in the list
        m_testResult.set(whichTest.ordinal(), tr);
        // store ordinal
        m_testOridnalsList.add(whichTest.ordinal());
    }

    private IAssessor.CountTestResult countTest(ETestCategories whichTest){
        assert m_testResult.get(whichTest.ordinal())!=null : "test is not in array";
        assert m_testResult.get(whichTest.ordinal()) instanceof IAssessor.CountTestResult : "Test is not of expected type (CountResultTest)";
        return (IAssessor.CountTestResult) m_testResult.get(whichTest.ordinal());
    }

    private SchoolTestResult schoolTest(ETestCategories whichTest){
        assert m_testResult.get(whichTest.ordinal())!=null : "test is not in array";
        assert m_testResult.get(whichTest.ordinal()) instanceof SchoolTestResult : "Test is not of expected type (SchoolResultTest)";
        return (SchoolTestResult) m_testResult.get(whichTest.ordinal());
    }

    @Override
    public void exitCompilationUnit(CParser.CompilationUnitContext ctx) {
        super.exitCompilationUnit(ctx);
        // compile beauty scores -- and done ;-)
        compileBeautyScores();
    }

    private void ProcessLLVM(final IAssessor.CodeInfo ci){
        // get llvm info from file
        m_llvmInfo = CodeMarker.getCodeMarkerInfoFromLLVM(ci.lparser_org);
        // remove all info on non-control-flow-features
        StrikeNonLoopCodeMarkers();

        // fill map with loopID's to codeMarkerID's
        // make list of expanded loops
        for (var item : m_llvmInfo.entrySet()){
            assert item.getValue().codeMarker instanceof LoopCodeMarker;// safe, since we selected before
            var lcm = (LoopCodeMarker) item.getValue().codeMarker;
            if (lcm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.BEFORE) {
                m_LoopIDToStartMarkerCMID.put(lcm.lngGetLoopID(), lcm.lngGetID());
            }
            if (lcm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.BODY){
                if (item.getValue().iNOccurrencesInLLVM>2){
                    // do not cut at 1, but at 2
                    // unrolled loops have a minimum number of iterations of 5
                    // 1 = not unrolled
                    // 2 = optimization result
                    // 5 + (thus more than 2) = unrolled
                    m_loopIDsUnrolledInLLVM.add(lcm.lngGetLoopID());
                }
            }
        }
        
        // determine upper limits
        countTest(ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL).setHighBound(m_LoopIDToStartMarkerCMID.size());
        countTest(ETestCategories.FEATURE1_NUMBER_OF_CORRECT_LOOP_COMMANDS).setHighBound(m_LoopIDToStartMarkerCMID.size());
        countTest(ETestCategories.FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP).setHighBound(m_loopIDsUnrolledInLLVM.size());

        // fill beauty score hashmap with all the LLVM-loops
        for (var item : m_llvmInfo.entrySet()){
            var lcm = (LoopCodeMarker) item.getValue().codeMarker;  // safe cast, as we've eliminated non-loop code markers from the list
            // create new score form for every possible loop
            long lngLoopID = lcm.lngGetLoopID();
            if (lngLoopID > 0) {
                m_beautyMap.put(lngLoopID, new LoopBeautyScore());
            }
        }
    }

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

    public List<IAssessor.TestResult> getTestResults(){
        // only copy non-nulls
        List<IAssessor.TestResult> out = new ArrayList<>();
        for (var item : m_testOridnalsList){
            out.add(m_testResult.get(item));
        }
        return out;
    }

    private void compileBeautyScores(){
        processScores();
        double sum = 0;
        int cnt = 0;
        for (var item : m_beautyMap.entrySet()){
            sum += item.getValue().dblGetTotal();
            cnt ++;
        }
        schoolTest(ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_OVERALL).setScore(sum/cnt);
    }

    private void processScores(){
        // determine A-E, H
        for (var item : m_fli.entrySet()){
            var score = m_beautyMap.get(item.getKey());
            var fli = item.getValue();
            // A-score: loop code marker was found in DC, so it was found, in some form, by the decompiler
            score.m_dblLoopProgramCodeFound = DBL_MAX_A_SCORE;
            // B-score: loop present as any loop
            score.m_dblLoopCommandFound = fli.m_loopCommandsInCode.isEmpty() ? 0 : DBL_MAX_B_SCORE;
            // C-score: correct loop command
            score.m_dblCorrectLoopCommand = dblScoreCorrectCommand(fli);
            // D-score: no loop doubling
            score.m_dblNoLoopDoubling = fli.m_iNBodyCodeMarkers == 2 ? 0 : DBL_MAX_D_SCORE;
            // E-score: correct loop continuation check
            score.m_dblEquationScore = dblScoreEquation(fli);
            // H-score: first body command is code marker
            score.m_dblNoCommandsBeforeBodyMarker = fli.m_bFirstBodyStatementIsBodyCodeMarker ? DBL_MAX_H_SCORE : 0;
        }
        // calculate G
        processBodyCodeMarkers();
        // F-score is done while processing the code
    }

    private void processBodyCodeMarkers(){
        // check if any code markers were found
        if (m_loopcodemarkerList.isEmpty()){
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
            for (int ptr = iFirstElement ; ptr<iLastPlusOneElement ; ++ptr){
                fli.m_lcm.add(purgedLoopCodeMarkerList.get(ptr));
            }
            fli.m_lcm.add(null);
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
                    var score = m_beautyMap.get(fli.m_DefiningLCM.lngGetLoopID());
                    score.m_dblBodyFlow = DBL_MAX_G_SCORE;
                }
            }
        }
    }

    private double dblScoreEquation(FoundLoopInfo fli){
        // only score if exactly 1 loop command is found
        if (fli.m_loopCommandsInCode.size() != 1) {
            return 0;
        }

        // score if loop has no loop var test expression
        // --> in which case it might be replaced by a while getchar()!=...
        if (fli.m_DefiningLCM.strGetTestExpression().isEmpty()){
            return DBL_MAX_E_SCORE;
        }

        // check that no getchar() is used
        String strCondensedLoopVarTest = fli.m_strLoopVarTest.replaceAll("\\s", "");
        if (strCondensedLoopVarTest.contains("getchar()")){
            return 0;
        }

        // check whether expression/value is ok
        if (strCondensedLoopVarTest.endsWith(fli.m_DefiningLCM.strGetTestExpression())){
            return DBL_MAX_E_SCORE;
        }
        // expression/equation not the same, so only half a score
        return DBL_E_SCORE_ONLY_NOT_GETCHAR;
    }

    private double dblScoreCorrectCommand(FoundLoopInfo fli){
        if (fli.m_loopCommandsInCode.size() == 1) {
            // assess correct command
            //
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

        m_strCurrentFunctionName = "";
        if (ctx.declarator() != null){
            if (ctx.declarator().directDeclarator()!=null){
                m_strCurrentFunctionName = ctx.declarator().directDeclarator().children.get(0).getText();
                m_currentLoopID.clear();
                m_lastCodeMarker = null;
            }
        }
    }

    @Override
    public void enterJumpStatement(CParser.JumpStatementContext ctx) {
        super.enterJumpStatement(ctx);

        // if we were looking for a body marker, it was unsuccessful
        // we weren't, we can keep it at null, saves an if
        m_lngLookForThisLoopIDInCompoundStatement = null;

        // only check goto's (jump statement can also be a continue, break or return)
        if (ctx.Goto() != null) {
            if (m_lastCodeMarker != null) {
                var loc = m_lastCodeMarker.getLoopCodeMarkerLocation();
                if (!((loc == ELoopMarkerLocationTypes.BEFORE_GOTO_FURTHER_AFTER) ||
                        (loc == ELoopMarkerLocationTypes.BEFORE_GOTO_BREAK_MULTIPLE))) {
                    // goto not wanted - reset goto score
                    var sc = m_beautyMap.get(m_lastCodeMarker.lngGetLoopID());
                    if (sc!=null) {
                        sc.m_dblGotoScore = 0;
                    }
                }
            }
            // no else
            // -------
            // we've found that retdec sometimes gets confused and puts a goto in the if-statement
            // preceding a TIL. That goto comes before the first code marker. The goto is no part of
            // the loop or it's construction. We simply ignore it.
        }
    }

    @Override
    public void enterPrimaryExpression(CParser.PrimaryExpressionContext ctx) {
        super.enterPrimaryExpression(ctx);
        if (!ctx.StringLiteral().isEmpty()){
            LoopCodeMarker lcm = (LoopCodeMarker) CodeMarker.findInStatement(EFeaturePrefix.CONTROLFLOWFEATURE, "x("+ctx.StringLiteral().get(0).getText() + ")");
            // this ^^^ is a safe cast. findInStatement either results null (when another type of code marker is found)
            // or a LoopCodeMarker object
            if (lcm!=null){
                // loop code marker, find loop ID
                long lngLoopID = lcm.lngGetLoopID();
                // store code marker for use in goto-code
                if (lcm.getLoopCodeMarkerLocation()!=ELoopMarkerLocationTypes.UNDEFINED) {
                    m_lastCodeMarker = lcm;
                }
                // add marker to list
                m_loopcodemarkerList.add(lcm);
                // process code marker
                if (lcm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.BEFORE) {
                    // add to stack
                    m_currentLoopID.push(lngLoopID);
                    // get info object on loop, should not be present yet
                    var fli = m_fli.get(lngLoopID);
                    if (fli!=null) {
                        // loop ID already found -- pre-loop code marker was doubled!
                        fli.m_bLoopBeforeCodeMarkerDuplicated = true;
                    }
                    else {
                        fli = new FoundLoopInfo();
                        fli.m_DefiningLCM = lcm;
                    }
                    m_fli.put(lngLoopID, fli);
                    fli.m_strInFunction = m_strCurrentFunctionName;
                }
                else if (lcm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.AFTER) {
                    // remove loop-ID from stack
                    while (true){
                        if (m_currentLoopID.empty()) {
                            break;
                        }
                        if (m_currentLoopID.pop()==lngLoopID) {
                            break;
                        }
                    }
                }
                else if (lcm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.BODY) {
                    // count number of body markers per loop
                    var fli = m_fli.get(lngLoopID);
                    if (fli!=null){
                        fli.m_iNBodyCodeMarkers++;
                    }
                }
            }
        }
    }

    @Override
    public void enterExpressionStatement(CParser.ExpressionStatementContext ctx) {
        super.enterExpressionStatement(ctx);

        // are we looking for a body marker?
        if (m_lngLookForThisLoopIDInCompoundStatement !=null){
            // yes we are! So process it
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
    }

    @Override
    public void enterLabeledStatement(CParser.LabeledStatementContext ctx) {
        super.enterLabeledStatement(ctx);

        // if the walker is looking for a first statement, we don't have to stop now
        // if we do nothing, the search continues; expressions may be labeled
    }

    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.enterSelectionStatement(ctx);

        // if the walker is looking for a first statement and encounters a selection
        // statement, the loop is in trouble - the body marker should be the first
        // - and unconditional - statement

        m_lngLookForThisLoopIDInCompoundStatement = null;
    }

    @Override
    public void enterCompoundStatement(CParser.CompoundStatementContext ctx) {
        super.enterCompoundStatement(ctx);

        // if the walker is looking for a first element, we don't have to stop now
        // compound statements may be nested, as long as the first non-compound-statement
        // is a body marker, it's ok

        // so... we do nothing
    }

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        super.enterIterationStatement(ctx);

        // if the walker is busy finding the first statement in a body loop, it can
        // stop looking, as it is an iteration statement. If it wasn't looking at all
        // it doesn't change a thing (but saves the trouble of an if)
        m_lngLookForThisLoopIDInCompoundStatement =null;

        // store loop command and, when present, loop test expression
        if (!m_currentLoopID.empty()){
            // get current loop ID
            var lngCurrentLoopID=m_currentLoopID.peek();
            // process loop command
            var fli = m_fli.get(lngCurrentLoopID);
            assert fli != null;       // safe assumption, as every m_lngCurrentLoopID update also puts a fli-object in the map
            var strLoopCommand = ctx.children.get(0).getText();
            switch (strLoopCommand) {
                case "for" -> {
                    fli.m_loopCommandsInCode.add(ELoopCommands.FOR);
                    if (!ctx.forCondition().forExpression().isEmpty()){
                        int iFirstSemi = -1;
                        for (int c=0;c<ctx.forCondition().getChildCount();c++) {
                            if (ctx.forCondition().getChild(c).getText().equals(";")){
                                if (iFirstSemi == -1){
                                    iFirstSemi = c;
                                }
                                else{
                                    if (c>(iFirstSemi+1)) {
                                        fli.m_strLoopVarTest = ctx.forCondition().getChild(iFirstSemi+1).getText();
                                    }
                                }
                            }
                        }
                    }
                }
                case "do" -> {
                    fli.m_loopCommandsInCode.add(ELoopCommands.DOWHILE);
                    fli.m_strLoopVarTest = ctx.expression().getText();
                }
                case "while" -> {
                    fli.m_loopCommandsInCode.add(ELoopCommands.WHILE);
                    fli.m_strLoopVarTest = ctx.expression().getText();
                }
            }

            // determine first body statement
            // ------------------------------
            //
            // this is done, in practice, by the enter-statement-calls
            // we only set it up here, by setting the loopID of the body marker we are looking for
            // we ignore compounds and labeled statements (as they contain statements themselves, which we check)
            // we end the search when finding a selection/iteration/jump-statement, as these rule out a first
            // expression-statement
            // if we find an expression statement while the search is still going on, it must be the first
            // statement and thus we check it for being a body code marker. If it is, the loop is deemed ok.
            m_lngLookForThisLoopIDInCompoundStatement = lngCurrentLoopID;
        }
    }
}