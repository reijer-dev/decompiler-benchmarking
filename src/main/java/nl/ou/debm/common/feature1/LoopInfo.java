package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static nl.ou.debm.common.Misc.cBooleanToChar;

public class LoopInfo {
    // loop root type
    private ELoopCommands loopCommand;               // do/for/while
    private ELoopFinitude loopFinitude;              // TrulyInfiniteLoop / Probably Finite Loop
    private ELoopExpressions loopExpressions;        // init/update/test available?
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



    private static long lngNextUsedID = 0;
    private long lngThisLoopsID = 0;



    public ELoopCommands getLoopCommand() {
        return loopCommand;
    }

    public ELoopFinitude getLoopFinitude() {
        return loopFinitude;
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

    
    private static List<LoopInfo> loopRepo = new ArrayList<>();

    // constructors
    public LoopInfo(ELoopFinitude loopFinitude, ELoopCommands loopCommand, ELoopExpressions loopExpressions){
        this.loopFinitude = loopFinitude;
        this.loopCommand = loopCommand;
        this.loopExpressions = loopExpressions;

        // always create new ID
        lngThisLoopsID = lngNextUsedID++;
    }

    // copy constructor (boring...)
    public LoopInfo(LoopInfo rhs){
        loopCommand = rhs.loopCommand;
        loopFinitude = rhs.loopFinitude;
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
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.DO, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.WHILE, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.FOR, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.FOR, ELoopExpressions.ONLY_INIT));
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.FOR, ELoopExpressions.ONLY_UPDATE));
        loopRepo.add(new LoopInfo(ELoopFinitude.TIL, ELoopCommands.FOR, ELoopExpressions.INIT_UPDATE));
        // PFL's
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.DO, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.WHILE, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.FOR, ELoopExpressions.NONE));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.FOR, ELoopExpressions.ONLY_INIT));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.FOR, ELoopExpressions.ONLY_UPDATE));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.FOR, ELoopExpressions.INIT_UPDATE));
        loopRepo.add(new LoopInfo(ELoopFinitude.PFL, ELoopCommands.DO, ELoopExpressions.ALL));
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
            if (src.loopFinitude == ELoopFinitude.PFL) {
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
                    ((src.loopCommand == ELoopCommands.FOR) && (src.loopExpressions.bAnyAvailable())) ||
                    ((src.loopCommand != ELoopCommands.FOR) && (src.loopFinitude == ELoopFinitude.PFL))
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
            if (!src.loopVar.bUseLoopVariable){
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
        var decreaseOperator = new ArrayList<ELoopVarTestOperators>();
        decreaseOperator.add(ELoopVarTestOperators.NON_EQUAL);
        decreaseOperator.add(ELoopVarTestOperators.GREATER_OR_EQUAL);
        decreaseOperator.add(ELoopVarTestOperators.GREATER_THAN);
        // make set of operators appropriate for decreasing loops
        var increaseOperator = new ArrayList<ELoopVarTestOperators>();
        increaseOperator.add(ELoopVarTestOperators.NON_EQUAL);
        increaseOperator.add(ELoopVarTestOperators.SMALLER_OR_EQUAL);
        increaseOperator.add(ELoopVarTestOperators.SMALLER_THAN);

        // add loopVar test whereever needed
        int decIndex = 0, incIndex = 0, curIndex;
        List<ELoopVarTestOperators> list;
        for (var loop : loopRepo){
            if (loop.loopVar.bUseLoopVariable){
                // loop var used, so implement a test
                //
                // switch between increase and decrease
                if (loop.loopVar.eUpdateType.bIsIncreasing()){
                    list = increaseOperator;
                    curIndex = ((incIndex++) % list.size());
                }
                else{
                    list = decreaseOperator;
                    curIndex = ((decIndex++) % list.size());
                }
                loop.loopVar.eTestType = list.get(curIndex);
            }
        }
    }

    private String strGetLoopVarName(){
        return "_LV" + lngThisLoopsID;
    }
    
    public CodeMarker getStartMarker(){
        var out = new CodeMarker();
        out.setProperty(ELoopMarkerTypes.STRPROPERTYNAME, ELoopMarkerTypes.BEFORE.strPropertyValue());
        out.setProperty(ELoopCommands.STRPROPERTYNAME, loopCommand.strPropertyValue());
        out.setProperty(ELoopFinitude.STRPROPERTYNAME, loopFinitude.strPropertyValue());
        return out;
    }

    public CodeMarker getEndMarker(){
        var out = getStartMarker();
        out.setProperty(ELoopMarkerTypes.STRPROPERTYNAME, ELoopMarkerTypes.AFTER.strPropertyValue());
        return out;
    }

    public String strGetLoopInit(){
        if (loopCommand == ELoopCommands.FOR){
            return "// for is not initialized";
        }
        if (loopVar.bUseLoopVariable){
            // TODO: get a variable name and save it somewhere
            return loopVar.eVarType.strGetCKeyword() + " " + strGetLoopVarName() + "=0;";
        }
        return "// no loop var used, so no init";
    }

    public String strGetLoopCommand(){
        switch (loopCommand) {
            case FOR -> {
                var out = new StringBuilder();
                if (!(loopExpressions.bInitAvailable()) && loopExpressions.bTestAvailable()){
                    out.append(loopVar.eVarType.strGetCKeyword()).append(" ").append(strGetLoopVarName()).append("; ");
                }
                out.append("for (");
                if (loopExpressions.bInitAvailable()){
                    out.append(loopVar.eVarType.strGetCKeyword()).append(" ").append(strGetLoopVarName()).append("=0");
                }
                out.append(";");
                if (loopExpressions.bTestAvailable()){
                    out.append(strGetLoopVarName()).append(loopVar.eTestType.strCOperator()).append("14");
                }
                out.append(";");
                if (loopExpressions.bUpdateAvailable()){
                    out.append(loopVar.eUpdateType.strGetUpdateExpression(strGetLoopVarName()));
                }
                out.append(") {");
                return out.toString();
            }
            case DO -> {
                return "do {";
            }
            case WHILE -> {
                if (loopFinitude == ELoopFinitude.TIL) {
                    return "while (true) {";
                }
                return "while (--false--)"; // TODO NIY
            }
        }
        return "";
    }

    public String strGetLoopTrailer(){
        if (loopCommand == ELoopCommands.DO){
            if (loopFinitude == ELoopFinitude.TIL) {
                return "} while (true);";
            }
            else {
                return "} while (false);"; // TODO -- MUST ADD VARIABLE
            }
        }
        else {
            return "}";
        }
    }


    public static String strToStringHeader(){
        return "I/F TYPE  IN UP TS  IC IB IE  EB EE ER ED EN EF  LV LU LT";
    }
    public String toString(){
        StringBuilder out;
        out = new StringBuilder(loopFinitude + " ");
        out.append(loopCommand);
        while (out.length() < 10) { out.append(" "); }
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
        out.append(loopVar.eTestType.strCOperator()).append(" ");
        return out.toString();
    }
}