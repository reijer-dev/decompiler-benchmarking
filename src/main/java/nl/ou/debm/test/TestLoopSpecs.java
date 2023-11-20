package nl.ou.debm.test;

import nl.ou.debm.common.Misc;
import nl.ou.debm.common.feature1.LoopInfo;

import java.util.ArrayList;

import static nl.ou.debm.common.feature1.LoopInfo.strToStringHeader;

public class TestLoopSpecs {

    @org.junit.jupiter.api.Test
    void strGetNumberWithPrefixZerosTest() {
        var li = new ArrayList<LoopInfo>();
        LoopInfo.FillLoopRepo(li);
        System.out.println("len = " + li.size());
        System.out.println(strToStringHeader());
        int cnt = 0;
        for (var q : li){
            System.out.print(Misc.strGetNumberWithPrefixZeros(cnt++,5) + ": ");
            System.out.println(q);
        }
    }
}