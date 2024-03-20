package nl.ou.debm.test;

import nl.ou.debm.common.feature1.OrthogonalArray;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrthogonalTest {



    @Test
    void BasicTest() {
        int[] f = {4, 2, 2, 2};
        int inruns = 16;
        int strength = 2;

        testSingleOA(f,inruns,strength);

        int[] f2 = {8, 4, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
        inruns = 32;
        strength = 2;

        testSingleOA(f2,inruns,strength);

    }

    private void testSingleOA(final int[] F, final int INRUNS, final int STRENGTH) {

        var oa = new OrthogonalArray(F, INRUNS, STRENGTH);

        assertEquals(F.length, oa.iNColumns());
        assertEquals(INRUNS, oa.iNRuns());

        // assert every value has the same frequency in every column
        for (int col = 0; col<oa.iNColumns(); col++){
            int[] item = {col};
            var map = oa.iCombinationFrequencies(item);
            int val = 0;
            for (var q : map.entrySet()){
                val = q.getValue();
                break;
            }
            for (var q : map.entrySet()){
                assertEquals(val, q.getValue());
            }
        }

        // assert every combination has the same frequency for every combination of columns
        List<int[]> v = new ArrayList<>();
        columnCombiRecurse(0, oa.iNColumns(), STRENGTH, new int[STRENGTH], 0, v);
        for (var item : v){
            var map = oa.iCombinationFrequencies(item);
            int val = 0;
            for (var q : map.entrySet()){
                val = q.getValue();
                break;
            }
            for (var q : map.entrySet()){
                assertEquals(val, q.getValue());
            }
        }

    }

    void columnCombiRecurse(int start, int end, int np, int[] f, int cl, List<int[]> out){
        for (f[cl]=start;f[cl]<end;++f[cl]){
            if (np==1){
                int[] l = new int[cl+1];
                System.arraycopy(f, 0, l, 0, cl+1);
                out.add(l);
            }
            else{
                columnCombiRecurse(f[cl]+1, end, np-1, f, cl+1, out);
            }
        }
    }

}
