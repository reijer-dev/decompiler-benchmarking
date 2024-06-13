package nl.ou.debm.common.feature5;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.F15BaseCListener;
import nl.ou.debm.common.antlr.CParser;

public class IndirectionCListener extends F15BaseCListener {


    /** compound statement level */                             int m_iCurrentCompoundLevel = 0;

    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.enterSelectionStatement(ctx);

        if (ctx.Switch()!=null){
            System.out.println(m_iCurrentCompoundLevel + " --- " + ctx.Switch().getText() + " (" + ctx.expression().getText() + ")");
        }
    }

    @Override
    public void exitCompoundStatement(CParser.CompoundStatementContext ctx) {
        super.exitCompoundStatement(ctx);
        m_iCurrentCompoundLevel--;
    }

    @Override
    public void setCodeMarkerFeature() {
        m_CodeMarkerTypeToLookFor = EFeaturePrefix.INDIRECTIONSFEATURE;
    }

    @Override
    public void resetCodeMarkerBuffersOnEnterPostfixExpression() {

    }

    @Override
    public void resetCodeMarkerBuffersOnEnterInitDeclarator() {

    }

    @Override
    public void processCodeMarker(CodeMarker cm) {
        System.out.println(cm);
    }

    @Override
    public void enterCompoundStatement(CParser.CompoundStatementContext ctx) {
        super.enterCompoundStatement(ctx);
        m_iCurrentCompoundLevel++;
    }

    @Override
    public void enterLabeledStatement(CParser.LabeledStatementContext ctx) {
        super.enterLabeledStatement(ctx);
        if (ctx.Case()!=null){
            System.out.println("  " + m_iCurrentCompoundLevel + ":" + ctx.Case().getText() + " " + ctx.constantExpression().getText());
        }
    }
}
