package nl.ou.debm.common.ortharray;

import nl.ou.debm.common.feature2.NormalForm;

import java.util.Arrays;

public interface IOrthogonalArray {
    int[] iGetFactors();
    int iGetNRuns();
    int iGetStrength();
    default boolean bIsValidFor(int[] iFactor, int iNRuns, int iStrength){
        if (iNRuns!=iGetNRuns()){
            return false;
        }
        if (iStrength!=iGetStrength()){
            return false;
        }
        return bIntArraysALike(iFactor, iGetFactors());
    }
    String[][] getArrayData();

    /**
     * compare two int-arrays
     *
     * @param a1 array 1
     * @param a2 array 2
     * @return alike when they both are null, or otherwise have the same contents
     */
    private boolean bIntArraysALike(int[] a1, int[] a2) {
        if (a1 == a2) {
            return true;
        }
        if ((a1 == null) || (a2 == null)) {
            return false;
        }
        if (a1.length != a2.length) {
            return false;
        }
        for (int p = 0; p < a1.length; p++) {
            if (a1[p] != a2[p]) {
                return false;
            }
        }
        return true;
    }
}
