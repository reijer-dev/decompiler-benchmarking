package nl.ou.debm.test;

import nl.ou.debm.common.feature1.ControlFlowProducer;
import nl.ou.debm.producer.CGenerator;

public class TestLoopSpecs {

    @org.junit.jupiter.api.Test
    void strGetNumberWithPrefixZerosTest() {
        var g = new CGenerator();
        var q = new ControlFlowProducer(g);
        q.ShowMeLoops();
    }
}