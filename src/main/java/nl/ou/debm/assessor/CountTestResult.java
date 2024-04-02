package nl.ou.debm.assessor;

import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.ECompiler;
import nl.ou.debm.common.EOptimize;


/**
 * Implementation class for test results. This class is used for results that use simple counting, for
 * example: we count the number of loops in de decoded c-code in relation to the number of loops
 * introduced in the original code.<br>
 * As we only count, we use longs instead of doubles. Target can be high bound or low bound,
 * depending on the tsargetmode
 *
 */

public class CountTestResult extends IAssessor.TestResult {
    public enum ETargetMode{
        HIGHBOUND, LOWBOUND;
    }

    /** low bound for the metric */
    protected long m_lngLowBound=0;
    /** actual value for the metric */
    protected long m_lngActualValue = 0;
    /** high bound for the metric */
    protected long m_lngHighBound = 0;
    /** target mode, by default: high bound */
    protected ETargetMode m_targetMode = ETargetMode.HIGHBOUND;

    public CountTestResult(){        }
    public CountTestResult(CountTestResult rhs){
        copyFrom(rhs);
    }
    public CountTestResult(ETestCategories whichTest){
        m_whichTest = whichTest;
    }
    public CountTestResult(long lngLowBound, long lngActualValue, long lngHighBound) {
        m_lngLowBound = lngLowBound;
        m_lngActualValue = lngActualValue;
        m_lngHighBound = lngHighBound;
    }

    public CountTestResult(ETestCategories whichTest, CompilerConfig compilerConfig,
                           long lngLowBound, long lngActualValue, long lngHighBound) {
        m_whichTest = whichTest;
        m_compilerConfig.copyFrom(compilerConfig);
        m_lngLowBound = lngLowBound;
        m_lngActualValue = lngActualValue;
        m_lngHighBound = lngHighBound;
    }

    public CountTestResult(ETestCategories whichTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize) {
        m_whichTest = whichTest;
        m_compilerConfig.architecture = architecture;
        m_compilerConfig.compiler = compiler;
        m_compilerConfig.optimization = optimize;
    }

    public CountTestResult(ETestCategories whichTest, CompilerConfig config) {
        m_whichTest = whichTest;
        m_compilerConfig.copyFrom(config);
    }

    public CountTestResult(ETestCategories whichTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize,
                           long lngLowBound, long lngActualValue, long lngHighBound) {
        m_whichTest = whichTest;
        m_compilerConfig.architecture = architecture;
        m_compilerConfig.compiler = compiler;
        m_compilerConfig.optimization = optimize;
        m_lngLowBound = lngLowBound;
        m_lngActualValue = lngActualValue;
        m_lngHighBound = lngHighBound;
    }

    public void copyFrom(IAssessor.TestResult rhs){
        assert rhs instanceof CountTestResult;
        var rhss = (CountTestResult) rhs;
        super.copyAbstractValues(rhs);
        m_lngLowBound = rhss.m_lngLowBound;
        m_lngActualValue = rhss.m_lngActualValue;
        m_lngHighBound = rhss.m_lngHighBound;
        m_targetMode = rhss.m_targetMode;
    }

    @Override
    public Double dblGetLowBound() {
        return (double)m_lngLowBound;
    }

    @Override
    public Double dblGetActualValue() {
        return (double)m_lngActualValue;
    }

    @Override
    public Double dblGetHighBound() {
        return (double)m_lngHighBound;
    }

    @Override
    public Double dblGetTarget() {
        if (m_targetMode == ETargetMode.HIGHBOUND) {
            return (double) m_lngHighBound;
        }
        else {
            return (double) m_lngLowBound;
        }
    }

    public void setLowBound(long lngLowBound){
        m_lngLowBound = lngLowBound;
    }
    public void setActualValue(long lngActualValue){
        m_lngActualValue=lngActualValue;
    }
    public void setHighBound(long lngHighBound){
        m_lngHighBound=lngHighBound;
    }
    public void setTargetMode(ETargetMode mode){
        m_targetMode = mode;
    }
    public ETargetMode getTargetMode(){ return m_targetMode; }

    public void increaseActualValue(){
        m_lngActualValue++;
    }
    public void increaseHighBound(){
        m_lngHighBound++;
    }

    @Override
    public int iGetNumberOfDecimalsToBePrinted() {
        return 0;
    }

    @Override
    public IAssessor.TestResult getNewInstance() {
        return new CountTestResult();
    }

    @Override
    public IAssessor.TestResult makeCopy() {
        return new CountTestResult(this);
    }

    @Override
    public void aggregateValues(IAssessor.TestResult rhs) {
        assert rhs instanceof CountTestResult : "aggregating different types of test results";
        super.aggregateAbstractValues(rhs);
        var rh = (CountTestResult) rhs;
        assert rh.m_targetMode==this.m_targetMode : "aggregating different target modes";
        m_lngLowBound += rh.m_lngLowBound;
        m_lngActualValue += rh.m_lngActualValue;
        m_lngHighBound += rh.m_lngHighBound;
    }
}