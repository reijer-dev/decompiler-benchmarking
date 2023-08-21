package org.example;

public interface IExpressionGenerator {
    String getNewExpression(int currentDepth, DataType type, boolean terminating);
}
