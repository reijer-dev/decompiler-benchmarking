package nl.ou.debm.test;

import nl.ou.debm.common.Misc;
import nl.ou.debm.common.feature1.ELoopUnrollTypes;
import nl.ou.debm.common.feature1.LoopInfo;
import nl.ou.debm.common.feature1.LoopPatternNode;
import nl.ou.debm.common.feature1.LoopProducer;
import nl.ou.debm.producer.CGenerator;
import nl.ou.debm.producer.DataType;
import nl.ou.debm.producer.Function;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static nl.ou.debm.common.feature1.LoopInfo.GetSingleTestLoopInfo;
import static nl.ou.debm.common.feature1.LoopInfo.strToStringHeader;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestLoopSpecs {

    @org.junit.jupiter.api.Test
    void TestLoopRepoBuild() {
        var li = new ArrayList<LoopInfo>();
        LoopInfo.FillLoopRepo(li);
        System.out.println("len = " + li.size());
        System.out.println("#####: " + strToStringHeader());
        int cnt = 0;
        for (var q : li){
            if (q.getUnrolling() == ELoopUnrollTypes.NO_ATTEMPT) {
                System.out.print(Misc.strGetAbsNumberWithPrefixZeros(cnt, 5) + ": ");
                System.out.print(q);
                if (q.getLoopVar()==null){
                    System.out.println();
                }
                else {
                    System.out.print(" I:" + q.getLoopVar().strInitExpression + "  ");
                    System.out.print(" U:" + q.getLoopVar().strUpdateExpression + "  ");
                    System.out.println(" T: " + q.getLoopVar().strTestExpression);
                }
            }
            cnt++;
        }

        // check ID uniqueness
        Map<Long, Boolean> map = new HashMap<>();
        for (var item:li){
            assertFalse(map.containsKey(item.lngGetLoopID()));
            map.put(item.lngGetLoopID(), true);
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
        var f = new Function(DataType.make_primitive("int", "0"), "main");

        System.out.println(strToStringHeader());


        // select loop
        LoopInfo loop;
        for (var l : li){
            if (
//                    (l.getLoopCommand() == ELoopCommands.FOR) &&
//                    (l.getLoopFinitude() == ELoopFinitude.PFL) &&
//                    (l.getLoopExpressions() == ELoopExpressions.ALL ) &&
//                    (l.bGetELC_UseBreak()) &&
//                    (l.bGetELC_UseReturn()) &&
//                    (l.bGetELC_UseExit()) &&
//                    (l.bGetELC_UseGotoDirectlyAfterThisLoop()) &&
//
//                    (l.bGetILC_UseContinue()) &&
//                    (l.bGetILC_UseGotoBegin()) &&
//                    (l.bGetILC_UseGotoEnd()) &&
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

    @Test
    public void ProduceLoopForPaper(){
        // setup
        var li = new ArrayList<LoopInfo>();
        LoopInfo.FillLoopRepo(li, false);
        var prod = new CGenerator();
        var f1 = new LoopProducer(prod);
        var output = new ArrayList<String>();
        var f = new Function(DataType.make_primitive("int", "0"), "main");

        var pattern = new LoopPatternNode();
        var p2 = new LoopPatternNode();
        pattern.addChild(p2);
        p2.setLoopInfo(GetSingleTestLoopInfo(0));
        pattern.setLoopInfo(GetSingleTestLoopInfo(1));

        f1.getLoopStatements(1, f, output, pattern);
        for (var line : output){
            System.out.println(line);
        }

    }


}