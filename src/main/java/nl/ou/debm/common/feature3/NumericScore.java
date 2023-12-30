package nl.ou.debm.common.feature3;

public class NumericScore {
    public int actual;
    public int lowBound;
    public int highBound;

    public NumericScore(int lowBound, int highBound, int actual) {
        this.lowBound = lowBound;
        this.highBound = highBound;
        this.actual = actual;
        if(actual != highBound)
        {
            System.out.println(actual + "!=" + highBound);
        }
    }
}

