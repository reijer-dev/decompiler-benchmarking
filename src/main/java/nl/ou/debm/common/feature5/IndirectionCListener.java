package nl.ou.debm.common.feature5;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.F15BaseCListener;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;

import java.util.*;

public class IndirectionCListener extends F15BaseCListener {

    private final static int ISWITCHIDNOTIDENTIFIEDYET = -1;
    private final static int ISWITCHIDNOTCONSISTENT = -2;

    private static class SelectionLevelInfo{
        public boolean bSwitchBody = false;
        public long lngSwitchID = ISWITCHIDNOTIDENTIFIEDYET;
        public CParser.ExpressionContext expression = null;
        final public List<FoundCaseInfo> fci_list = new ArrayList<>();
        public FoundCaseInfo current_fci = null;
        @Override
        public String toString(){
            return "SW=" + bSwitchBody + ";SE=" + (expression == null ? "null" : expression.getText())  + ";FCI=" + fci_list;
        }
    }

    private static class FoundCaseInfo{
        /** the case ID for this case*/                         public long lngCaseIDInCode;
        /** the case begin code marker */                       public IndirectionsCodeMarker caseBeginCM = null;
        /** true if the case was a first degree child case */   public boolean bFirstDegreeChild = true;
        /** true if the case is reachable without disturbance*/ public boolean bPathToCaseOk = true;
        @Override
        public String toString(){
            return "CID=" + lngCaseIDInCode + ";POC=" + bPathToCaseOk + ";FDC=" + bFirstDegreeChild + ";CM=" + ((caseBeginCM == null) ? "null" : caseBeginCM.strDebugOutput());
        }
    }

    private static class FoundSwitchInfo{
        /** switch iD */                                        public long lngSwitchID = ISWITCHIDNOTIDENTIFIEDYET;
        /** all cases */                                        public List<FoundCaseInfo> fci = null;
        public String toString(){
            return "FSI:ID=" + lngSwitchID + ";CI=" + fci;
        }
    }


    /** info per selection level */                                 private Stack<SelectionLevelInfo> m_sli = new Stack<>();
    /** info per switch (only our switches), mapped by switch ID */ private final Map<Long, FoundSwitchInfo> m_fsi = new HashMap<>();

    @Override
    public void enterLabeledStatement(CParser.LabeledStatementContext ctx) {
        super.enterLabeledStatement(ctx);

        // only cases and defaults
        if (ctx.Case() != null) {
            ProcessCase(ctx);
        }
        if (ctx.Default() != null) {
            //
        }
    }

    private void ProcessCase(CParser.LabeledStatementContext ctx) {
        // case-statement
        // --------------
        //
        // any current statement object is already added to the list, so we don't have to do that now
        // we can simply create a new one (later this function)

        // case must always be in a switch compound statement...
        assert m_sli.peek().bSwitchBody : "case found outside switch body";

        // determine the value of the expression
        // we expect an integer value, possibly in hex, possibly negative
        String strCaseIndex = ctx.constantExpression().getText();
        var x = new Misc.ConvertCNumeral(strCaseIndex);
        if (!x.bIsInteger()){
            // in theory, symbolic constants or constant calculations could be emitted by the decompiler,
            // but we consider the chance negligible and therefore, we do not try to work with those
            m_sli.peek().current_fci = null;
            return;
        }

        // make new current case object + set case index
        var fci = new FoundCaseInfo();
        fci.lngCaseIDInCode = x.LngGetIntegerLikeValue();

        // now we add it
        m_sli.peek().fci_list.add(fci);

        // and we keep it as current
        m_sli.peek().current_fci = fci;
    }

    @Override
    public void exitCompilationUnit(CParser.CompilationUnitContext ctx) {
        super.exitCompilationUnit(ctx);

        // work is done!

        for (var v : m_fsi.values()){
            System.out.println(v);
        }

    }

    @Override
    public void enterExpressionStatement(CParser.ExpressionStatementContext ctx) {
        super.enterExpressionStatement(ctx);
    }

    @Override
    public void enterCompoundStatement(CParser.CompoundStatementContext ctx) {
        super.enterCompoundStatement(ctx);
    }
    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.enterSelectionStatement(ctx);

        // add new level to stack
        var sli = new SelectionLevelInfo();
        m_sli.push(sli);
        sli.bSwitchBody=(ctx.Switch()!=null);
        sli.expression = ctx.expression();
    }

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        super.enterIterationStatement(ctx);
    }

    @Override
    public void enterJumpStatement(CParser.JumpStatementContext ctx) {
        super.enterJumpStatement(ctx);
    }

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        super.enterFunctionDefinition(ctx);
    }

    @Override
    public void exitCompoundStatement(CParser.CompoundStatementContext ctx) {
        super.exitCompoundStatement(ctx);

    }

    @Override
    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.exitSelectionStatement(ctx);

        // process stack
        var curLev = m_sli.pop();

        // done when no switch body was closed
        if (!curLev.bSwitchBody) {
            return;
        }

        // switch ID
        long lngSwitchID = curLev.lngSwitchID;
        if (lngSwitchID==-1){
            // not one of our switches
            return;
        }

        System.out.println(m_sli.size() + "  " + curLev);
        System.out.println("Switch ID=" + lngSwitchID);

        // should the cases be propagated to a higher switch?
        // --------------------------------------------------
        //
        // why would we want to do that?
        // well, we found patterns like these:
        // switch (a) {                 // <-- A
        //    case 1:
        //    case 2:
        //    default:
        //       if (a==5) {
        //       } else {
        //           switch (a) {       // <-- B
        //              case 3:
        //              case 4:
        //              default:
        //                 switch (b){  // <-- C
        //                    case 6:
        //                    case 7:
        //                 }
        //           }
        //       }
        // }
        // all these cases belong to 1 switch, but they were emitted less readable
        // we can punish the decompiler for readability, but not for not finding cases 3 and 4.
        // - cases 1 and 2 are first degree children of switch A
        // - cases 3 and 4 are first degree children of switch B
        // - cases 6 and 7 are first degree children of switch C, but they do not belong to the A/B family,
        //   as their switch expression is different
        // - cases 3 and 4 may only be propagated when
        //      - the previous switch had the same expression
        //      - the cases in the previous switch have the same switch ID
        //
        // so, we propagate cases 3 and 4 to the first switch and consider them one big switch
        boolean bMerged = false;
        boolean bPathOK = true;
        for (int lev = m_sli.size()-1 ; lev >= 0; --lev){
            var highLev = m_sli.get(lev);
            if (highLev.bSwitchBody){
                // parent switch found
                if (highLev.lngSwitchID == lngSwitchID){
                    // higher switch found with the same ID, so add these cases to the other's,
                    // but remove FirstChild flag
                    for (var ci : curLev.fci_list){
                        ci.bFirstDegreeChild=false;
                    }
                    // check switch expressions
                    if (!bExpressionsAreAlike(curLev.expression, highLev.expression)){
                        bPathOK = false;
                    }
                    // process path status
                    if (!bPathOK){
                        for (var ci : curLev.fci_list){
                            ci.bPathToCaseOk = false;
                        }
                    }
                    highLev.fci_list.addAll(curLev.fci_list);
                    bMerged = true;
                    // no further propagating
                    // a grandparent of the just closed level, is a parent of the found higher level, so no need
                    // to look further now
                    break;
                }
                else {
                    // continue looking for a parent, but mark path as interrupted by other switch (or if)
                    bPathOK = false;
                }
            }
            else{
                // if found
                // check if path is ok, by comparing the switch expression to the if expression
                if (!bExpressionsAreAlike(curLev.expression, highLev.expression)){
                    bPathOK = false;
                }
            }
        }

        // if there was a merge, we do nothing, because we process the parent switch
        // no merge? Than this was the highest switch!
        if (!bMerged){
            // make new switch info object
            var fsi = new FoundSwitchInfo();
            fsi.lngSwitchID=lngSwitchID;
            fsi.fci = curLev.fci_list;

            // put it in the map
            m_fsi.put(lngSwitchID, fsi);




            System.out.print(">>>>>>>>>>>>>>>");
        }


        System.out.println("-----------------------------------------");

    }

    /**
     * Compare two expressions. The expressions may be switch expressions or if expressions.
     * They are deemed alike when: <br>
     * - there is at least one common identifier
     * @param e1 expression1
     * @param e2 expression2
     * @return true if criteria are met
     */
    private boolean bExpressionsAreAlike(CParser.ExpressionContext e1, CParser.ExpressionContext e2){
        var nodes1 = Misc.getAllTerminalNodes(e1);
        var nodes2 = Misc.getAllTerminalNodes(e2);

        for (var n1 : nodes1){
            for (var n2: nodes2){
                if ((n1.iTokenID== CLexer.Identifier) &&
                        (n2.iTokenID == CLexer.Identifier) &&
                        (n1.strText.equals(n2.strText))){
                    return true;
                }
            }
        }
        return false;
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
    public void processWantedCodeMarker(CodeMarker cm) {
        if (cm instanceof IndirectionsCodeMarker icm) {
            System.out.println(icm);
            if (!m_sli.isEmpty()) {
                var cur_sli = m_sli.peek();
                var cur_fci = cur_sli.current_fci;
                if (cur_fci != null) {
                    // we are in a case...
                    if (icm.getCodeMarkerLocation() == EIndirectionMarkerLocationTypes.CASEBEGIN) {
                        // store first begin code marker
                        cur_fci.caseBeginCM = icm;
                        // process switch ID
                        if (cur_sli.lngSwitchID == ISWITCHIDNOTIDENTIFIEDYET){
                            cur_sli.lngSwitchID = icm.lngGetSwitchID();
                        }
                        else if (cur_sli.lngSwitchID != ISWITCHIDNOTCONSISTENT) {
                            if (cur_sli.lngSwitchID != icm.lngGetSwitchID()){
                                cur_sli.lngSwitchID = ISWITCHIDNOTCONSISTENT;
                            }
                        }
                    }
                }
            }
        }
    }
}
