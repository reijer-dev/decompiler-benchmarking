package nl.ou.debm.common.feature1;

import nl.ou.debm.assessor.CountTestResult;
import nl.ou.debm.assessor.IAssessor;

public class CountNoLimitTestResult extends CountTestResult {

    public CountNoLimitTestResult(){
        super();
    }
    public CountNoLimitTestResult(CountNoLimitTestResult rhs){
        super(rhs);
    }
    @Override
    public void aggregateValues(IAssessor.TestResult rhs) {
        assert rhs instanceof CountNoLimitTestResult : "aggregating different types of test results (CountNoLimitTestResult)";
        super.aggregateAbstractValues(rhs);
        var rh = (CountTestResult) rhs;
        m_lngLowBound += rh.dblGetLowBound();
        m_lngActualValue += rh.dblGetActualValue();
    }

    @Override
    public Double dblGetHighBound() {
        // no high bound available
        return null;
    }

    @Override
    public void increaseHighBound() {
        // do nothing
    }

    @Override
    public void setHighBound(long lngHighBound) {
        // do nothing
    }

    @Override
    public IAssessor.TestResult getNewInstance() {
        return new CountNoLimitTestResult();
    }

    @Override
    public IAssessor.TestResult makeCopy() {
        return new CountNoLimitTestResult(this);
    }

    public CountNoLimitTestResult(CountTestResult rhs) {
        copyFrom(rhs);
    }

    @Override
    public void copyFrom(IAssessor.TestResult rhs) {
        super.copyAbstractValues(rhs);
        assert rhs instanceof CountNoLimitTestResult;
        var rhss = (CountNoLimitTestResult) rhs;
        m_lngLowBound = rhss.m_lngLowBound;
        m_lngActualValue = rhss.m_lngActualValue;
    }
}
