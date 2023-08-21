package org.example;

import org.example.feature1.FunctionParameter;

import java.util.ArrayList;
import java.util.List;

public class Struct extends DataType {

    private final List<Variable> properties = new ArrayList<>();

    public Struct(String name) {
        super(name);
    }

    public void appendCode(StringBuilder sb) {
        sb.append("\nstruct ");
        sb.append(name);
        sb.append(" {\n");
        for(var property : properties){
            sb.append(property.getType().getNameForUse());
            sb.append(' ');
            sb.append(property.getName());
            sb.append(';');
            sb.append('\n');
        }
        sb.append("};\n");
    }

    @Override
    public String getNameForUse() {
        return "struct " + name;
    }

    public List<Variable> getProperties(){
        return properties;
    }

    public void addProperty(Variable property){
        properties.add(property);
    }
}
