package nl.ou.debm.common;

import nl.ou.debm.common.antlr.CBaseListener;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class F15BaseCListener extends CBaseListener {

    /** level counter for postfix expressions */                                                private int m_iPostFixExpressionLevel = 0;
    /** what code marker to look for */                                                         protected EFeaturePrefix m_CodeMarkerTypeToLookFor = null;
    /** current nesting level of compound statements, function compound statement = level 0*/   protected int m_iCurrentCompoundStatementNestingLevel = 0;
    /** are we currently within a declaration?*/                                                private int m_iInDeclarationCount = 0;


    private enum EIfBranches{
        NOIF, TRUEBRANCH, ELSEBRANCH
    }
    private static class AssignmentInfo{
        public String strVarName = "";
        public String strVarValue = "";
        public int iIfLevel = 0;
        public F15BaseCListener.EIfBranches eIfBranch;

        public AssignmentInfo(){}
        public AssignmentInfo(String strVarName, String strVarValue, int iIfLevel, F15BaseCListener.EIfBranches eIfBranch){
            this.strVarName=strVarName;
            this.strVarValue=strVarValue;
            this.iIfLevel =iIfLevel;
            this.eIfBranch = eIfBranch;
        }
        public String toString(){
            return strVarName + " = \"..." + Misc.strSafeRightString(strVarValue,5) + " (" + iIfLevel + ", " + eIfBranch + ")";
        }
    }

    /** map variable name to variable info*/            private Map<String, AssignmentInfo> m_CMAssignmentsMap = new HashMap<>();
    private int m_iCurrentConditionalLevel = 0;


    @Override
    public void enterPostfixExpression(CParser.PostfixExpressionContext ctx) {
        super.enterPostfixExpression(ctx);

        // a code marker has the form: call("....")
        // this is a postfix expression.
        // unfortunately, the "..." is also a postfix expression
        // if we do nothing, the code marker found when we find call("....") will be
        // overwritten by the next enterPostFixExpression, because we don't recognize
        // code markers that are only string literals.
        //
        // to prevent this, we keep track of a postfix level. Normally, it is set to 0 -- we check for
        // code markers. When found, it is increased. Every next call to enterPostFixExpression
        // will increase the level **BUT NOT TEST**. This basically means we ignore all other postfix
        // expressions until we're really done with this one.
        //
        // the level is always lowered when the exit-routine is called (see below)

        if (m_iPostFixExpressionLevel==0) {
            resetCodeMarkerBuffersOnEnterPostfixExpression();

            // is this a loop code marker?
            var nodes = Misc.getAllTerminalNodes(ctx, true);
            // try to substitute vars for code markers
            //
            // we have seen patterns like this:
            // ..  v10 = "<code marker>";
            // ..  while (1) {
            // ..      printf(v10);
            // ..  }
            //
            // The printf(v10) is actually a code marker, but it is not easy to recognize
            // we therefor keep a table of variable assignments. This table stores information on the variable
            // identifier, the value assigned, the nesting level of if-statements and the last if-statement's
            // branch (true-branch or false/else-branch). If we have a token list with at least 4 elements, it
            // may a code marker. We check the third element for being an identifier, if it is, we try to
            // substitute it with the previously assigned code marker value.
            //
            // a few rules:
            // - we only store code markers in the map
            // - as soon as we exit an if, we throw out all code markers added during that if,
            //   because we cannot be sure of a variable's value if it was assigned in an if preceding
            //   the printf() call
            // - we check that a variable is set in the same if-branch, so we do not use an assignment
            //   found in the true-branch in the false-branch

            if (nodes.size()>=4){
                for (int tokenPtr=2; tokenPtr<nodes.size(); tokenPtr++) {
                    var item = nodes.get(tokenPtr);
                    if (item.iTokenID == CLexer.Identifier) {
                        var data = m_CMAssignmentsMap.get(item.strText);
                        if (data != null) {
                            F15BaseCListener.EIfBranches tf = inTrueOrElseBranch(ctx);
                            if (bBranchesCheckoutOK(tf, data)) {
                                item.strText = data.strVarValue;
                                item.iTokenID = CLexer.StringLiteral;
                            }
                        }
                    }
                }
            }
            CodeMarker cm = CodeMarker.findInListOfTerminalNodes(nodes, m_CodeMarkerTypeToLookFor);
            if (cm != null) {
                processCodeMarker(cm);
                m_iPostFixExpressionLevel++;
            }
        }
        else{
            m_iPostFixExpressionLevel++;
        }
    }

    @Override
    public void exitPostfixExpression(CParser.PostfixExpressionContext ctx) {
        super.exitPostfixExpression(ctx);
        // lower the postfix expression level
        if (m_iPostFixExpressionLevel>0){
            --m_iPostFixExpressionLevel;
        }
    }

    /**
     * determine whether current context is in an if-statement and if so, whether it is the true
     * or false/else-branch
     * @param ctx context to be searched
     * @return the if-branch (true or else/false) when in an if statement, otherwise NOIF
     */
    private EIfBranches inTrueOrElseBranch(ParserRuleContext ctx){
        if (m_iCurrentConditionalLevel==0){
            return EIfBranches.NOIF;
        }
        ParserRuleContext ifCtx= ctx;
        ParserRuleContext statCtx = null;
        // find the if-statement by moving up **and** keep track of the statement traversed
        // before the if-statement
        do {
            statCtx = ifCtx;
            ifCtx = ifCtx.getParent();
        } while (! (ifCtx instanceof CParser.SelectionStatementContext));
        // we have the if-statement in ifCtx and the statement immediately 'below' the if-statement in
        // statCtx. We can now check if the last statement before bubbling to the if-statement is
        // from the true branch (always available, that's why we test that one) or otherwise, the
        // false branch
        if (((CParser.SelectionStatementContext) ifCtx).statement().get(0).equals(statCtx)){
            return EIfBranches.TRUEBRANCH;
        }
        return EIfBranches.ELSEBRANCH;
    }

    /**
     * check printf-location to variable assignment location
     */
    private boolean bBranchesCheckoutOK(EIfBranches tf, AssignmentInfo data) {
        if (tf == EIfBranches.NOIF) {
            // we are not in an if-statement, so any assignment is ok (because
            // all assignments left in the table must also be non-nested assignments, so
            // we're good
            return true;
        }
        else if (m_iCurrentConditionalLevel > data.iIfLevel) {
            // the current if-nesting level is higher than the if-nesting level of the assignment
            // this is ok, it's something like this:
            // .. v10 = "..."
            // .. if () {
            //         printf(v10);
            // .. }
            return true;
        }
        // the current nesting level must be equal to the if-nesting level of the assignment.
        // it cannot be lower, as exiting if's will throw out higher nested assignments from the hashmap.
        // when equal, we must make sure that they are in the same branch.
        //
        // .. if () {
        // ..    v10 = "...";
        // ..    printf(v10); // this is ok, it is the same branch
        // .. }
        // .. else {
        // ..    printf(v10); // this is not ok, as v10 is not set in the else-branch
        // .. }
        return (data.eIfBranch == tf);
    }

    @Override
    public void enterAssignmentExpression(CParser.AssignmentExpressionContext ctx) {
        super.enterAssignmentExpression(ctx);

        // keep track of the variable assignments

        if (ctx.assignmentOperator()!=null) {
            // get the assigned value
            var exp = Misc.getAllTerminalNodes(ctx.assignmentExpression(), true);
            // only continue on single argument (strings are automatically concatenated)
            if (exp.size()==1){
                // only continue on string
                if (exp.get(0).iTokenID == CLexer.StringLiteral){
                    // only continue on code marker
                    CodeMarker cm = CodeMarker.MatchCodeMarkerStringLiteral(exp.get(0).strText, m_CodeMarkerTypeToLookFor);
                    if (cm!=null) {
                        // determine true or false branch
                        EIfBranches tf = inTrueOrElseBranch(ctx);

                        // store assignment
                        String strVarName = ctx.getChild(0).getText();
                        m_CMAssignmentsMap.put(strVarName, new AssignmentInfo(strVarName, exp.get(0).strText, m_iCurrentConditionalLevel, tf));
                    }
                }
            }
        }
    }

    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.enterSelectionStatement(ctx);

        // keep track of the depth of nested if's
        m_iCurrentConditionalLevel++;
    }

    @Override
    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.exitSelectionStatement(ctx);

        // delete all the assignments from the last if
        //
        // .. v1 = ...;             // add v1
        // .. if () {
        // ..    v2 = ...           // add v2
        // ..    v3 = ...           // add v3
        // ..    if () {
        // ..       v3 = ...        // overwrite v3
        // ..       v4 = ...        // add v4
        // ..    }                  // upon if-exit: drop v3 and v4
        // ..    printf(v3);        // no code marker (v3 dropped, we don't know its value)
        // ..    printf(v4);        // no code marker (likewise)
        // ..    printf(v2);        // code marker: v2 was set in the same if and not updated in the second if
        // ..    printf(v1);        // code marker: v1 was set before the first if and not updated
        // .. }
        // .. printf(v1)            // code marker: v1 was not updated by the if-constructs
        List<String> keysToDelete = new ArrayList<>(m_CMAssignmentsMap.size()+2);
        for (var item : m_CMAssignmentsMap.values()){
            if (item.iIfLevel == m_iCurrentConditionalLevel) {
                keysToDelete.add(item.strVarName);
            }
        }
        for (var item : keysToDelete){
            m_CMAssignmentsMap.remove(item);
        }

        m_iCurrentConditionalLevel--;
    }

    @Override
    public void enterCompoundStatement(CParser.CompoundStatementContext ctx) {
        super.enterCompoundStatement(ctx);

        // if the walker is looking for a first element, we don't have to stop now
        // compound statements may be nested, as long as the first non-compound-statement
        // is a body marker, it's ok
        // so... we do nothing

        // we don't have anything to do in our search for a code marker immediately
        // preceding a goto, as this would be perfectly fine: <code marker> { goto _LAB }

        // keep track of current compound statement nesting level
        m_iCurrentCompoundStatementNestingLevel++;
    }

    @Override
    public void exitCompoundStatement(CParser.CompoundStatementContext ctx) {
        super.exitCompoundStatement(ctx);

        // keep track of current compound statement nesting level
        m_iCurrentCompoundStatementNestingLevel--;
        assert m_iCurrentCompoundStatementNestingLevel >= 0 : "negative compound statement nesting level";
    }

    @Override
    public void enterDeclaration(CParser.DeclarationContext ctx) {
        super.enterDeclaration(ctx);
        //
        // a declaration is only comparable to a statement if it contains an initializer
        // we have to check this in the enterInitDeclarator function
        //
        m_iInDeclarationCount++;
    }

    @Override
    public void exitDeclaration(CParser.DeclarationContext ctx) {
        super.exitDeclaration(ctx);
        m_iInDeclarationCount--;
    }

    @Override
    public void enterInitDeclarator(CParser.InitDeclaratorContext ctx) {
        super.enterInitDeclarator(ctx);
        if (m_iInDeclarationCount>0){
            if (ctx.getChildCount()>1){
                // more than one child means some kind of init expression, which really is an expression,
                // so reset the just-before-variables
                resetCodeMarkerBuffersOnEnterInitDeclarator();
            }
        }
    }


    protected F15BaseCListener(){
        super();
        setCodeMarkerFeature();
    }

    public abstract void setCodeMarkerFeature();
    public abstract void resetCodeMarkerBuffersOnEnterPostfixExpression();
    public abstract void resetCodeMarkerBuffersOnEnterInitDeclarator();
    public abstract void processCodeMarker(CodeMarker cm);

}
