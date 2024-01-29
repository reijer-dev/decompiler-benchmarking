package nl.ou.debm.test;

import nl.ou.debm.assessor.Assessor;
import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.ECompiler;
import nl.ou.debm.common.EOptimize;
import nl.ou.debm.common.Misc;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AggregateAndReportTest {

    @Test
    public void Aggregate(){
        final int LIST_SIZE=16;

        List<IAssessor.SingleTestResult> list = new ArrayList<>();
        for (int i=0; i<LIST_SIZE; ++i){
            list.add(new IAssessor.SingleTestResult(
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
        list3.sort(new IAssessor.SingleTestResultComparator());
        showList(list3);
        var list2 = IAssessor.SingleTestResult.aggregate(list);
        showList(list2);
        var list4 = IAssessor.SingleTestResult.aggregateOverArchitecture(list);
        showList(list4);
        var list5 = IAssessor.SingleTestResult.aggregateOverCompiler(list);
        showList(list5);
        var list6 = IAssessor.SingleTestResult.aggregateOverOptimization(list);
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

        Assessor.generateReport(list,"/tmp/list.html");
        Assessor.generateReport(list4,"/tmp/list4.html");
        Assessor.generateReport(list5,"/tmp/list5.html");
        Assessor.generateReport(list6,"/tmp/list6.html");
    }

    private <T> void showList(List<T> list){
        System.out.println("-------------------------------");
        for (var item : list){
            System.out.println(item);
        }
    }

    private double dblLowBoundSum(List<IAssessor.SingleTestResult> list){
        double out=0;
        for (var item: list) {
            out += item.dblLowBound;
        }
        return out;
    }
    private double dblHighBoundSum(List<IAssessor.SingleTestResult> list){
        double out=0;
        for (var item: list) {
            out += item.dblHighBound;
        }
        return out;
    }
    private double dblActualSum(List<IAssessor.SingleTestResult> list){
        double out=0;
        for (var item: list) {
            out += item.dblActualValue;
        }
        return out;
    }
}
