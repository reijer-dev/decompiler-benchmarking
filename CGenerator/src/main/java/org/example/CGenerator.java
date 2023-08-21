package org.example;

import org.example.feature1.FunctionFeature;
import org.example.feature2.DataStructuresFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CGenerator {

    private final StringBuilder sb = new StringBuilder();
    private final List<IFeature> features = new ArrayList<>();
    private int featureIndex = 0;

    public HashMap<DataType, List<Function>> functionsByReturnType = new HashMap<>();
    //Also store functions in list, to maintain their creation order
    public List<Function> functions = new ArrayList<>();
    public HashMap<DataType, List<Variable>> globalsByType = new HashMap<>();
    public List<Struct> structs = new ArrayList<>();
    public final DataType[] rawDataTypes = new DataType[6];
    Function mainFunction;

    public CGenerator() {
        features.add(new FunctionFeature(this));
        features.add(new DataStructuresFeature(this));
        rawDataTypes[0] = new DataType("short");
        rawDataTypes[1] = new DataType("int");
        rawDataTypes[2] = new DataType("long");
        rawDataTypes[3] = new DataType("char");
        rawDataTypes[4] = new DataType("float");
        rawDataTypes[5] = new DataType("double");
    }

    private void writeStructs() {
        for (var struct : structs) {
            struct.appendCode(sb);
        }
    }

    private void writeGlobalVariables() {
        for (var globalType : globalsByType.keySet()) {
            for (var global : globalsByType.get(globalType)) {
                sb.append(global.getType().getNameForUse());
                sb.append(' ');
                sb.append(global.getName());
                if (global.hasValue()) {
                    sb.append(" = ");
                    sb.append(global.getValue());
                }
                sb.append(';');
                sb.append('\n');
            }
        }
    }

    private void writeFunctions() {
        //Write main function as last
        for (var function : functions) {
            function.appendCode(sb);
        }
        mainFunction.appendCode(sb);
    }

    private void createMainFunction() {
        mainFunction = new Function(rawDataTypes[1], "main");
        while (!allFeaturesSatisfied()) {
            mainFunction.addStatement(getNewStatement());
        }
        mainFunction.addStatement("return 0;");
        if (!functionsByReturnType.containsKey(rawDataTypes[1]))
            functionsByReturnType.put(rawDataTypes[1], new ArrayList<>());
        functionsByReturnType.get(rawDataTypes[1]).add(mainFunction);
    }

    private boolean allFeaturesSatisfied() {
        for (var feature : features) {
            if (!feature.isSatisfied())
                return false;
        }
        return true;
    }

    public String getNewExpression(DataType type, boolean terminating) {
        return getNewExpression(terminating ? Integer.MAX_VALUE : 1, type);
    }

    public String getNewExpression(int currentDepth, DataType type) {
        IFeature currentFeature;
        do {
            if (featureIndex >= features.size())
                featureIndex = 0;
            currentFeature = features.get(featureIndex++);
        } while (!(currentFeature instanceof IExpressionGenerator));
        if (currentFeature instanceof IExpressionGenerator expressionGenerator)
            return expressionGenerator.getNewExpression(currentDepth, type, Math.random() < 0.5 || currentDepth > 5);
        return null;
    }

    public String getNewStatement() {
        //An expression is also a statement
        if (Math.random() < 0.5) {
            return getNewExpression(1, getDataType()) + ";\n";
        }

        IFeature currentFeature;
        do {
            if (featureIndex >= features.size())
                featureIndex = 0;
            currentFeature = features.get(featureIndex++);
        } while (!(currentFeature instanceof IStatementGenerator));
        if (currentFeature instanceof IStatementGenerator statementGenerator)
            return statementGenerator.getNewStatement();
        return null;
    }

    public void generateSourceFile(String path) throws Exception {
        //Check prefixes are unique
        for (var feature : features) {
            if (features.stream().anyMatch(x -> x.getPrefix().equals(feature.getPrefix()) && x.getClass() != feature.getClass()))
                throw new Exception("Prefix " + feature.getPrefix() + " is not unique!");
        }

        createMainFunction();
        writeStructs();
        writeGlobalVariables();
        writeFunctions();
        var writer = new OutputStreamWriter(new FileOutputStream(path));
        writer.write(sb.toString());
        writer.flush();
        writer.close();
    }

    public Function getFunction() {
        return getFunction(null);
    }

    public Function getFunction(DataType type) {
        var createNew = false;
        if (functionsByReturnType.isEmpty())
            createNew = true;
        if (Math.random() < 0.5)
            createNew = true;
        if (type != null && !functionsByReturnType.containsKey(type))
            createNew = true;
        if (createNew) {
            IFeature currentFeature;
            do {
                if (featureIndex >= features.size())
                    featureIndex = 0;
                currentFeature = features.get(featureIndex++);
            } while (!(currentFeature instanceof IFunctionGenerator));
            if (currentFeature instanceof IFunctionGenerator functionGenerator) {
                var newFunction = functionGenerator.getNewFunction(type);
                newFunction.setName(currentFeature.getPrefix() + "_" + newFunction.getName());
                if (!functionsByReturnType.containsKey(newFunction.getType()))
                    functionsByReturnType.put(newFunction.getType(), new ArrayList<>());
                functionsByReturnType.get(newFunction.getType()).add(newFunction);
                functions.add(newFunction);
                return newFunction;
            }
            return null;
        } else {
            if (type == null) {
                var keys = functionsByReturnType.keySet().stream().toList();
                type = keys.get(ThreadLocalRandom.current().nextInt(0, keys.size()));
            }
            return functionsByReturnType.get(type).get(ThreadLocalRandom.current().nextInt(0, functionsByReturnType.get(type).size()));
        }
    }

    public Struct getStruct() {
        if (structs.isEmpty() || Math.random() < 0.5) {
            IFeature currentFeature;
            do {
                if (featureIndex >= features.size())
                    featureIndex = 0;
                currentFeature = features.get(featureIndex++);
            } while (!(currentFeature instanceof IStructGenerator));
            if (currentFeature instanceof IStructGenerator structGenerator) {
                var newStruct = structGenerator.getNewStruct();
                newStruct.prefixName(currentFeature.getPrefix());
                structs.add(newStruct);
                return newStruct;
            }
            return null;
        } else {
            return structs.get(ThreadLocalRandom.current().nextInt(0, structs.size()));
        }
    }

    public DataType getRawDataType() {
        return rawDataTypes[ThreadLocalRandom.current().nextInt(0, rawDataTypes.length)];
    }

    public DataType getDataType() {
        if (Math.random() < 0.5)
            return getStruct();
        return getRawDataType();
    }

    public Variable getGlobal(DataType type) {
        var createNew = false;
        if (globalsByType.isEmpty())
            createNew = true;
        if (Math.random() < 0.5)
            createNew = true;
        if (type != null && !globalsByType.containsKey(type))
            createNew = true;
        if (createNew) {
            IFeature currentFeature;
            do {
                if (featureIndex >= features.size())
                    featureIndex = 0;
                currentFeature = features.get(featureIndex++);
            } while (!(currentFeature instanceof IStructGenerator));
            if (currentFeature instanceof IGlobalVariableGenerator globalVariableGenerator) {
                var newGlobalVariable = globalVariableGenerator.getNewGlobalVariable(type);
                newGlobalVariable.setName(currentFeature.getPrefix() + "_" + newGlobalVariable.getName());
                if (!globalsByType.containsKey(newGlobalVariable.getType()))
                    globalsByType.put(newGlobalVariable.getType(), new ArrayList<>());
                globalsByType.get(newGlobalVariable.getType()).add(newGlobalVariable);
                return newGlobalVariable;
            }
            return null;
        } else {
            if (type == null) {
                var keys = globalsByType.keySet().stream().toList();
                type = keys.get(ThreadLocalRandom.current().nextInt(0, keys.size()));
            }
            return globalsByType.get(type).get(ThreadLocalRandom.current().nextInt(0, globalsByType.get(type).size()));
        }
    }
}
