package nl.ou.debm.test;

import nl.ou.debm.common.Misc;
import nl.ou.debm.common.feature1.*;
import nl.ou.debm.producer.CGenerator;
import nl.ou.debm.producer.DataType;
import nl.ou.debm.producer.Function;
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
            //if (q.getLoopVar().eUpdateType == ELoopVarUpdateTypes.INCREASE_OTHER) {
                System.out.print(Misc.strGetNumberWithPrefixZeros(cnt, 5) + ": ");
                System.out.println(q);
            //}
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
        var f = new Function(new DataType("int"), "main");

        System.out.println(strToStringHeader());


        // select loop
        LoopInfo loop;
        for (var l : li){
            if (
                    (l.getLoopCommand() == ELoopCommands.FOR) &&
                    (l.getLoopFinitude() == ELoopFinitude.PFL) &&
                    (l.getLoopExpressions() == ELoopExpressions.ALL ) &&
                    (l.bGetELC_UseBreak()) &&
                    (l.bGetELC_UseReturn()) &&
                    (l.bGetELC_UseExit()) &&
                    (l.bGetELC_UseGotoDirectlyAfterThisLoop()) &&

                    (l.bGetILC_UseContinue()) &&
                    (l.bGetILC_UseGotoBegin()) &&
                    (l.bGetILC_UseGotoEnd()) &&
//                    (l.getLoopVar().eUpdateType == ELoopVarUpdateTypes.DECREASE_BY_INPUT ) &&
//                    (l.getLoopExpressions().bTestAvailable()) &&
//                    (l.getLoopVar().eTestType == ELoopVarTestOperators.NON_EQUAL) &&
//                    (l.bGetELC_UseBreak() == true) &&


                    (true)
            ){
                System.out.println(l);
                loop = l;
                var pattern = LoopPatternNode.getPatternRepo().get(LoopPatternNode.getPatternRepo().size()-2);
                pattern.setLoopInfo(loop);

                f1.getLoopStatements(1, f, output, pattern);
                for (var line : output){
                    System.out.println(line);
                }

                break;
            }
        }

    }
}