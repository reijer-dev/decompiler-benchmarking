package nl.ou.debm.common.feature3;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.EArchitecture;

public class NumericScore extends IAssessor.TestResult {
    public int actual;
    public int lowBound;
    public int highBound;
    public String name;
    public EArchitecture architecture;

    public NumericScore(){}

    public NumericScore(ETestCategories whichTest, EArchitecture architecture, int lowBound, int highBound, int actual) {
        this.m_whichTest = whichTest;
        this.architecture = architecture;
        this.lowBound = lowBound;
        this.highBound = highBound;
        this.actual = actual;
    }

    @Override
    public double dblGetLowBound() {
        return lowBound;
    }

    @Override
    public double dblGetActualValue() {
        return actual;
    }

    @Override
    public double dblGetHighBound() {
        return highBound;
    }

    @Override
    public double dblGetTarget() {
        return highBound;
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

