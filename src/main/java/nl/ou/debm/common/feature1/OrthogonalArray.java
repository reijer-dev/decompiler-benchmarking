package nl.ou.debm.common.feature1;

import java.util.HashMap;
import java.util.Map;

/**
 * This class stores orthogonal arrays. At the moment, all arrays only have 1 implementation. This suffices
 * for now. In the future, storing multiple arrays per factor would add to randomness. It would also be
 * possible to dynamically create OA's, but that is all way too much work for now.
 *
 * We used OA Package to help us: https://oapackage.readthedocs.io/en/latest/oapackage-intro.html
 */
public class OrthogonalArray
{
    private final static String[] s_oa4222_16_2 = {
            "0001",
            "0010",
            "0100",
            "0111",
            "1001",
            "1010",
            "1100",
            "1111",
            "2001",
            "2010",
            "2100",
            "2111",
            "3001",
            "3010",
            "3100",
            "3111"
    };

    private final static String[] s_oa2 = {
            "000000000",
            "000000011",
            "000111100",
            "000111111",
            "011001100",
            "011001111",
            "011110000",
            "011110011",
            "101010101",
            "101010110",
            "101101001",
            "101101010",
            "110011001",
            "110011010",
            "110100101",
            "110100110"
    };

    private final static String[] s_oa_842222222222_32_2 = {
            "000000000000",
            "010000000111",
            "021111111000",
            "031111111111",
            "100001111000",
            "110001111111",
            "121110000000",
            "131110000111",
            "200110011011",
            "210110011100",
            "221001100011",
            "231001100100",
            "300111100011",
            "310111100100",
            "321000011011",
            "331000011100",
            "401010101101",
            "411010101010",
            "420101010101",
            "430101010010",
            "501011010101",
            "511011010010",
            "520100101101",
            "530100101010",
            "601100110110",
            "611100110001",
            "620011001110",
            "630011001001",
            "701101001110",
            "711101001001",
            "720010110110",
            "730010110001"
    };

    private String [] m_oa;

    /**
     * Construct an orthogonal array
     * @param iFactorLevels  array containing the factor levels, should be sorted high to low
     * @param iNRuns number of runs requested
     * @param iStrength OA-strength
     */
    public OrthogonalArray(int[] iFactorLevels, int iNRuns, int iStrength) {
        CreateOrthogonalArray(iFactorLevels, iNRuns, iStrength);
    }

    /**
     * Return the number of runs
     * @return the number of runs
     */
    public int iNRuns(){
        return m_oa.length;
    }

    /**
     * Return number of columns
     * @return number of columns
     */
    public int iNColumns(){
        return m_oa[0].length();
    }

    /**
     * Get the value per run/column
     * @param iRun number of the run
     * @param iColumn number of the column
     * @return value, or -1 for illegal inputs
     */
    public int iValuePerRunPerColumn(int iRun, int iColumn){
        if ((iRun<0) || (iRun>=iNRuns())){
            return -1;
        }
        if ((iColumn<0) || (iColumn>=iNColumns())){
            return -1;
        }
        return m_oa[iRun].charAt(iColumn) - '0';
    }

    /**
     * Make a map of all the unique combinations for the given set of columns and count their frequencies
     * @param iColumnPointer array containing the columns to be examined
     * @return the map, indexed by combinations
     */
    public Map<String, Integer> iCombinationFrequencies(int[] iColumnPointer){
        Map<String, Integer> out = new HashMap<>();
        var sb = new StringBuilder();
        for (int run=0; run< iNRuns(); ++run){
            sb.setLength(0);
            for (int i : iColumnPointer) {
                sb.append(iValuePerRunPerColumn(run, i));
            }
            int oldVal=0;
            if (out.containsKey(sb.toString())){
                oldVal = out.get(sb.toString());
            }
            out.put(sb.toString(), ++oldVal);
        }
        return out;
    }

    public void CreateOrthogonalArray(int[] iFactorLevels, int iNRuns, int iStrength) {
        // return array based on input

        {
            int[] fl = {4, 2, 2, 2};
            if ((bIntArraysALike(fl, iFactorLevels)) && (iNRuns == 16) && (iStrength == 2)) {
                m_oa = s_oa4222_16_2;
                return;
            }
        }

        {
            int[] fl = {8, 4, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
            if ((bIntArraysALike(fl, iFactorLevels)) && (iNRuns == 32) && (iStrength == 2)) {
                m_oa = s_oa_842222222222_32_2;
                return;
            }
        }

        throw new RuntimeException("No implementation for requested orthogonal array");
    }

    /**
     * compare two int-arrays
     * @param a1 array 1
     * @param a2 array 2
     * @return alike when they both are null, or otherwise have the same contents
     */
    private boolean bIntArraysALike(int[] a1, int[] a2){
        if (a1==a2) {
            return true;
        }
        if ((a1==null) || (a2==null)){
            return false;
        }
        if (a1.length != a2.length){
            return false;
        }
        for (int p=0; p<a1.length; p++){
            if (a1[p]!=a2[p]){
                return false;
            }
        }
        return true;
    }
}
