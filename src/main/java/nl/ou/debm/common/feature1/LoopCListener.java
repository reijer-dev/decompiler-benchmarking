package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.antlr.CBaseListener;
import nl.ou.debm.common.antlr.CParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class LoopCListener extends CBaseListener {

    public long m_lngNLoopsFound = 0;
    public long m_lngNStartMarkersFound = 0;
    public long m_lngNMarkers = 0;

    public final Stack<Long> m_LoopIDStack = new Stack<>();
    public final Map<Long, String> m_loopCommandMap = new HashMap<>();

    @Override
    public void enterStatement(CParser.StatementContext ctx) {
        super.enterStatement(ctx);

        // cast is safe, as the function returns either null when the statement is not a loop code marker
        // or a LoopCodeMarker when found
        LoopCodeMarker cm = (LoopCodeMarker)CodeMarker.findInStatement(EFeaturePrefix.CONTROLFLOWFEATURE,ctx.getText());
        if (cm==null){
            return;
        }

        // LoopCodeMarker found!
        m_lngNMarkers++;        // count all markers


        // is this a before-marker?
        if (cm.getLoopCodeMarkerLocation() == ELoopMarkerLocationTypes.BEFORE){
            // start marker found
            m_lngNStartMarkersFound++;
            m_LoopIDStack.push(cm.lngGetLoopID());
        }
        else if (cm.getLoopCodeMarkerLocation() == ELoopMarkerLocationTypes.BODY){
            // body marker found
            //
            // assert it is the same loop
            if (m_LoopIDStack.peek() != cm.lngGetLoopID()){
                System.out.println("Body marker does not match start marker");
            }
        }
    }

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        super.enterIterationStatement(ctx);

        m_lngNLoopsFound++;

        // loop found, store it
        long lngLoopID = m_LoopIDStack.peek();
        m_loopCommandMap.put(lngLoopID, ctx.getText());
    }
}
