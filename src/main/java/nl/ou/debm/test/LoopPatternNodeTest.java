package nl.ou.debm.test;

import nl.ou.debm.common.feature1.ELoopUnrollTypes;
import nl.ou.debm.common.feature1.LoopPatternNode;
import nl.ou.debm.common.feature1.LoopProducer;
import nl.ou.debm.producer.CGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LoopPatternNodeTest {

    private LoopPatternNode s_pat = null;

    @Test
    public void MakePattern(){
        var pat0 = new LoopPatternNode();
        var pat1 = new LoopPatternNode();
        var pat2 = new LoopPatternNode();
        var pat3 = new LoopPatternNode();
        var pat4 = new LoopPatternNode();
        var pat5 = new LoopPatternNode();
        var pat6 = new LoopPatternNode();
        var pat7 = new LoopPatternNode();
        var pat8 = new LoopPatternNode();

        pat7.addChild(pat8);

        pat5.addChild(pat6);
        pat5.addChild(pat7);

        pat1.addChild(pat2);
        pat1.addChild(pat3);
        pat1.addChild(pat4);
        pat1.addChild(pat5);

        pat0.addChild(pat1);

        s_pat = pat0;
    }

    @Test
    public void ShowPattern(){
        MakePattern();
        LoopPatternNode pat = s_pat;

        ShowNodeAndChildren(pat);
    }

    private void ShowPattern(LoopPatternNode p){
        ShowNodeAndChildren(p);
    }

    private void ShowNodeAndChildren(LoopPatternNode p) {
        ShowNodeAndChildren(p, "");
    }
    private void ShowNodeAndChildren(LoopPatternNode p, String strInset){
        System.out.print(strInset + "Node ID: " + p.iGetID() + ", N parents: " + p.iGetNumParents() + ", children: ");
        for (int i=0; i<p.iGetNumChildren(); ++i){
            System.out.print(p.getChild(i).iGetID());
            if (i<(p.iGetNumChildren()-1)){
                System.out.print(", ");
            }
        }
        System.out.println(" (" + p.iGetNumChildren() + ")");
        for (int i=0; i<p.iGetNumChildren(); ++i){
            ShowNodeAndChildren( p.getChild(i), strInset + " ");
        }
    }

    @Test
    public void CopyTest(){
        MakePattern();
        ShowPattern(s_pat);
        var p2 = s_pat;
        ShowPattern(p2);
        var p3 = new LoopPatternNode(p2);
        ShowPattern(p3);
    }

    @Test
    public void TestRepo(){
        var rep2 = LoopPatternNode.getPatternRepo();
        var rep = new ArrayList<>(rep2);
        for (var item : rep){
            System.out.println("------------------------------------");
            ShowPattern(item);
        }
    }

    int m_iTotalNodesChecked = 0;
    int m_iTotalUnrollAssertions = 0;

    @Test
    public void TestLoopAttaching(){
        // can only be run when these are (temporarily) made public:
        //
        //: LoopProducer.getNextLoopPattern();
        //: LoopProducer.AttachLoops();


        var gen = new CGenerator();
        var prod = new LoopProducer(gen);
        m_iTotalNodesChecked = 0;

        for (int c=0; c<10000; ++c){
            var node = prod.getNextLoopPattern();
            prod.AttachLoops(node);
            checkBreakMulti(node);
//            recurseCheckNodesForUnrolling(node, 0);
        }

        System.out.println("Total nodes checked: " + m_iTotalNodesChecked);
        System.out.println("Total unrolled nodes asserted: " + m_iTotalUnrollAssertions);

    }

    private void recurseCheckNodesForUnrolling(LoopPatternNode node, int iNumberOfParents){
        m_iTotalNodesChecked ++;

        // assert that, if this node has an unroll-able loop, it has no children
        if (node.getLoopInfo().getUnrolling() != ELoopUnrollTypes.NO_ATTEMPT){
            assertEquals(0, node.iGetNumChildren());
            m_iTotalUnrollAssertions ++;
        }
        // check children
        for (int c=0; c<node.iGetNumChildren(); ++c){
            recurseCheckNodesForUnrolling(node.getChild(c), iNumberOfParents + 1);
        }
    }

    private void checkBreakMulti(LoopPatternNode node){
        List<List<LoopPatternNode>> lst = new ArrayList<>();
        for (int q=0; q<10; ++q){
            lst.add(new ArrayList<>());
        }
        checkBreakMultiRecurse(lst, node);
        int mode = 0;
        boolean bOneAtTheEnd = false, bOneInTheMiddle = false;
        for (int lev=9; lev>=0; --lev){
            if (mode == 0){
                for (var item : lst.get(lev)){
                    mode = 1;
                    if (item.getLoopInfo().bGetELC_BreakOutNestedLoops()){
                        bOneAtTheEnd = true;
                        lev = -10;
                    }
                }
            }
            else {
                assertFalse(lst.get(lev).isEmpty());
                for (var item : lst.get(lev)){
                    if (item.getLoopInfo().bGetELC_BreakOutNestedLoops()){
                        bOneInTheMiddle = true;
                        lev = -10;
                    }
                }
            }
            if (bOneAtTheEnd) { break; }
            if (bOneInTheMiddle) { break; }
        }
//        System.out.println(bOneInTheMiddle + " - " + bOneAtTheEnd);
        if (!bOneAtTheEnd) {
            if (bOneInTheMiddle){
                ShowNodeAndChildren(node);
                for (int lev = 0; lev < 9; ++lev){
                    for (var item : lst.get(lev)) {
                        System.out.println("lev=" + lev + ", ID=" + item.iGetID() + ", breakout=" + item.getLoopInfo().bGetELC_BreakOutNestedLoops() +
                                ", unrollable=" + item.getLoopInfo().getUnrolling());
                    }
                }
            }
            assertFalse(bOneInTheMiddle);
        }
    }

    private void checkBreakMultiRecurse(List<List<LoopPatternNode>> lst, LoopPatternNode node){
        lst.get(node.iGetNumParents()).add(node);
        for (int c=0; c< node.iGetNumChildren(); ++c){
            checkBreakMultiRecurse(lst, node.getChild(c));
        }
    }

}
