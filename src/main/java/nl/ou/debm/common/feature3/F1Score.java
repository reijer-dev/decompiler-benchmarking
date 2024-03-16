package nl.ou.debm.common.feature3;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CompilerConfig;

public class F1Score extends IAssessor.TestResult {
    public boolean expected;
    public boolean actual;
    public String name;

    //Used for aggregation
    public int truePositives;
    public int falsePositives;
    public int trueNegatives;
    public int falseNegatives;

    public F1Score(){

    }

    public F1Score(ETestCategories whichTest, CompilerConfig compilerConfig, boolean expected, boolean actual) {
        this.m_compilerConfig.copyFrom(compilerConfig);
        this.m_whichTest = whichTest;
        this.expected = expected;
        this.actual = actual;
        if(expected && actual)
            truePositives++;
        else if(!expected && actual)
            falsePositives++;
        else if(expected)
            falseNegatives++;
        else
            trueNegatives++;
    }

    @Override
    public Double dblGetLowBound() {
        return 0.0;
    }

    @Override
    public Double dblGetActualValue() {
        //Edge case
        if(truePositives == 0 && falseNegatives == 0 && falsePositives == 0)
            return null;
        return (2 * truePositives) / (2d * (truePositives + falsePositives + falseNegatives));
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
        assert rhs instanceof F1Score;
        super.copyAbstractValues(rhs);
        var rhss = (F1Score) rhs;
        actual = rhss.actual;
        expected = rhss.expected;
        name = rhss.name;
        truePositives = rhss.truePositives;
        trueNegatives = rhss.trueNegatives;
        falsePositives = rhss.falsePositives;
        falseNegatives = rhss.falseNegatives;
    }

    @Override
    public IAssessor.TestResult getNewInstance() {
        return new F1Score();
    }

    @Override
    public IAssessor.TestResult makeCopy() {
        var copy = new F1Score();
        copy.copyFrom(this);
        return copy;
    }

    @Override
    public void aggregateValues(IAssessor.TestResult rhs) {
        super.aggregateAbstractValues(rhs);
        var score = (F1Score)rhs;
        truePositives += score.truePositives;
        trueNegatives += score.trueNegatives;
        falsePositives += score.falsePositives;
        falseNegatives += score.falseNegatives;
    }
}
