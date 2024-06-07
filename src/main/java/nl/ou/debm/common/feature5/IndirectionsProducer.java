package nl.ou.debm.common.feature5;

import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.Misc;
import nl.ou.debm.producer.*;

import java.util.*;

import static nl.ou.debm.common.feature5.ProducedSwitchInfo.IMAXMINIMUMNUMBEROFSWITCHCASEDUMMIES;
import static nl.ou.debm.common.feature5.ProducedSwitchInfo.IMINIMUMNUMBEROFSWITCHCASEDUMMIES;

public class IndirectionsProducer implements IFeature, IStatementGenerator, IFunctionGenerator {

    ///////////
    // settings
    ///////////
    /** maximum nesting level (=max number of parents for a switch */   public static final int IMAXSWITCHNESTING = 0;
    /** minimum number of switches wanted */                            public static final int IMINIMUMSWITCHESWANTEDLOW = 23;
    /** highest minimum number of switches wanted */                    public static final int IMINIMUMSWITCHESWANTEDHIGH = 43;

    /////////////////////////////////
    // class attributes/methods etc.
    /////////////////////////////////

    /** switch info repo */                     private static final List<ProducedSwitchInfo> s_SwitchInfoRepo = new ArrayList<>();
    /** repo current index */                   private static int s_iNextRepoIndex = 0;
    /** repo lock */                            private final static Object s_objRepoLock = new Object();
    /** current nesting level per function */   private final static Map<Function, Integer> s_NestingLevelMap = new HashMap<>();

    static {
        // fill static repo
        ProducedSwitchInfo.getSwitchInfoRepo(s_SwitchInfoRepo);
        // and shuffle for the first time
        Collections.shuffle(s_SwitchInfoRepo, Misc.rnd);
    }

    /**
     * Get the next switch definition. When the repo is exhausted, all factored properties are
     * refactored and the resulting repo is shuffled for good measure. Thread safe.
     * @return a new switch definition
     */
    private static ProducedSwitchInfo getNextSwitchInfo(){
        ProducedSwitchInfo out;
        synchronized (s_objRepoLock){
            // return deep copy
            out = new ProducedSwitchInfo(s_SwitchInfoRepo.get(s_iNextRepoIndex));
            // prepare next case; when all cases are use, start again, refactor properties and shuffle (for good measure ;-))
            s_iNextRepoIndex++;
            if (s_iNextRepoIndex>=s_SwitchInfoRepo.size()){
                s_iNextRepoIndex=0;
                ProducedSwitchInfo.refactorOASwitchProperties(s_SwitchInfoRepo);
                Collections.shuffle(s_SwitchInfoRepo, Misc.rnd);
            }
        }
        return out;
    }


    ////////////////////////////////////
    // instance attributes/methods etc.
    ////////////////////////////////////

    /** reference to the C generator object */              private final CGenerator m_cgenerator;
    /** number of switches wanted */                        private final int m_iSwitchesWanted;
    /** number of switches produced */                      private int m_iNSwitchesProduced;

    public IndirectionsProducer(CGenerator generator) {
        this.m_cgenerator = generator;
        this.m_iSwitchesWanted = Misc.rnd.nextInt(IMINIMUMSWITCHESWANTEDLOW, IMINIMUMSWITCHESWANTEDHIGH);
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

        // count switches created (for recursion purposes, we count now instead of in the end)
        m_iNSwitchesProduced++;

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
        var switchMarker = new IndirectionsCodeMarker(EIndirectionMarkerLocationTypes.BEFORE);
        switchMarker.setSwitchID(switchId);
        switchMarker.setUseBreaks(si.bGetUseBreaks());
        switchMarker.setDefaultBranch(si.bGetUseDefault());
        switchMarker.setMultipleIndicesPerCase(si.bGetMultipleIndicesPerCase());
        switchMarker.setCaseNumbering(si.getCaseNumberingType());
        switchMarker.setCaseInterval(si.iGetCaseInterval());
        switchMarker.setEqualCaseSize(si.bGetMakeCasesEquallyLong());
        switchMarker.setCases(si.getCaseInfo());
        result.add(switchMarker.strPrintfInteger(switchSubject));

        // switch statement start
        result.add("switch (" + switchSubject + "){");

        // make cases
        for (var ci : si.getCaseInfo()){
            // add case label
            result.add("\tcase " + ci.iCaseIndex + ":");
            // only add code when wanted
            if (ci.bFillCase){
                addCaseCode(result, si, ci.iCaseIndex, currentDepth, f);
            }
        }

        // make default branch, when requested
        if (si.bGetUseDefault()){
            result.add("\tdefault:");
            addCaseCode(result, si, -1, currentDepth, f);
        }

        // switch statement end
        result.add("}");
        var endMarker = new IndirectionsCodeMarker(EIndirectionMarkerLocationTypes.AFTER, switchId);
        result.add(endMarker.strPrintf());

        // lower nesting level for this function
        iCurrentNestingLevel--;
        synchronized (s_NestingLevelMap) {
            s_NestingLevelMap.put(f, iCurrentNestingLevel);
        }

        // return output;
        return result;
    }

    /**
     * Add code for a single switch-case to the list
     * @param out list to add the code to
     * @param si settings for this switch
     * @param iCaseIndex current switch case index ("case #")
     * @param iCurrentDepth current function recursion depth
     * @param f current function
     */
    private void addCaseCode(List<String> out, ProducedSwitchInfo si, int iCaseIndex, int iCurrentDepth, Function f){
        // case contents
        List<String> caseContents = new ArrayList<>();

        var caseStartMarker = new IndirectionsCodeMarker(EIndirectionMarkerLocationTypes.CASEBEGIN, si.lngGetSwitchID());
        caseStartMarker.setCaseID(iCaseIndex);
        caseContents.add(caseStartMarker.strPrintf());

        // equal length or random contents?
        if (!si.bGetMakeCasesEquallyLong()){
            // equal length means just a start and end code marker (possibly a break)
            // unequal length means adding autogenerated code
            List<String> extraCode = new ArrayList<>();
            int iWantedSize = Misc.rnd.nextInt(IMINIMUMNUMBEROFSWITCHCASEDUMMIES, IMAXMINIMUMNUMBEROFSWITCHCASEDUMMIES);
            while (extraCode.size()<iWantedSize) {
                extraCode.addAll(m_cgenerator.getNewStatements(iCurrentDepth + 1, f));
            }
            caseContents.addAll(extraCode);
        }

        var caseEndMarker = new IndirectionsCodeMarker(EIndirectionMarkerLocationTypes.CASEEND, si.lngGetSwitchID());
        caseEndMarker.setCaseID(iCaseIndex);
        caseContents.add(caseEndMarker.strPrintf());

        // add break? (when wanted in general, but never for a default branch)
        if ((si.bGetUseBreaks()) && (iCaseIndex>=0)){
            caseContents.add("break;");
        }

        // add case contents to output, with indentation
        for (var s : caseContents){
            if (!s.isBlank()) {
                int p;
                for (p=s.length()-1; p>=0 ; p--) {
                    if (s.charAt(p) != '\n'){
                        break;
                    }
                }
                if (p==(s.length()-1)) {
                    out.add("\t\t" + s);
                }
                else{
                    out.add("\t\t" + s.substring(0, p+1));
                }
            }
        }
    }

    /**
     * check whether prefs allow for switch statement
     * @param prefs prefs to check
     * @return true when ok
     */
    private boolean bMeetsPrefs(StatementPrefs prefs){
        if (prefs==null){
            return true;
        }
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
        return m_iNSwitchesProduced >= m_iSwitchesWanted;
    }

    @Override
    public EFeaturePrefix getPrefix() {
        return EFeaturePrefix.INDIRECTIONSFEATURE;
    }

    @Override
    public Function getNewFunction(int currentDepth, DataType type, EWithParameters withParameters) {
        assert m_cgenerator != null : "No C-generator object";

        // basics: data type and empty function object
        if (type == null) {
            type = m_cgenerator.getRawDataType();
        }
        var function = new Function(type);    // use auto-name constructor

        // add a parameter, when requested
        if(withParameters != EWithParameters.NO){
            function.addParameter(new FunctionParameter("p" + 1, m_cgenerator.getRawDataType()));
        }

        // add loop statements
        var list = new ArrayList<String>();
        this.getNewStatements(currentDepth, function, null);
        function.addStatements(list);

        // add return statement
        if(type.getName().equals("void")) {
            function.addStatement("return;");
        }
        else {
            function.addStatement("return " + type.strDefaultValue(m_cgenerator.structsByName) + ";");
        }

        // and done ;-)
        return function;
    }
}
