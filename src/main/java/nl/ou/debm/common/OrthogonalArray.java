package nl.ou.debm.common;

import nl.ou.debm.common.ortharray.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class stores orthogonal arrays. At the moment, all arrays only have 1 implementation. This suffices
 * for now. In the future, storing multiple arrays per factor would add to randomness. It would also be
 * possible to dynamically create OA's, but that is all way too much work for now.
 *
 * We used OA Package to help us: https://oapackage.readthedocs.io/en/latest/oapackage-intro.html
 */
public class OrthogonalArray {

    private final static List<IOrthogonalArray> s_oa = new ArrayList<>();

    static {
        s_oa.add(new OA4222_16_2());
        s_oa.add(new OA622222_48_2());
        s_oa.add(new OA84222222_32_2());
        s_oa.add(new OA842222222_32_2());
        s_oa.add(new OA842222222222_32_2());
    }

    private String[] m_oa;

    /**
     * Construct an orthogonal array
     *
     * @param iFactorLevels array containing the factor levels, should be sorted high to low
     * @param iNRuns        number of runs requested
     * @param iStrength     OA-strength
     */
    public OrthogonalArray(int[] iFactorLevels, int iNRuns, int iStrength) {
        CreateOrthogonalArray(iFactorLevels, iNRuns, iStrength);
    }

    /**
     * Return the number of runs
     *
     * @return the number of runs
     */
    public int iNRuns() {
        return m_oa.length;
    }

    /**
     * Return number of columns
     *
     * @return number of columns
     */
    public int iNColumns() {
        return m_oa[0].length();
    }

    /**
     * Get the value per run/column
     *
     * @param iRun    number of the run
     * @param iColumn number of the column
     * @return value, or -1 for illegal inputs
     */
    public int iValuePerRunPerColumn(int iRun, int iColumn) {
        if ((iRun < 0) || (iRun >= iNRuns())) {
            return -1;
        }
        if ((iColumn < 0) || (iColumn >= iNColumns())) {
            return -1;
        }
        return m_oa[iRun].charAt(iColumn) - '0';
    }

    /**
     * Make a map of all the unique combinations for the given set of columns and count their frequencies
     *
     * @param iColumnPointer array containing the columns to be examined
     * @return the map, indexed by combinations
     */
    public Map<String, Integer> iCombinationFrequencies(int[] iColumnPointer) {
        Map<String, Integer> out = new HashMap<>();
        var sb = new StringBuilder();
        for (int run = 0; run < iNRuns(); ++run) {
            sb.setLength(0);
            for (int i : iColumnPointer) {
                sb.append(iValuePerRunPerColumn(run, i));
            }
            int oldVal = 0;
            if (out.containsKey(sb.toString())) {
                oldVal = out.get(sb.toString());
            }
            out.put(sb.toString(), ++oldVal);
        }
        return out;
    }

    /**
     * return an orthogonal array, or an RTE when no array is present for parameters
     * @param iFactorLevels array of factor levels; must be sorted highest first
     * @param iNRuns number of runs requested
     * @param iStrength strength of the array
     */
    public void CreateOrthogonalArray(int[] iFactorLevels, int iNRuns, int iStrength) {
        for (var oa : s_oa){
            if (oa.bIsValidFor(iFactorLevels, iNRuns, iStrength)){
                var ad = oa.getArrayData();
                m_oa = ad[Misc.rnd.nextInt(ad.length)];
                return;
            }
        }

        throw new RuntimeException("No implementation for requested orthogonal array");
    }

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





