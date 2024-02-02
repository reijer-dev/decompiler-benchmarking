package nl.ou.debm.common.feature1;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.antlr.CBaseListener;
import nl.ou.debm.common.antlr.CParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoopCListener extends CBaseListener {

    private static class FoundLoopInfo{
        public String m_strInFunction = "";
        public final List<ELoopCommands> m_loopCommandsInCode = new ArrayList<>();
        public boolean m_bLoopBeforeCodeMarkerDuplicated = false;
        public LoopCodeMarker cm;
    }

    private final IAssessor.SingleTestResult m_basicLoopTestResult = new IAssessor.SingleTestResult(ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL);
    private final IAssessor.SingleTestResult m_correctLoopInstructionTestResult = new IAssessor.SingleTestResult(ETestCategories.FEATURE1_NUMBER_OF_CORRECT_LOOP_COMMANDS);
    private final Map<Long, FoundLoopInfo> m_fli = new HashMap<>(); // info on all loops found

    private final static long NO_CURRENT_LOOP = -1;

    private String m_strCurrentFunctionName;
    private long m_lngCurrentLoopID = NO_CURRENT_LOOP;

    public LoopCListener(final CompilerConfig compilerConfig){
        m_basicLoopTestResult.compilerConfig.copyFrom(compilerConfig);
        m_correctLoopInstructionTestResult.compilerConfig.copyFrom(compilerConfig);
    }

    public IAssessor.SingleTestResult getBasicLoopTestResult(){
        return m_basicLoopTestResult;
    }

    public IAssessor.SingleTestResult getCorrectLoopInstructionTestResult() {
        return m_correctLoopInstructionTestResult;
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
                        System.out.print(cm.lngGetLoopID() + "/");
                        if (m_fli.size() % 10==9){
                            System.out.println();
                        }
                    }
                    m_fli.put(m_lngCurrentLoopID, fli);
                    m_basicLoopTestResult.dblHighBound++;
                    m_correctLoopInstructionTestResult.dblHighBound++;
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
        if (m_lngCurrentLoopID != NO_CURRENT_LOOP){
            var fli = m_fli.get(m_lngCurrentLoopID);
            assert fli!=null;       // safe assumption, as every m_lngCurrentLoopID update also puts a fli-object in the map
            var strLoopCommand = ctx.children.get(0).getText();
            if (strLoopCommand.equals("for")){
                fli.m_loopCommandsInCode.add(ELoopCommands.FOR);
            }
            else if (strLoopCommand.equals("do")){
                fli.m_loopCommandsInCode.add(ELoopCommands.DOWHILE);
            }
            else if (strLoopCommand.equals("while")){
                fli.m_loopCommandsInCode.add(ELoopCommands.WHILE);
            }
            if (fli.m_loopCommandsInCode.size()==1){
                // count loop commands found, but only one per loop marker
                m_basicLoopTestResult.dblActualValue++;
                // assess correct command
                //
                // 1. found command is expected command
                if (fli.m_loopCommandsInCode.get(0)==fli.cm.getLoopCommand()){
                    m_correctLoopInstructionTestResult.dblActualValue++;
                }
                // 2. interchangeability for and do
                else if (
                        ((fli.m_loopCommandsInCode.get(0)==ELoopCommands.FOR)   && (fli.cm.getLoopCommand()==ELoopCommands.WHILE)) ||
                        ((fli.m_loopCommandsInCode.get(0)==ELoopCommands.WHILE) && (fli.cm.getLoopCommand()==ELoopCommands.FOR))
                        ) {
                    m_correctLoopInstructionTestResult.dblActualValue++;
                }
                // 3. when a TIL is found, the loop command is irrelevant
                // TODO: CONTINUE HERE!
            }

        }
    }
}
