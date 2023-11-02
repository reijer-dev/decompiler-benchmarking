package nl.ou.debm.producer;

/**
 * This class serves as a struct to easily set preferences for when a statement is requested.
 */

public class StatementPrefs {
    /**
     * Default values for empty construction
     */
    public ENumberOfStatementsPref numberOfStatements = ENumberOfStatementsPref.SINGLE;
    public EStatementPref compoundStatement = EStatementPref.NOT_WANTED;
    public EStatementPref expression = EStatementPref.DON_T_CARE;
    public EStatementPref assignment = EStatementPref.DON_T_CARE;
    public EStatementPref loop = EStatementPref.NOT_WANTED;

    /**
     * empty constructor, do absolutely nothing but set the defaults as
     * explained above: <br>
     * number of statements: 1<br>
     * compound/loop: not wanted <br>
     * assignment/expression: don't care
     */
    public StatementPrefs(){;}

    /**
     * This constructor only has a parameter to distinguish from the default
     * The parameter is not used at all. The constructor makes sure that
     * the preferences are all set to 'don't care'.
     * @param o     only used to distinguish between constructors
     */
    public StatementPrefs(Object o){
        SetDonTCare();
    }

    /**
     * Set all preferences to 'don't care'.
     */
    public void SetDonTCare(){
        numberOfStatements = ENumberOfStatementsPref.DON_T_CARE;
        compoundStatement = EStatementPref.DON_T_CARE;
        expression = EStatementPref.DON_T_CARE;
        assignment = EStatementPref.DON_T_CARE;
        loop = EStatementPref.DON_T_CARE;
    }
}
