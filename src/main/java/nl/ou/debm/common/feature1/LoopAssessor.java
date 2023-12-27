package nl.ou.debm.common.feature1;

import nl.ou.debm.common.IAssessor;


public class LoopAssessor implements IAssessor  {

    public LoopAssessor(){

    }

    @Override
    public SingleTestResult GetSingleTestResult(CodeInfo ci) {
        var tr = new SingleTestResult();
        tr.dlbLowBound = 0;
        tr.dblHighBound = 15;
        tr.dblActualValue = 15;
        return tr;
    }
}
