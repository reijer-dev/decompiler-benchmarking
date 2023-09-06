package nl.ou.debm.common.feature3;
import nl.ou.debm.common.*;
import nl.ou.debm.producer.*;

public class FunctionParameter {
    private String name;
    private DataType type;

    public FunctionParameter(String name, DataType type){
        this.name = name;
        this.type = type;
    }

    public void appendCode(StringBuilder sb){
        sb.append(type.getNameForUse());
        sb.append(' ');
        sb.append(name);
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
}
