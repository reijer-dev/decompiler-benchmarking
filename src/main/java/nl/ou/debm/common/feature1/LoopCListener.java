package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.antlr.CBaseListener;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.producer.EFeaturePrefix;

public class LoopCListener extends CBaseListener {

    public long m_lngNLoopsFound = 0;
    public long m_lngNStartMarkersFound = 0;
    public long m_lngNMarkers = 0;


    @Override
    public void enterStatement(CParser.StatementContext ctx) {
        super.enterStatement(ctx);

        var cm = CodeMarker.findInStatement(EFeaturePrefix.CONTROLFLOWFEATURE,ctx.getText());
        if (cm!=null){
            m_lngNMarkers++;
            if (cm.strPropertyValue(ELoopMarkerTypes.STRPROPERTYNAME).equals(ELoopMarkerTypes.BEFORE.strPropertyValue())) {
                m_lngNStartMarkersFound++;
            }
        }
    }

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        super.enterIterationStatement(ctx);

        m_lngNLoopsFound++;
    }
}
