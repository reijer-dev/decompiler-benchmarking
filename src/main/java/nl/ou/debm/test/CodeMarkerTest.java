package nl.ou.debm.test;

import nl.ou.debm.common.CodeMarker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

        // assert presence of ID field
        assertNotEquals("", cm.toString());
        assertEquals(1, cm.iNProperties());
        assertNotNull(cm.getID());

        // set values and assert table size
        int x;
        for (x=0; x<P.length ; ++x) {
            assertEquals(x + 1, cm.iNProperties());
            cm.setProperty(P[x], V[x]);
            assertEquals(x + 2, cm.iNProperties());
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
            assertEquals(P.length + 1, cm.iNProperties());
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
            assertEquals(P.length + 1, cm.iNProperties());
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
        // assert only 1 field
        assertNotEquals("", cm.toString());
        assertEquals(1, cm.iNProperties());
        // assert copy works even with only ID
        cm2.fromCodeMarker(cm);
        assertNotEquals("", cm2.toString());
        assertEquals(1, cm.iNProperties());
        // another copy
        cm2.fromCodeMarker(cm3);
        assertEquals(cm3.iNProperties(), cm2.iNProperties());

        // remove a property
        cm2.removeProperty(P[0]);
        // assert count
        assertEquals(P.length, cm2.iNProperties());

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
