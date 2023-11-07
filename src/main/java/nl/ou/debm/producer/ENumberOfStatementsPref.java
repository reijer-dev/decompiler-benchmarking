package nl.ou.debm.producer;

/**
 * request single or multiple statements
 */
public enum ENumberOfStatementsPref {
    SINGLE,             // single statement requested
    MULTIPLE,           // multiple statements requested
    DON_T_CARE;         // single or multiple: don't care
}
