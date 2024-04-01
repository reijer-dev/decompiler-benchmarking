package nl.ou.debm.assessor;

public enum EAssessorWorkModes {
    DECOMPILE_AND_ASSESS, DECOMPILE_ONLY, DECOMPILE_WHEN_NEEDED_AND_ASSESS;

    public String strOutput(){
        String out;
        switch (this){
            case DECOMPILE_AND_ASSESS ->                    out = "assess after forced decompilation";
            case DECOMPILE_ONLY ->                          out = "force decompilation, don't assess";
            case DECOMPILE_WHEN_NEEDED_AND_ASSESS ->        out = "assess, only decompile when necessary";
            default -> out="";
        }
        return out;
    }
}
