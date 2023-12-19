package nl.ou.debm.test;

import nl.ou.debm.common.feature1.LoopPatternNode;
import org.junit.jupiter.api.Test;

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
        var rep = LoopPatternNode.getPatternRepo();

        for (var item : rep){
            System.out.println("------------------------------------");
            ShowPattern(item);
        }
    }


}
