package nl.ou.debm.producer;

public class Variable {
    private String name;
    private DataType type;
    private String value;

    public Variable(String name, DataType type){
        this.name = name;
        this.type = type;
    }

    public Variable(String name, DataType type, String value){
        this(name, type);
        this.value = value;
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

    public String getValue() {
        return value;
    }

    public boolean hasValue(){
        return value != null;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
