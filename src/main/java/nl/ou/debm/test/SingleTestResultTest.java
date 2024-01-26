package nl.ou.debm.test;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.ECompiler;
import nl.ou.debm.common.EOptimize;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SingleTestResultTest 
{
    @Test
    void Basics(){
        var s1 = new IAssessor.SingleTestResult();
        var s2 = new IAssessor.SingleTestResult();
        assertEquals(s1,s2);
        s1.dblActualValue=10; s1.dblHighBound=10;
        assertEquals(s1,s2);
        var s3 = new IAssessor.SingleTestResult(ETestCategories.FEATURE1_AGGREGATED, EArchitecture.X64ARCH, ECompiler.CLANG, EOptimize.OPTIMIZE,
                0, 10, 15);
        var s4 = new IAssessor.SingleTestResult(ETestCategories.FEATURE1_AGGREGATED, EArchitecture.X64ARCH, ECompiler.CLANG, EOptimize.OPTIMIZE,
                0, 10, 15);
        assertEquals(s3,s4);
        assertNotEquals(s1,s4);
        s3.compilerConfig.architecture=null;
        assertNotEquals(s3,s4);
        s4.compilerConfig.architecture=null;
        assertEquals(s3,s4);
        assertEquals(0, s3.compareTo(s4));
        s3.compilerConfig.architecture=EArchitecture.X86ARCH;
        assertNotEquals(s3,s4);
        assertEquals(1, s3.compareTo(s4));
    }
}
