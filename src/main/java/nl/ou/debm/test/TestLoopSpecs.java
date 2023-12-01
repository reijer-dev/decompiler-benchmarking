package nl.ou.debm.test;

import nl.ou.debm.common.Misc;
import nl.ou.debm.common.feature1.*;
import nl.ou.debm.producer.CGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static nl.ou.debm.common.feature1.LoopInfo.strToStringHeader;

public class TestLoopSpecs {

    @org.junit.jupiter.api.Test
    void TestLoopRepoBuild() {
        var li = new ArrayList<LoopInfo>();
        LoopInfo.FillLoopRepo(li);
        System.out.println("len = " + li.size());
        System.out.println("#####: " + strToStringHeader());
        int cnt = 0;
        for (var q : li){
            if (q.getLoopVar().eUpdateType == ELoopVarUpdateTypes.INCREASE_OTHER) {
                System.out.print(Misc.strGetNumberWithPrefixZeros(cnt, 5) + ": ");
                System.out.println(q);
            }
            cnt++;
        }
    }

    @Test
    void TestSingleLoopInfo(){
        // setup
        var li = new ArrayList<LoopInfo>();
        LoopInfo.FillLoopRepo(li, false);
        var prod = new CGenerator();
        var f1 = new LoopProducer(prod);
        var output = new ArrayList<String>();

        System.out.println(strToStringHeader());

        // select loop
        LoopInfo loop;
        for (var l : li){
            if (
                    (l.getLoopCommand() == ELoopCommands.FOR) &&
                    (l.getLoopFinitude() == ELoopFinitude.PFL) &&
//                    (l.getLoopExpressions().bTestAvailable()) &&
//                    (l.getLoopVar().eTestType == ELoopVarTestOperators.NON_EQUAL) &&
//                    (l.bGetELC_UseBreak() == true) &&


                    (true)
            ){
                System.out.println(l);
                loop = l;

                f1.getLoopStatements(output, loop);
                for (var line : output){
                    System.out.println(line);
                }

                break;
            }
        }

    }
}