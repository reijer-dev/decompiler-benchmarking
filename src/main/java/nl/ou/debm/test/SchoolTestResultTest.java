package nl.ou.debm.test;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.ECompiler;
import nl.ou.debm.common.EOptimize;
import nl.ou.debm.common.feature1.SchoolTestResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SchoolTestResultTest {

    @Test
    public void BasicTesting(){
        var tr1 = new SchoolTestResult(5.0);
        assertEquals(1, tr1.iGetNumberOfTests());
        assertEquals(5.0, tr1.dblGetActualValue());
        var tr2 = new SchoolTestResult(ETestCategories.FEATURE1_LOOP_BEAUTY_SCORE_UNROLLED,  new CompilerConfig(EArchitecture.X64ARCH, ECompiler.CLANG, EOptimize.OPTIMIZE));
        assertEquals(1, tr2.iGetNumberOfTests());
        assertEquals(0.0, tr2.dblGetActualValue());
        var tr3 = new SchoolTestResult(tr1);
        tr3.setScore(7.0);
        assertEquals(1, tr3.iGetNumberOfTests());
        assertEquals(7.0, tr3.dblGetActualValue());
        tr1.aggregateValues(tr3);
        assertEquals(1, tr3.iGetNumberOfTests());
        assertEquals(7.0, tr3.dblGetActualValue());
        assertEquals(2, tr1.iGetNumberOfTests());
        assertEquals(6.0, tr1.dblGetActualValue());
        tr3.setScore(1.0);
        assertEquals(2, tr1.iGetNumberOfTests());
        assertEquals(6.0, tr1.dblGetActualValue());
        tr1.setScore(10.0);
        assertEquals(2, tr1.iGetNumberOfTests());
        assertEquals(8.5, tr1.dblGetActualValue());
        var tr4 = tr1.getNewInstance();
        assertTrue(tr4 instanceof SchoolTestResult);
        var tr5 = tr1.makeCopy();
        assertTrue(tr5 instanceof SchoolTestResult);
    }
}
