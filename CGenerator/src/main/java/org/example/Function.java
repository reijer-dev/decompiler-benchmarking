package org.example;

import org.example.feature3.FunctionParameter;

import java.util.ArrayList;
import java.util.List;

public class Function {
    private String name;                    // function name
    private DataType type;                  // function return-datatype
    private final List<FunctionParameter> parameters = new ArrayList<>();   // function parameter list
    private final List<String> statements = new ArrayList<>();  // function statements

    /**
     * Construct a function object
     * @param type  function name
     * @param name  function return data type (may be void, may not be empty)
     */
    public Function(DataType type, String name){
        setType(type);
        setName(name);
    }

    /**
     * Emit the code to a StringBuilder-object that accumulates all the generated c-code parts
     * @param sb    StringBuilder to which this functions written code will be added.
     */
    public void appendCode(StringBuilder sb){
        sb.append('\n');                                    // new line
        sb.append(type.getNameForUse());                    // return type
        sb.append(' ');
        sb.append(name);                                    // function name
        sb.append('(');                                     // list parameters
        for(var i = 0; i < parameters.size(); i++){
            if(i > 0)
                sb.append(", ");
            parameters.get(i).appendCode(sb);
        }
        sb.append("){\n");
        for(var statement : statements){                    // list all statements
           sb.append(statement);
           sb.append('\n');
        }
        sb.append("\n}\n");                                 // close function
    }

    // getters & setters
    // =================
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

    // Add new information to function
    public void addParameter(FunctionParameter parameter){
        parameters.add(parameter);
    }

    public void addStatement(String statement){
        statements.add(statement);
    }
}
