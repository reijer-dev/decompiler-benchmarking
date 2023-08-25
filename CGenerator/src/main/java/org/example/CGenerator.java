package org.example;

import org.example.feature1.ControlFlowFeature;
import org.example.feature3.FunctionFeature;
import org.example.feature2.DataStructuresFeature;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// TODO: implement proper procedures (which in C are functions returning void)


public class CGenerator {

    private final double CHANCE_OF_EXPRESSION_AS_STATEMENT = 0.5;

    private final StringBuilder sb = new StringBuilder();
    private final List<IFeature> features = new ArrayList<>();
    private int featureIndex = 0;   // make sure that all feature classes are used throughout code building

    public HashMap<DataType, List<Function>> functionsByReturnType = new HashMap<>();
    //Also store functions in list, to maintain their creation order
    public List<Function> functions = new ArrayList<>();
    public HashMap<DataType, List<Variable>> globalsByType = new HashMap<>();
    public List<Struct> structs = new ArrayList<>();
    public final DataType[] rawDataTypes = new DataType[6];
    Function mainFunction;

    public CGenerator() {
        // constructor
        // -----------

        // fill array of feature-objects
        features.add(new FunctionFeature(this));
        //features.add(new DataStructuresFeature(this));
        features.add(new ControlFlowFeature(this));

        // fill array of raw data types
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

    /**
     * Create main function for the code. This function will add statements to
     * the main function as long as it takes to satisfy all feature requirements.
     */
    private void createMainFunction() {
        // make new function object, int main()
        mainFunction = new Function(rawDataTypes[1], "main");

        // Because of the recursive nature of getNewStatement,
        // the main function may, in the end, turn out to be very short:
        // it may even have only one line!
        while (!allFeaturesSatisfied()) {
            mainFunction.addStatement(getNewStatement());
        }

        // Use standard exit code as a last statement
        mainFunction.addStatement("return 0;");

        // Add the function to the collection of functions, sorted by return type
        addFunctionToFunctionsByReturnType(mainFunction);
        if (!functionsByReturnType.containsKey(rawDataTypes[1]))
            functionsByReturnType.put(rawDataTypes[1], new ArrayList<>());
        functionsByReturnType.get(rawDataTypes[1]).add(mainFunction);
    }

    /**
     * This adds a function to the return-type-sorted list of functions.
     * @param function  The function to be added.
     */
    private void addFunctionToFunctionsByReturnType(Function function){
        // make sure that the hashmap has a key/value-pair
        if (!functionsByReturnType.containsKey(function.getType()))
            functionsByReturnType.put(function.getType(), new ArrayList<>());
        // add the function to the list of values (=list of functions) for the key (=list of return data types)
        functionsByReturnType.get(function.getType()).add(function);
    }

    /**
     * Test whether all features are satisfied in their output.
     * @return  true if all the selected are satisfied, false if one or more aren't
     */
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

    /**
     * Return index to the current feature to be used and then switch to the next feature
     * @return  feature-index, ranging 0... features.size()
     */
    private int iNextFeatureIndex () {
        int q = featureIndex;
        featureIndex = (featureIndex++) % features.size();
        return q;
    }

    /**
     * Get a new statement. As any expression is a statement as well, it may return
     * an expression (see: getNewExpression).
     * @return   String containing a statement
     */
    public String getNewStatement() {
/*
        Reijers code
        Refactored because of the possibilty of an infinte loop

        // An expression is also a statement, so use it as such
        if (Math.random() < CHANCE_OF_EXPRESSION_AS_STATEMENT) {
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
        return null;*/


        // return an expression as statement??
        // do so: 1. if chance will have it
        //        2. if none of the features returns a statement
        boolean bReturnStatement = !(Math.random() < CHANCE_OF_EXPRESSION_AS_STATEMENT);

        // return non-expression statement?
        if (bReturnStatement){
            IFeature currentFeature;
            for (int count = 0; count < features.size(); count ++) {   // only loop all the features once
                currentFeature = features.get(iNextFeatureIndex());
                if (currentFeature instanceof IStatementGenerator statementGenerator) {
                    return statementGenerator.getNewStatement();
                }
            }
            // if the loop is complete, all of the feature classes have been examined and none of
            // them has returned a statement. This shouldn't happen of course, but it might happen
            // during testing and building and this avoids infinite loops
        }

        // there is no non-expression statement to return (either because of
        // chance, or by lack of features that produce statements)
        // so, return an expression
        return getNewExpression(1, getDataType()) + ";\n";
    }

    /**
     * Generate a source file at the specified location.
     * @param path   Full path and file name of the output file
     * @throws Exception
     */
    public void generateSourceFile(String path) throws IOException, Exception {
        //Check prefixes are unique
        // TODO: this part should be refactored to test code, which should test the /
        //  uniqueness of the names & abbrevs in EFeaturePrefix /
        //  in which case Exception needs no longer be in the function signature
        for (var feature : features) {
            if (features.stream().anyMatch(x -> x.getPrefix().equals(feature.getPrefix()) && x.getClass() != feature.getClass()))
                throw new Exception("Prefix " + feature.getPrefix() + " is not unique!");
        }

        // set the wheels in motion
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

                ////////////////////////
                if (!functionsByReturnType.containsKey(newFunction.getType()))
                    functionsByReturnType.put(newFunction.getType(), new ArrayList<>());
                functionsByReturnType.get(newFunction.getType()).add(newFunction);
                ////////////////////////



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
