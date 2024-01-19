package nl.ou.debm.common.feature3;

import nl.ou.debm.common.EArchitecture;

public class BooleanScore {
    public boolean expected;
    public boolean actual;
    public String name;
    public EArchitecture architecture;

    public BooleanScore(String name, EArchitecture architecture, boolean expected, boolean actual) {
        this.name = name;
        this.architecture = architecture;
        this.expected = expected;
        this.actual = actual;
    }
}
