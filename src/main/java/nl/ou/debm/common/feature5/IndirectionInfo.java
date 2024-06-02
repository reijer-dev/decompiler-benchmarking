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

import java.util.ArrayList;
import java.util.List;

public class IndirectionInfo {

    // class attributes
    /** repo containing all wanted switch types*/   private final List<IndirectionInfo> s_SwitchRepo = new ArrayList<>();

    // class init
    static {
        // static init
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // object attributes
    /** int value of the first case */              private int m_iFirstCaseIndex = 0;
    /** number of cases */                          private int m_iNCases = 5;
    /** case numbering */                           private ESwitchCaseNumbering m_eCaseNumbering = ESwitchCaseNumbering.REGULAR;
    /** case interval when regular/skipping */      private int m_iCaseInterval = 1;
    /** all cases have similar code */              private boolean m_bMakeCasesEquallyLong = true;
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
}
