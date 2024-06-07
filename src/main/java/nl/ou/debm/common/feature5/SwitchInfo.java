package nl.ou.debm.common.feature5;

public class SwitchInfo {
    private boolean implementedAsIndirection;
    private int numberOfBranchesInLLVM;
    private int numberOfDecompiledBranches;


    public boolean isImplementedAsIndirection() {
        return implementedAsIndirection;
    }

    public void setImplementedAsIndirection(boolean implementedAsIndirection) {
        this.implementedAsIndirection = implementedAsIndirection;
    }
}
