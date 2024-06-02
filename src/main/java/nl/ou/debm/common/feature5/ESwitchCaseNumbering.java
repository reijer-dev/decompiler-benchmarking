package nl.ou.debm.common.feature5;

public enum ESwitchCaseNumbering {
    REGULAR,            // regular interval, (1 or more)
    BINARY,             // 1, 2, 4, 8 etc.
    SKIPPING,           // regular interval (1 or more), skipping some cases
    RANDOM;             // completely random
}
