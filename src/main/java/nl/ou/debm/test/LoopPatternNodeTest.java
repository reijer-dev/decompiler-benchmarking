package nl.ou.debm.test;

import nl.ou.debm.common.feature1.ELoopUnrollTypes;
import nl.ou.debm.common.feature1.LoopInfo;
import nl.ou.debm.common.feature1.LoopPatternNode;
import nl.ou.debm.common.feature1.LoopProducer;
import nl.ou.debm.producer.CGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LoopPatternNodeTest {

    private LoopPatternNode s_pat = null;

    public void MakePattern(){
        List<LoopInfo> li = new ArrayList<>();
        LoopInfo.FillLoopRepo(li);
        int idx=0;
        var pat0 = new LoopPatternNode(li.get(idx++));
        var pat1 = new LoopPatternNode(li.get(idx++));
        var pat2 = new LoopPatternNode(li.get(idx++));
        var pat3 = new LoopPatternNode(li.get(idx++));
        var pat4 = new LoopPatternNode(li.get(idx++));
        var pat5 = new LoopPatternNode(li.get(idx++));
        var pat6 = new LoopPatternNode(li.get(idx++));
        var pat7 = new LoopPatternNode(li.get(idx++));
        var pat8 = new LoopPatternNode(li.get(idx++));

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

    private void ShowPattern(LoopPatternNode p){
        ShowNodeAndChildren(p);
    }

    private void ShowNodeAndChildren(LoopPatternNode p) {
        ShowNodeAndChildren(p, "");
    }
    private void ShowNodeAndChildren(LoopPatternNode p, String strInset){
        System.out.print(strInset + "Node ID: " + ", N parents: " + p.iGetNumParents() + ", children: ");
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
    public void BasicTesting(){
        MakePattern();
        assertUniqueIDs(s_pat, new ArrayList<Integer>());
        ShowPattern(s_pat);
        var p3 = new LoopPatternNode(s_pat);
        assertUniqueIDs(p3, new ArrayList<Integer>());
        assertGoodCopy(s_pat, p3);
        ShowPattern(p3);

    }

    void assertUniqueIDs(LoopPatternNode n, List<Integer> list){
        assertFalse(list.contains(n.iGetID()));
        list.add(n.iGetID());
        for (int i=0; i< n.iGetNumChildren(); i++){
            assertUniqueIDs(n.getChild(i), list);
        }
    }

    void assertGoodCopy(LoopPatternNode n1, LoopPatternNode n2){
        assertEquals(n1.iGetNumParents(), n2.iGetNumParents());
        assertEquals(n1.iGetNumChildren(), n2.iGetNumChildren());
        if (n1.getLoopInfo() == null){
            assertNull(n2.getLoopInfo());
        }
        else{
            assertNotNull(n2.getLoopInfo());
            assertEquals(n1.getLoopInfo().toString(), n2.getLoopInfo().toString());
        }
        for (int i = 0; i< n1.iGetNumChildren(); ++i){
            assertGoodCopy(n1.getChild(i), n2.getChild(i));
        }
    }

    int m_iTotalNodesChecked = 0;
    int m_iTotalUnrollAssertions = 0;

    @Test
    public void TestLoopAttaching(){
        var prod = new LoopProducer(new CGenerator());
        m_iTotalNodesChecked = 0;

        for (int c=0; c<100000; ++c){
            var node = prod.getNextFilledLoopPattern();
            checkBreakMulti(node);
            recurseCheckNodesForUnrolling(node, 0);
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
