package nl.ou.debm.common.feature1;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.antlr.CBaseListener;
import nl.ou.debm.common.antlr.CParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    loop body command only found exactly once.

    ad E.
    When no loop var test is present: score
    When loop var test is present: only score when it is in the loop command

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


    public int m_iComCount = 0;

    private static class FoundLoopInfo{
        public String m_strInFunction = "";
        public final List<ELoopCommands> m_loopCommandsInCode = new ArrayList<>();
        public boolean m_bLoopBeforeCodeMarkerDuplicated = false;
        public LoopCodeMarker cm;
    }

    private static class LoopBeautyScore {
        public int m_iScoreFoundInDC = 0;
        public int m_iScoreFoundLoopCommand = 0;
        public int m_iScoreCorrectLoopCommand = 0;
        public int m_iNoLoopDoubling = 0;
        public int m_iEquationScore = 0;
        public int m_iGotoScore = 0;
        public int m_iBodyFlow = 0;
        public double dblGetTotal(){
            int sum = m_iScoreFoundInDC +
                      m_iScoreFoundLoopCommand +
                      m_iScoreCorrectLoopCommand +
                      m_iNoLoopDoubling +
                      m_iEquationScore +
                      m_iGotoScore +
                      m_iBodyFlow;
            return m_iScoreFoundInDC == 0 ? 0 : sum;
        }
    }
    private final Map<Long, LoopBeautyScore> m_beautyMap = new HashMap<>();




    private final List<IAssessor.TestResult> m_testResult = new ArrayList<>();
    private final Map<Long, FoundLoopInfo> m_fli = new HashMap<>(); // info on all loops found
    private Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_llvmInfo;    // info on codemarkers in LLVM
    final private Map<Long, Long> m_LoopIDToStartMarkerCMID = new HashMap<>(); // map loop ID to start code marker
    private List<Long> m_loopIDsUnrolledInLLVM = new ArrayList<>();
    private List<Integer> m_testList = new ArrayList<>();

    private final static long NO_CURRENT_LOOP = -1;

    private String m_strCurrentFunctionName;
    private long m_lngCurrentLoopID = NO_CURRENT_LOOP;

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
        System.out.println("DONE!");
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
            m_beautyMap.put(lcm.lngGetLoopID(), new LoopBeautyScore());
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
        // compile beauty scores
        compileBeautyScores();
        // only copy non-nulls
        List<IAssessor.TestResult> out = new ArrayList<>();
        for (var item : m_testList){
            out.add(m_testResult.get(item));
        }
        return out;
    }

    private void compileBeautyScores(){
        double sum = 0;
        int cnt = 0;
        for (var item : m_beautyMap.entrySet()){
            sum += item.getValue().dblGetTotal();
            cnt ++;
        }
        schoolTest(ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_OVERALL).setScore(sum/cnt);
    }

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        super.enterFunctionDefinition(ctx);

        m_strCurrentFunctionName = "";
        if (ctx.declarator() != null){
            if (ctx.declarator().directDeclarator()!=null){
                m_strCurrentFunctionName = ctx.declarator().directDeclarator().children.get(0).getText();
                m_lngCurrentLoopID = -1;
            }
        }
    }

    @Override
    public void enterJumpStatement(CParser.JumpStatementContext ctx) {
        super.enterJumpStatement(ctx);
//        if (ctx.Goto() != null) {
//            System.out.println(ctx.getText());
//        }
    }

    @Override
    public void enterPrimaryExpression(CParser.PrimaryExpressionContext ctx) {
        super.enterPrimaryExpression(ctx);
        if (!ctx.StringLiteral().isEmpty()){
            LoopCodeMarker cm = (LoopCodeMarker) CodeMarker.findInStatement(EFeaturePrefix.CONTROLFLOWFEATURE, "x("+ctx.StringLiteral().get(0).getText() + ")");
            // this ^^^ is a safe cast. findInStatement either results null (when another type of code marker is found)
            // or a LoopCodeMarker object
            if (cm!=null){
                if (cm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.BEFORE) {
                    m_lngCurrentLoopID = cm.lngGetLoopID();
                    ////////////////////////////////////////////////////////////////////////////
                    // loop ID should not be present yet
                    var fli = m_fli.get(m_lngCurrentLoopID);
                    if (fli!=null) {
                        // loop ID already found -- pre-loop code marker was doubled!
                        fli.m_bLoopBeforeCodeMarkerDuplicated = true;
                    }
                    else {
                        fli = new FoundLoopInfo();
                        fli.cm = cm;
                    }
                    m_fli.put(m_lngCurrentLoopID, fli);
                    //////////////////////////////////////////////////////////////// new stuff
                    //
                    // A-score: loop code marker was found in DC, so it was found, in some form, by the decompiler
                    m_beautyMap.get(m_lngCurrentLoopID).m_iScoreFoundInDC = 1;
                }
                else if (cm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.AFTER) {
                    m_lngCurrentLoopID = NO_CURRENT_LOOP;
                }
            }
        }
    }

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        super.enterIterationStatement(ctx);
        m_iComCount++;
        if (m_lngCurrentLoopID != NO_CURRENT_LOOP){
            // only process iteration statements within a loop
            if (m_loopIDsUnrolledInLLVM.contains(m_lngCurrentLoopID)) {
                // process iteration statement for an unrolled loop

            }
            else {
                //  process iteration statements for a normal loop
                var fli = m_fli.get(m_lngCurrentLoopID);
                assert fli != null;       // safe assumption, as every m_lngCurrentLoopID update also puts a fli-object in the map
                var strLoopCommand = ctx.children.get(0).getText();
                switch (strLoopCommand) {
                    case "for" -> fli.m_loopCommandsInCode.add(ELoopCommands.FOR);
                    case "do" -> fli.m_loopCommandsInCode.add(ELoopCommands.DOWHILE);
                    case "while" -> fli.m_loopCommandsInCode.add(ELoopCommands.WHILE);
                }
                if (fli.m_loopCommandsInCode.size() == 1) {
                    // count loop commands found, but only one per loop marker
                    countTest(ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL).increaseActualValue();
                    // assess correct command
                    //
                    // 1. found command is expected command
                    if (fli.m_loopCommandsInCode.get(0) == fli.cm.getLoopCommand()) {
                        countTest(ETestCategories.FEATURE1_NUMBER_OF_CORRECT_LOOP_COMMANDS).increaseActualValue();
                    }
                    // 2. interchangeability for and do
                    else if (
                            ((fli.m_loopCommandsInCode.get(0) == ELoopCommands.FOR) && (fli.cm.getLoopCommand() == ELoopCommands.WHILE)) ||
                            ((fli.m_loopCommandsInCode.get(0) == ELoopCommands.WHILE) && (fli.cm.getLoopCommand() == ELoopCommands.FOR))
                    ) {
                        countTest(ETestCategories.FEATURE1_NUMBER_OF_CORRECT_LOOP_COMMANDS).increaseActualValue();
                    }
                    // 3. when a TIL is found, the loop command is irrelevant
                    else if (fli.cm.getLoopFinitude()==ELoopFinitude.TIL) {
                        countTest(ETestCategories.FEATURE1_NUMBER_OF_CORRECT_LOOP_COMMANDS).increaseActualValue();
                    }
                }
            }
        }
    }
}
