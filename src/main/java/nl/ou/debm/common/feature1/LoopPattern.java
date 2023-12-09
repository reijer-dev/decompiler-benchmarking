package nl.ou.debm.common.feature1;

import java.util.ArrayList;
import java.util.List;

public class LoopPattern {

    private final static List<LoopPatternNode> s_pattern = new ArrayList<>();

    public static List<LoopPatternNode> getPatternRepo(){
        if (s_pattern.isEmpty()){
            InitPatternArray();
        }
        return new ArrayList<>(s_pattern);
    }

    private static void InitPatternArray(){
        // single loop
        //////////////
        var n0 = new LoopPatternNode();
        s_pattern.add(n0);

        // nested loops, 1 to 4 deep
        ////////////////////////////
        var n1 = new LoopPatternNode();
        n1.addChild();
        s_pattern.add(n1);

        var n2 = new LoopPatternNode();
        n2.addChild(new LoopPatternNode(n1));
        s_pattern.add(n2);

        var n3 = new LoopPatternNode();
        n3.addChild(new LoopPatternNode(n2));
        s_pattern.add(n3);

        var n4 = new LoopPatternNode();
        n4.addChild(new LoopPatternNode(n3));
        s_pattern.add(n4);

        // nested loops, twins
        //////////////////////
        // twins have no children
        var q = new LoopPatternNode(n1);
        q.addChild();
        s_pattern.add(q);

        // twin one has child
        q = new LoopPatternNode(n0);
        q.addChild(new LoopPatternNode(n1));
        q.addChild(new LoopPatternNode(n0));
        s_pattern.add(q);

        // twin two has child
        q = new LoopPatternNode(n0);
        q.addChild(new LoopPatternNode(n0));
        q.addChild(new LoopPatternNode(n1));
        s_pattern.add(q);

        // both twins have children
        q = new LoopPatternNode(n0);
        q.addChild(new LoopPatternNode(n1));
        q.addChild(new LoopPatternNode(n1));
        s_pattern.add(q);

        // three children
        /////////////////
        // three children, no grandchildren
        q = new LoopPatternNode(n0);
        q.addChild(new LoopPatternNode(n0));
        q.addChild(new LoopPatternNode(n0));
        q.addChild(new LoopPatternNode(n0));
        s_pattern.add(q);

        // three children, all have grandchildren (1, 2 and 3)
        q = new LoopPatternNode(n0);
        q.addChild(new LoopPatternNode(n1));
        q.addChild(new LoopPatternNode(n2));
        q.addChild(new LoopPatternNode(n3));
        s_pattern.add(q);

        // binary tree
        q = new LoopPatternNode(n0);
        AddTwoChildren(q, 2);
        s_pattern.add(q);
    }

    private static void AddTwoChildren(LoopPatternNode parent, int level){
        var n0 = parent.addChild();
        var n1 = parent.addChild();
        if (level > 0) {
            AddTwoChildren(n0, level-1);
            AddTwoChildren(n1, level-1);
        }
    }
}
