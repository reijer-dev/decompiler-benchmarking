package nl.ou.debm.test;

import nl.ou.debm.common.feature5.SwitchInfo;
import nl.ou.debm.producer.CGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SwitchInfoTest {

    @Test
    public void BasicTesting(){
        List<SwitchInfo> l = new ArrayList<>();
        SwitchInfo.getSwitchInfoRepo(l);

        for (var si : l){
            System.out.println(si);
        }
    }
}
