package nl.ou.debm.test;

import nl.ou.debm.common.feature1.LoopTestInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoopTestInfoTest {

    @Test
    void BasicTesting(){
        for (int round = 0; round<2; round ++) {
            LoopTestInfo lti;

            switch (round){
                case  1 -> {lti = new LoopTestInfo("10", "<", "");}
                default -> {lti = new LoopTestInfo("", ">", "10");}
            }

            assertThrows(AssertionError.class, () -> {
                lti.equalsTestExpression("hallo");
            });
            assertThrows(AssertionError.class, () -> {
                lti.equalsTestExpression(">=hallo");
            });

            assertTrue(lti.equalsTestExpression(">10"));
            assertTrue(lti.equalsTestExpression(">=11"));
            assertFalse(lti.equalsTestExpression(">11"));
            assertFalse(lti.equalsTestExpression(">=9"));
            assertFalse(lti.equalsTestExpression(">=10"));
            assertFalse(lti.equalsTestExpression("==10"));

            assertTrue(lti.equalsTestExpression(">10.0"));
            assertFalse(lti.equalsTestExpression(">=11.0"));
            assertFalse(lti.equalsTestExpression(">=11.0"));
            assertFalse(lti.equalsTestExpression(">11.0"));
            assertFalse(lti.equalsTestExpression(">=9.0"));
            assertFalse(lti.equalsTestExpression(">=10.0"));
            assertFalse(lti.equalsTestExpression("==10.0"));
        }
    }
}
