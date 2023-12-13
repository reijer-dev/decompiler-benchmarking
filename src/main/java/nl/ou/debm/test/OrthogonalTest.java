package nl.ou.debm.test;

import nl.ou.debm.common.Misc;
import nl.ou.debm.common.feature1.OrthogonalArray;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class OrthogonalTest {

    public OrthogonalTest(){
        def.add(new OrthogonalArray.ColumnDefinition(2,"var type"));
        def.add(new OrthogonalArray.ColumnDefinition(8, "update type"));
        def.add(new OrthogonalArray.ColumnDefinition(4, "test type"));
        def.add(new OrthogonalArray.ColumnDefinition(8,"internal control"));
        def.add(new OrthogonalArray.ColumnDefinition(64, "external control"));
        oa = new OrthogonalArray(def);
    }

    List<OrthogonalArray.ColumnDefinition> def = new ArrayList<>();
    OrthogonalArray oa;



    @Test
    void BasisTests(){

        System.out.println("cases = " + oa.iGetNRows());
        System.out.println("column = " + oa.iGetNColumns());

        int[] sc = {0,1};
        for (int sortCol=1; sortCol<oa.iGetNColumns(); ++ sortCol) {
            System.out.println("-----------------------------------");
            sc[1]=sortCol;
            oa.SortResultOnColumn(sc);
            for (int row = 0; row < oa.iGetNRows(); ++row) {
                System.out.print(Misc.strGetNumberWithPrefixZeros(row, 4) + ": ");
                for (int col = 0; col < oa.iGetNColumns(); ++col) {
                    int v = oa.iGetResult(row, col);
                    if (v >= 0) {
                        System.out.print(Misc.strGetNumberWithPrefixZeros(v, 2) + " ");
                    } else {
                        System.out.print("-- ");
                    }
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    @Test
    void CombinationTest(){
        int c1,c2;
        int v1,v2;
        int n, tn;

        System.out.println("cases = " + oa.iGetNRows());
        System.out.println("column = " + oa.iGetNColumns());

        for (c1=0; c1<oa.iGetNColumns()-1 ; c1++) {
            for (c2=c1+1; c2<oa.iGetNColumns(); c2++){
                System.out.println("columns: " + c1 + ", " + c2);
                tn = 0;
                for (v1=0;v1<oa.getColumnDefinition(c1).m_iNPossibilities;++v1){
                    for (v2=0;v2<oa.getColumnDefinition(c2).m_iNPossibilities;++v2){
                        n=0;
                        for (int row=0; row<oa.iGetNRows(); ++row){
                            if ((oa.iGetResult(row,c1)==v1) &&
                                (oa.iGetResult(row,c2)==v2)){
                                ++n;
                            }
                        }
                        System.out.println(v1 + "/" + v2 + "-->" + n);
                        tn+=n;
                    }
                }
                System.out.println("tn = " + tn);
            }
        }
    }
}
