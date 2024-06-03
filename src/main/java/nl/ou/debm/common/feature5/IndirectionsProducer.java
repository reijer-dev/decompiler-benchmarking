package nl.ou.debm.common.feature5;

import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.Misc;
import nl.ou.debm.producer.*;

import java.util.*;

public class IndirectionsProducer implements IFeature, IStatementGenerator {

    private int _switchedProduced = 0;
    private final static int SWITCHES_WANTED = 15;
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
    /** current nesting level per function */   private final static Map<Function, Integer> s_NestingLevelMap = new HashMap();

    static {
        // fill repo
        SwitchInfo.getSwitchInfoRepo(s_SwitchInfoRepo);
        Collections.shuffle(s_SwitchInfoRepo, Misc.rnd);
    }

    private static SwitchInfo getNextSwitchInfo(){
        SwitchInfo out;
        synchronized (s_objRepoLock){
            // return deep copy
            out = new SwitchInfo(s_SwitchInfoRepo.get(s_iNextRepoIndex));
            // prepare next case; when all cases are use, start again, refactor properties and shuffle (for good measure ;-))
            s_iNextRepoIndex++;
            if (s_iNextRepoIndex>=s_SwitchInfoRepo.size()){
                s_iNextRepoIndex=0;
                SwitchInfo.refactorOASwitchProperties(s_SwitchInfoRepo);
                Collections.shuffle(s_SwitchInfoRepo, Misc.rnd);
            }
        }
        return out;
    }


    ////////////////////////////////////
    // instance attributes/methods etc.
    ////////////////////////////////////

    public IndirectionsProducer(CGenerator generator) {
        this.generator = generator;
    }

    @Override
    public List<String> getNewStatements(int currentDepth, Function f, StatementPrefs prefs) {
        // make sure that statement preferences allow for a switch
        if (!bMeetsPrefs(prefs)){
            return null;
        }

        // get nesting level per function
        int iCurrentNestingLevel;
        synchronized (s_NestingLevelMap) {
            Integer i;
            i = s_NestingLevelMap.get(f);
            if (i==null){
                iCurrentNestingLevel = 0;
            }
            else {
                iCurrentNestingLevel = i;
            }
        }

        // make sure no unwanted nesting takes place
        if (iCurrentNestingLevel>IMAXSWITCHNESTING){
            return null;
        }

        // set new nesting level for this function
        iCurrentNestingLevel++;
        s_NestingLevelMap.put(f, iCurrentNestingLevel);

        // get what switch to produce
        var si = getNextSwitchInfo();

        // init output
        var result = new ArrayList<String>();

        // switch ID + var
        var switchId = si.lngGetSwitchID();
        var switchSubject = "SV_" + switchId;

        // declare + init var and print code marker
        // by using the var in the code marker, we prevent the compiler to
        // switch the init and the code marker
        result.add("int " + switchSubject + " = (int)getchar();");
        var switchMarker = new IndirectionCodeMarker(EIndirectionMarkerLocationTypes.BEFORE);
        switchMarker.setSwitchID(switchId);
        result.add(switchMarker.strPrintfInteger(switchSubject));

        // switch statement start
        result.add("switch (" + switchSubject + "){");



        for (var i = 0; i < Misc.rnd.nextInt(1, 20); i++) {
            result.add("\tcase " + i + ":");
            var caseMarker = new IndirectionCodeMarker(EIndirectionMarkerLocationTypes.CASEBEGIN);
            caseMarker.setCaseID(i);
            result.add(caseMarker.strPrintf());

            result.addAll(generator.getNewStatements(currentDepth + 1, f));

            caseMarker = new IndirectionCodeMarker(EIndirectionMarkerLocationTypes.CASEEND);
            caseMarker.setCaseID(i);
            result.add(caseMarker.strPrintf());
        }

        // switch statement end
        result.add("}");
        _switchedProduced++;

        // lower nesting level for this function
        iCurrentNestingLevel--;
        synchronized (s_NestingLevelMap) {
            s_NestingLevelMap.put(f, iCurrentNestingLevel);
        }

        // return output;
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
        return _switchedProduced >= SWITCHES_WANTED;
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
