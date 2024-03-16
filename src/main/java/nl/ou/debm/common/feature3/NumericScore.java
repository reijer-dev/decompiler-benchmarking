package nl.ou.debm.common.feature3;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CompilerConfig;

public class NumericScore extends IAssessor.TestResult {
    public Double actual;
    public Double lowBound;
    public Double highBound;
    public String name;

    public NumericScore(){}

    public NumericScore(ETestCategories whichTest, CompilerConfig compilerConfig, Double lowBound, Double highBound, Double actual) {
        this.m_compilerConfig.copyFrom(compilerConfig);
        this.m_whichTest = whichTest;
        this.lowBound = lowBound;
        this.highBound = highBound;
        this.actual = actual;
    }

    public NumericScore(ETestCategories whichTest, CompilerConfig compilerConfig, int lowBound, int highBound, int actual) {
        this.m_compilerConfig.copyFrom(compilerConfig);
        this.m_whichTest = whichTest;
        this.lowBound = (double)lowBound;
        this.highBound = (double)highBound;
        this.actual = (double)actual;
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
    public void copyFrom(IAssessor.TestResult rhs) {
        super.copyAbstractValues(rhs);
        assert rhs instanceof NumericScore;
        var rhss = (NumericScore) rhs;
        actual = rhss.actual;
        highBound = rhss.highBound;
        lowBound = rhss.lowBound;
        name = rhss.name;
    }

    @Override
    public IAssessor.TestResult getNewInstance() {
        return new NumericScore();
    }

    @Override
    public IAssessor.TestResult makeCopy() {
        var copy = new NumericScore();
        copy.copyFrom(this);
        return copy;
    }

    @Override
    public void aggregateValues(IAssessor.TestResult rhs) {
        super.aggregateAbstractValues(rhs);
        var score = (NumericScore)rhs;
        highBound += score.highBound;
        lowBound += score.lowBound;
        actual += score.actual;
    }
}

