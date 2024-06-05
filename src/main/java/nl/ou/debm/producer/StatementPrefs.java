package nl.ou.debm.producer;

/**
 * This class serves as a struct to easily set preferences for when a statement is requested.
 */

public class StatementPrefs {
    /**
     * Default values for empty construction
     */
    /** number of statements requested */       public ENumberOfStatementsPref numberOfStatements = ENumberOfStatementsPref.SINGLE;
    /** compound statement requested */         public EStatementPref compoundStatement = EStatementPref.NOT_WANTED;
    /** expression statement requested */       public EStatementPref expression = EStatementPref.DON_T_CARE;
    /** assignment expression requested */      public EStatementPref assignment = EStatementPref.DON_T_CARE;
    /** loop statement requested*/              public EStatementPref loop = EStatementPref.NOT_WANTED;
    /** number of nested loops allowed */       public int iAllowHowManyLevelsOfNestedLoops = 0;

    /**
     * empty constructor, do absolutely nothing but set the defaults as
     * explained above:<br>
     * number of statements: 1<br>
     * compound/loop: not wanted<br>
     * assignment/expression: don't care<br>
     * nested loops: not allowed
     */
    public StatementPrefs(){;}

    /**
     * Copy constructor
     * When right hand side is null, all preferences are set to 'don't care'
     * @param rhs     source object, when null: all prefs are set to don't care
     */
    public StatementPrefs(StatementPrefs rhs) {
        if (rhs==null){
            SetDonTCare();
        }
        else{
            numberOfStatements = rhs.numberOfStatements;
            compoundStatement = rhs.compoundStatement;
            expression = rhs.expression;
            assignment = rhs.assignment;
            loop = rhs.loop;
            iAllowHowManyLevelsOfNestedLoops = rhs.iAllowHowManyLevelsOfNestedLoops;
        }
    }

    /**
     * Set all preferences to 'don't care'.
     * The number of maximum allowed nested loops is set to 10
     */
    public void SetDonTCare(){
        numberOfStatements = ENumberOfStatementsPref.DON_T_CARE;
        compoundStatement = EStatementPref.DON_T_CARE;
        expression = EStatementPref.DON_T_CARE;
        assignment = EStatementPref.DON_T_CARE;
        loop = EStatementPref.DON_T_CARE;
        iAllowHowManyLevelsOfNestedLoops = 10;
    }
}
