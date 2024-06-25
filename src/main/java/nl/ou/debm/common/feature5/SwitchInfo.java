package nl.ou.debm.common.feature5;

import java.util.ArrayList;
import java.util.List;

import static nl.ou.debm.common.feature5.IndirectionsProducer.ICASEINDEXFORDEFAULTBRANCH;

public class SwitchInfo {

    ////////////////
    // classes/enums
    ////////////////

    /** class to store information on cases found in the LLVM */
    static public class LLVMCaseInfo {
        /** case/branch value */                                public long m_lngBranchValue;
        /** label used for this case in the LLVM, no : or % */  public String m_strLLVMLabel;
        /** branch values that also point to this branch */     public final List<Long> m_otherBranchValues = new ArrayList<>();
        @Override
        public String toString() {
            return "brv: " + m_lngBranchValue +
                    ", lab: " + m_strLLVMLabel +
                    ", same branches: " + m_otherBranchValues;
        }
    }

    /** implementation types in assembly */
    public enum SwitchImplementationType{
        IF_STATEMENTS,
        JUMP_TABLE,
        DIRECT_CALCULATED_JUMP
    }

    //////////////////////
    // instance attributes
    //////////////////////

    /** LLVM-IR function this switch is found in */                 private String m_strInLLVMFunction = "";
    /** main switch code marker, containing all properties */       private IndirectionsCodeMarker m_icm = null;
    /** how is this switch implemented in assembly? */              private SwitchImplementationType implementationType;
    /** info on all branches/cases in LLVM, including default */    private final List<LLVMCaseInfo> m_LLVMCaseInfo = new ArrayList<>();

    //////////////////////
    // getters and setters
    //////////////////////

    public void setLLVMFunctionName(String strLLVMFunction){
        m_strInLLVMFunction = strLLVMFunction;
    }
    public String strGetLLVMFunctionName(){
        return m_strInLLVMFunction;
    }
    public SwitchImplementationType getImplementationType() {
        return implementationType;
    }
    public void setImplementationType(SwitchImplementationType implementationType) {
        this.implementationType = implementationType;
    }
    public IndirectionsCodeMarker getICM(){
        return m_icm;
    }
    public void setICM(IndirectionsCodeMarker icm){
        m_icm=icm;
    }
    public List<LLVMCaseInfo> LLVMCaseInfo(){
        return m_LLVMCaseInfo;
    }
    public boolean bLLVMHasDefaultBranch(){
        if (m_LLVMCaseInfo.isEmpty()){
            return false;
        }
        return (m_LLVMCaseInfo.get(m_LLVMCaseInfo.size()-1).m_lngBranchValue==ICASEINDEXFORDEFAULTBRANCH);
    }
    public long lngGetSwitchID(){
        assert m_icm!=null : "indirections code marker not yet defined in SwitchInfo";
        return m_icm.lngGetSwitchID();
    }

    ////////////////
    // other methods
    ////////////////

    /**
     * Get the number of cases from this LLVM switch, required for the B-score of the SQS
     * @return number of cases, ignoring default
     */
    public int iGetNumberOfBSCoreCases(){
        // count the number of branches and decrease with 1 if a default branch is present
        return m_LLVMCaseInfo.size() - (bLLVMHasDefaultBranch() ? 1 : 0);
    }

    public String toString(){
        return "LLVMSWID = " + (m_icm!=null ? m_icm.lngGetSwitchID() : null) + ";" + m_LLVMCaseInfo;
    }
}
