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
    G loop body control flow correct:                   +2
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
    Loop body must begin with loop body start marker, all markers in the loop must be in ascending order (marker ID
    is used)

    if A == 0, total score will always be 0.

 */


public class LoopCListener extends CBaseListener {
    private final static double DBL_MAX_A_SCORE = 1,
                                DBL_MAX_B_SCORE = 2,
                                DBL_MAX_C_SCORE = 1,
                                DBL_MAX_D_SCORE = 1,
                                DBL_E_SCORE_ONLY_NOT_GETCHAR = .5,
                                DBL_MAX_E_SCORE = 1,
                                DBL_MAX_F_SCORE = 2;


    private static class FoundLoopInfo{
        public String m_strInFunction = "";
        public final List<ELoopCommands> m_loopCommandsInCode = new ArrayList<>();
        public boolean m_bLoopBeforeCodeMarkerDuplicated = false;
        public LoopCodeMarker m_lcm;
        public int m_iNBodyCodeMarkers = 0;
        public String m_strLoopVarTest = "";
    }

    private static class LoopBeautyScore {
        public double m_dblLoopProgramCodeFound = 0;
        public double m_dblLoopCommandFound = 0;
        public double m_dblCorrectLoopCommand = 0;
        public double m_dblNoLoopDoubling = 0;
        public double m_dblEquationScore = 0;
        public double m_dblGotoScore = DBL_MAX_F_SCORE;
        public double m_dblBodyFlow = 0;
        public double dblGetTotal(){
            double sum = m_dblLoopProgramCodeFound +
                         m_dblLoopCommandFound +
                         m_dblCorrectLoopCommand +
                         m_dblNoLoopDoubling +
                         m_dblEquationScore +
                         m_dblGotoScore +
                         m_dblBodyFlow;
            return m_dblLoopProgramCodeFound == 0 ? 0 : sum;
        }
    }
    private final Map<Long, LoopBeautyScore> m_beautyMap = new HashMap<>();
    private final List<LoopCodeMarker> m_loopcodemarkerList = new ArrayList<>();



    private final List<IAssessor.TestResult> m_testResult = new ArrayList<>();
    private final Map<Long, FoundLoopInfo> m_fli = new HashMap<>(); // info on all loops found
    private Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_llvmInfo;    // info on code markers in LLVM
    private final Map<Long, Long> m_LoopIDToStartMarkerCMID = new HashMap<>(); // map loop ID to start code marker
    private final List<Long> m_loopIDsUnrolledInLLVM = new ArrayList<>();
    private final List<Integer> m_testList = new ArrayList<>();
    private LoopCodeMarker m_lastCodeMarker;

    private String m_strCurrentFunctionName;
    private final Stack<Long> m_currentLoopID = new Stack<>();

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
        m_testList.add(whichTest.ordinal());
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
        for (var item : m_testList){
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
        // A-E
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
        }
        // F-score: goto's -- done on the fly
        // ---
    }

    private double dblScoreEquation(FoundLoopInfo fli){
        // only score if exactly 1 loop command is found
        if (fli.m_loopCommandsInCode.size() != 1) {
            return 0;
        }

        // score if loop has no loop var test expression
        // --> in which case it might be replaced by a while getchar()!=...
        if (fli.m_lcm.strGetTestExpression().isEmpty()){
            return DBL_MAX_E_SCORE;
        }

        // check that no getchar() is used
        String strCondensedLoopVarTest = fli.m_strLoopVarTest.replaceAll("\\s", "");
        if (strCondensedLoopVarTest.contains("getchar()")){
            return 0;
        }

        // check whether expression/value is ok
        var strConditionFromDC = strCondensedLoopVarTest.substring(strCondensedLoopVarTest.length()-fli.m_lcm.strGetTestExpression().length());
        if (strConditionFromDC.equals(fli.m_lcm.strGetTestExpression())){
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
            if (fli.m_loopCommandsInCode.get(0) == fli.m_lcm.getLoopCommand()) {
                return DBL_MAX_C_SCORE;
            }
            // 2. interchangeability for and do
            else if (
                    ((fli.m_loopCommandsInCode.get(0) == ELoopCommands.FOR) && (fli.m_lcm.getLoopCommand() == ELoopCommands.WHILE)) ||
                    ((fli.m_loopCommandsInCode.get(0) == ELoopCommands.WHILE) && (fli.m_lcm.getLoopCommand() == ELoopCommands.FOR))
            ) {
                return DBL_MAX_C_SCORE;
            }
            // 3. when a TIL is found, the loop command is irrelevant
            else if (fli.m_lcm.getLoopFinitude()==ELoopFinitude.TIL) {
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
        if (ctx.Goto() != null) {
            var loc = m_lastCodeMarker.getLoopCodeMarkerLocation();
            if (!((loc==ELoopMarkerLocationTypes.BEFORE_GOTO_FURTHER_AFTER) ||
                  (loc==ELoopMarkerLocationTypes.BEFORE_GOTO_BREAK_MULTIPLE))){
                // goto not wanted - reset goto score
                m_beautyMap.get(m_lastCodeMarker.lngGetLoopID()).m_dblGotoScore=0;
            }
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
                m_lastCodeMarker = lcm;
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
                        fli.m_lcm = lcm;
                    }
                    m_fli.put(lngLoopID, fli);
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
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        super.enterIterationStatement(ctx);

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
        }
    }
}

/*
    TODO:

    denk na over control flow test, ook bruikbaar voor unrolled loops...

 */