package nl.ou.debm.assessor;

public enum EAssessorWorkModes {
    DECOMPILE_AND_ASSESS, DECOMPILE_ONLY, ASSESS_ONLY, DECOMPILE_WHEN_NECESSARY;

    /**
     * display output for user interface
     * @return description of the workmode
     */
    public String strOutput(){
        String out;
        switch (this){
            case DECOMPILE_AND_ASSESS ->                    out = "assess after forced decompilation";
            case DECOMPILE_ONLY ->                          out = "force decompilation, don't assess";
            case ASSESS_ONLY ->                             out = "assess only, no decompilation";
            case DECOMPILE_WHEN_NECESSARY ->                out = "decompile only when no cached output is found, don't assess";
            default -> out="";
        }
        return out;
    }

    /**
     * determine if in this mode decompilation can be done
     * @return true if and only if, decompiler may be invoked
     */
    public boolean bDecompilationPossible(){
        boolean out = false;
        switch (this){
            case DECOMPILE_AND_ASSESS, DECOMPILE_ONLY, DECOMPILE_WHEN_NECESSARY ->  out = true;
            case ASSESS_ONLY ->                                                     out = false;
        }
        return out;
    }

    /**
     * determine if in this mode, decompilation is forced
     * @return true and only if, the decompiler is always invoked
     */
    public boolean bForceDecompilation(){
        boolean out = false;
        switch (this) {
            case DECOMPILE_AND_ASSESS, DECOMPILE_ONLY ->    out=true;
            case ASSESS_ONLY, DECOMPILE_WHEN_NECESSARY ->   out=false;
        }
        return out;
    }

    /**
     * determine if in this mode, assessing is done
     * @return true if, and only if, assessing is done
     */
    public boolean bAssessingDone(){
        boolean out = false;
        switch (this) {
            case DECOMPILE_AND_ASSESS, ASSESS_ONLY ->        out = true;
            case DECOMPILE_ONLY, DECOMPILE_WHEN_NECESSARY -> out = false;
        }
        return out;
    }
}
