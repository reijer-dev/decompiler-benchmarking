package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static nl.ou.debm.common.Misc.cBooleanToChar;
import static nl.ou.debm.common.Misc.rnd;
import static nl.ou.debm.common.feature1.LoopProducer.*;

public class LoopInfo {
    // loop root type
    private final ELoopCommands m_loopCommand;        // do/for/while
    private final ELoopFinitude m_loopFinitude;       // TrulyInfiniteLoop / Probably Finite Loop
    private final ELoopExpressions loopExpressions; // init/update/test available?
    // internal loop control
    private boolean bILC_UseContinue = false;        // put continue statement in loop
    private boolean bILC_UseGotoBegin = false;       // put goto begin in loop
    private boolean bILC_UseGotoEnd = false;         // put goto begin in loop
    // external loop control
    private boolean bELC_UseBreak = false;           // put break statement in loop
    private boolean bELC_UseExit = false;            // put exit call in loop
    private boolean bELC_UseReturn = false;          // put return statement in loop
    private boolean bELC_UseGotoDirectlyAfterThisLoop = false;   // put goto next-statement-after-loop in loop
    private boolean bELC_UseGotoFurtherFromThisLoop = false;     // goto somewhere further than immediately after loop

    private int m_iCurrentNestingLevel = 0;         // current nesting level


    private static long lngNextUsedID = 0;
    private long lngThisLoopsID = 0;

    public ELoopCommands getLoopCommand() {
        return m_loopCommand;
    }

    public ELoopFinitude getLoopFinitude() {
        return m_loopFinitude;
    }

    public ELoopExpressions getLoopExpressions() {
        return loopExpressions;
    }

    public boolean bGetILC_UseContinue() {
        return bILC_UseContinue;
    }

    public boolean bGetILC_UseGotoBegin() {
        return bILC_UseGotoBegin;
    }

    public boolean bGetILC_UseGotoEnd() {
        return bILC_UseGotoEnd;
    }

    public boolean bGetELC_UseBreak() {
        return bELC_UseBreak;
    }

    public boolean bGetELC_UseExit() {
        return bELC_UseExit;
    }

    public boolean bGetELC_UseReturn() {
        return bELC_UseReturn;
    }

    public boolean bGetELC_UseGotoDirectlyAfterThisLoop() {
        return bELC_UseGotoDirectlyAfterThisLoop;
    }

    public boolean bGetELC_UseGotoFurtherFromThisLoop() {
        return bELC_UseGotoFurtherFromThisLoop;
    }

    public boolean bGetELC_BreakOutNestedLoops() {
        return bELC_BreakOutNestedLoops;
    }

    public LoopVariable getLoopVar() {
        return loopVar;
    }

    public int iGetNumberOfImplementations() {
        return iNumberOfImplementations;
    }

    private boolean bELC_BreakOutNestedLoops = false;            // break out nested loops
    // loop variable
    private LoopVariable loopVar = new LoopVariable();           // loop variable details
    // number of implementations
    private int iNumberOfImplementations = 0;

    public void IncreaseImplementations(){
        iNumberOfImplementations++;
    }

    public int iGetCurrentNestingLevel() {
        return m_iCurrentNestingLevel;
    }

    public void setCurrentNestingLevel(int iCurrentNestingLevel) {
        this.m_iCurrentNestingLevel = iCurrentNestingLevel;
    }

    public final String STRLOOPIDPROPERTY="loopID";
    public final String STRNESTINGLEVELPROPERTY="nestlev";
    
    private static List<LoopInfo> loopRepo = new ArrayList<>();

    // constructors
    public LoopInfo(ELoopFinitude loopFinitude, ELoopCommands loopCommand, ELoopExpressions loopExpressions){
        this.m_loopFinitude = loopFinitude;
        this.m_loopCommand = loopCommand;
        this.loopExpressions = loopExpressions;

        // always create new ID
        lngThisLoopsID = lngNextUsedID++;
    }

    // copy constructor (boring...)
    public LoopInfo(LoopInfo rhs){
        m_loopCommand = rhs.m_loopCommand;
        m_loopFinitude = rhs.m_loopFinitude;
        loopExpressions = rhs.loopExpressions;
        bILC_UseContinue = rhs.bILC_UseContinue;
        bILC_UseGotoBegin = rhs.bILC_UseGotoBegin;
        bILC_UseGotoEnd = rhs.bILC_UseGotoEnd;
        bELC_UseBreak = rhs.bELC_UseBreak;
        bELC_UseExit = rhs.bELC_UseExit;
        bELC_UseReturn = rhs.bELC_UseReturn;
        bELC_UseGotoDirectlyAfterThisLoop = rhs.bELC_UseGotoDirectlyAfterThisLoop;
        bELC_UseGotoFurtherFromThisLoop = rhs.bELC_UseGotoFurtherFromThisLoop;
        bELC_BreakOutNestedLoops = rhs.bELC_BreakOutNestedLoops;
        loopVar = new LoopVariable(rhs.loopVar);
        iNumberOfImplementations = rhs.iNumberOfImplementations;
        m_iCurrentNestingLevel = rhs.m_iCurrentNestingLevel;

        // always create new ID
        lngThisLoopsID = lngNextUsedID++;
    }

    public static void FillLoopRepo(List<LoopInfo> destRepo) {
        FillLoopRepo(destRepo, false);
    }
    public static void FillLoopRepo(List<LoopInfo> destRepo, boolean bShuffle){
        // init repository
        InitLoopRepo();
        // make deep copy
        destRepo.clear();
        for (var li : loopRepo){
            destRepo.add(new LoopInfo(li));
        }
        // shuffle?
        if (bShuffle){
            Collections.shuffle(destRepo);
        }
    }

    private static void InitLoopRepo() {
        // init only when necessary
        if (!loopRepo.isEmpty()) {
            return;
        }

        ///////////////////////
        // add basic loop types
        ///////////////////////
        // TILS
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.DOWHILE, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.WHILE, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.FOR, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.FOR, ELoopExpressions.ONLY_INIT));
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.FOR, ELoopExpressions.ONLY_UPDATE));
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.FOR, ELoopExpressions.INIT_UPDATE));
        // PFL's
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.DOWHILE, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.WHILE, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.FOR, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.FOR, ELoopExpressions.ONLY_INIT));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.FOR, ELoopExpressions.ONLY_UPDATE));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.FOR, ELoopExpressions.INIT_UPDATE));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.DOWHILE, ELoopExpressions.ALL));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.WHILE, ELoopExpressions.ALL));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.FOR, ELoopExpressions.ALL));

        /////////////////////////////////
        // add all internal control flows
        /////////////////////////////////
        AddInternalControlFlows();

        /////////////////////////////////
        // add all external control flows
        /////////////////////////////////
        AddExternalControlFlows();

        ///////////////////////////////////////
        // add loop variables where appropriate
        ///////////////////////////////////////
        AddLoopVars();

        ///////////////////////////////////////
        // make sure loop variables are updated
        ///////////////////////////////////////
        UpdateLoopVars();

        //////////////////////////////////////
        // make sure loop variables are tested
        //////////////////////////////////////
        TestLoopVars();

        ////////////////////////////////
        // set init and test expressions
        ////////////////////////////////
        MakeLoopVarExpressions();
    }

    private static void AddInternalControlFlows() {
        // store reference to current repo
        var repo2 = loopRepo;

        // build new repo: for each loop, add any combination of internal control flow settings
        loopRepo = new ArrayList<>();
        for (var src : repo2) {                 // loop through every item set so far
            for (int i = 0; i < 8; ++i) {       // 3 options T/F = 2^3 = 8 combinations
                var dest = new LoopInfo(src);   // true copy (not just a reference)
                dest.bILC_UseContinue = ((i & 1) != 0);
                dest.bILC_UseGotoBegin = ((i & 2) != 0);
                dest.bILC_UseGotoEnd = ((i & 4) != 0);
                loopRepo.add(dest);             // add new copy to repo
            }
        }
    }

    private static void AddExternalControlFlows(){
        // store reference to current repo
        var repo2 = loopRepo;

        // build new repo: for each loop, add combination of external control flow settings
        // (well, sometimes: don't add -- see below)
        loopRepo = new ArrayList<>();
        for (var src : repo2) {
            int min = 0, max = 1;
            if (src.m_loopFinitude == ELoopFinitude.PFL) {
                // this loop is a probably finite one
                // these come in two flavours: - the type while (true)  -->  LES required
                //                             - the type while (x<10)  -->  LES optional
                // use test-expression as discriminator
                max = 64;             // all combinations of LES possible anyway
                if (!src.loopExpressions.bTestAvailable()) {
                    // without a test in the loop statement, we *must* add a LES, so the
                    // only LES-less option (:-D) must be skipped, which is done by beginning
                    // with case 1 instead of case 0
                    min = 1;
                }
            }
            //else{
            // nothing needed for a TIL loop, as the min/max initializers make sure that
            // only case 0 is processed in the loop below, which comes down to a straight
            // copy (all the booleans are false by default anyway)
            //}
            for (int i=min ; i<max ; ++i){
                var dest = new LoopInfo(src);
                dest.bELC_UseBreak                     = ((i &  1)!=0);
                dest.bELC_UseExit                      = ((i &  2)!=0);
                dest.bELC_UseReturn                    = ((i &  4)!=0);
                dest.bELC_UseGotoDirectlyAfterThisLoop = ((i &  8)!=0);
                dest.bELC_BreakOutNestedLoops          = ((i & 16)!=0);
                dest.bELC_UseGotoFurtherFromThisLoop   = ((i & 32)!=0);
                loopRepo.add(dest);
            }
        }
    }

    private static void AddLoopVars(){
        // no new repo needed, just a setting in the current repo
        for (var src : loopRepo){
            src.loopVar.bUseLoopVariable =(
                    ((src.m_loopCommand == ELoopCommands.FOR) && (src.loopExpressions.bAnyAvailable())) ||
                    ((src.m_loopCommand != ELoopCommands.FOR) && (src.m_loopFinitude == ELoopFinitude.PFL))
                    );
            if (src.loopVar.bUseLoopVariable){
                // default: use int as variable type
                src.loopVar.eVarType = ELoopVarTypes.INT;
            }
        }
    }

    private static void UpdateLoopVars(){
        // store reference to current repo
        var repo2 = loopRepo;

        // build new repo: make sure every loop var is updated in all ways
        loopRepo = new ArrayList<>();
        for (var src : repo2) {
            if (!src.loopExpressions.bUpdateAvailable()){
                // no loop var used, so simple copy is enough
                loopRepo.add(new LoopInfo(src));
            }
            else {
                // loop var used, set types
                for (var ut : ELoopVarUpdateTypes.values()) {
                    if (ut != ELoopVarUpdateTypes.UNUSED) {
                        var dest = new LoopInfo(src);
                        dest.loopVar.eUpdateType = ut;
                        loopRepo.add(dest);
                    }
                }
            }
        }
    }

    private static void TestLoopVars(){
        // make set of operators appropriate for decreasing loops
        var decreaseOperator = new ArrayList<ELoopVarTestTypes>();
        decreaseOperator.add(ELoopVarTestTypes.NON_EQUAL);
        decreaseOperator.add(ELoopVarTestTypes.GREATER_OR_EQUAL);
        decreaseOperator.add(ELoopVarTestTypes.GREATER_THAN);
        // make set of operators appropriate for decreasing loops
        var increaseOperator = new ArrayList<ELoopVarTestTypes>();
        increaseOperator.add(ELoopVarTestTypes.NON_EQUAL);
        increaseOperator.add(ELoopVarTestTypes.SMALLER_OR_EQUAL);
        increaseOperator.add(ELoopVarTestTypes.SMALLER_THAN);

        // add loopVar test where ever needed
        int decIndex = 0, incIndex = 0, curIndex;
        List<ELoopVarTestTypes> list;
        for (var loop : loopRepo){
            if (loop.loopExpressions.bTestAvailable()){
                // loop var used, so implement a test
                //
                // switch between increase and decrease
                if (loop.loopVar.eUpdateType.bIsDecreasing()){
                    // use a decrease operator if updating is decreasing
                    list = decreaseOperator;
                    curIndex = ((decIndex++) % list.size());
                }
                else{
                    // there may not be an update after all, so use increase in any other case
                    list = increaseOperator;
                    curIndex = ((incIndex++) % list.size());
                }
                loop.loopVar.eTestType = list.get(curIndex);
            }
        }
    }

    private static void MakeLoopVarExpressions(){
        for (var loop : loopRepo) {
            if (loop.loopExpressions.bInitAvailable()){
                int low = ILOOPVARLOWVALUELOWBOUND;
                int high = ILOOPVARLOWVALUEHIGHBOUND;
                if (loop.loopVar.eUpdateType.bIsDecreasing()){
                    low = ILOOPVARHIGHVALUELOWBOUND;
                    high = ILOOPVARHIGHVALUEHIGHBOUND;
                }
                loop.loopVar.strInitExpression = "" + rnd.nextInt(low, high);
            }
            if (loop.loopExpressions.bUpdateAvailable()){
                loop.loopVar.strUpdateExpression = loop.loopVar.eUpdateType.strGetUpdateExpression();
            }
            if (loop.loopExpressions.bTestAvailable()){
                // only return test expression when wanted
                int low = ILOOPVARHIGHVALUELOWBOUND;
                int high = ILOOPVARHIGHVALUEHIGHBOUND;
                if (loop.loopVar.eUpdateType.bIsDecreasing()){
                    low = ILOOPVARLOWVALUELOWBOUND;
                    high = ILOOPVARLOWVALUEHIGHBOUND;
                }
                loop.loopVar.strTestExpression = loop.loopVar.eTestType.strCOperator() + rnd.nextInt(low,high);
            }
        }
    }

    public String strGetLoopVarName(){
        return "_LV" + lngThisLoopsID;
    }
    
    public CodeMarker getStartMarker(){
        var out = new CodeMarker();
        out.setProperty(STRLOOPIDPROPERTY, "" + lngThisLoopsID);
        out.setProperty(ELoopMarkerTypes.STRPROPERTYNAME, ELoopMarkerTypes.BEFORE.strPropertyValue());
        out.setProperty(ELoopCommands.STRPROPERTYNAME, m_loopCommand.strPropertyValue());
        out.setProperty(ELoopFinitude.STRPROPERTYNAME, m_loopFinitude.strPropertyValue());
        out.setProperty(ELoopVarTestTypes.STRPROPERTYNAME, loopVar.eTestType.strPropertyValue());
        out.setProperty(STRNESTINGLEVELPROPERTY, "" + m_iCurrentNestingLevel);
        return out;
    }

    public CodeMarker getEndMarker(){
        var out = getStartMarker();
        out.setProperty(ELoopMarkerTypes.STRPROPERTYNAME, ELoopMarkerTypes.AFTER.strPropertyValue());
        return out;
    }

    public CodeMarker getBodyMarker(){
        var out = new CodeMarker();
        out.setProperty(ELoopMarkerTypes.STRPROPERTYNAME, ELoopMarkerTypes.BODY.strPropertyValue());
        return out;
    }

    public String strGetLoopInit(){
        if (m_loopCommand == ELoopCommands.FOR){
            // for init is in the statement itself
            return "// for is not initialized separately";
        }
        if (loopVar.bUseLoopVariable){
            // do/while: only init a loop var when it is used
            return strGetCompleteLoopInitExpression() + "; // loop var init";
        }
        return "// no loop var used, so no init";
    }

    private String strGetCompleteLoopInitExpression(){
        if (loopVar.bUseLoopVariable){
            // only init a loop var when it is used
            if (loopVar.strUpdateExpression.isEmpty()){
                return loopVar.eVarType.strGetCKeyword() + " " + strGetLoopVarName();
            }
            else {
                return loopVar.eVarType.strGetCKeyword() + " " + strGetLoopVarName() + "=" + loopVar.strInitExpression;
            }
        }
        return "";
    }

    public String strGetCompleteLoopUpdateExpression(){
        if (loopVar.bUseLoopVariable){
            return strGetLoopVarName() + loopVar.strUpdateExpression;
        }
        return "// no loop variable, so no update";
    }

    private String strGetCompleteLoopTestExpression(){
        if (loopExpressions.bTestAvailable()){
            return strGetLoopVarName() + loopVar.strTestExpression;
        }
        return "";
    }

    public String strGetLoopCommand(){
        switch (m_loopCommand) {
            case FOR -> {
                var out = new StringBuilder();
                if ( (loopVar.bUseLoopVariable) && (!loopExpressions.bInitAvailable())){
                    out.append(loopVar.eVarType.strGetCKeyword()).append(" ").append(strGetLoopVarName()).append("; ");
                }
                out.append("for (");
                if (loopExpressions.bInitAvailable()){
                    out.append(strGetCompleteLoopInitExpression());
                }
                out.append(";");
                if (loopExpressions.bTestAvailable()){
                    out.append(strGetCompleteLoopTestExpression());
                }
                out.append(";");
                if (loopExpressions.bUpdateAvailable()){
                    out.append(strGetCompleteLoopUpdateExpression());
                }
                out.append(") {");
                return out.toString();
            }
            case DOWHILE -> {
                return "do {";
            }
            case WHILE -> {
                if (m_loopFinitude == ELoopFinitude.TIL) {
                    return "while (true) {";
                }
                return "while (" + strGetCompleteLoopTestExpression() + ")";
            }
        }
        return "";
    }

    public String strGetLoopTrailer(){
        if (m_loopCommand == ELoopCommands.DOWHILE){
            // do: close block and add while command
            var t = strGetCompleteLoopTestExpression();
            if (t.isEmpty() || (m_loopFinitude == ELoopFinitude.TIL)){
                return "} while (true);";
            }
            else {
                return "} while (" + strGetCompleteLoopTestExpression() + ");";
            }
        }
        else {
            // for/while: just close block
            return "}";
        }
    }


    public static String strToStringHeader(){
        return "I/F TYPE    IN UP TS  IC IB IE  EB EE ER ED EN EF  LV LU LT";
    }
    public String toString(){
        StringBuilder out;
        out = new StringBuilder(m_loopFinitude + " ");
        out.append(m_loopCommand);
        while (out.length() < 12) { out.append(" "); }
        out.append(cBooleanToChar(loopExpressions.bInitAvailable())).append("  ");
        out.append(cBooleanToChar(loopExpressions.bUpdateAvailable())).append("  ");
        out.append(cBooleanToChar(loopExpressions.bTestAvailable())).append("   ");

        out.append(cBooleanToChar(bILC_UseContinue)).append("  ");
        out.append(cBooleanToChar(bILC_UseGotoBegin)).append("  ");
        out.append(cBooleanToChar(bILC_UseGotoEnd)).append("   ");

        out.append(cBooleanToChar(bELC_UseBreak)).append("  ");
        out.append(cBooleanToChar(bELC_UseExit)).append("  ");
        out.append(cBooleanToChar(bELC_UseReturn)).append("  ");
        out.append(cBooleanToChar(bELC_UseGotoDirectlyAfterThisLoop)).append("  ");
        out.append(cBooleanToChar(bELC_BreakOutNestedLoops)).append("  ");
        out.append(cBooleanToChar(bELC_UseGotoFurtherFromThisLoop)).append("   ");

        out.append(cBooleanToChar(loopVar.bUseLoopVariable)).append("  ");
        out.append(loopVar.eUpdateType.strShortCode()).append(" ");
        out.append(loopVar.eTestType.strCOperator()).append(" "); if (loopVar.eTestType.strCOperator().length()==1){out.append(" ");}
        return out.toString();
    }
}