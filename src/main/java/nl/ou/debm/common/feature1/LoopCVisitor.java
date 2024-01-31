package nl.ou.debm.common.feature1;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CParser;

public class LoopCVisitor extends CBaseVisitor<Boolean> {

    private final IAssessor.SingleTestResult m_basicLoopTestResult = new IAssessor.SingleTestResult();

    public LoopCVisitor(final CompilerConfig compilerConfig){
        m_basicLoopTestResult.whichTest = ETestCategories.FEATURE1_NUMBER_OF_LOOPS_GENERAL;
        m_basicLoopTestResult.compilerConfig.copyFrom(compilerConfig);
    }



    public IAssessor.SingleTestResult getBasicLoopTestResult(){
        return m_basicLoopTestResult;
    }

    @Override
    public Boolean visitPrimaryExpression(CParser.PrimaryExpressionContext ctx) {
        var b = super.visitPrimaryExpression(ctx);

        if (ctx.StringLiteral().size() == 1) {
            System.out.println(ctx.StringLiteral().size() + "--" + ctx.getText());
        }

        return b;
    }

    @Override
    public Boolean visitExpressionStatement(CParser.ExpressionStatementContext ctx) {
        var b = super.visitExpressionStatement(ctx);

//        if (ctx.getText().startsWith("printf")) {
//            System.out.print(ctx.invokingState + ": ");
//            System.out.println(ctx.getText());
//        }


        return b;
    }

    @Override
    public Boolean visitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        var b= super.visitFunctionDefinition(ctx);

        System.out.println(ctx.getText());

        return b;
    }
}
