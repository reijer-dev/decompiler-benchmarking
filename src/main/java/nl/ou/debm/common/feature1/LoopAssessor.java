package nl.ou.debm.common.feature1;

import nl.ou.debm.common.IAssessor;
import org.antlr.v4.runtime.tree.ParseTreeWalker;


public class LoopAssessor implements IAssessor  {

    public LoopAssessor(){

    }

    @Override
    public SingleTestResult GetSingleTestResult(CodeInfo ci) {
        var tr = new SingleTestResult();

        tr.dlbLowBound = 0;

        var tree = ci.cparser_dec.compilationUnit();
        var walker = new ParseTreeWalker();
        var listener = new LoopCListener();

        walker.walk(listener, tree);

        System.out.println("Startmarkers: " + listener.m_lngNStartMarkersFound + ", loops: " + listener.m_lngNLoopsFound + ", total markers: " + listener.m_lngNMarkers);
        tr.dblHighBound = listener.m_lngNStartMarkersFound;
        tr.dblActualValue = listener.m_lngNLoopsFound;
        return tr;
    }
}
