package nl.ou.debm.common.feature1;

import nl.ou.debm.common.IAssessor;
import org.antlr.v4.runtime.tree.ParseTreeWalker;


public class LoopAssessor implements IAssessor  {

    public LoopAssessor(){

    }

    @Override
    public SingleTestResult GetSingleTestResult(CodeInfo ci) {
        var tr = new SingleTestResult();
        tr.dlbLowBound=0;
        tr.dblActualValue=15;
        tr.dblHighBound=15;

        useWalker(ci);
        //useVisitor(ci);


        return tr;
    }

    private void useWalker(CodeInfo ci){
        var tree = ci.cparser_dec.compilationUnit();
        var walker = new ParseTreeWalker();
        var listener = new LoopCListener();

        walker.walk(listener, tree);

        System.out.println("Start markers: " + listener.m_lngNStartMarkersFound + ", loops: " + listener.m_lngNLoopsFound + ", total markers: " + listener.m_lngNMarkers);
        int cnt = 0;
        int c_do = 0, c_for = 0, c_while = 0;
        for (var item : listener.m_loopCommandMap.entrySet()){
            if (item.getValue().equals("do")){c_do++;}
            else if (item.getValue().equals("for")){c_for++;}
            else if (item.getValue().equals("while")){c_while++;}
        }
        System.out.println("do: " + c_do + ", for: " + c_for + ", while " + c_while);
    }

    private void useVisitor(CodeInfo ci){
        var myVisitor = new LoopCVisitor();

        myVisitor.visit(ci.cparser_dec.compilationUnit());
    }
}
