package nl.ou.debm.test;

import nl.ou.debm.assessor.Assessor;
import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.ECompiler;
import nl.ou.debm.common.EOptimize;
import org.junit.jupiter.api.Test;


import java.util.*;

import static java.lang.Math.pow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AggregateAndReportTest {

    @Test
    public void AggregateSetOfMaps(){
        List<Map<IAssessor.TestParameters, IAssessor.SingleTestResult>> list = new ArrayList<>();
        for (int i=0; i<32; i++){
            Map<IAssessor.TestParameters, IAssessor.SingleTestResult> map = new HashMap<>();
            list.add(map);
            for (int j=0; j<16; ++j){
                IAssessor.TestParameters pars;
                if (j %2 == 0) {
                    pars = new IAssessor.TestParameters(ETestCategories.FEATURE1_AGGREGATED, new CompilerConfig(EArchitecture.X64ARCH, ECompiler.CLANG, EOptimize.OPTIMIZE));
                }
                else{
                    pars = new IAssessor.TestParameters(ETestCategories.FEATURE1_AGGREGATED, new CompilerConfig(EArchitecture.X64ARCH, ECompiler.CLANG, EOptimize.NO_OPTIMIZE));
                }
                var result = new IAssessor.SingleTestResult(0, pow(2, j) * ((double)i/16), pow(2, j));
                map.put(pars, result);
                Assessor.generateReport(map, "/tmp/report" + i);
                var map2 = Assessor.AggregateOverOptimization(map);
                Assessor.generateReport(map2, "/tmp/report_a" + i);
            }
        }

    }

    @Test
    public void MapTest(){
        Map<IAssessor.TestParameters, IAssessor.SingleTestResult> map = new HashMap<>();
        var key1 = new IAssessor.TestParameters(ETestCategories.FEATURE1_AGGREGATED, new CompilerConfig(EArchitecture.X64ARCH, ECompiler.CLANG, EOptimize.NO_OPTIMIZE));
        var key2 = new IAssessor.TestParameters(ETestCategories.FEATURE1_AGGREGATED, new CompilerConfig(EArchitecture.X64ARCH, ECompiler.CLANG, EOptimize.NO_OPTIMIZE));
        var v1 = new IAssessor.SingleTestResult(0,23,100);
        var v2 = new IAssessor.SingleTestResult(0,19,100);
        assertEquals(key1, key2);
        map.put(key1, v1);
        map.put(key2, v2);
        var set = map.entrySet();
        List<Map.Entry<IAssessor.TestParameters, IAssessor.SingleTestResult>> list = new ArrayList<>(set);
        Collections.sort(list, new Comparator<Map.Entry<IAssessor.TestParameters, IAssessor.SingleTestResult>>() {
            @Override
            public int compare(Map.Entry<IAssessor.TestParameters, IAssessor.SingleTestResult> o1, Map.Entry<IAssessor.TestParameters, IAssessor.SingleTestResult> o2) {
                return 0;
            }
        });

    }
}
