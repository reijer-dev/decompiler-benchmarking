package nl.ou.debm.common.feature3;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.List;

public class FunctionProducer implements IFeature, IExpressionGenerator, IFunctionGenerator {

    // keep track of the work that has been done
    private boolean literal = false;
    private boolean global = false;
    private boolean functionCallWithParameters = false;
    private boolean functionCallWithoutParameters = false;
    private int tailCallCount = 0;
    private int varArgsCount = 0;
    private int functionCount = 0;
    final CGenerator generator;
    //Since it is universally unique, every code line having this is a marker from feature3, no matter how the wrapping method call is decompiled
    public static final String FunctionMarkerPrefix = "2fe02671-d357-4998-aae6-08b438e6da78";

    // constructor
    public FunctionProducer(CGenerator generator){
        this.generator = generator;
    }

    @Override
    public String getNewExpression(int currentDepth, DataType type, boolean terminating) {
        if(Math.random() < 0.7 || terminating || currentDepth >= 3){
            if(!global || Math.random() < 0.5 || type instanceof Struct) {
                global = true;
                return generator.getGlobal(type).getName();
            }else{
                literal = true;
                if ("char".equals(type.getName())) {
                    return "'a'";
                }
                return "0";
            }
        }else{
            return getFunctionCall(currentDepth, type, null);
        }
    }

    private String getFunctionCall(int currentDepth, DataType type, Boolean withParameters){
        var function = generator.getFunction(type, withParameters);
        if(function.getParameters().isEmpty() && !function.hasVarArgs()) {
            functionCallWithoutParameters = true;
            return function.getName() + "()";
        }else{
            functionCallWithParameters = true;
            var arguments = new ArrayList<String>();
            for(var parameter : function.getParameters())
                arguments.add(generator.getNewExpression(currentDepth+1, parameter.getType()));
            if(function.hasVarArgs()) {
                arguments.add(generator.getNewExpression(currentDepth + 1, generator.getDataType()));
                arguments.add(generator.getNewExpression(currentDepth + 1, generator.getDataType()));
            }

            return function.getName() + "(" + String.join(", ", arguments) + ")";
        }
    }

    @Override
    public boolean isSatisfied() {
        return literal && functionCallWithoutParameters && functionCallWithParameters && global && functionCount >= 3 && varArgsCount >= 2;
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
    public Function getNewFunction(DataType type, Boolean withParameters) {
        assert generator != null;
        if(type == null)
            type = generator.getDataType();
        functionCount++;                    // keep track of the number of functions produced
        var function = new Function(type);    // use auto-name constructor

        var parameterCount = 0;
        if(withParameters != null && withParameters == true)
            function.addParameter(new FunctionParameter("p" + parameterCount++, generator.getDataType()));
        while((withParameters == null || withParameters == true) && Math.random() < 0.7)
            function.addParameter(new FunctionParameter("p" + parameterCount++, generator.getDataType()));

        function.addStatement(getStartMarker(function));

        // add three statements
        // prefer exactly one statement per call
        function.addStatements(generator.getNewStatements(function));
        function.addStatements(generator.getNewStatements(function));
        function.addStatements(generator.getNewStatements(function));

        if(tailCallCount < 2){
            tailCallCount++;
            //Call a function with parameters. Parameterless functions do not result in a tail call
            function.addStatement(getEndMarker(function));
            function.addStatement("return " + getFunctionCall(1, type, true) + ";");
        }else if(varArgsCount < 2 ){
            varArgsCount++;
            return getVarargsFunction(type);
        }else{
            function.addStatement(type.getNameForUse() + " " + getPrefix() + "_x = " + generator.getNewExpression(1, type) + ';');
            function.addStatement(getEndMarker(function));
            function.addStatement("return " + getPrefix() + "_x;");
        }


        return function;
    }

    private Function getVarargsFunction(DataType type){
        functionCount++;                    // keep track of number of created functions
        var function = new Function(type);    // use default name constructor
        function.addParameter(new FunctionParameter("p1", generator.getDataType()));
        function.setHasVarArgs(true);

        function.addStatement(getStartMarker(function));

        function.addStatement("va_list va;");
        function.addStatement("va_start(va, p1);");
        function.addStatement(type.getNameForUse() + " first = __builtin_va_arg(va, "+type.getNameForUse()+");");
        function.addStatement("va_end(va);");
        function.addStatement(getEndMarker(function));
        function.addStatement("return first;");
        return function;
    }

    public String getStartMarker(Function function) {
        var startMarker = new CodeMarker();
        startMarker.setProperty("functionName", function.getName());
        startMarker.setProperty("position", "start");
        startMarker.setProperty("isVariadic", String.valueOf(function.hasVarArgs()));
        return "printf(\""+FunctionMarkerPrefix+startMarker+"\");";
    }

    public String getEndMarker(Function function) {
        var endMarker = new CodeMarker();
        endMarker.setProperty("functionName", function.getName());
        endMarker.setProperty("position", "end");
        return "printf(\""+FunctionMarkerPrefix+endMarker+"\");";
    }

}
