package nl.ou.debm.common.feature5;

import java.util.ArrayList;
import java.util.List;

public class SwitchInfo {

    static public class CaseInfo{
        public long m_lngBranchValue;
        public String m_strLLVMLabel;
    }

    private String m_strInLLVMFunction = "";

    public void setFunctionName(String strLLVMFunction){
        m_strInLLVMFunction = strLLVMFunction;
    }

    public String strGetLLVMFunctionName(){
        return m_strInLLVMFunction;
    }

    private Boolean implementedAsIndirection = null;
    final public List<CaseInfo> m_caseInfo = new ArrayList<>();
    private int numberOfDecompiledBranches;


    public Boolean isImplementedAsIndirection() {
        return implementedAsIndirection;
    }

    public void setImplementedAsIndirection(Boolean implementedAsIndirection) {
        this.implementedAsIndirection = implementedAsIndirection;
    }

}
