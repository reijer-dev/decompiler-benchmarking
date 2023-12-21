package nl.ou.debm.common.feature1;

import java.util.ArrayList;
import java.util.List;

/**
 * class LoopPatternNode<br>
 * This class helps in making loop patterns.<br>
 * The basic pattern is a single loop, but loops may be nested.<br>
 * A loop can have a nested branch, which is considered a child in a tree structure. A loop may have more than one
 * direct child and children (nested loops) may also have nested loops.<br>
 * <br>
 * example:<br>
 * for (int x=0;x<10;++x){for (int y=0; y<20;++y) {...} for (int y2=0;y2<15;++y2) { for (int z=0; z<8; ++z) {...}} } <br>
 * the x-loop has two children (y, y2) and one grand child (z) and no parent<br>
 * the y-loop has no children and a parent (x)<br>
 * the y2-loop has one child (z) and a parent (x)<br>
 * the z-loop had no children and a parent (and a grandparent, even)<br>
 *
 * The instances of the class are the nodes in a tree. A static function provides a set of root nodes, each
 * root node depicting a different pattern.<br>
 * The ID's in the instanced are used differentiate them in output, enabling to test the lot.
 */

public class LoopPatternNode {
    //////////////////////
    // Instance attributes
    //////////////////////
    private final List<LoopPatternNode> m_child = new ArrayList<>();        // list of children
    private final int m_ID = s_ID++;                                        // unique ID, created upon instantiation
    private LoopPatternNode m_parent = null;                                // parent loop, null if none
    private int m_iNParents = 0;                                            // number of parent loops
    private LoopInfo m_loopInfo = null;                                     // loop definition attached to this node

    ////////////////////
    // static attributes
    ////////////////////
    private static int s_ID = 0;                                            // last used ID, to keep track of all instances
    private final static List<LoopPatternNode> s_pattern = new ArrayList<>();// set of root nodes

    ///////////////
    // construction
    ///////////////
    public LoopPatternNode(){
        // empty default constructor, as defaults are set in attribute definitions
    }

    public LoopPatternNode(LoopPatternNode rhs){
        // copy constructor
        m_parent = null;        // assume copy has no parent
        for (var item : rhs.m_child){
            addChild(new LoopPatternNode(item));    // the copied child initiates as no parent, but... parent is set in addChild
        }
        m_loopInfo = new LoopInfo(rhs.m_loopInfo);
        m_iNParents = 0;
    }

    /////////////////
    // normal methods
    /////////////////

    /**
     * Add a previously created child node to this node. The child node's parent is set to this node.
     * @param child     Child node to be added. If null, nothing happens.
     * @return          the added node object = child-object (so, may be null if null was passed)
     */
    public LoopPatternNode addChild(LoopPatternNode child){
        if (child!=null) {
            m_child.add(child);
            child.m_parent = this;
            IncreaseParentCount(child);
        }
        return child;
    }

    /**
     * Increase the number of parents for this node and all its descendants.
     * @param node  root node to be affected
     */
    private void IncreaseParentCount(LoopPatternNode node){
        node.m_iNParents++;
        for (var item : node.m_child){
            IncreaseParentCount(item);
        }
    }

    /**
     * Create and add a child node
     * @return  the newly added node object
     */
    public LoopPatternNode addChild(){
        var out = new LoopPatternNode();
        return addChild(out);
    }

    /**
     * Get number of child nodes
     * @return  number of child nodes
     */
    public int iGetNumChildren() { return m_child.size(); }

    public int iGetNumParents() { return m_iNParents;}

    /**
     * Get child node object
     * @param iWhichChild   range 0...number of children-1
     * @return              the child object, or null if out of bounds
     */
    public LoopPatternNode getChild(int iWhichChild) {
        try {
            return m_child.get(iWhichChild);
        }
        catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    /**
     * Attach a LoopInfo object to node
     * @param loopInfo  loop definition to be attached
     */
    public void setLoopInfo(LoopInfo loopInfo){
        m_loopInfo = loopInfo;
    }

    /**
     * get LoopInfo object attached to this node
     * @return attached LoopInfo
     */
    public LoopInfo getLoopInfo(){
        return m_loopInfo;
    }

    /**
     * determine whether or not node has parents
     * @return  true = has parent, false = has no parent (=root node)
     */
    public boolean bHasParent(){
        return m_parent != null;
    }

    /**
     * get node ID
     * @return  ID (1...)
     */
    public int iGetID(){
        return m_ID;
    }

    /////////////////
    // static methods
    /////////////////

    /**
     * get a set of root level nodes, effectively returning a set of patterns
     * @return  list of root level nodes
     */
    public static List<LoopPatternNode> getPatternRepo(){
        // check whether repo is filled
        if (s_pattern.isEmpty()){
            InitPatternArray();
        }
        // make deep copy
        var out = new ArrayList<LoopPatternNode>();
        for (var item : s_pattern){
            out.add(new LoopPatternNode(item));
        }
        return out;
    }

    private static void InitPatternArray(){
        // make different patterns


        // single loop
        //////////////
        //
        // *
        //
        var n0 = new LoopPatternNode();
        s_pattern.add(n0);

        // nested loops, 1 to 4 deep
        ////////////////////////////
        //
        // *
        // |
        // *
        var n1 = new LoopPatternNode();
        n1.addChild();
        s_pattern.add(n1);

        // *
        // |
        // *
        // |
        // *
        var n2 = new LoopPatternNode();
        n2.addChild(new LoopPatternNode(n1));
        s_pattern.add(n2);

        // *
        // |
        // *
        // |
        // *
        // |
        // *
        var n3 = new LoopPatternNode();
        n3.addChild(new LoopPatternNode(n2));
        s_pattern.add(n3);

        // *
        // |
        // *
        // |
        // *
        // |
        // *
        // |
        // *
        var n4 = new LoopPatternNode();
        n4.addChild(new LoopPatternNode(n3));
        s_pattern.add(n4);

        // nested loops, twins.
        ///////////////////////
        // twins have no children
        //
        // *
        // +---+
        // *   *
        var q = new LoopPatternNode(n1);
        q.addChild();
        s_pattern.add(q);

        // twin one has child
        //
        // *
        // +---+
        // *   *
        // |
        // *
        q = new LoopPatternNode(n0);
        q.addChild(new LoopPatternNode(n1));
        q.addChild(new LoopPatternNode(n0));
        s_pattern.add(q);

        // twin two has child
        //
        // *
        // +---+
        // *   *
        //     |
        //     *
        q = new LoopPatternNode(n0);
        q.addChild(new LoopPatternNode(n0));
        q.addChild(new LoopPatternNode(n1));
        s_pattern.add(q);

        // both twins have children
        //
        // *
        // +---+
        // *   *
        // |   |
        // *   *
        q = new LoopPatternNode(n0);
        q.addChild(new LoopPatternNode(n1));
        q.addChild(new LoopPatternNode(n1));
        s_pattern.add(q);

        // three children.
        //////////////////
        // three children, no grandchildren
        //
        // *
        // +---+---+
        // *   *   *
        q = new LoopPatternNode(n0);
        q.addChild(new LoopPatternNode(n0));
        q.addChild(new LoopPatternNode(n0));
        q.addChild(new LoopPatternNode(n0));
        s_pattern.add(q);

        // three children, all have grandchildren (1, 2 and 3)
        // *
        // +---+---+
        // *   *   *
        // |   |   |
        // *   *   *
        //     |   |
        //     *   *
        //         |
        //         *
        q = new LoopPatternNode(n0);
        q.addChild(new LoopPatternNode(n1));
        q.addChild(new LoopPatternNode(n2));
        q.addChild(new LoopPatternNode(n3));
        s_pattern.add(q);

        // binary tree
        // *
        // +-------+
        // *       *
        // +---+   +---+
        // *   *   *   *
        q = new LoopPatternNode(n0);
        AddTwoChildren(q, 2);       // do recursively
        s_pattern.add(q);
    }

    /**
     * add two children to a node and recurse if necessary
     * @param parent        parent node
     * @param level         recursion level, only recurse when >0
     */
    private static void AddTwoChildren(LoopPatternNode parent, int level){
        var n0 = parent.addChild();
        var n1 = parent.addChild();
        if (level > 0) {
            AddTwoChildren(n0, level-1);
            AddTwoChildren(n1, level-1);
        }
    }




}
