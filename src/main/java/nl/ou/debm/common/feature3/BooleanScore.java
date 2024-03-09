package nl.ou.debm.common.feature3;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CompilerConfig;

public class BooleanScore extends IAssessor.TestResult {
    public boolean expected;
    public boolean actual;
    public String name;

    //Used for aggregation
    public int truePositives;
    public int falsePositives;
    public int trueNegatives;
    public int falseNegatives;

    public BooleanScore(){

    }

    public BooleanScore(ETestCategories whichTest, CompilerConfig compilerConfig, boolean expected, boolean actual) {
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
        var precision = truePositives / (double)(truePositives + falsePositives);
        var recall = truePositives / (double)(truePositives + falseNegatives);
        return 2 * (precision * recall) / (precision + recall);
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
    public IAssessor.TestResult getNewInstance() {
        return new BooleanScore();
    }

    @Override
    public IAssessor.TestResult makeCopy() {
        var copy = new BooleanScore();
        copy.actual = actual;
        copy.expected = expected;
        copy.name = name;
        copy.m_whichTest = m_whichTest;
        copy.truePositives = truePositives;
        copy.trueNegatives = trueNegatives;
        copy.falsePositives = falsePositives;
        copy.falseNegatives = falseNegatives;
        copy.m_compilerConfig.copyFrom(m_compilerConfig);
        return copy;
    }

    @Override
    public void aggregateValues(IAssessor.TestResult rhs) {
        var score = (BooleanScore)rhs;
        truePositives += score.truePositives;
        trueNegatives += score.trueNegatives;
        falsePositives += score.falsePositives;
        falseNegatives += score.falseNegatives;
    }
}
