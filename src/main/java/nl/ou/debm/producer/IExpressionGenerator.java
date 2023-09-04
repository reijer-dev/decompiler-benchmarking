package nl.ou.debm.producer;

public interface IExpressionGenerator {
    String getNewExpression(int currentDepth, DataType type, boolean terminating);
}
