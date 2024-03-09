package nl.ou.debm.common.feature1;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.ECompiler;
import nl.ou.debm.common.EOptimize;

/**
 * Class to implement test scores based on school scores: 0=minimum, 10=maximum.
 * Any aggregating is done by a weighted average. So, if an average of 8 (for 8 tests) is
 * aggregated to an average of 5 (for 2 tests), the result will be 7.4 and not 6.5.
 *
 */
public class SchoolTestResult extends IAssessor.TestResult {
    private double m_dblCumulativeScore = 0;
    private double m_dblMyScore = 0;

    @Override
    public Double dblGetLowBound() {
        return 0.0;
    }

    @Override
    public Double dblGetActualValue() {
        return m_dblCumulativeScore / m_iNTests;
    }

    @Override
    public Double dblGetHighBound() {
        return 10.0;
    }

    @Override
    public Double dblGetTarget() {
        return 10.0;
    }

    @Override
    public int iGetNumberOfDecimalsToBePrinted() {
        return 1;
    }

    @Override
    public IAssessor.TestResult getNewInstance() {
        return new SchoolTestResult();
    }

    @Override
    public IAssessor.TestResult makeCopy() {
        return new SchoolTestResult(this);
    }

    @Override
    public void aggregateValues(IAssessor.TestResult rhs) {
        assert rhs instanceof SchoolTestResult : "Trying to aggregate non-SchoolTestResult to a SchoolTestResult";
        super.aggregateAbstractValues(rhs);
        m_dblCumulativeScore += ((SchoolTestResult) rhs).m_dblCumulativeScore;  // cast is safe because of the assertion
    }

    public void setScore(double dblSchoolScore){
        assert 0 <= dblSchoolScore : "No school sores below zero allowed";
        assert dblSchoolScore <=10 : "No school score above ten allowed";
        m_dblCumulativeScore -= m_dblMyScore;
        m_dblMyScore = dblSchoolScore;
        m_dblCumulativeScore += m_dblMyScore;
    }

    public SchoolTestResult(){}

    public SchoolTestResult(SchoolTestResult rhs){
        copyFrom(rhs);
    }

    public SchoolTestResult(ETestCategories whichTest){
        m_whichTest = whichTest;
    }
    public SchoolTestResult(double dblSchoolScore) {
        setScore(dblSchoolScore);
    }

    public SchoolTestResult(ETestCategories whichTest, CompilerConfig compilerConfig) {
        m_whichTest = whichTest;
        m_compilerConfig.copyFrom(compilerConfig);
    }

    public SchoolTestResult(ETestCategories whichTest, CompilerConfig compilerConfig, boolean bSkipped) {
        m_whichTest = whichTest;
        m_compilerConfig.copyFrom(compilerConfig);
        m_bTestSkipped=bSkipped;
    }

    public SchoolTestResult(ETestCategories whichTest, CompilerConfig compilerConfig,
                            double dblSchoolScore) {
        m_whichTest = whichTest;
        m_compilerConfig.copyFrom(compilerConfig);
        m_dblCumulativeScore = dblSchoolScore;
    }

    public SchoolTestResult(ETestCategories whichTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize) {
        m_whichTest = whichTest;
        m_compilerConfig.architecture = architecture;
        m_compilerConfig.compiler = compiler;
        m_compilerConfig.optimization = optimize;
    }

    public SchoolTestResult(ETestCategories whichTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize,
                            double dblSchoolScore) {
        m_whichTest = whichTest;
        m_compilerConfig.architecture = architecture;
        m_compilerConfig.compiler = compiler;
        m_compilerConfig.optimization = optimize;
        setScore(dblSchoolScore);
    }

    public void copyFrom(SchoolTestResult rhs){
        super.copyFrom(rhs);
        m_dblCumulativeScore = rhs.m_dblCumulativeScore;
        m_dblMyScore = rhs.m_dblMyScore;
    }
}
