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

public class LoopCListener extends CBaseListener {


    public int m_iComCount = 0;

    private static class FoundLoopInfo{
        public String m_strInFunction = "";
        public final List<ELoopCommands> m_loopCommandsInCode = new ArrayList<>();
        public boolean m_bLoopBeforeCodeMarkerDuplicated = false;
        public LoopCodeMarker cm;
    }

    private final List<IAssessor.CountTestResult> m_testResult = new ArrayList<>();
    private final Map<Long, FoundLoopInfo> m_fli = new HashMap<>(); // info on all loops found
    private Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_llvmInfo;    // info on codemarkers in LLVM
    final private Map<Long, Long> m_LoopIDToStartMarkerCMID = new HashMap<>(); // map loop ID to start code marker
    private List<Long> m_loopIDsUnrolledInLLVM = new ArrayList<>();
    private List<Long> m_loopIDsDoubleDoWhileBody = new ArrayList<>();

    private final static long NO_CURRENT_LOOP = -1;

    private String m_strCurrentFunctionName;
    private long m_lngCurrentLoopID = NO_CURRENT_LOOP;

    public LoopCListener(final IAssessor.CodeInfo ci) {
        // add empty test objects
        m_testResult.add(new IAssessor.CountTestResult(ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL));
        m_testResult.add(new IAssessor.CountTestResult(ETestCategories.FEATURE1_NUMBER_OF_CORRECT_LOOP_COMMANDS));
        m_testResult.add(new IAssessor.CountTestResult(ETestCategories.FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP));

        // set configs
        for (var item : m_testResult) {
            item.setCompilerConfig(ci.compilerConfig);
        }

        // process LLVM info
        ProcessLLVM(ci);
    }

    private IAssessor.CountTestResult test(ETestCategories whichTest){
        for (var item : m_testResult){
            if (item.getWhichTest() == whichTest){
                return item;
            }
        }
        throw new RuntimeException("test is not in array");
    }

    private void ProcessLLVM(final IAssessor.CodeInfo ci){
        // get llvm info from file
        m_llvmInfo = CodeMarker.getCodeMarkerInfoFromLLVM(ci.lparser_org);
        // remove all info on non-control-flow-features
        List<Long> removeList = new ArrayList<>();  // list with all ID's to be removed
        for (var item : m_llvmInfo.entrySet()){
            if (!(item.getValue().codeMarker instanceof LoopCodeMarker)){
                removeList.add(item.getKey());
            }
        }
        for (var item : removeList){    // remove all items to be removed
            m_llvmInfo.remove(item);
        }

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
        test(ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL).setHighBound(m_LoopIDToStartMarkerCMID.size());
        test(ETestCategories.FEATURE1_NUMBER_OF_CORRECT_LOOP_COMMANDS).setHighBound(m_LoopIDToStartMarkerCMID.size());
        test(ETestCategories.FEATURE1_NUMBER_OF_UNROLLED_LOOPS_AS_LOOP).setHighBound(m_loopIDsUnrolledInLLVM.size());
    }

    public List<IAssessor.TestResult> getTestResults(){
        return new ArrayList<>(m_testResult);
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
            // this is a safe cast. findInStatement either results null (when another type of code marker is found)
            // or a LoopCodeMarker object
            LoopCodeMarker cm = (LoopCodeMarker) CodeMarker.findInStatement(EFeaturePrefix.CONTROLFLOWFEATURE, "x("+ctx.StringLiteral().get(0).getText() + ")");
            if (cm!=null){
                if (cm.getLoopCodeMarkerLocation()==ELoopMarkerLocationTypes.BEFORE) {
                    m_lngCurrentLoopID = cm.lngGetLoopID();
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
                    test(ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL).increaseActualValue();
                    // assess correct command
                    //
                    // 1. found command is expected command
                    if (fli.m_loopCommandsInCode.get(0) == fli.cm.getLoopCommand()) {
                        test(ETestCategories.FEATURE1_NUMBER_OF_CORRECT_LOOP_COMMANDS).increaseActualValue();
                    }
                    // 2. interchangeability for and do
                    else if (
                            ((fli.m_loopCommandsInCode.get(0) == ELoopCommands.FOR) && (fli.cm.getLoopCommand() == ELoopCommands.WHILE)) ||
                            ((fli.m_loopCommandsInCode.get(0) == ELoopCommands.WHILE) && (fli.cm.getLoopCommand() == ELoopCommands.FOR))
                    ) {
                        test(ETestCategories.FEATURE1_NUMBER_OF_CORRECT_LOOP_COMMANDS).increaseActualValue();
                    }
                    // 3. when a TIL is found, the loop command is irrelevant
                    else if (fli.cm.getLoopFinitude()==ELoopFinitude.TIL) {
                        test(ETestCategories.FEATURE1_NUMBER_OF_CORRECT_LOOP_COMMANDS).increaseActualValue();
                    }
                }
            }
        }
    }
}
