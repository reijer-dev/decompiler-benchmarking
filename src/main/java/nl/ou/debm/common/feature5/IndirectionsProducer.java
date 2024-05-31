package nl.ou.debm.common.feature5;

import nl.ou.debm.common.BaseCodeMarker;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        var result = new ArrayList<String>();
        if (inSwitch || prefs.numberOfStatements == ENumberOfStatementsPref.MULTIPLE)
            return result;
        inSwitch = true;
        var switchId = _random.nextInt(100000, 999999);
        var switchSubject = "switchSubj" + switchId;
        result.add("int " + switchSubject + " = (int)getchar();");
        var switchMarker = new BaseCodeMarker(EFeaturePrefix.INDIRECTIONSFEATURE);
        switchMarker.setProperty("switchId", String.valueOf(switchId));
        result.add(switchMarker.strPrintf());
        result.add("switch (" + switchSubject + "){");
        for (var i = 0; i < _random.nextInt(1, 20); i++) {
            result.add("\tcase " + i + ":");
            var caseMarker = new BaseCodeMarker(EFeaturePrefix.INDIRECTIONSFEATURE);
            caseMarker.setProperty("caseId", String.valueOf(i));
            result.add(caseMarker.strPrintf());
            result.addAll(generator.getNewStatements(currentDepth + 1, f));
        }
        result.add("}");
        _switchedProduced++;
        inSwitch = false;
        return result;
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
