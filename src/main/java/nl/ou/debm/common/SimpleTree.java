package nl.ou.debm.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Very simple generalized tree
 * @param <T> object type to be stored in the tree
 */
public class SimpleTree <T>{
    /**
     * tree node
     * @param <T> object type to be stored in the tree
     */
    public static class SimpleTreeNode<T>{
        /** node data */                            public T data = null;
        /** parent node */                          public SimpleTreeNode<T> parent = null;
        /** node children */                        public final List<SimpleTreeNode<T>> children = new ArrayList<>();

        // construction
        public SimpleTreeNode(){};
        public SimpleTreeNode(T data){
            this.data=data;
        }
        public SimpleTreeNode(SimpleTreeNode<T> parent, T data){
            this.data=data;
            parent.addChild(this);
        }

        /**
         * add a node to a tree
         * @param newChild child to be added
         * @return the added child node
         */
        public SimpleTreeNode<T> addChild(SimpleTreeNode<T> newChild){
            newChild.parent=this;
            this.children.add(newChild);
            return newChild;
        }

        /**
         * add a data object to a tree; the data node is created automatically
         * @param newDataChild child data to be added
         * @return the added child node
         */
        public SimpleTreeNode<T> addChild(T newDataChild){
            return addChild(new SimpleTreeNode<T>(newDataChild));
        }
    }

    // object attributes
    /** root node */                    final private SimpleTreeNode<T> m_rootNode = new SimpleTreeNode<>();

    public SimpleTreeNode<T> getRoot() {
        return m_rootNode;
    }

    /**
     * dump node contents to stdOut
     */
    public void dumpTree(){
        dumpTree(m_rootNode, "");
    }

    private void dumpTree(SimpleTreeNode<T> root, String strIntend){
        System.out.println(strIntend + root.data);
        for (var ch : root.children){
            dumpTree(ch, strIntend + "  ");
        }
    }

    /**
     * look for node containing the data
     * @param data data to lok for
     * @return node containing the data
     */
    public SimpleTreeNode<T> getNode(T data){
        return getNode(m_rootNode, data);
    }

    private SimpleTreeNode<T> getNode(SimpleTreeNode<T> parent, T data){
        if ((parent.data!=null) && (parent.data.equals(data))){
            return parent;
        }
        for (var ch : parent.children){
            var out = getNode(ch, data);
            if (out!=null){
                return out;
            }
        }
        return null;
    }

    /**
     * clear the tree
     */
    public void clear(){
        clearNode(m_rootNode);
    }

    private void clearNode(SimpleTreeNode<T> node){
        node.data = null;
        for (var ch : node.children){
            clearNode(ch);
        }
        node.children.clear();
    }

    /**
     * get all the values (including nulls if they exist)
     * @return list of all data values
     */
    public List<T> values(){
        List<T> out = new ArrayList<>();
        addNodeDataToOutput(out, m_rootNode, true);
        return out;
    }

    /**
     * get all the values (no nulls)
     * @return list of all data values (no nulls)
     */
    public List<T> valuesNoNull(){
        List<T> out = new ArrayList<>();
        addNodeDataToOutput(out, m_rootNode, false);
        return out;
    }

    private void addNodeDataToOutput(List<T> out, SimpleTreeNode<T> node, boolean bAllowNull){
        if (bAllowNull || node.data != null){
            out.add(node.data);
        }
        for (var ch : node.children){
            addNodeDataToOutput(out, ch, bAllowNull);
        }
    }
}
