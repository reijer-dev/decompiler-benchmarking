package nl.ou.debm.common.feature5;

/*

    variations:
    first case: 0 or other
    number of cases (random between 5 and 25) {<5 will probably be optimized away)
    case numbering: regular intervals (1 or other), irregular intervals (random numbers or just a few missing)
    case lengths: equal (all just one CM-call) or unequal (force dummy commands)
    case ending: break or no break
    default present or not




*/

import nl.ou.debm.common.Misc;
import nl.ou.debm.common.OrthogonalArray;

import java.util.ArrayList;
import java.util.List;

public class SwitchInfo {

    // public constants
    /** minimal value of non-zero first index*/     public final static int IFIRSTNONZEROINDEXLOW = 5;
    /** maximal value of non-zero first index*/     public final static int IFIRSTNONZEROINDEXHIGH = 23;
    /** minimal value of non-one case interval */   public final static int INONONECASEINTERVALLOW = 2;
    /** maximal value of non-one case interval */   public final static int INONONECASEINTERVALHIGH = 11;



    // class attributes
    /** repo containing all wanted switch types */  private final static List<SwitchInfo> s_SwitchRepo = new ArrayList<>();
    /** next switch ID */                           private static long s_lngNextSwitchID = 0;
    /** switch ID Lock */                           private static final Object s_SwitchIDLock = new Object();

    /** number of runs used in the OA*/             private final static int INUMBEROFOARUNS = 48;

    // class init/ methods
    static {
        // static init

        // maka empty repo
        for (int n=0;n<INUMBEROFOARUNS;n++){
            s_SwitchRepo.add(new SwitchInfo());
        }

        // fill repo stats
        refactorOASwitchProperties(s_SwitchRepo);
    }

    /**
     * get switch info repo, make deep copy of the internally built repo
     * @param repo_out must be valid object, will be filled with the switch info
     */
    public static void getSwitchInfoRepo(List<SwitchInfo> repo_out){
        repo_out.clear();
        for (var si : s_SwitchRepo){
            repo_out.add(new SwitchInfo(si));
        }
    }

    /**
     * refactor all properties in a switch info repo, using a random OA
     * @param switchRepo repo to be refacored
     */
    public static void refactorOASwitchProperties(List<SwitchInfo> switchRepo){
        // OA properties + lookup
        final int [] LEVELS = {6, 2, 2, 2, 2, 2};
        final int STRENGTH = 2;
        var oa = new OrthogonalArray(LEVELS, INUMBEROFOARUNS, STRENGTH);

        // OA Columns
        final int COL_NUMBERING = 0;
        final int COL_STARTVALUE = 1;
        final int COL_CASELENGTH = 2;
        final int COL_MULTIPLEINDICES = 3;
        final int COL_BREAKS = 4;
        final int COL_DEFAULT = 5;

        int iRun = -1;
        for (var si : switchRepo){
            iRun++;

            // start value
            if (oa.iValuePerRunPerColumn(iRun, COL_STARTVALUE) == 0){
                si.m_iFirstCaseIndex = 0;
            }
            else {
                si.m_iFirstCaseIndex = Misc.rnd.nextInt(IFIRSTNONZEROINDEXLOW, IFIRSTNONZEROINDEXHIGH);
            }

            // type of numbering
            switch (oa.iValuePerRunPerColumn(iRun, COL_NUMBERING)){
                case 0 -> {
                    si.m_eCaseNumbering = ESwitchCaseNumbering.REGULAR;
                    si.m_iCaseInterval = 1;
                }
                case 1 -> {
                    si.m_eCaseNumbering = ESwitchCaseNumbering.REGULAR;
                    si.m_iCaseInterval = Misc.rnd.nextInt(INONONECASEINTERVALLOW, INONONECASEINTERVALHIGH);
                }
                case 2 -> {
                    si.m_eCaseNumbering = ESwitchCaseNumbering.BINARY;
                    si.m_iCaseInterval = -1;
                }
                case 3 -> {
                    si.m_eCaseNumbering = ESwitchCaseNumbering.SKIPPING;
                    si.m_iCaseInterval = 1;
                }
                case 4 -> {
                    si.m_eCaseNumbering = ESwitchCaseNumbering.SKIPPING;
                    si.m_iCaseInterval = Misc.rnd.nextInt(INONONECASEINTERVALLOW, INONONECASEINTERVALHIGH);
                }
                case 5 -> {
                    si.m_eCaseNumbering = ESwitchCaseNumbering.RANDOM;
                    si.m_iCaseInterval = -1;
                }
            }

            // simple binaries
            si.m_bMakeCasesEquallyLong =                (oa.iValuePerRunPerColumn(iRun, COL_CASELENGTH) == 1);
            si.m_bUseMultipleIndices =                  (oa.iValuePerRunPerColumn(iRun, COL_MULTIPLEINDICES) == 1);
            si.m_bUseBreaks =                           (oa.iValuePerRunPerColumn(iRun, COL_BREAKS) == 1);
            si.m_bUseDefault =                          (oa.iValuePerRunPerColumn(iRun, COL_DEFAULT) == 1);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    // object attributes
    /** int value of the first case */              private int m_iFirstCaseIndex = 0;
    /** number of cases */                          private int m_iNCases = 5;
    /** case numbering */                           private ESwitchCaseNumbering m_eCaseNumbering = ESwitchCaseNumbering.REGULAR;
    /** case interval when regular/skipping */      private int m_iCaseInterval = 1;
    /** all cases have similar code */              private boolean m_bMakeCasesEquallyLong = true;
    /** use multiple indices per case */            private boolean m_bUseMultipleIndices = false;
    /** use breaks at the end of cases */           private boolean m_bUseBreaks = true;
    /** use default */                              private boolean m_bUseDefault = true;
    /** switch ID */                                private long m_lngSwitchID = 0;

    // object getters/setters
    public void setFirstCaseIndex(int iFirstCaseIndex){
        m_iFirstCaseIndex = iFirstCaseIndex;
    }
    public int iGetFirstCaseIndex(){
        return m_iFirstCaseIndex;
    }
    public void setNCases(int iNCases){
        m_iNCases = iNCases;
    }
    public int getNCases(){
        return m_iNCases;
    }
    public void setCaseNumberingType(ESwitchCaseNumbering type){
        m_eCaseNumbering = type;
    }
    public ESwitchCaseNumbering getCaseNumberingType(){
        return m_eCaseNumbering;
    }
    public void setCaseInterval(int iCaseInterval){
        m_iCaseInterval = iCaseInterval;
    }
    public int iGetCaseInterval(){
        return m_iCaseInterval;
    }
    public void setMakeCasesEquallyLong(boolean bEquallyLong){
        m_bMakeCasesEquallyLong = bEquallyLong;
    }
    public boolean bGetMakeCasesEquallyLong(){
        return m_bMakeCasesEquallyLong;
    }
    public void setUseBreaks(boolean bUseBreaks){
        m_bUseBreaks = bUseBreaks;
    }
    public boolean bGetUseBreaks(){
        return m_bUseBreaks;
    }
    public void setUseDefault(boolean bUseDefault){
        m_bUseDefault = bUseDefault;
    }
    public boolean bGetUseDefault(){
        return m_bUseDefault;
    }
    public void setSwitchID(long lngID){
        m_lngSwitchID=lngID;
    }
    public long lngGetSwitchID(){
        return m_lngSwitchID;
    }
    public void setMultipleIndicesPerCase(boolean bUseNultipleIndicesPerCase){
        m_bUseMultipleIndices = bUseNultipleIndicesPerCase;
    }
    public boolean bGetMultipleIndicesPerCase(){
        return m_bUseMultipleIndices;
    }

    // construction
    public SwitchInfo(){
        setID();
    }
    public SwitchInfo(SwitchInfo rhs){
        copyFrom(rhs);
        setID();
    }

    // internals
    public void copyFrom(SwitchInfo rhs){
        this.m_iFirstCaseIndex=rhs.m_iFirstCaseIndex;
        this.m_iNCases=rhs.m_iNCases;
        this.m_eCaseNumbering=rhs.m_eCaseNumbering;
        this.m_iCaseInterval=rhs.m_iCaseInterval;
        this.m_bMakeCasesEquallyLong=rhs.m_bMakeCasesEquallyLong;
        this.m_bUseMultipleIndices=rhs.m_bUseMultipleIndices;
        this.m_bUseBreaks=rhs.m_bUseBreaks;
        this.m_bUseDefault=rhs.m_bUseDefault;
    }
    private void setID(){
        synchronized (s_SwitchIDLock){
            m_lngSwitchID = s_lngNextSwitchID;
            s_lngNextSwitchID++;
        }
    }
}
