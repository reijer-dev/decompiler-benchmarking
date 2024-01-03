package nl.ou.debm.common.feature1;

import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CParser;

public class LoopCVisitor extends CBaseVisitor<Boolean> {

    @Override
    public Boolean visitStatement(CParser.StatementContext ctx) {
        super.visitStatement(ctx);

        System.out.print(ctx.invokingState   + ": ");
        System.out.println(ctx.getText());


        return true;
    }
}
