package nl.ou.debm.common.feature3;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CompilerConfig;

public class RecallScore extends IAssessor.TestResult {
    public boolean expected;
    public boolean actual;
    public String name;

    //Used for aggregation
    public int relevantInstances;
    public int foundInstances;

    public RecallScore(){

    }

    public RecallScore(ETestCategories whichTest, CompilerConfig compilerConfig, boolean found) {
        this.m_compilerConfig.copyFrom(compilerConfig);
        this.m_whichTest = whichTest;
            relevantInstances++;
        if(found)
            foundInstances++;
    }

    @Override
    public Double dblGetLowBound() {
        return 0.0;
    }

    @Override
    public Double dblGetActualValue() {
        //Edge case
        if(relevantInstances == 0)
            return 0.0;
        return foundInstances / (double)relevantInstances;
    }

    @Override
    public Double dblGetHighBound() {
        return 1.0;
    }

    @Override
    public Double dblGetTarget() {
        return 1.0;
    }

    @Override
    public int iGetNumberOfDecimalsToBePrinted() {
        return 3;
    }

    @Override
    public void copyFrom(IAssessor.TestResult rhs) {
        super.copyAbstractValues(rhs);
        assert rhs instanceof RecallScore;
        var rhss = (RecallScore) rhs;
        actual = rhss.actual;
        expected = rhss.expected;
        name = rhss.name;
        relevantInstances = rhss.relevantInstances;
        foundInstances = rhss.foundInstances;
    }

    @Override
    public IAssessor.TestResult getNewInstance() {
        return new RecallScore();
    }

    @Override
    public IAssessor.TestResult makeCopy() {
        var copy = new RecallScore();
        copy.copyFrom(this);
        return copy;
    }

    @Override
    public void aggregateValues(IAssessor.TestResult rhs) {
        super.aggregateAbstractValues(rhs);
        var score = (RecallScore)rhs;
        relevantInstances += score.relevantInstances;
        foundInstances += score.foundInstances;
    }
}
