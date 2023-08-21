package org.example;

public class DataType {
    protected String name;
    public DataType(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getNameForUse(){
        return name;
    }

    public void prefixName(String prefix){
        name = prefix + "_" + name;
    }
}
