package nl.ou.debm.common.feature5;

public class SwitchInfo {
    private Boolean implementedAsIndirection = null;
    private int numberOfBranchesInLLVM;
    private int numberOfDecompiledBranches;


    public Boolean isImplementedAsIndirection() {
        return implementedAsIndirection;
    }

    public void setImplementedAsIndirection(Boolean implementedAsIndirection) {
        this.implementedAsIndirection = implementedAsIndirection;
    }
}
