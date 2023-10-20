package nl.ou.debm.common.feature3;

import nl.ou.debm.producer.*;

import java.util.ArrayList;

public class FunctionFeature implements IFeature, IExpressionGenerator, IFunctionGenerator {

    // attributes
    // ----------
    //
    // keep track of the work that has been done
    private boolean literal = false;
    private boolean global = false;
    private boolean functionCallWithParameters = false;
    private boolean functionCallWithoutParameters = false;
    private int tailCallCount = 0;
    private int varArgsCount = 0;
    private int functionCount = 0;
    final CGenerator generator;

    // constructor
    public FunctionFeature(CGenerator generator){
        this.generator = generator;
    }
    public FunctionFeature(){
        this.generator = null;
    }

    public String getNewExpression(int currentDepth, DataType type, boolean terminating) {
        if(Math.random() < 0.5 || terminating){
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
    public Function getNewFunction(DataType type, Boolean withParameters) {
        if(type == null)
            type = generator.getDataType();
        var result = new Function(type, "function_" + functionCount++);

        var parameterCount = 0;
        if(withParameters != null && withParameters == true)
            result.addParameter(new FunctionParameter("p" + parameterCount++, generator.getDataType()));
        while((withParameters == null || withParameters == true) && Math.random() < 0.7)
            result.addParameter(new FunctionParameter("p" + parameterCount++, generator.getDataType()));

        result.addStatement(generator.getNewStatement());
        result.addStatement(generator.getNewStatement());
        result.addStatement(generator.getNewStatement());

        if(tailCallCount < 10){
            tailCallCount++;
            //Call a function with parameters. Parameterless functions do not result in a tail call
            result.addStatement("return " + getFunctionCall(1, type, true) + ";");
        }else if(varArgsCount < 2 ){
            varArgsCount++;
            return getVarargsFunction(type);
        }else{
            result.addStatement(type.getNameForUse() + " " + getPrefix() + "_x = " + generator.getNewExpression(1, type) + ';');
            result.addStatement("return " + getPrefix() + "_x;");
        }

        return result;
    }

    private Function getVarargsFunction(DataType type){
        var result = new Function(type, "function_" + functionCount++);
        result.addParameter(new FunctionParameter("p1", generator.getDataType()));
        result.setHasVarArgs(true);
        result.addStatement("va_list ap;");
        result.addStatement("va_start(ap, p1);");
        result.addStatement(type + " first = va_arg(ap, double);");
        result.addStatement("va_end(ap);");
        result.addStatement("return first");
        return result;
    }
}
