package org.example;

import org.example.feature3.FunctionParameter;

import java.util.ArrayList;
import java.util.List;

public class Function {
    private String name;
    private DataType type;
    private final List<FunctionParameter> parameters = new ArrayList<>();
    private final List<String> statements = new ArrayList<>();

    public Function(DataType type, String name){
        this.type = type;
        this.name = name;
    }

    public void appendCode(StringBuilder sb){
        sb.append('\n');
        sb.append(type.getNameForUse());
        sb.append(' ');
        sb.append(name);
        sb.append('(');
        for(var i = 0; i < parameters.size(); i++){
            if(i > 0)
                sb.append(", ");
            parameters.get(i).appendCode(sb);
        }
        sb.append("){\n");
        for(var statement : statements){
           sb.append(statement);
           sb.append('\n');
        }
        sb.append("\n}\n");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public List<FunctionParameter> getParameters(){
        return parameters;
    }

    public void addParameter(FunctionParameter parameter){
        parameters.add(parameter);
    }

    public void addStatement(String statement){
        statements.add(statement);
    }
}
