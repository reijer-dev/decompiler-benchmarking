package nl.ou.debm.common.feature3;

public class AsmLineInfo {
    public AsmLineInfo(AsmType type){
        this.type = type;
    }

    public AsmLineInfo(AsmType type, String value){
        this.type = type;
        this.value = value;
    }

    public AsmType type;
    public String value;
}
