package nl.ou.debm.test;

import nl.ou.debm.common.SimpleTree;
import org.junit.jupiter.api.Test;

public class SimpleTreeTest {

    @Test
    public void BasicTest(){
        var myTree = new SimpleTree<Integer>();

        var rootNode = myTree.getRoot();
        var child1 = rootNode.addChild(14);
        var child2 = rootNode.addChild(23);
        var child11 = child1.addChild(141);
        var child12 = child1.addChild(142);
        var child21 = child2.addChild(231);
        var child211 = child21.addChild(2311);

        myTree.dumpTree();

        System.out.println("-------------");

        var iter = myTree.getNode(2311);

        while (iter!=null){
            System.out.println(iter.data);
            iter = iter.parent;
        }

        System.out.println("-------------");

        System.out.println(myTree.values());


        System.out.println("-------------");

        myTree.clear();
        myTree.dumpTree();
    }
}
