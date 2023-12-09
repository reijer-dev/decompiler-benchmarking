package nl.ou.debm.common.feature1;

import java.util.ArrayList;
import java.util.List;

public class LoopPatternNode {
    private final List<LoopPatternNode> m_child = new ArrayList<>();
    private static int s_ID = 0;
    private final int m_ID = s_ID++;

    public int iGetNumChildren() { return m_child.size(); }
    public LoopPatternNode getChild(int iWhichChild) {
        return m_child.get(iWhichChild);
    }
    public void addChild(LoopPatternNode child){
        m_child.add(child);
    }
    public LoopPatternNode addChild(){
        var out = new LoopPatternNode();
        m_child.add(out);
        return out;
    }

    public int iGetID(){
        return m_ID;
    }

    public LoopPatternNode(){
        // no init needed
    }

    public LoopPatternNode(LoopPatternNode rhs){
        // copy constructor
        for (var item : rhs.m_child){
            addChild(new LoopPatternNode(item));
        }
    }
}
