package nl.ou.debm.test;

import nl.ou.debm.common.feature1.OrthogonalArray;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class OrthogonalTest {



    @Test
    void BasicTest() throws Exception{
        int[] f = {4, 2, 2, 2};
        var oa = new OrthogonalArray(f, 16, 2);

        System.out.println("Columns " + oa.iNColumns());
        System.out.println("Runs " + oa.iNRuns());
        for (int c = 0; c < oa.iNColumns(); ++c) {
            System.out.println("Value stats for col " + c);
            System.out.print("Range is 0-" + oa.iHighestValuePerColumn(c) + ", ");
            int[] v = oa.iNAppearancesPerColumn(c);
            for (var x : v) {
                System.out.print(x + " ");
            }
            System.out.println();
        }

        System.out.println("2 column combi test");
        int[] cp = new int[2];
        for (cp[0] = 0; cp[0] < oa.iNColumns() - 1 ; ++cp[0]) {
            for (cp[1] = cp[0] + 1; cp[1] < oa.iNColumns() ; ++cp[1]) {
                System.out.println("Columns: " + cp[0] + "+" + cp[1]);
                var freq = oa.iCombinationFrequencies(cp);

                freq.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(System.out::println);

//                for (var item:freq.entrySet()){
//                    System.out.println(item.getKey() + "  " + item.getValue());
//                }
            }
        }


        System.out.println("Full output ");
        for (int r = 0; r < oa.iNRuns(); ++r) {
            for (int c = 0; c < oa.iNColumns(); ++c) {
                System.out.print(oa.iValuePerRunPerColumn(r, c));
            }
            System.out.println();
        }


    }

}
