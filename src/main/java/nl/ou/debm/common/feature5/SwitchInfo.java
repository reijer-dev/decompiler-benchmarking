package nl.ou.debm.common.feature5;

import java.util.ArrayList;
import java.util.List;

public class SwitchInfo {

    static public class CaseInfo{
        public long m_lngBranchValue;
        public String m_strLLVMLabel;
    }

    private SwitchImplementationType implementationType;
    final public List<CaseInfo> m_caseInfo = new ArrayList<>();
    private int numberOfDecompiledBranches;

    public SwitchImplementationType getImplementationType() {
        return implementationType;
    }

    public void setImplementationType(SwitchImplementationType implementationType) {
        this.implementationType = implementationType;
    }

    public enum SwitchImplementationType{
        IF_STATEMENTS,
        JUMP_TABLE,
        DIRECT_CALCULATED_JUMP
    }

}
