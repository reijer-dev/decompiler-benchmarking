package nl.ou.debm.common.ortharray;

/**
 * Interface for orthogonal array
 * Necessary because hard coding takes a lot of data and JVM does not like that...
 */
public interface IOrthogonalArray {
    /**
     * This OA is valid for these factors
     * @return set of factors, sorted high to low
     */
    int[] iGetFactors();

    /**
     * This OA uses this many runs
     * @return number of runs in this OA.
     */
    int iGetNRuns();

    /**
     * This OA uses this strength
     * @return strength of this OA
     */
    int iGetStrength();

    /**
     * Test if this OA is valid with the requested parameters
     * @param iFactor array of factors, sorted high to low
     * @param iNRuns number of runs
     * @param iStrength strength
     * @return true is this OA implements the requested pars
     */
    default boolean bIsValidFor(int[] iFactor, int iNRuns, int iStrength){
        if (iNRuns!=iGetNRuns()){
            return false;
        }
        if (iStrength!=iGetStrength()){
            return false;
        }
        return bIntArraysALike(iFactor, iGetFactors());
    }

    /**
     * Use this function to hard code the OA in
     * @return array of string arrays. All cases in one run are a string. A set of strings makes up one OA. Sets of
     * these sets make up multiple OA's.
     */
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
