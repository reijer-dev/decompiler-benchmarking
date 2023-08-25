package org.example.feature1;
import org.example.*;


public class ControlFlowFeature implements  IFeature, IExpressionGenerator {
    @Override
    public String getNewExpression(int currentDepth, DataType type, boolean terminating) {
        return null;
    }

    @Override
    public boolean isSatisfied() {
        return false;
    }

    @Override
    public String getPrefix() {
        return EFeaturePrefix.CONTROLFLOWFEATURE.toString();
    }
}
