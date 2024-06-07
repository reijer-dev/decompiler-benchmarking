package nl.ou.debm.common.feature5;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.Misc;

import java.util.ArrayList;
import java.util.List;

public class IndirectionsCodeMarker extends CodeMarker {

    /** switch ID field */              private final static String STRSWITCHIDPROP = "switchID";
    /** case ID field */                private final static String STRCASEIDPROP = "caseID";
    /** similar case code? */           private final static String STRSIMCASESPROP = "simcases";
    /** case interval */                private final static String STRCASEINTERVAL = "cintv";
    /** multiple indices */             private final static String STRMULTINDICES = "mulin";
    /** use breaks */                   private final static String STRBREAKPROP = "brk";
    /** default present */              private final static String STRDEFAULTPROP = "def";
    /** cases list */                   private final static String STRALLCASESPROP = "cases";

    /**
     * Default constructor
     */
    public IndirectionsCodeMarker(){
        super(EFeaturePrefix.INDIRECTIONSFEATURE);
    }

    /**
     * Construct a LoopCodeMarker and set properties from CodedString
     * @param strCodedProperties  CodeMarker output to be used as input again
     */
    public IndirectionsCodeMarker(String strCodedProperties){
        super(strCodedProperties);
    }

    public IndirectionsCodeMarker(EIndirectionMarkerLocationTypes location){
        super(EFeaturePrefix.INDIRECTIONSFEATURE);
        setCodeMarkerLocation(location);
    }

    public IndirectionsCodeMarker(EIndirectionMarkerLocationTypes location, long lngSwitchID){
        super(EFeaturePrefix.INDIRECTIONSFEATURE);
        setCodeMarkerLocation(location);
        setSwitchID(lngSwitchID);
    }

    /* getters/setters */
    public long lngGetSwitchID(){
        return Misc.lngRobustStringToLong(strPropertyValue(STRSWITCHIDPROP));
    }

    public void setSwitchID(long lngSwitchID){
        setProperty(STRSWITCHIDPROP, lngSwitchID + "");
    }

    public long lngGetCaseID(){
        return Misc.lngRobustStringToLong(strPropertyValue(STRCASEIDPROP));
    }

    public void setCaseID(long lngCaseID){
        setProperty(STRCASEIDPROP, lngCaseID + "");
    }


    public void setCodeMarkerLocation(EIndirectionMarkerLocationTypes location){
        setProperty(EIndirectionMarkerLocationTypes.STRPROPERTYNAME, location.toString());
    }

    public EIndirectionMarkerLocationTypes getCodeMarkerLocation(){
        return EIndirectionMarkerLocationTypes.valueOf(strPropertyValue(EIndirectionMarkerLocationTypes.STRPROPERTYNAME));
    }

    public void setDefaultBranch(boolean bDefaultPresent){
        addBooleanToCodeMarker(STRDEFAULTPROP, bDefaultPresent);
    }
    public boolean bGetDefaultBranch(){
        return Misc.bIsTrue(strPropertyValue(STRDEFAULTPROP));
    }
    public void setUseBreaks(boolean bUseReturn){
        addBooleanToCodeMarker(STRBREAKPROP, bUseReturn);
    }
    public boolean bGetUseBreaks(){
        return Misc.bIsTrue(strPropertyValue(STRBREAKPROP));
    }
    public void setMultipleIndicesPerCase(boolean bMultiple){
        addBooleanToCodeMarker(STRMULTINDICES, bMultiple);
    }
    public boolean bGetMultipleIndicesPerCase(){
        return Misc.bIsTrue(strPropertyValue(STRMULTINDICES));
    }
    public void setCaseNumbering(ESwitchCaseNumbering caseNumbering){
        setProperty(ESwitchCaseNumbering.STRPROPERTYNAME, caseNumbering.toString());
    }
    public ESwitchCaseNumbering eGetCaseNumbering(){
        return ESwitchCaseNumbering.valueOf(strPropertyValue(ESwitchCaseNumbering.STRPROPERTYNAME));
    }
    public void setCaseInterval(int iCaseInterval){
        setProperty(STRCASEINTERVAL, iCaseInterval + "");
    }
    public int iGetCaseInterval(){
        return Misc.iRobustStringToInt(strPropertyValue(STRCASEINTERVAL));
    }
    public void setEqualCaseSize(boolean bEqualCaseSize){
        addBooleanToCodeMarker(STRSIMCASESPROP, bEqualCaseSize);
    }
    public boolean bGetEqualCaseSize(){
        return Misc.bIsTrue(strPropertyValue(STRSIMCASESPROP));
    }
    public void setCases(List<ProducedSwitchInfo.CaseInfo> caseInfoList){
        var sb = new StringBuilder((caseInfoList.size() * 2) + 10);
        for (var ci : caseInfoList){
            sb.append(ci.toString());
        }
        setProperty(STRALLCASESPROP, sb.toString());
    }
    public List<ProducedSwitchInfo.CaseInfo> getCases(){
        List<ProducedSwitchInfo.CaseInfo> out = new ArrayList<>();
        var propstring = strPropertyValue(STRALLCASESPROP);
        int v = 0;
        for (int p=0; p<propstring.length(); p++){
            var c = propstring.charAt(p);
            if ((c>='0') && (c<='9')){
                v*=10;
                v+=(c-'0');
            }
            else if ((c=='+') || (c=='-')) {
                out.add(new ProducedSwitchInfo.CaseInfo(v, (c=='+')));
                v=0;
            }
        }
        return out;
    }
}
