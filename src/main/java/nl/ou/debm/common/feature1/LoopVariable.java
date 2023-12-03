package nl.ou.debm.common.feature1;

public class LoopVariable {
    // use loopvar/don't use loopvar
    public boolean bUseLoopVariable = false;        // use variable in loop
    // loop var details
    public ELoopVarUpdateTypes eUpdateType = ELoopVarUpdateTypes.UNUSED;
    public ELoopVarTestTypes eTestType = ELoopVarTestTypes.UNUSED;
    public ELoopVarTypes eVarType = ELoopVarTypes.UNUSED;

    public String strInitExpression = "";
    public String strTestExpression = "";
    public String strUpdateExpression = "";

    public LoopVariable(){
        // do nothing :-)
    }

    public LoopVariable(LoopVariable rhs){
        bUseLoopVariable = rhs.bUseLoopVariable;
        eUpdateType = rhs.eUpdateType;
        eTestType = rhs.eTestType;
        eVarType = rhs.eVarType;
        strInitExpression = rhs.strInitExpression;
        strTestExpression = rhs.strTestExpression;
        strUpdateExpression = rhs.strUpdateExpression;
    }
}
