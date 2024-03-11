package nl.ou.debm.common.feature3;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CompilerConfig;

public class NumericScore extends IAssessor.TestResult {
    public int actual;
    public int lowBound;
    public int highBound;
    public String name;

    public NumericScore(){}

    public NumericScore(ETestCategories whichTest, CompilerConfig compilerConfig, int lowBound, int highBound, int actual) {
        this.m_compilerConfig.copyFrom(compilerConfig);
        this.m_whichTest = whichTest;
        this.lowBound = lowBound;
        this.highBound = highBound;
        this.actual = actual;
    }

    @Override
    public Double dblGetLowBound() {
        return (double)lowBound;
    }

    @Override
    public Double dblGetActualValue() {
        return (double)actual;
    }

    @Override
    public Double dblGetHighBound() {
        return (double)highBound;
    }

    @Override
    public Double dblGetTarget() {
        return (double)highBound;
    }

    @Override
    public int iGetNumberOfDecimalsToBePrinted() {
        return 1;
    }

    @Override
    public IAssessor.TestResult getNewInstance() {
        return new NumericScore();
    }

    @Override
    public IAssessor.TestResult makeCopy() {
        var copy = new NumericScore();
        copy.actual = actual;
        copy.highBound = highBound;
        copy.lowBound = lowBound;
        copy.name = name;
        copy.m_whichTest = m_whichTest;
        copy.m_compilerConfig.copyFrom(m_compilerConfig);
        return copy;
    }

    @Override
    public void aggregateValues(IAssessor.TestResult rhs) {
        var score = (NumericScore)rhs;
        highBound += score.highBound;
        lowBound += score.lowBound;
        actual += score.actual;
    }
}

