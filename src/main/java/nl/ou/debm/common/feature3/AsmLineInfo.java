package nl.ou.debm.common.feature3;

public class AsmLineInfo {
    public AsmLineInfo(String line, AsmType type){
        this.type = type;
        this.line = line;
    }

    public AsmLineInfo(String line, AsmType type, String value){
        this.type = type;
        this.value = value;
        this.line = line;
    }

    public AsmLineInfo(String line, AsmType type, String value, String value2){
        this.type = type;
        this.value = value;
        this.value2 = value2;
        this.line = line;
    }

    public AsmType type;
    public String value;
    public String value2;
    public String line;
}
