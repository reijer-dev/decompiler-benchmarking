package nl.ou.debm.producer;

import java.util.List;

public interface IStatementGenerator {
    List<String> getNewStatement();
    List<String> getNewStatement(StatementPrefs prefs);
}
