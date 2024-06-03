package nl.ou.debm.common.feature5;

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


    ///////////
    // settings
    ///////////
    /** maximum nesting level (=max number of parents for a switch */   public static final int IMAXSWITCHNESTING = 0;

    /////////////////////////////////
    // class attributes/methods etc.
    /////////////////////////////////

    /** switch info repo */                     private static final List<SwitchInfo> s_SwitchInfoRepo = new ArrayList<>();
    /** repo current index */                   private static int s_iNextRepoIndex = 0;
    /** repo lock */                            private final static Object s_objRepoLock = new Object();

    static {
        // fill repo
        SwitchInfo.getSwitchInfoRepo(s_SwitchInfoRepo);
    }

    private static SwitchInfo getNextSwitchInfo(){
        SwitchInfo out;
        synchronized (s_objRepoLock){
            // return deep copy
            out = new SwitchInfo(s_SwitchInfoRepo.get(s_iNextRepoIndex));
            // prepare next case; when all cases are use, start again and refactor properties
            s_iNextRepoIndex++;
            if (s_iNextRepoIndex>=s_SwitchInfoRepo.size()){
                s_iNextRepoIndex=0;
                SwitchInfo.refactorOASwitchProperties(s_SwitchInfoRepo);
            }
        }
        return out;
    }


    ////////////////////////////////////
    // instance attributes/methods etc.
    ////////////////////////////////////

    /** keep track of nesting */                        private int m_iCurrentNestingLevel = 0;

    public IndirectionsProducer(CGenerator generator) {
        this.generator = generator;
    }

    @Override
    public List<String> getNewStatements(int currentDepth, Function f, StatementPrefs prefs) {
        // make sure that statement preferences allow for a switch
        if (!bMeetsPrefs(prefs)){
            return null;
        }

        // make sure no unwanted nesting takes place
        if (m_iCurrentNestingLevel>IMAXSWITCHNESTING){
            return null;
        }
        m_iCurrentNestingLevel++;

        // get what switch to produce
        var si = getNextSwitchInfo();

        // init output
        var result = new ArrayList<String>();
        var switchId = si.lngGetSwitchID();
        var switchSubject = "SV_" + switchId;
        result.add("int " + switchSubject + " = (int)getchar();");
        var switchMarker = new IndirectionCodeMarker(EIndirectionMarkerLocationTypes.BEFORE);
        switchMarker.setSwitchID(switchId);
        result.add(switchMarker.strPrintf());
        result.add("switch (" + switchSubject + "){");
        for (var i = 0; i < _random.nextInt(1, 20); i++) {
            result.add("\tcase " + i + ":");
            var caseMarker = new IndirectionCodeMarker(EIndirectionMarkerLocationTypes.CASEBEGIN);
            caseMarker.setCaseID(i);
            result.add(caseMarker.strPrintf());

            result.addAll(generator.getNewStatements(currentDepth + 1, f));

            caseMarker = new IndirectionCodeMarker(EIndirectionMarkerLocationTypes.CASEEND);
            caseMarker.setCaseID(i);
            result.add(caseMarker.strPrintf());
        }
        result.add("}");
        _switchedProduced++;



        m_iCurrentNestingLevel--;
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
