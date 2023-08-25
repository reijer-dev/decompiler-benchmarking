package org.example.feature2;

import org.example.*;

public class DataStructuresFeature implements IFeature, IStatementGenerator, IStructGenerator, IGlobalVariableGenerator {
    private int structCount = 0;
    private int variableCount = 0;
    final CGenerator generator;

    public DataStructuresFeature(CGenerator generator){
        this.generator = generator;
    }

    @Override
    public String getNewStatement() {
        return "int DS_" + variableCount++ + " = 3;\n";
    }

    @Override
    public boolean isSatisfied() {
        return true;
    }

    @Override
    public String getPrefix(){ return EFeaturePrefix.DATASTRUCTUREFEATURE.toString(); }

    @Override
    public Struct getNewStruct() {
        var result = new Struct("struct_" + structCount++);
        result.addProperty(new Variable("a", generator.getDataType()));
        return result;
    }

    @Override
    public Variable getNewGlobalVariable(DataType type) {
        if(type == null)
            type = generator.getDataType();
        return new Variable("var_" + variableCount++, type);
    }
}
