package nl.ou.debm.common.feature1;

public class LoopVariable {
    // loop var details
    public ELoopVarUpdateTypes eUpdateType = ELoopVarUpdateTypes.INCREASE_BY_ONE;
    public ELoopVarTestTypes eTestType = ELoopVarTestTypes.UNUSED;
    public ELoopVarTypes eVarType = ELoopVarTypes.INT;

    public String strInitExpression = "";
    public String strTestExpression = "";
    public String strUpdateExpression = "";

    public LoopVariable(){
        // do nothing :-)
    }

    public LoopVariable(LoopVariable rhs){
        if (rhs!=null) {
            eUpdateType = rhs.eUpdateType;
            eTestType = rhs.eTestType;
            eVarType = rhs.eVarType;
            strInitExpression = rhs.strInitExpression;
            strTestExpression = rhs.strTestExpression;
            strUpdateExpression = rhs.strUpdateExpression;
        }
    }
}
