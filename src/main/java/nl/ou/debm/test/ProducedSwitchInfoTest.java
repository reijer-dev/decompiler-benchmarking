package nl.ou.debm.test;

import nl.ou.debm.common.feature5.IndirectionsProducer;
import nl.ou.debm.common.feature5.ProducedSwitchInfo;
import nl.ou.debm.producer.CGenerator;
import nl.ou.debm.producer.Function;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProducedSwitchInfoTest {

    @Test
    public void BasicTesting(){
        List<ProducedSwitchInfo> l = new ArrayList<>();
        ProducedSwitchInfo.getSwitchInfoRepo(l);

        for (var si : l){
            System.out.println(si);
            checkCaseList(si);
        }
    }

    private void checkCaseList(ProducedSwitchInfo si){
        // check case indices for uniqueness
        List<Integer> ci = new ArrayList<>();
        for (var c : si.getCaseInfo()){
            ci.add(c.iCaseIndex);
        }
        Collections.sort(ci);
        for (int i=0; i<(ci.size()-1); i++){
            assertNotEquals(ci.get(i), ci.get(i+1));
        }
        assertTrue(si.getCaseInfo().get(si.getCaseInfo().size()-1).bFillCase);
    }

    @Test
    public void SampleCode(){
        CGenerator gen = new CGenerator();
        IndirectionsProducer ip = new IndirectionsProducer(gen);

        Function f = new Function(gen.getRawDataType(), "JBC");

        for (int i=0; i<100; i++) {
            System.out.println(i + "-------------------------------------------------------------------------------------");
            List<String> o = ip.getNewStatements(0, f, null);

            for (var l : o) {
                System.out.println(l);
            }
        }
    }
}
