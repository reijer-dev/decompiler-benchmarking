package nl.ou.debm.common.feature3;

public class AsmLineInfo {
    public AsmLineInfo(AsmType type){
        this.type = type;
    }

    public AsmLineInfo(AsmType type, String value){
        this.type = type;
        this.value = value;
    }

    public AsmLineInfo(AsmType type, String value, String value2){
        this.type = type;
        this.value = value;
        this.value2 = value2;
    }

    public AsmType type;
    public String value;
    public String value2;
}
