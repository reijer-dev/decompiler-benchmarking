package nl.ou.debm.common.feature1;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.antlr.CBaseListener;
import nl.ou.debm.common.antlr.CParser;

public class LoopCListener extends CBaseListener {

    private final IAssessor.SingleTestResult m_basicLoopTestResult = new IAssessor.SingleTestResult();

    private String m_strCurrentFunctionName;

    public LoopCListener(final CompilerConfig compilerConfig){
        m_basicLoopTestResult.whichTest = ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL;
        m_basicLoopTestResult.compilerConfig.copyFrom(compilerConfig);
    }

    public IAssessor.SingleTestResult getBasicLoopTestResult(){
        return m_basicLoopTestResult;
    }

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        super.enterFunctionDefinition(ctx);

        m_strCurrentFunctionName = "";
        if (ctx.declarator() != null){
            if (ctx.declarator().directDeclarator()!=null){
                m_strCurrentFunctionName = ctx.declarator().directDeclarator().children.get(0).getText();
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
                System.out.println(cm);
            }
        }
    }
}
