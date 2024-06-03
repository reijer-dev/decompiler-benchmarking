package nl.ou.debm.common.feature5;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.Misc;

public class IndirectionCodeMarker extends CodeMarker {

    /** switch ID field */              private final static String STRSWITCHID = "switchID";
    /** case ID field */                private final static String STRCASEID = "caseID";

    /**
     * Default constructor
     */
    public IndirectionCodeMarker(){
        super(EFeaturePrefix.INDIRECTIONSFEATURE);
    }

    /**
     * Construct a LoopCodeMarker and set properties from CodedString
     * @param strCodedProperties  CodeMarker output to be used as input again
     */
    public IndirectionCodeMarker(String strCodedProperties){
        super(strCodedProperties);
    }

    public IndirectionCodeMarker(EIndirectionMarkerLocationTypes location){
        super(EFeaturePrefix.INDIRECTIONSFEATURE);
        setCodeMarkerLocation(location);
    }

    public IndirectionCodeMarker(EIndirectionMarkerLocationTypes location, long lngSwitchID){
        super(EFeaturePrefix.INDIRECTIONSFEATURE);
        setCodeMarkerLocation(location);
        setSwitchID(lngSwitchID);
    }

    /* getters/setters */
    public long lngGetSwitchID(){
        return Misc.lngRobustStringToLong(strPropertyValue(STRSWITCHID));
    }

    public void setSwitchID(long lngSwitchID){
        setProperty(STRSWITCHID, lngSwitchID + "");
    }

    public long lngGetCaseID(){
        return Misc.lngRobustStringToLong(strPropertyValue(STRCASEID));
    }

    public void setCaseID(long lngCaseID){
        setProperty(STRCASEID, lngCaseID + "");
    }


    public void setCodeMarkerLocation(EIndirectionMarkerLocationTypes location){
        setProperty(EIndirectionMarkerLocationTypes.STRPROPERTYNAME, location.toString());
    }

    public EIndirectionMarkerLocationTypes getCodeMarkerLocation(){
        return EIndirectionMarkerLocationTypes.valueOf(strPropertyValue(EIndirectionMarkerLocationTypes.STRPROPERTYNAME));
    }
}
