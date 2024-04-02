package nl.ou.debm.assessor;

public enum EAssessorWorkModes {
    DECOMPILE_AND_ASSESS, DECOMPILE_ONLY, ASSESS_ONLY;

    public String strOutput(){
        String out;
        switch (this){
            case DECOMPILE_AND_ASSESS ->                    out = "assess after forced decompilation";
            case DECOMPILE_ONLY ->                          out = "force decompilation, don't assess";
            case ASSESS_ONLY ->                             out = "assess only, no decompilation";
            default -> out="";
        }
        return out;
    }
}
