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

    private final Stack<Long> m_loopIDStack = new Stack<>();
    public final Map<Long, String> m_loopCommandMap = new HashMap<>();

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        super.enterFunctionDefinition(ctx);

        // empty stack?
        if (!m_loopIDStack.isEmpty()){
            System.out.println("Stack not empty");
        }

        // reset loopID-stack
        m_loopIDStack.clear();
    }

    @Override
    public void enterExpressionStatement(CParser.ExpressionStatementContext ctx) {
        //
        // I'm using enterExpressionStatement to filter all the code markers, as they are basically expression
        // (function calls returning voids). This makes sure that parent statement constructs (such as
        // compound statement or labeled statement) will be left out and no marker will be processed more than once
        //

        // superclass work
        super.enterExpressionStatement(ctx);

        // test for a code marker of my feature
        // the cast is safe, as the function returns either:
        // - null when the statement is not a loop code marker (not a marker or not a /loop/ code marker)
        // - a LoopCodeMarker when found
        LoopCodeMarker cm = (LoopCodeMarker) CodeMarker.findInStatement(EFeaturePrefix.CONTROLFLOWFEATURE,ctx.getText());
        if (cm==null){
            return;
        }

        // process marker
        switch (cm.getLoopCodeMarkerLocation()){
            case BEFORE -> { processBeforeMarker(cm);      }
            case BODY ->   { processStartOfBodyMarker(cm); }
            case AFTER ->  { processAfterLoopMarker(cm);   }
        }
    }

    private void processBeforeMarker(LoopCodeMarker cm) {
        // put loopID on stack as being current loop to process
        m_loopIDStack.push(cm.lngGetLoopID());
    }

    private void processStartOfBodyMarker(LoopCodeMarker cm) {
        // check that loop body marker ID equals current loop to be processed
        if (m_loopIDStack.peek()!=cm.lngGetLoopID()){
            System.out.println("Body ID does not match current start ID");
        }
    }

    private void processAfterLoopMarker(LoopCodeMarker cm) {
        // check that loop body marker ID equals current loop to be processed
        if (m_loopIDStack.peek()!=cm.lngGetLoopID()){
            System.out.println("End ID does not match current start ID");
        }
        m_loopIDStack.pop();
    }

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        super.enterIterationStatement(ctx);

        m_lngNLoopsFound++;

        // loop found, store it
        long lngLoopID = m_loopIDStack.peek();
        if (m_loopCommandMap.containsKey(lngLoopID)){
            System.out.println("Overwrite: " + m_loopCommandMap.get(lngLoopID));
            System.out.println("     with: " + ctx.getText());
        }
        //m_loopCommandMap.put(lngLoopID, ctx.getStart().getText());
        m_loopCommandMap.put(lngLoopID, ctx.getText());
    }
}
