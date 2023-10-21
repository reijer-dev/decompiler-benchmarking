package nl.ou.debm.test;

import nl.ou.debm.common.CodeMarker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeMarkerTest {
    @Test
    void BasicTests(){
        var cm = new CodeMarker();

        String P[] = {"Name", "Address", "ZIP",
                        "Key 0", "Key 1", "Key 2",
                        "Key \\0", "Key \\1", "Key \\2",
                        "Key ,0", "Key ,1", "Key ,2",
                        "Key :0", "Key :1", "Key :2"};
        String V[] = {"Harry", "Privet Drive", "O.W.L.P.O.S.T.",
                        "A", "B", "C",
                        "D", "E", "F",
                        "G", "H", "I",
                        "J", "K", "L"};
        assertEquals(P.length, V.length);

        // assert empty output on empty input
        assertEquals("", cm.toString());

        // set values and assert table size
        int x;
        for (x=0; x<P.length ; ++x) {
            assertEquals(x, cm.iNProperties());
            cm.setProperty(P[x], V[x]);
            assertEquals(x + 1, cm.iNProperties());
        }

        // assert values
        for (x=0; x<P.length ; ++x) {
            assertEquals(V[x], cm.strPropertyValue(P[x]));
        }

        // shift values
        P = shift(P,1);

        // update values and assert table size
        for (x=0; x<P.length ; ++x) {
            cm.setProperty(P[x], V[x]);
            assertEquals(P.length, cm.iNProperties());
        }
        // assert values
        for (x=0; x<P.length ; ++x) {
            assertEquals(V[x], cm.strPropertyValue(P[x]));
        }


        // shift values
        P = shift(P,-1);

        // update values and assert table size
        for (x=0; x<P.length ; ++x) {
            cm.setProperty(P[x], V[x]);
            assertEquals(P.length, cm.iNProperties());
        }
        // assert values
        for (x=0; x<P.length ; ++x) {
            assertEquals(V[x], cm.strPropertyValue(P[x]));
        }


        // assert valid return on non-existing property
        assertEquals("", cm.strPropertyValue("doesNotExist"));

        // show result
        System.out.println("Resulting marker o: " + cm);

        // copy 1
        var cm2 = new CodeMarker(cm.toString());
        // assert values
        for (x=0; x<P.length ; ++x) {
            assertEquals(V[x], cm2.strPropertyValue(P[x]));
        }



        // show result
        System.out.println("Resulting marker 2: " + cm2);

        // copy 2
        var cm3 = new CodeMarker(cm);
        // assert values
        for (x=0; x<P.length ; ++x) {
            assertEquals(V[x], cm3.strPropertyValue(P[x]));
        }

        // clear original one
        cm.clear();
        // assert empty output on empty input
        assertEquals("", cm.toString());
        // assert empty copy works
        cm2.fromCodeMarker(cm);
        assertEquals("", cm2.toString());
        // another copy
        cm2.fromCodeMarker(cm3);
        assertEquals(cm3.iNProperties(), cm2.iNProperties());

        // remove a property
        cm2.removeProperty(P[0]);
        // assert count
        assertEquals(P.length - 1, cm2.iNProperties());

        // show result
        System.out.println("Resulting marker 3: " + cm3);
    }

    private String[] shift(String[] in, int sh){
        if (sh<0){sh+=in.length;}
        String [] out = new String[in.length];
        for (int x=0; x<in.length; ++x){
            out[(x + sh) % in.length] = in [x];
        }
        return out;
    }
}
