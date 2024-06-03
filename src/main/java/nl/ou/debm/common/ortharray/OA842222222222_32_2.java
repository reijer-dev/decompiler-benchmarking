package nl.ou.debm.common.ortharray;

public class OA842222222222_32_2 implements IOrthogonalArray{
    @Override
    public int[] iGetFactors() {
        return new int[]{8,4,2,2,2,2,2,2,2,2,2,2};
    }

    @Override
    public int iGetNRuns() {
        return 32;
    }

    @Override
    public int iGetStrength() {
        return 2;
    }

    @Override
    public String[][] getArrayData() {
        return new String[][] {{
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
                "730010110001"}
        };
    }
}
