package nl.ou.debm.common.antlr;

public class MyCListener extends CBaseListener{
/*
    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        super.enterEveryRule(ctx);
        System.out.println("rule entered: " + ctx.getText());
    }
*/

    @Override
    public void enterForDeclaration(CParser.ForDeclarationContext ctx) {
        super.exitForDeclaration(ctx);
        System.out.println("for-declaration entered: " + ctx.getText());
    }

    @Override
    public void enterForCondition(CParser.ForConditionContext ctx) {
        super.enterForCondition(ctx);
        System.out.println("for-condition entered: " + ctx.getText());
    }

    @Override
    public void enterForExpression(CParser.ForExpressionContext ctx) {
        System.out.println("for expression entered: " + ctx.getText());
        super.enterForExpression(ctx);
    }
}
