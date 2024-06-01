package nl.ou.debm.common.feature5;

import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/*

    variations:
    first case: 0 or other
    number of cases (random between 5 and 25) {<5 will probably be optimized away)
    case numbering: regular intervals (1 or other), irregular intervals (random numbers or just a few missing)
    case lengths: equal (all just one CM-call) or unequal (force dummy commands)
    case ending: break or no break

    32 combinations possible




 */


public class IndirectionsProducer implements IFeature, IStatementGenerator {

    private int _switchedProduced = 0;
    private final int SWITCHED_WANTED = 15;
    private final Random _random = new Random();
    final CGenerator generator;
    private boolean inSwitch = false;

    public IndirectionsProducer(CGenerator generator) {
        this.generator = generator;
    }

    @Override
    public List<String> getNewStatements(int currentDepth, Function f, StatementPrefs prefs) {
        if (!bMeetsPrefs(prefs)){
            return null;
        }

        var result = new ArrayList<String>();
        if (inSwitch || prefs.numberOfStatements == ENumberOfStatementsPref.MULTIPLE)
            return result;
        inSwitch = true;
        var switchId = _random.nextInt(100000, 999999);
        var switchSubject = "switchSubj" + switchId;
        result.add("int " + switchSubject + " = (int)getchar();");
        var switchMarker = new IndirectionCodeMarker(EIndirectionMarkerLocationTypes.BEFORE);
        switchMarker.setSwitchID(switchId);
        result.add(switchMarker.strPrintf());
        result.add("switch (" + switchSubject + "){");
        for (var i = 0; i < _random.nextInt(1, 20); i++) {
            result.add("\tcase " + i + ":");
            var caseMarker = new IndirectionCodeMarker(EIndirectionMarkerLocationTypes.CASE);
            caseMarker.setCaseID(i);
            result.add(caseMarker.strPrintf());
            result.addAll(generator.getNewStatements(currentDepth + 1, f));
        }
        result.add("}");
        _switchedProduced++;
        inSwitch = false;
        return result;
    }

    /**
     * check whether prefs allow for switch statement
     * @param prefs prefs to check
     * @return true when ok
     */
    private boolean bMeetsPrefs(StatementPrefs prefs){
        if (prefs.numberOfStatements==ENumberOfStatementsPref.SINGLE){
            // we produce multiple statements
            return false;
        }
        if (prefs.compoundStatement==EStatementPref.NOT_WANTED){
            // we produce a compound statement
            return false;
        }
        if (prefs.expression==EStatementPref.REQUIRED){
            // we don't do expression statements
            return false;
        }
        if (prefs.assignment==EStatementPref.REQUIRED){
            // we don't do assignments
            return false;
        }
        if (prefs.loop==EStatementPref.REQUIRED){
            // we don't do loops
            return false;
        }

        // all checked, so ok
        return true;
    }

    @Override
    public boolean isSatisfied() {
        return _switchedProduced >= SWITCHED_WANTED;
    }

    @Override
    public EFeaturePrefix getPrefix() {
        return null;
    }

    @Override
    public List<String> getIncludes() {
        return null;
    }
}
