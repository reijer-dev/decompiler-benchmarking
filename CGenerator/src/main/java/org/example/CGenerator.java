package org.example;

import org.example.feature1.*;
import org.example.feature2.*;
import org.example.feature3.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// TODO: implement proper procedures (which in C are functions returning void)


public class CGenerator {

    // constants that define the behavior publicly available and grouped, so they can be
    // easily identified and modified
    public final double CHANCE_OF_EXPRESSION_AS_STATEMENT = 0.5;
    public final double CHANCE_OF_CREATION_OF_A_NEW_FUNCTION = 0.5;
    public final double CHANCE_OF_CREATION_OF_A_NEW_STRUCT = 0.5;
    public final double CHANCE_OF_CREATION_OF_A_NEW_GLOBAL=0.5;
    public final double CHANCE_OF_TERMINATION_EXPRESSION_RECURSION=0.5;
    public final double CHANCE_OF_STRUCT_WHEN_ASKING_FOR_RANDOM_DATATYPE=0.5;
    public final int MAX_EXPRESSION_DEPTH = 5;


    private final StringBuilder sb = new StringBuilder();       // the string builder is used to accumulate all generated code
    private final List<IFeature> features = new ArrayList<>();  // keep track of all feature classes
    private int featureIndex = 0;                               // make sure that all feature classes are used throughout code building
    public HashMap<DataType, List<Function>> functionsByReturnType = new HashMap<>();   // Store functions sorted by return type
    public List<Function> functions = new ArrayList<>();        // Store functions in list, to maintain their creation order
    public HashMap<DataType, List<Variable>> globalsByType = new HashMap<>();   // store global variables
    public List<Struct> structs = new ArrayList<>();            // store structs
    public final DataType[] rawDataTypes = new DataType[6];     // table of basic data types
    private Function mainFunction;                              // main function

    public CGenerator() {
        // constructor
        // -----------

        // fill array of feature-objects
        features.add(new FunctionFeature(this));
        features.add(new DataStructuresFeature(this));
        features.add(new ControlFlowFeature(this));

        // fill array of raw data types
        rawDataTypes[0] = new DataType("short");
        rawDataTypes[1] = new DataType("int");
        rawDataTypes[2] = new DataType("long");
        rawDataTypes[3] = new DataType("char");
        rawDataTypes[4] = new DataType("float");
        rawDataTypes[5] = new DataType("double");
    }

    /**
     * Export structs to the string builder, making sure that they all
     * are placed properly in the c-source file.
     */
    private void writeStructs() {
        for (var struct : structs) {
            struct.appendCode(sb);
        }
    }

    /**
     * Export global variables to the string builder, making sure that they
     * all are placed properly in the c-source file.
     */
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

    /**
     * Export all functions to the string builder.
     */
    private void writeFunctions() {
        /*
            In c, a function may only be called after it is defined (or at least
            declared). The way the builder works, this rule is automatically satisfied.
            Suppose main wants to call a function. No functions are defined yet, so the system
            tries to define a new function. In this function, another function call is inserted.
            This function call can be one of two: a recursion call or a new function call.
            If it is the first: no problem, as a function calling itself is declared before the call.
            If it is the second: no problem either. The system makes another new function. Only
            when the second function is completed, it is added to the function list. Only then
            the first function can be completed and added to the function list. So, the
            calling function is always later added to the list than the caller function.
            By manually adding main as last, it is made sure that any function called by main
            precedes it.
         */

        // write all the created function, except main
        for (var function : functions) {
            function.appendCode(sb);
        }
        // write main
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

    /**
     * Get a new expression. It may terminate at the start when instructed as such,
     * otherwise it will allow recursion.
     * @param type  Type of expression requested.
     * @param terminating  Force direct recursion termination when true, otherwise
     *                     allow subexpressions being part of the expression
     * @return  An expression in text format.
     */
    public String getNewExpression(DataType type, boolean terminating) {
        return getNewExpression(terminating ? Integer.MAX_VALUE : 1, type);
    }

    /**
     * Get a new expression. This may use recursion, but to prevent endless
     * recursion, it asks for the current depth of the recursion.
     * if recursion is too deep, the feature class will be instructed not to
     * use recursion anymore.
     * @param currentDepth  Current depth of recursion.
     * @param type  Type of expression requested.
     * @return  An expression in text format.
     */
    public String getNewExpression(int currentDepth, DataType type) {
        IFeature currentFeature;
        // idea indicates the for loop will not loop if all the features implement IExpressionGenerator,
        // which is true. But is is no problem, as the current feature always switches at the beginning
        // of the loop, so there is still a variety of features called.
        for (int count = 0; count < features.size(); count ++) {   // only loop all the features once
            currentFeature = features.get(iNextFeatureIndex());
            if (currentFeature instanceof IExpressionGenerator expressionGenerator) {
                return expressionGenerator.getNewExpression(currentDepth, type,
                            Math.random() < CHANCE_OF_TERMINATION_EXPRESSION_RECURSION || currentDepth > MAX_EXPRESSION_DEPTH);
            }
        }
        return null;
    }

    /**
     * Return index to the current feature to be used and then switch to the next feature
     * @return  feature-index, ranging 0... features.size()
     */
    private int iNextFeatureIndex () {
        int q = featureIndex;
        featureIndex++;
        featureIndex%=features.size();
        return q;
    }

    /**
     * Get a new statement. As any expression is a statement as well, it may return
     * an expression (see: getNewExpression).
     * @return   String containing a statement
     */
    public String getNewStatement() {
        // return an expression as statement?
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
            // if the loop is complete, all the feature classes have been examined and none of
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

        // clear the string builder
        sb.setLength(0);
        // start with data structures
        writeStructs();
        // continue with globals, as they may use data structures
        writeGlobalVariables();
        // end with all the functions, as they may both use globlas and data structures
        writeFunctions();
        // export the lot to the designated file
        var writer = new OutputStreamWriter(new FileOutputStream(path));
        writer.write(sb.toString());
        writer.flush();
        writer.close();
    }

    /**
     * Get a function object, returning a function that returs data of a
     * random type.
     * @return  function object
     */
    public Function getFunction() {
        return getFunction(null);
    }

    /**
     * Get a function object, making sure the function returns data of the type
     * passed to this method.
     * @param type  describes the data type that the function should return
     * @return  function object; returns null on error.
     */
    public Function getFunction(DataType type) {
        // determine whether or not to create a new function
        //
        // default: only a new function needed when there are no functions at all
        var createNew = functionsByReturnType.isEmpty();
        // when a specific return type is specified, check the existence of such function
        if (type != null && !functionsByReturnType.containsKey(type))
            createNew = true;
        // make sure in a certain percentage of calls a new function is created anywas
        if (Math.random() < CHANCE_OF_CREATION_OF_A_NEW_FUNCTION)
            createNew = true;

        // if a new function is wanted, make it
        if (createNew) {
            // new function wanted
            IFeature currentFeature;
            for (int count = 0; count < features.size(); count ++) {   // only loop all the features once
                currentFeature = features.get(iNextFeatureIndex());
                if (currentFeature instanceof IFunctionGenerator functionGenerator) {
                    // feature has a function generator, so use it
                    var newFunction = functionGenerator.getNewFunction(type);
                    // attach prefix to function, so function names will be unique
                    newFunction.setName(currentFeature.getPrefix() + "_" + newFunction.getName());
                    // store function in the two sets
                    addFunctionToFunctionsByReturnType(newFunction);
                    functions.add(newFunction);
                    // and be done ;-)
                    return newFunction;
                }
            }
            // no new function could be constructed
            return null;
        } else {
            // old function wanted.
            //
            // make sure the parameter object is not affected
            var wantedType = type;
            // if no particular return type is wanted, pick a s random one
            if (wantedType == null) {
                var keys = functionsByReturnType.keySet().stream().toList();
                wantedType = keys.get(ThreadLocalRandom.current().nextInt(0, keys.size()));
            }
            // pick a random function on the basis of the return type
            return functionsByReturnType.get(wantedType).get(ThreadLocalRandom.current().nextInt(0, functionsByReturnType.get(wantedType).size()));
        }
    }

    /**
     * Get a struct.
     * @return struct object, null in case of error.
     */
    public Struct getStruct() {
        // TODO: Implement getStruct where caller can specify a datatype that must be
        //       included in the struct that is returned

        // slightly refactored to prepare for the TODO

        // default: only create new on empty struct
        boolean createNew=structs.isEmpty();
        if (Math.random() < CHANCE_OF_CREATION_OF_A_NEW_STRUCT)
            createNew=true;
        // TODO: check list for struct with certain datatype

        // if new struct is wanted, create it
        if (createNew) {
            // new struct wanted
            IFeature currentFeature;
            for (int count = 0; count < features.size(); count ++) {   // only loop all the features once
                currentFeature = features.get(iNextFeatureIndex());
                if (currentFeature instanceof IStructGenerator structGenerator) {
                    // feature has struct generator, so use it
                    var newStruct = structGenerator.getNewStruct();
                    // attach prefix to struct name
                    newStruct.prefixName(currentFeature.getPrefix());
                    // add struct to array of structs
                    structs.add(newStruct);
                    // and return the result
                    return newStruct;
                }
            }
            // no struct could be created, so return null
            return null;
        } else {
            // no new struct wanted, so return a random one
            return structs.get(ThreadLocalRandom.current().nextInt(0, structs.size()));
        }
    }

    public DataType getRawDataType() {
        return rawDataTypes[ThreadLocalRandom.current().nextInt(0, rawDataTypes.length)];
    }

    public DataType getDataType() {
        if (Math.random() < CHANCE_OF_STRUCT_WHEN_ASKING_FOR_RANDOM_DATATYPE)
            return getStruct();
        return getRawDataType();
    }

    public Variable getGlobal(DataType type) {
        var createNew = false;
        if (globalsByType.isEmpty())
            createNew = true;
        if (Math.random() < CHANCE_OF_CREATION_OF_A_NEW_GLOBAL)
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
