package nl.ou.debm.common.feature3;

import nl.ou.debm.common.BaseCodeMarker;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.List;

public class FunctionProducer implements IFeature, IExpressionGenerator, IFunctionGenerator, IFunctionBodyInjector {

    // keep track of the work that has been done
    private int functionCallsWithArgsCount = 0;
    private final int FUNCTION_CALLS_WITH_ARGS_MIN = 15;
    private int functionCallsWithoutArgsCount = 0;
    private final int FUNCTION_CALLS_WITHOUT_ARGS_MIN = 15;
    private int tailCallCount = 0;
    private final int TAIL_CALL_MIN = 2;
    private int unreachableFunctionCount = 0;
    private final int UNREACHABLE_FUNCTION_MIN = 10;
    private int varArgsCount = 0;
    private final int VAR_ARGS_MIN = 3;
    private int functionCount = 0;
    private final int FUNCTIONS_MIN = 50;
    final CGenerator generator;
    //Since it is universally unique, every code line having this is a marker from feature3, no matter how the wrapping method call is decompiled
    public static final String FunctionMarkerPrefix = CodeMarker.STRCODEMARKERGUID + EFeaturePrefix.FUNCTIONFEATURE;

    // constructor
    public FunctionProducer(CGenerator generator){
        this.generator = generator;
    }

    @Override
    public String getNewExpression(int currentDepth, DataType type, boolean terminating) {
        if(terminating || Math.random() < 0.6){
            return type.strDefaultValue(generator.structsByName);
        }else{
            return getFunctionCall(currentDepth + 1, type, null);
        }
    }

    private String getFunctionCall(int currentDepth, DataType type, Boolean withParameters){
        var function = generator.getFunction(currentDepth, type, withParameters);
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
        return tailCallCount >= TAIL_CALL_MIN &&
                unreachableFunctionCount >= UNREACHABLE_FUNCTION_MIN &&
                functionCallsWithArgsCount >= FUNCTION_CALLS_WITH_ARGS_MIN &&
                functionCallsWithoutArgsCount >= FUNCTION_CALLS_WITHOUT_ARGS_MIN &&
                functionCount >= FUNCTIONS_MIN &&
                varArgsCount >= VAR_ARGS_MIN;
    }

    @Override
    public String getPrefix() { return EFeaturePrefix.FUNCTIONFEATURE.toString(); }

    @Override
    public List<String> getIncludes() {
        return new ArrayList<>(){
            { add("<stdarg.h>"); }
        };
    }

    @Override
    public Function getNewFunction(int currentDepth, DataType type, Boolean withParameters) {
        assert generator != null;
        if(type == null)
            type = generator.getDataType();
        functionCount++;                    // keep track of the number of functions produced
        var function = new Function(type);    // use auto-name constructor

        var parameterCount = 0;
        if(withParameters != null && withParameters == true)
            function.addParameter(new FunctionParameter("p" + parameterCount++, generator.getRawDataType()));
        while((withParameters == null || withParameters == true) && Math.random() < 0.7)
            function.addParameter(new FunctionParameter("p" + parameterCount++, generator.getRawDataType()));

        //We want to create some unreachable functions
        if(unreachableFunctionCount < UNREACHABLE_FUNCTION_MIN) {
            function.setCallable(false);
            unreachableFunctionCount++;
        }

        // add three statements
        // prefer exactly one statement per call
        var prefs = new StatementPrefs();
        prefs.assignment = EStatementPref.REQUIRED;
        function.addStatements(generator.getNewStatements(currentDepth + 1, function, prefs));
        function.addStatements(generator.getNewStatements(currentDepth + 1, function, prefs));
        function.addStatements(generator.getNewStatements(currentDepth + 1, function, prefs));

        if(tailCallCount < TAIL_CALL_MIN || Math.random() < 0.2){
            tailCallCount++;
            //Call a function with parameters. Parameterless functions do not result in a tail call
            function.addStatement("return " + getFunctionCall(currentDepth + 1, type, true) + ";");
        }else if(varArgsCount < 2 || Math.random() < 0.2){
            varArgsCount++;
            return getVarargsFunction(type);
        }else{
            //Normal function ending
            function.addStatement(type.getNameForUse() + " " + getPrefix() + "_x = " + generator.getNewExpression(currentDepth + 1, type) + ';');
            function.addStatement("return " + getPrefix() + "_x;");
        }

        return function;
    }

    private Function getVarargsFunction(DataType type){
        functionCount++;                    // keep track of number of created functions
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
        return function;
    }

    @Override
    public CodeMarker appendCodeMarkerAtStart(Function function) {
        var startMarker = new BaseCodeMarker(this);
        startMarker.setProperty("functionName", function.getName());
        return startMarker;
    }

    @Override
    public CodeMarker appendCodeMarkerAtEnd(Function function) {
        var endMarker = new BaseCodeMarker(this);
        endMarker.setProperty("functionName", function.getName());
        return endMarker;
    }
}
