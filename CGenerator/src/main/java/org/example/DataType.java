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

    public boolean bIsChar(){
        return name.equals("char");
    }
    public boolean bIsInt(){
        return name.equals("int");
    }
    public boolean bIsFloat(){
        return name.equals("float");
    }
    public boolean bIsDouble(){
        return name.equals("double");
    }
    public boolean bIsShortInt(){
        return name.equals("short");
    }
    public boolean bIsLongInt(){
        return name.equals("long");
    }
    public boolean bIsPrimitive(){
        return bIsChar() || bIsDouble() || bIsFloat() || bIsInt() || bIsShortInt() || bIsLongInt();
    }
    public boolean bIsNumberic(){
        return bIsPrimitive() && !bIsChar();
    }
}
