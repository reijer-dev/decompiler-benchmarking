package nl.ou.debm.common.feature3;

import nl.ou.debm.common.EArchitecture;

public class NumericScore {
    public int actual;
    public int lowBound;
    public int highBound;
    public String name;
    public EArchitecture architecture;

    public NumericScore(String name, EArchitecture architecture, int lowBound, int highBound, int actual) {
        this.name = name;
        this.architecture = architecture;
        this.lowBound = lowBound;
        this.highBound = highBound;
        this.actual = actual;
    }
}

