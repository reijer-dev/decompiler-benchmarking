package nl.ou.debm.common.ortharray;

public class OA4222_16_2 implements IOrthogonalArray{
    @Override
    public int[] iGetFactors() {
        return new int[]{4,2,2,2};
    }

    @Override
    public int iGetNRuns() {
        return 16;
    }

    @Override
    public int iGetStrength() {
        return 2;
    }

    @Override
    public String[][] getArrayData() {
        return new String[][] {{
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
                "3111"}
        };
    }
}
