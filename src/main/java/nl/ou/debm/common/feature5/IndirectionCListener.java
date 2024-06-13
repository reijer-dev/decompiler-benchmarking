package nl.ou.debm.common.feature5;

import nl.ou.debm.common.antlr.CBaseListener;
import nl.ou.debm.common.antlr.CParser;

public class IndirectionCListener extends CBaseListener {

    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.enterSelectionStatement(ctx);

        if (ctx.Switch()!=null){
            System.out.println(ctx.Switch().getText());
            System.out.println(ctx.expression().getText());
        }
    }
}
