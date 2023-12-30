package nl.ou.debm.producer;

public interface IFunctionBodyInjector {
    void appendStatementAtStart(CGenerator generator, StringBuilder sb, Function function);

    void appendStatementAtEnd(CGenerator generator, StringBuilder sb, Function function);
}
