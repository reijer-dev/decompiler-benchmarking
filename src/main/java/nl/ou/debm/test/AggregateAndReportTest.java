package nl.ou.debm.test;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.*;
import nl.ou.debm.common.feature1.CountNoLimitTestResult;
import nl.ou.debm.common.feature1.SchoolTestResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test TestResult implementations:
 * - CountTestResult
 * - SchoolTestResult
 * - CountNoLimitTestResult
 */

public class AggregateAndReportTest {

    @Test
    public void CountTestResult(){
        var tr1 = new IAssessor.CountTestResult();
        // check defaults
        assertEquals(0.0, tr1.dblGetActualValue());
        assertEquals(0.0, tr1.dblGetHighBound());
        assertEquals(0.0, tr1.dblGetTarget());
        assertEquals(0.0, tr1.dblGetLowBound());
        assertEquals(1, tr1.iGetNumberOfTests());
        assertFalse(tr1.getSkipped());
        assertNull(tr1.getWhichTest());

        // check getters and setters
        long[] d = {1, 2, 3};
        for (int t = 0; t<100; ++t){
            for (int dp=0; dp<d.length; dp++){
                d[dp]=Misc.rnd.nextLong(2323);
            }
            tr1.setLowBound(d[0]);
            tr1.setHighBound(d[1]);
            tr1.setActualValue(d[2]);
            assertEquals(d[0], tr1.dblGetLowBound());
            assertEquals(d[1], tr1.dblGetHighBound());
            assertEquals(d[2], tr1.dblGetActualValue());
            assertEquals(d[1], tr1.dblGetTarget());
        }

        // check construction
        IAssessor.CountTestResult[] tr = new IAssessor.CountTestResult[7];
        int v[] = {1, 4, 6};
        for (int t = 0; t<100; ++t){
            for (int dp=0; dp<d.length; dp++) {
                d[dp] = Misc.rnd.nextLong(2323);
            }
            tr[0] = new IAssessor.CountTestResult(ETestCategories.FEATURE1_AGGREGATED);
            tr[1] = new IAssessor.CountTestResult(d[0], d[2], d[1]);
            tr[2] = new IAssessor.CountTestResult(ETestCategories.FEATURE2_AGGREGATED, new CompilerConfig(EArchitecture.X64ARCH, ECompiler.CLANG, EOptimize.OPTIMIZE));
            tr[3] = new IAssessor.CountTestResult(ETestCategories.FEATURE3_AGGREGATED, new CompilerConfig(EArchitecture.X86ARCH, ECompiler.CLANG, EOptimize.OPTIMIZE), true);
            tr[4] = new IAssessor.CountTestResult(ETestCategories.FEATURE1_AGGREGATED, new CompilerConfig(EArchitecture.X86ARCH, ECompiler.CLANG, EOptimize.NO_OPTIMIZE), d[0], d[2], d[1]);
            tr[5] = new IAssessor.CountTestResult(ETestCategories.FEATURE2_AGGREGATED, EArchitecture.X64ARCH, ECompiler.CLANG, EOptimize.NO_OPTIMIZE);
            tr[6] = new IAssessor.CountTestResult(ETestCategories.FEATURE3_AGGREGATED, EArchitecture.X64ARCH, ECompiler.CLANG, EOptimize.NO_OPTIMIZE, d[0], d[2], d[1]);

            for (var i : v){
                assertEquals(d[0], tr[i].dblGetLowBound());
                assertEquals(d[1], tr[i].dblGetHighBound());
                assertEquals(d[2], tr[i].dblGetActualValue());
                assertEquals(d[1], tr[i].dblGetTarget());
                tr[i].increaseActualValue();
                assertEquals(d[2]+1, tr[i].dblGetActualValue());
                tr[i].increaseHighBound();
                assertEquals(d[1]+1, tr[i].dblGetHighBound());
                assertEquals(d[1]+1, tr[i].dblGetTarget());
            }

            assertEquals(ETestCategories.FEATURE1_AGGREGATED, tr[0].getWhichTest());
            assertEquals(ETestCategories.FEATURE2_AGGREGATED, tr[2].getWhichTest());
            assertEquals(ETestCategories.FEATURE3_AGGREGATED, tr[3].getWhichTest());
            assertEquals(ETestCategories.FEATURE1_AGGREGATED, tr[4].getWhichTest());
            assertEquals(ETestCategories.FEATURE2_AGGREGATED, tr[5].getWhichTest());
            assertEquals(ETestCategories.FEATURE3_AGGREGATED, tr[6].getWhichTest());

            assertEquals(EArchitecture.X64ARCH, tr[2].getArchitecture());
            assertEquals(EArchitecture.X86ARCH, tr[3].getArchitecture());
            assertEquals(EArchitecture.X86ARCH, tr[4].getArchitecture());
            assertEquals(EArchitecture.X64ARCH, tr[5].getArchitecture());
            assertEquals(EArchitecture.X64ARCH, tr[6].getArchitecture());

            assertEquals(EOptimize.OPTIMIZE, tr[2].getOptimization());
            assertEquals(EOptimize.OPTIMIZE, tr[3].getOptimization());
            assertEquals(EOptimize.NO_OPTIMIZE, tr[4].getOptimization());
            assertEquals(EOptimize.NO_OPTIMIZE, tr[5].getOptimization());
            assertEquals(EOptimize.NO_OPTIMIZE, tr[6].getOptimization());
        }

        var tr2 = tr1.getNewInstance();
        assertInstanceOf(IAssessor.CountTestResult.class, tr2);
        var tr3 = tr1.makeCopy();
        assertInstanceOf(IAssessor.CountTestResult.class, tr3);
        assertEquals(tr1.dblGetActualValue(), tr3.dblGetActualValue());
        assertEquals(tr1.dblGetHighBound(), tr3.dblGetHighBound());
        assertEquals(tr1.dblGetLowBound(), tr3.dblGetLowBound());
        assertEquals(tr1.dblGetTarget(), tr3.dblGetTarget());
    }

    @Test
    public void AggregateCountResults(){
        final int LIST_SIZE=16;

        List<IAssessor.TestResult> list = new ArrayList<>();
        for (int i=0; i<LIST_SIZE; ++i){
            list.add(new IAssessor.CountTestResult(
                    Misc.rnd.nextDouble() < .3 ? ETestCategories.FEATURE1_AGGREGATED : Misc.rnd.nextDouble() < .5 ? ETestCategories.FEATURE2_AGGREGATED : ETestCategories.FEATURE3_AGGREGATED,
                    Misc.rnd.nextDouble() < .5 ? EArchitecture.X64ARCH : EArchitecture.X86ARCH,
                    ECompiler.CLANG,
                    Misc.rnd.nextDouble() < .5 ? EOptimize.OPTIMIZE : EOptimize.NO_OPTIMIZE,
                    1,55,100
            ));
        }

        assertEquals(LIST_SIZE, dblLowBoundSum(list));
        assertEquals(LIST_SIZE*55, (int)dblActualSum(list));
        assertEquals(LIST_SIZE*100, (int)dblHighBoundSum(list));

        var list3 = new ArrayList<>(list);
        list3.sort(new IAssessor.TestResultComparator());
        showList(list3);
        var list2 = IAssessor.TestResult.aggregate(list);
        showList(list2);
        var list4 = IAssessor.TestResult.aggregateOverArchitecture(list);
        showList(list4);
        var list5 = IAssessor.TestResult.aggregateOverCompiler(list);
        showList(list5);
        var list6 = IAssessor.TestResult.aggregateOverOptimization(list);
        showList(list6);

        assertEquals(LIST_SIZE, dblLowBoundSum(list));
        assertEquals(LIST_SIZE*55, (int)dblActualSum(list));
        assertEquals(LIST_SIZE*100, (int)dblHighBoundSum(list));

        assertEquals(LIST_SIZE, dblLowBoundSum(list4));
        assertEquals(LIST_SIZE*55, (int)dblActualSum(list4));
        assertEquals(LIST_SIZE*100, (int)dblHighBoundSum(list4));

        assertEquals(LIST_SIZE, dblLowBoundSum(list5));
        assertEquals(LIST_SIZE*55, (int)dblActualSum(list5));
        assertEquals(LIST_SIZE*100, (int)dblHighBoundSum(list5));

        assertEquals(LIST_SIZE, dblLowBoundSum(list6));
        assertEquals(LIST_SIZE*55, (int)dblActualSum(list6));
        assertEquals(LIST_SIZE*100, (int)dblHighBoundSum(list6));


        var trx = new SchoolTestResult();
        assertThrows(Throwable.class, () -> { list.get(0).aggregateValues(trx);  });

    }

    private <T> void showList(List<T> list){
        System.out.println("-------------------------------");
        for (var item : list){
            System.out.println(item);
        }
    }

    private double dblLowBoundSum(List<IAssessor.TestResult> list){
        double out=0;
        for (var item: list) {
            out += item.dblGetLowBound();
        }
        return out;
    }
    private double dblHighBoundSum(List<IAssessor.TestResult> list){
        double out=0;
        for (var item: list) {
            out += item.dblGetHighBound();
        }
        return out;
    }
    private double dblActualSum(List<IAssessor.TestResult> list){
        double out=0;
        for (var item: list) {
            out += item.dblGetActualValue();
        }
        return out;
    }

    @Test
    public void AggregateSchoolMarkTestResult(){
        final int LIST_SIZE=16;

        List<IAssessor.TestResult> list = new ArrayList<>();
        for (int i=0; i<LIST_SIZE; ++i){
            var cr=new CountNoLimitTestResult();
            cr.setWhichTest(Misc.rnd.nextDouble() < .3 ? ETestCategories.FEATURE1_AGGREGATED : Misc.rnd.nextDouble() < .5 ? ETestCategories.FEATURE2_AGGREGATED : ETestCategories.FEATURE3_AGGREGATED);
            cr.setCompilerConfig(new CompilerConfig(                    Misc.rnd.nextDouble() < .5 ? EArchitecture.X64ARCH : EArchitecture.X86ARCH,
                    ECompiler.CLANG,
                    Misc.rnd.nextDouble() < .5 ? EOptimize.OPTIMIZE : EOptimize.NO_OPTIMIZE
                    ));
            cr.setLowBound(1);
            cr.setActualValue(55);
            cr.setHighBound(100);
            list.add(cr);
        }

        assertEquals(LIST_SIZE, dblLowBoundSum(list));
        assertEquals(LIST_SIZE*55, (int)dblActualSum(list));

        var list3 = new ArrayList<>(list);
        list3.sort(new IAssessor.TestResultComparator());
        showList(list3);
        var list2 = IAssessor.TestResult.aggregate(list);
        showList(list2);
        var list4 = IAssessor.TestResult.aggregateOverArchitecture(list);
        showList(list4);
        var list5 = IAssessor.TestResult.aggregateOverCompiler(list);
        showList(list5);
        var list6 = IAssessor.TestResult.aggregateOverOptimization(list);
        showList(list6);

        assertEquals(LIST_SIZE, dblLowBoundSum(list));
        assertEquals(LIST_SIZE*55, (int)dblActualSum(list));

        assertEquals(LIST_SIZE, dblLowBoundSum(list4));
        assertEquals(LIST_SIZE*55, (int)dblActualSum(list4));

        assertEquals(LIST_SIZE, dblLowBoundSum(list5));
        assertEquals(LIST_SIZE*55, (int)dblActualSum(list5));

        assertEquals(LIST_SIZE, dblLowBoundSum(list6));
        assertEquals(LIST_SIZE*55, (int)dblActualSum(list6));


        var trx = new SchoolTestResult();
        assertThrows(Throwable.class, () -> { list.get(0).aggregateValues(trx);  });
        
    }
}
