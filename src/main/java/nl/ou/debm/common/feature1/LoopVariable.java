package nl.ou.debm.common.feature1;

public class LoopVariable {
    // use loopvar/don't use loopvar
    public boolean bUseLoopVariable = false;        // use variable in loop
    // loop var details
    public ELoopVarUpdateTypes eUpdateType = ELoopVarUpdateTypes.UNUSED;
    public ELoopVarTestOperators eTestType = ELoopVarTestOperators.UNUSED;
    public ELoopVarTypes eVarType = ELoopVarTypes.UNUSED;

    public LoopVariable(){
        // do nothing :-)
    }

    public LoopVariable(LoopVariable rhs){
        bUseLoopVariable = rhs.bUseLoopVariable;
        eUpdateType = rhs.eUpdateType;
        eTestType = rhs.eTestType;
        eVarType = rhs.eVarType;
    }
}
