package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.Misc;

public class LoopCodeMarker extends CodeMarker {

    private final static String STRNESTINGLEVELPROPERTY="NESTLEV";      // field name for this loop's nesting level
    private final static String STRLOOPIDPROPERTY="LOOPID";             // field name for this loop's ID
    private final static String STRUSECONTINUE="ICC";                   // field name for use of continue
    private final static String STRUSEGOTOEND="IGE";                    // field name for use of goto-end
    private final static String STRUSEBREAK="EBR";                      // field name for use of break
    private final static String STRUSEEXIT="EEX";                       // field name for use of exit()
    private final static String STRUSERETURN="EXR";                     // field name for use of return
    private final static String STRUSEGOTOAFTER="EGA";                  // field name for use of goto after loop
    private final static String STRUSEGOTOFURTHER="EGF";                // field name for use of goto further after loop
    private final static String STRUSEBREAKNESTED="EBM";                // field name for use of break multiple loops
    private final static String STRINITEXPRESSION="INEXP";              // field name for init expression
    private final static String STRUPDATEEXPRESSION="UPEXP";            // field name for update expression
    private final static String STRTESTEXPRESSION="TSEXP";              // field name for test expression
    private final static String STRLOOPVARNAME="LVN";                   // field name for loop variable name
    private final static String STRLOOPVARTYPE="LVT";                   // field name for loop variable type
    private final static String STRATTEMPTUNROLLING = "UNR";            // field name for loop unrolling attempt
    private final static String STRDUMMYMARKER = "DUMMY";               // field name for indicating dummy marker
    private final static String STRNUMBEROFFUNROLLITERATIONS = "UNRIT"; // field name for the number of iterations in an unrollable
    private final static String STRPARENTLOOPID = "PLID";               // field name for the parent loop ID

    /**
     * Default constructor
     */
    public LoopCodeMarker(){
        super(EFeaturePrefix.CONTROLFLOWFEATURE);
        setLoopCodeMarkerLocation(ELoopMarkerLocationTypes.UNDEFINED);
    }

    public LoopCodeMarker(ELoopMarkerLocationTypes location){
        super(EFeaturePrefix.CONTROLFLOWFEATURE);
        setLoopCodeMarkerLocation(location);
    }

    /**
     * Construct a LoopCodeMarker and set properties from CodedString
     * @param strCodedProperties  CodeMarker output to be used as input again
     */
    public LoopCodeMarker(String strCodedProperties){
        super(strCodedProperties);
    }

    /**
     * Set type of loop code marker (begin, body, end)
     * @param type type of loop code marker
     */
    public void setLoopCodeMarkerLocation(ELoopMarkerLocationTypes type)
    {
        setProperty(ELoopMarkerLocationTypes.STRPROPERTYNAME, type.strPropertyValue());
    }
    public ELoopMarkerLocationTypes getLoopCodeMarkerLocation(){
        return ELoopMarkerLocationTypes.valueOf(strPropertyValue(ELoopMarkerLocationTypes.STRPROPERTYNAME));
    }

    public void setLoopID(long lngLoopID){
        setProperty(STRLOOPIDPROPERTY, "" + lngLoopID);
    }
    public long lngGetLoopID(){
        return Misc.lngRobustStringToLong(strPropertyValue(STRLOOPIDPROPERTY), -1);
    }
    public void setLoopCommand(ELoopCommands command){
        setProperty(ELoopCommands.STRPROPERTYNAME, command.strPropertyValue());
    }
    public ELoopCommands getLoopCommand(){
        return ELoopCommands.valueOf(strPropertyValue(ELoopCommands.STRPROPERTYNAME));
    }
    public void setLoopFinitude(ELoopFinitude finitude){
        setProperty(ELoopFinitude.STRPROPERTYNAME, finitude.strPropertyValue());
    }
    public ELoopFinitude getLoopFinitude(){
        return ELoopFinitude.valueOf(strPropertyValue(ELoopFinitude.STRPROPERTYNAME));
    }
    public void setNestingLevel(int iNestingLevel){
        setProperty(STRNESTINGLEVELPROPERTY, "" + iNestingLevel);
    }
    public int iGetNestingLevel(){
        return Misc.iRobustStringToInt(strPropertyValue(STRNESTINGLEVELPROPERTY));
    }
    public void setParentLoopID(long lngParentLoopID) {
        setProperty(STRPARENTLOOPID, "" + lngParentLoopID);
    }
    public long lngGetParentLoopID() {
        return Misc.lngRobustStringToLong(strPropertyValue(STRPARENTLOOPID));
    }
    public void setInitExpression(String strInitExpression){
        setProperty(STRINITEXPRESSION, strInitExpression);
    }
    public String strGetInitExpression(){
        return strPropertyValue(STRINITEXPRESSION);
    }
    public void setUpdateExpression(String strUpdateExpression){
        setProperty(STRUPDATEEXPRESSION, strUpdateExpression);
    }
    public String strGetUpdateExpression(){
        return strPropertyValue(STRUPDATEEXPRESSION);
    }
    public void setTestExpression(String strTestExpression){
        setProperty(STRTESTEXPRESSION, strTestExpression);
    }
    public String strGetTestExpression(){
        return strPropertyValue(STRTESTEXPRESSION);
    }
    public void setUseContinue(boolean bValue){
        addBooleanToCodeMarker(STRUSECONTINUE, bValue);
    }
    public boolean bGetUseContinue(){
        return Misc.bIsTrue(strPropertyValue(STRUSECONTINUE));
    }
    public void setUseGotoEnd(boolean bValue){
        addBooleanToCodeMarker(STRUSEGOTOEND, bValue);
    }
    public boolean bGetUseGotoEnd(){
        return Misc.bIsTrue(strPropertyValue(STRUSEGOTOEND));
    }
    public void setUseBreak(boolean bValue){
        addBooleanToCodeMarker(STRUSEBREAK, bValue);
    }
    public boolean bGetUseBreak(){
        return Misc.bIsTrue(strPropertyValue(STRUSEBREAK));
    }
    public void setUseExit(boolean bValue){
        addBooleanToCodeMarker(STRUSEEXIT, bValue);
    }
    public boolean bGetUseExit(){
        return Misc.bIsTrue(strPropertyValue(STRUSEEXIT));
    }
    public void setUseReturn(boolean bValue){
        addBooleanToCodeMarker(STRUSERETURN, bValue);
    }
    public boolean bGetUseReturn(){
        return Misc.bIsTrue(strPropertyValue(STRUSERETURN));
    }
    public void setUseGotoDirectlyAfterLoop(boolean bValue){
        addBooleanToCodeMarker(STRUSEGOTOAFTER, bValue);
    }
    public boolean bGetUseGotoDirectlyAfterLoop(){
        return Misc.bIsTrue(strPropertyValue(STRUSEGOTOAFTER));
    }
    public void setUseGotoFurtherFromThisLoop(boolean bValue){
        addBooleanToCodeMarker(STRUSEGOTOFURTHER, bValue);
    }
    public boolean bGetUseGotoFurtherFromThisLoop(){
        return Misc.bIsTrue(strPropertyValue(STRUSEGOTOFURTHER));
    }
    public void setUseBreakOutNestedLoops(boolean bValue){
        addBooleanToCodeMarker(STRUSEBREAKNESTED, bValue);
    }
    public boolean bGetUseBreakOutNestedLoops(){
        return Misc.bIsTrue(strPropertyValue(STRUSEBREAKNESTED));
    }
    public void setLoopVarName(String strLoopVarName){
        setProperty(STRLOOPVARNAME, strLoopVarName);
    }
    public void setLoopVarType(ELoopVarTypes varType){
        setProperty(STRLOOPVARTYPE, varType.toString());
    }
    public ELoopVarTypes getLoopVarType(){
        return ELoopVarTypes.valueOf(strPropertyValue(STRLOOPVARTYPE));
    }
    public String strGetLoopVarName(){
        return strPropertyValue(STRLOOPVARNAME);
    }
    public void setLoopUnrolling(ELoopUnrollTypes bValue){
        setProperty(STRATTEMPTUNROLLING, bValue.strPropertyValue());
    }
    public ELoopUnrollTypes getLoopUnrolling(){
        return ELoopUnrollTypes.stringToType(strPropertyValue(STRATTEMPTUNROLLING));
    }
    public void setDummyStatementStatus(boolean bIsDummy){
        addBooleanToCodeMarker(STRDUMMYMARKER, bIsDummy);
    }
    public void setAsDummyStatement(){
        setDummyStatementStatus(true);
    }
    public void setNumberOfUnrolledIterations(int iNumberOfIterations){
        setProperty(STRNUMBEROFFUNROLLITERATIONS, "" + iNumberOfIterations);
    }
    public int iGetNumberOfUnrolledIterations(){
        return (int)Misc.lngRobustStringToLong(strPropertyValue(STRNUMBEROFFUNROLLITERATIONS));
    }

    public boolean bIsDummyStatementMarker(){
        return Misc.bIsTrue(strPropertyValue(STRDUMMYMARKER));
    }
    /**
     * Process a binary value for adding to CM object. If binary is TRUE, the
     * field is added with value "T", otherwise the field is omitted, indicating a false
     * @param strPropertyName  field name
     * @param bPropertyValue  field value
     */
    private void addBooleanToCodeMarker(String strPropertyName, boolean bPropertyValue){
        if (bPropertyValue){
            setProperty(strPropertyName, Misc.cBooleanToChar(true) + "");
        }
        else {
            removeProperty(strPropertyName);
        }
    }

    @Override
    public boolean fromString(String strCodedProperties, boolean bClearTable) {
        var success = super.fromString(strCodedProperties, bClearTable);
        if (strPropertyValue(ELoopMarkerLocationTypes.STRPROPERTYNAME).isEmpty()){
            setLoopCodeMarkerLocation(ELoopMarkerLocationTypes.UNDEFINED);
        }
        return success;
    }
}
