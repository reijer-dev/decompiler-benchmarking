package nl.ou.debm.test;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.feature1.ELoopMarkerLocationTypes;
import nl.ou.debm.common.feature1.LoopCodeMarker;
import nl.ou.debm.common.feature1.LoopProducer;
import nl.ou.debm.producer.CGenerator;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CodeMarkerTest {
    @Test
    void BasicTests(){
        var f= new LoopProducer(new CGenerator());
        var cm = new CodeMarker(f);

        String[] P = {"Name", "Address", "ZIP",
                "Key 0", "Key 1", "Key 2",
                "Key \\0", "Key \\1", "Key \\2",
                "Key ,0", "Key ,1", "Key ,2",
                "Key :0", "Key :1", "Key :2"};
        String[] V = {"Harry", "Privet Drive", "O.W.L.P.O.S.T.",
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

    @Test
    void IDTests() {
        // make a series of CM-objects
        var f= new LoopProducer(new CGenerator());
        CodeMarker[] cm = new CodeMarker[10];
        for (int z=0;z<cm.length;++z){cm[z] = new CodeMarker(f);}
        CheckUnique(cm);

        // copy from other object
        cm[0] = new CodeMarker(cm[1]);
        CheckUnique(cm);

        // try setting ID directly
        final String FAKEID="aabbccdd";
        cm[0].setProperty("ID",FAKEID);
        assertNotEquals(FAKEID, cm[0].getID());

        // try setting ID from string
        cm[0].fromString(CodeMarker.STRCODEMARKERGUID + f.getPrefix() + ">>ID:" + FAKEID, false);
        assertEquals(FAKEID, cm[0].getID());

        // get new ID for new object and assert its value
        cm[0] = new CodeMarker(f);
        assertEquals(Long.parseLong(FAKEID,16) + 1, Long.parseLong(cm[0].getID(),16));
        CheckUnique(cm);
    }

    @Test
    void EscapeTesting(){
        // make CM-objects
        var f= new LoopProducer(new CGenerator());
        CodeMarker cm = new CodeMarker(f);
        CodeMarker cm2 = new CodeMarker(f);

        // assume that any escape char lies between ascii 32 and 127
        // and make all sorts of combinations
        final char LOW=' ', HIGH=127;
        var sb = new StringBuilder();
        char[] c = new char[3];
        int p;
        for (p=0; p<c.length; ++p) {c[p]=LOW; sb.append(" ");};
        while (true){
            // construct name
            for (p=0; p<c.length; ++p){
                ++c[p];
                if (c[p]>HIGH){
                    c[p]=LOW;
                }
                else{
                    break;
                }
            }
            if (p==c.length){
                break;
            }
            for (p=0; p<c.length; ++p) {
                sb.setCharAt(p, c[p]);
            }
            // set name as both property name and value
            cm.setProperty(sb.toString(), sb.toString());
        }
        // copy the lot to the other thing
        cm2.fromString(cm.toString());
        // test quote absence
        assertFalse(cm.toString().contains("\""));
        // test it -- after copy all should be accessible
        // (and any problem with any escape char should show up)
        while (true){
            // construct name
            for (p=0; p<c.length; ++p){
                ++c[p];
                if (c[p]>HIGH){
                    c[p]=LOW;
                }
                else{
                    break;
                }
            }
            if (p==c.length){
                break;
            }
            for (p=0; p<c.length; ++p) {
                sb.setCharAt(p, c[p]);
            }
            // test it
            assertEquals(sb.toString(), cm2.strPropertyValue((sb.toString())));
        }
    }

    @Test
    void TestPrintf(){
        var f= new LoopProducer(new CGenerator());
        CodeMarker cm = new CodeMarker(f);
        final String STRVARNAME="sunshine";

        cm.setProperty("Name","Harry");
        cm.setProperty("BookTitle", "\"Harry Potter and the Prisoner of Azkaban\"");

        for (int t=0;t<2;++t) {

            // 0 = test printf
            // 1 = test printf + numberic variable

            String statement;
            if (t==0){
                statement =cm.strPrintf();
            }
            else {
                statement = cm.strPrintfInteger(STRVARNAME);
            }
            System.out.println(statement);

            // make sure the wrap is ok
            assertEquals(0, statement.indexOf("printf("));                      // starts with printf(
            assertEquals(statement.length() - 2, statement.indexOf(");"));      // ends with );
            // check quotes
            int lp=0;
            for (int q=0; q<3; q++){
                // look for a quote
                int np = statement.indexOf("\"",lp);
                if (q==0){
                    // first quote must be directly after 'printf('
                    assertEquals(7, np);
                }
                else if (q==1){
                    // there must be a second quote
                    assertNotEquals(-1,np);
                }
                else {
                    // there may not be a third quote
                    assertEquals(-1, np);
                }

                // next quote
                lp = np + 1;
            }

            if (t==0){
                assertEquals(statement.length() - 3, statement.indexOf("\");"));      // ends with ");
            }
            else {
                // var name found after quotes?
                int p2 = statement.indexOf("\"",8);
                int p3 = statement.indexOf(STRVARNAME, p2);
                assertNotEquals(-1, p3);
                // , before varname?
                int p4= statement.indexOf(",", p2);
                assertNotEquals(-1,p4);
                assertTrue(p4<p3);
            }
        }

    }

    void CheckUnique (CodeMarker[] cm){
        // make sure all ID's are unique
        int x, y;
        for (x = 0; x < (cm.length - 1); ++x) {
            for (y = x + 1; y < cm.length; ++y) {
                assertNotEquals(cm[x].getID(), cm[y].getID());
            }
        }
    }

        private String[] shift(String[] in, int sh){
        if (sh<0){sh+=in.length;}
        String [] out = new String[in.length];
        for (int x=0; x<in.length; ++x){
            out[(x + sh) % in.length] = in [x];
        }
        return out;
    }

    @Test
    void CheckLoopCodeMarkerProperties(){
        var lcm = new LoopCodeMarker();
        boolean v[] = new boolean[9];
        for (int c=0; c<512;++c){
            int p=0;
            for (int m=1; m<512; m*=2){
                v[p++]=((c & m)>0);
            }
            lcm.setUseContinue(               v[0]);
            lcm.setUseGotoBegin(              v[1]);
            lcm.setUseGotoEnd(                v[2]);
            lcm.setUseBreak(                  v[3]);
            lcm.setUseExit(                   v[4]);
            lcm.setUseReturn(                 v[5]);
            lcm.setUseGotoDirectlyAfterLoop(  v[6]);
            lcm.setUseGotoFurtherFromThisLoop(v[7]);
            lcm.setUseBreakOutNestedLoops(    v[8]);
            assertEquals(v[0], lcm.bGetUseContinue());
            assertEquals(v[1], lcm.bGetUseGotoBegin());
            assertEquals(v[2], lcm.bGetUseGotoEnd());
            assertEquals(v[3], lcm.bGetUseBreak());
            assertEquals(v[4], lcm.bGetUseExit());
            assertEquals(v[5], lcm.bGetUseReturn());
            assertEquals(v[6], lcm.bGetUseGotoDirectlyAfterLoop());
            assertEquals(v[7], lcm.bGetUseGotoFurtherFromThisLoop());
            assertEquals(v[8], lcm.bGetUseBreakOutNestedLoops());
        }

        for (int n=0; n<1000;n++){
            var v1 = Misc.rnd.nextInt();
            var v2 = Misc.rnd.nextLong();
            lcm.setNestingLevel(v1);
            lcm.setLoopID(v2);
            assertEquals(v1, lcm.iGetNestingLevel());
            assertEquals(v2, lcm.lngGetLoopID());
        }

        for (var item: ELoopMarkerLocationTypes.values()){
            lcm.setLoopCodeMarkerLocation(item);
            assertEquals(item, lcm.getLoopCodeMarkerLocation());
        }

        for (int n=0; n<1000; n++){
            UUID t1 = UUID.randomUUID();
            UUID t2 = UUID.randomUUID();
            UUID t3 = UUID.randomUUID();
            lcm.setInitExpression(t1.toString());
            lcm.setUpdateExpression(t2.toString());
            lcm.setTestExpression(t3.toString());
            assertEquals(t1.toString(), lcm.strGetInitExpression());
            assertEquals(t2.toString(), lcm.strGetUpdateExpression());
            assertEquals(t3.toString(), lcm.strGetTestExpression());
        }
    }
}
