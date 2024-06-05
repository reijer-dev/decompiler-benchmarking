package nl.ou.debm.common.ortharray;

import nl.ou.debm.common.Misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles orthogonal arrays. The arrays themselves are stored in classes implementing IOrthogonalArray.
 *
 * We used OA Package to help us: https://oapackage.readthedocs.io/en/latest/oapackage-intro.html
 */
public class OrthogonalArray {

    private final static List<IOrthogonalArray> s_oa = new ArrayList<>();
    private String[] m_oa;

    static {
        s_oa.add(new OA4222_16_2());
        s_oa.add(new OA622222_48_2());
        s_oa.add(new OA84222222_32_2());
        s_oa.add(new OA842222222_32_2());
        s_oa.add(new OA842222222222_32_2());
    }

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

    public void CreateOrthogonalArray(int[] iFactorLevels, int iNRuns, int iStrength) {
        // return array based on input

        for (var oa : s_oa){
            if (oa.bIsValidFor(iFactorLevels, iNRuns, iStrength)){
                String[][] ad = oa.getArrayData();
                m_oa = ad[Misc.rnd.nextInt(ad.length)];
                return;
            }
        }

        throw new RuntimeException("No implementation for requested orthogonal array");
    }
}