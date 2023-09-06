package nl.ou.debm.producer.feature1;
import nl.ou.debm.producer.*;


public class ControlFlowFeature implements  IFeature, IExpressionGenerator {

    // attributes
    // ----------

    // what work has been done jet?
    private int m_iNForLoops = 0;               // count number of for loops introduced
    private int m_iDeepestNestingLevel = 0;     // deepest number of nested for loops

    final CGenerator generator;     // see note in IFeature.java

    // construction
    public ControlFlowFeature(CGenerator generator){
        this.generator = generator;
    }


    @Override
    public String getNewExpression(int currentDepth, DataType type, boolean terminating) {
        return null;
    }

    @Override
    public boolean isSatisfied() {
        return (m_iNForLoops >= 10) ;
    }

    @Override
    public String getPrefix() {
        return EFeaturePrefix.CONTROLFLOWFEATURE.toString();
    }
}
