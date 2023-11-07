package nl.ou.debm.producer;

import java.util.List;


/**
 * Interface required for statement implementation
 */
public interface IStatementGenerator {
    /**
     * Overloaded function, will call getNewStatements(null)
     * @return  new statement(s)
     * see: {@link IStatementGenerator#getNewStatements(StatementPrefs)}
     */
    List<String> getNewStatements();

    /**
     * Will return one or more statements, according to prefs.
     * @param prefs set of preferences for statement(s). If set to
     *              null, all prefs will be set to 'don't care'.
     * @return  one or more statements. If no statements meeting the
     *          requirements can be produced, the list is empty
     */
    List<String> getNewStatements(StatementPrefs prefs);
}
