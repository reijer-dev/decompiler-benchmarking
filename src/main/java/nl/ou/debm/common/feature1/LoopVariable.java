package nl.ou.debm.common.feature1;

public class LoopVariable {
    // use loopvar/don't use loopvar
    public boolean bUseLoopVariable = false;        // use variable in loop
    // loop var details
    public ELoopVarUpdateTypes eUpdateType = ELoopVarUpdateTypes.INCREASE_BY_ONE;
    public EloopVarTestOperator eTestType = EloopVarTestOperator.SMALLER_THAN;

    public LoopVariable(){
        // do nothing :-)
    }

    public LoopVariable(LoopVariable rhs){
        bUseLoopVariable = rhs.bUseLoopVariable;
        eUpdateType = rhs.eUpdateType;
        eTestType = rhs.eTestType;
    }
}
