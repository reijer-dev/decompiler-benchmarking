package nl.ou.debm.test;

import nl.ou.debm.common.feature1.CountNoLimitTestResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CountNoLimitTest {
    @Test
    public void BasicTest (){
        var tr = new CountNoLimitTestResult();
        assertNull(tr.dblGetHighBound());
        tr.setActualValue(88);
        assertEquals(88, tr.dblGetActualValue());
        assertEquals("", tr.strGetPercentage());
    }
}
