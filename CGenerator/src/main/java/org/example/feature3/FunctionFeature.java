package org.example.feature3;

import org.example.*;

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
    private int functionCount = 0;
    final CGenerator generator;

    // constructor
    public FunctionFeature(CGenerator generator){
        this.generator = generator;
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
            var function = generator.getFunction(type);
            if(function.getParameters().isEmpty()) {
                functionCallWithoutParameters = true;
                return function.getName() + "()";
            }else{
                functionCallWithParameters = true;
                var arguments = new ArrayList<String>();
                for(var parameter : function.getParameters())
                    arguments.add(generator.getNewExpression(currentDepth++, parameter.getType()));
                return function.getName() + "(" + String.join(", ", arguments) + ")";
            }
        }
    }

    @Override
    public boolean isSatisfied() {
        return literal && functionCallWithoutParameters && functionCallWithParameters && global && functionCount >= 3;
    }

    @Override
    public String getPrefix() { return EFeaturePrefix.FUNCTIONFEATURE.toString(); }

    @Override
    public Function getNewFunction(DataType type) {
        if(type == null)
            type = generator.getDataType();
        var result = new Function(type, "function_" + functionCount++);
        var parameterCount = 0;
        while(Math.random() < 0.7){
            result.addParameter(new FunctionParameter("p" + parameterCount++, generator.getDataType()));
        }
        result.addStatement(generator.getNewStatement());
        result.addStatement(generator.getNewStatement());
        result.addStatement(generator.getNewStatement());
        result.addStatement(type.getNameForUse() + " " + getPrefix() + "_x = " + generator.getNewExpression(1, type) + ';');
        //Tailcall
        result.addStatement("return " + getPrefix() + "_x;");
        return result;
    }
}
