package nl.ou.debm.test;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.lang.Math.pow;

public class AggregateAndReportTest {

    @Test
    public void AggregateSetOfMaps(){
        List<Map<String, IAssessor.SingleTestResult>> list = new ArrayList<>();
        for (int i=0; i<32; i++){
            Map<String, IAssessor.SingleTestResult> map = new HashMap<>();
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
            }
        }

    }

    @Test
    public void ListTest(){
        List<IAssessor.TestParameters> list = new ArrayList<>();
        for (int x=0; x<50; x++){
            var tp = new IAssessor.TestParameters(Misc.rnd.nextDouble()<.3 ? ETestCategories.FEATURE1_AGGREGATED :
                    Misc.rnd.nextDouble()<.5 ? ETestCategories.FEATURE2_AGGREGATED : ETestCategories.FEATURE3_AGGREGATED ,
                    new CompilerConfig(Misc.rnd.nextDouble()<.5 ? EArchitecture.X64ARCH : EArchitecture.X86ARCH,
                                       ECompiler.CLANG,
                                       Misc.rnd.nextDouble()<.5 ? EOptimize.NO_OPTIMIZE : EOptimize.OPTIMIZE));
            list.add(tp);
        }

        for (var item : list){
            System.out.println(item);
        }
        list.sort(new IAssessor.TestParametersComparator());
        System.out.println("--------------------");
        for (var item : list){
            System.out.println(item);
        }
    }
}
