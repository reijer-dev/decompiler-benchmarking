package nl.ou.debm.common.feature2;

import nl.ou.debm.producer.*;

import java.util.List;

public class DataStructuresFeature implements IFeature, IStatementGenerator, IStructGenerator, IGlobalVariableGenerator {
    private int structCount = 0;
    private int variableCount = 0;
    final CGenerator generator;

    public DataStructuresFeature(CGenerator generator){
        this.generator = generator;
    }
    public DataStructuresFeature(){
        this.generator = null;
    }

    @Override
    public String getNewStatement() {
        return "int " + getPrefix() + "_" + variableCount++ + " = 3;\n";
    }

    @Override
    public boolean isSatisfied() {
        return true;
    }

    @Override
    public String getPrefix(){ return EFeaturePrefix.DATASTRUCTUREFEATURE.toString(); }

    @Override
    public List<String> getIncludes() {
        return null;
    }

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
