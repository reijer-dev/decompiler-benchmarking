package nl.ou.debm.common.task;

import java.util.ArrayList;
import java.util.List;

public class SimpleTree <T>{
    public static class SimpleTreeNode<T>{
        public T data = null;
        public SimpleTreeNode<T> parent = null;
        public final List<SimpleTreeNode<T>> children = new ArrayList<>();
        public SimpleTreeNode(T data){
            this.data=data;
        }
        public SimpleTreeNode(SimpleTreeNode<T> parent, T data){
            this.data=data;
            parent.addChild(this);
        }

        public SimpleTreeNode(){};

        public SimpleTreeNode<T> addChild(SimpleTreeNode<T> newChild){
            newChild.parent=this;
            this.children.add(newChild);
            return newChild;
        }

        public SimpleTreeNode<T> addChild(T newDataChild){
            return addChild(new SimpleTreeNode<T>(newDataChild));
        }
    }

    final private SimpleTreeNode<T> m_rootNode = new SimpleTreeNode<>();

    public SimpleTreeNode<T> getRoot() {
        return m_rootNode;
    }

    public void dumpTree(){
        dumpTree(m_rootNode, "");
    }

    private void dumpTree(SimpleTreeNode<T> root, String strIntend){
        System.out.println(strIntend + root.data);
        for (var ch : root.children){
            dumpTree(ch, strIntend + "  ");
        }
    }

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
}
