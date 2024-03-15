package nl.ou.debm.common.feature1;

import nl.ou.debm.assessor.IAssessor;

public class CountNoLimitTestResult extends IAssessor.CountTestResult {
    @Override
    public void aggregateValues(IAssessor.TestResult rhs) {
        assert rhs instanceof CountNoLimitTestResult : "aggregating different types of test results (CountNoLimitTestResult)";
        super.aggregateAbstractValues(rhs);
        var rh = (IAssessor.CountTestResult) rhs;
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
}