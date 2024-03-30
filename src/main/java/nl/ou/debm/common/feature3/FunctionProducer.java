package nl.ou.debm.common.feature3;

import nl.ou.debm.common.BaseCodeMarker;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.ProjectSettings;
import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.List;

import static nl.ou.debm.common.ProjectSettings.CHANCE_OF_CREATION_OF_A_NEW_FUNCTION;

public class FunctionProducer implements IFeature, IExpressionGenerator, IFunctionGenerator, IFunctionBodyInjector {

    // keep track of the work that has been done
    private int functionCallsWithArgsCount = 0;
    private final int FUNCTION_CALLS_WITH_ARGS_MIN = 15;
    private int functionCallsWithoutArgsCount = 0;
    private final int FUNCTION_CALLS_WITHOUT_ARGS_MIN = 15;
    private int unreachableFunctionCount = 0;
    private final int UNREACHABLE_FUNCTION_MIN = 10;
    private int varArgsCount = 0;
    private final int VAR_ARGS_MIN = 3;
    private final int FUNCTIONS_MIN = 50;
    private int intermediateReturnsCount = 0;
    private final int INTERMEDIATE_RETURNS_MIN = 15;
    private int intermediateConditionalReturnsCount = 0;
    private final int INTERMEDIATE_CONDITIONAL_RETURNS_MIN = 6;
    final CGenerator generator;

    // constructor
    public FunctionProducer(CGenerator generator){
        this.generator = generator;
    }

    @Override
    public String getNewExpression(int currentDepth, DataType type, boolean terminating) {
        if(currentDepth < 10 && terminating && !isSatisfied())
            return getFunctionCall(currentDepth + 1, type);

        if(terminating || Math.random() < 0.6){
            return type.strDefaultValue(generator.structsByName);
        }else{
            return getFunctionCall(currentDepth + 1, type);
        }
    }

    private String getFunctionCall(int currentDepth, DataType type){
        var function = varArgsCount < VAR_ARGS_MIN && Math.random() < 0.5 ? getVarargsFunction(type) : generator.getFunction(currentDepth, type);
        if(function.getParameters().isEmpty() && !function.hasVarArgs()) {
            functionCallsWithoutArgsCount++;
            return function.getName() + "()";
        }else{
            functionCallsWithArgsCount++;
            var arguments = new ArrayList<String>();
            for(var parameter : function.getParameters())
                arguments.add(generator.getNewExpression(currentDepth+1, parameter.getType()));
            if(function.hasVarArgs()) {
                arguments.add(generator.getNewExpression(currentDepth + 1, generator.getRawDataType()));
                arguments.add(generator.getNewExpression(currentDepth + 1, generator.getRawDataType()));
            }

            return function.getName() + "(" + String.join(", ", arguments) + ")";
        }
    }

    @Override
    public boolean isSatisfied() {
        var result = unreachableFunctionCount >= UNREACHABLE_FUNCTION_MIN &&
                functionCallsWithArgsCount >= FUNCTION_CALLS_WITH_ARGS_MIN &&
                functionCallsWithoutArgsCount >= FUNCTION_CALLS_WITHOUT_ARGS_MIN &&
                intermediateReturnsCount >= INTERMEDIATE_RETURNS_MIN &&
                varArgsCount >= VAR_ARGS_MIN;
        if(!result)
            return false;

        if(generator.functions.size() < FUNCTIONS_MIN) {
            if(ProjectSettings.CHANCE_OF_CREATION_OF_A_NEW_FUNCTION < 0.8)
                ProjectSettings.CHANCE_OF_CREATION_OF_A_NEW_FUNCTION = 0.8;
            return false;
        }else{
            ProjectSettings.CHANCE_OF_CREATION_OF_A_NEW_FUNCTION = 0.8;
        }
        return true;
    }

    @Override
    public String getPrefix() { return EFeaturePrefix.FUNCTIONFEATURE.toString(); }

    @Override
    public List<String> getIncludes() {
        return new ArrayList<>(){
            { add("<stdarg.h>"); }
            { add("<stdio.h>"); }
            { add("<stdlib.h>"); }
        };
    }

    @Override
    public Function getNewFunction(int currentDepth, DataType type, EWithParameters withParameters) {
        assert generator != null;
        if(type == null)
            type = generator.getDataType();
        var function = new Function(type);    // use auto-name constructor

        var parameterCount = 0;
        if(withParameters == EWithParameters.YES)
            function.addParameter(new FunctionParameter("p" + parameterCount++, generator.getRawDataType()));
        while(withParameters != EWithParameters.NO && Math.random() < 0.7)
            function.addParameter(new FunctionParameter("p" + parameterCount++, generator.getRawDataType()));

        //We want to create some unreachable functions
        if(unreachableFunctionCount < UNREACHABLE_FUNCTION_MIN) {
            function.setCallable(false);
            unreachableFunctionCount++;
            if(type.getName().equals("void"))
                function.addStatement("\treturn;");
            else
                function.addStatement("\treturn " + type.strDefaultValue(generator.structsByName) + ";");
            return function;
        }

        // add three statements
        // prefer exactly one statement per call
        var prefs = new StatementPrefs();
        prefs.assignment = EStatementPref.REQUIRED;
        function.addStatements(generator.getNewStatements(currentDepth + 1, function, prefs));
        var max = 0;
        while(max < 5 && orRandom(0.2, intermediateReturnsCount < INTERMEDIATE_RETURNS_MIN)) {
            var isConditional = orRandom(0.3, intermediateConditionalReturnsCount < INTERMEDIATE_CONDITIONAL_RETURNS_MIN);
            if(isConditional)
                function.addStatement("if (rand() < 1) {");
            if(type.getName().equals("void"))
                function.addStatement("\treturn;");
            else
                function.addStatement("\treturn " + type.strDefaultValue(generator.structsByName) + ";");
            if(isConditional) {
                function.addStatement("}");
                intermediateConditionalReturnsCount++;
            }
            intermediateReturnsCount++;
            function.addStatements(generator.getNewStatements(currentDepth + 1, function, prefs));
            max++;
        }

        if(withParameters != EWithParameters.NO && (varArgsCount < 2 || Math.random() < 0.2)){
            return getVarargsFunction(type);
        }else{
            //Normal function ending
            function.addStatement(type.getNameForUse() + " " + getPrefix() + "_x = " + generator.getNewExpression(currentDepth + 1, type) + ';');
            function.addStatement("return " + getPrefix() + "_x;");
        }

        return function;
    }

    private Function getVarargsFunction(DataType type){
        var function = new Function(type);    // use default name constructor
        function.addParameter(new FunctionParameter("p1", generator.getDataType()));
        function.setHasVarArgs(true);

        function.addStatement("va_list va;");
        function.addStatement("va_start(va, p1);");
        function.addStatement(type.getNameForUse() + " first = __builtin_va_arg(va, "+type.getNameForUse()+");");
        function.addStatement("va_end(va);");

        //Call itself several times with a varying amount of arguments
        var functionCallBuilder = new StringBuilder();
        functionCallBuilder.append(EFeaturePrefix.FUNCTIONFEATURE);
        functionCallBuilder.append('_');
        functionCallBuilder.append(function.getName());
        functionCallBuilder.append("(");
        functionCallBuilder.append(generator.getNewExpression(function.getParameters().get(0).getType(), true));
        functionCallBuilder.append(",1);");
        function.addStatement(functionCallBuilder.toString());

        functionCallBuilder.setLength(0);
        functionCallBuilder.append(EFeaturePrefix.FUNCTIONFEATURE);
        functionCallBuilder.append('_');
        functionCallBuilder.append(function.getName());
        functionCallBuilder.append("(");
        functionCallBuilder.append(generator.getNewExpression(function.getParameters().get(0).getType(), true));
        functionCallBuilder.append(",1,2,3);");
        function.addStatement(functionCallBuilder.toString());

        function.addStatement("return first;");
        varArgsCount++;
        return function;
    }

    @Override
    public CodeMarker appendCodeMarkerAtStart(Function function) {
        var startMarker = new BaseCodeMarker(this);
        startMarker.setProperty("functionName", function.getName());
        startMarker.setProperty("callable", function.isCallable() ? "1" : "0");
        return startMarker;
    }

    @Override
    public CodeMarker appendCodeMarkerAtEnd(Function function) {
        var endMarker = new BaseCodeMarker(this);
        endMarker.setProperty("functionName", function.getName());
        return endMarker;
    }

    private Boolean orRandom(double chance, Boolean bool){
        return bool || Math.random() < chance;
    }
}
