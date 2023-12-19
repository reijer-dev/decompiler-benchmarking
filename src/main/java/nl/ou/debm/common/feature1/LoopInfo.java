package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.Misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static nl.ou.debm.common.Misc.cBooleanToChar;
import static nl.ou.debm.common.feature1.LoopProducer.*;


// TODO: cleaning up the code
// TODO: full codeMarker implementation
// TODO: implement recording current nesting level
// TODO: ensure break out of multiple loops is looked at



public class LoopInfo {

    // loop properties are:
    // loop command             for/while/do-while
    // use of loop var          no/yes
    // loop var properties         type: int/float
    //                             update direction: positive/negative
    //                             update type: one/2+ (fixed)/non-fixed/[multiply or divide]
    //                             test types: no test, non equal, [>=, <=] , [>, <]  --> greater or smaller depends on
    //                                                                                    update direction
    // internal control flow    any combination of these (incl empty set): continue, jump begin, jump end
    // external control flow    any combination of these (incl empty set): break, return, exit, goto after, goto further,
    //                                                                             goto <break_out_of_any_loop>
    //
    // based on the other properties, one can distinguish TIL from PFL loops
    // TIL's are loops that have no loop var tested and no external control flow. All other loops are PFL's
    //


    // class attributes
    // ----------------
    private static long s_lngNextUsedID = 0;            // next new object gets this ID
    private static final List<LoopInfo> s_loopRepo = new ArrayList<>();   // repository containing all loops that need to be implemented
    private static final ELoopCommands s_defaultLoopCommand = ELoopCommands.FOR;  // default loop command

    // class constants
    private final static String STRLOOPIDPROPERTY="LOOPID";             // field name for this loop's ID
    private final static String STRNESTINGLEVELPROPERTY="NESTLEV";      // field name for this loop's nesting level


    // object attributes
    // -----------------
    //
    // part A: all information required to make the loop statements
    // basics
    private ELoopCommands m_loopCommand = s_defaultLoopCommand;// do/for/while
    private LoopVariable m_loopVar = null;                     // loop variable details (null = unused)
    // internal loop flow control
    private boolean m_bILC_UseContinue = false;             // put continue statement in loop
    private boolean m_bILC_UseGotoBegin = false;            // put goto begin in loop
    private boolean m_bILC_UseGotoEnd = false;              // put goto end in loop
    // external loop flow control
    private boolean m_bELC_UseBreak = false;                // put break statement in loop
    private boolean m_bELC_UseExit = false;                 // put exit call in loop
    private boolean m_bELC_UseReturn = false;                      // put return statement in loop
    private boolean m_bELC_UseGotoDirectlyAfterThisLoop = false;   // put goto next-statement-after-loop in loop
    private boolean m_bELC_UseGotoFurtherFromThisLoop = false;     // goto somewhere further than immediately after loop
    private boolean m_bELC_BreakOutNestedLoops = false;            // break out nested loops
    // part B: all information for loop objects
    private long m_lngLoopID = 0;                        // unique loop-object ID
    private int m_iNumberOfImplementations = 0;               // number of times this loop is actually in the code
    private int m_iCurrentNestingLevel = 0;                     // nesting level (number of parents, 0 = not nested at all)
    private String m_strVariablePrefix = "";                        // prefix for this loop's variable

    // class access
    // ------------

    /**
     * Copy internal loop repository to the destination list
     * @param destRepo  list object receiving the destination list
     */
    public static void FillLoopRepo(List<LoopInfo> destRepo) {
        FillLoopRepo(destRepo, false);
    }
    /**
     * Copy internal loop repository to the destination list
     * @param destRepo  list object receiving the destination list
     * @param bShuffle  shuffle repo after copy
     */
    public static void FillLoopRepo(List<LoopInfo> destRepo, boolean bShuffle){
        // init repository
        InitLoopRepo();
        // make deep copy
        destRepo.clear();
        for (var li : s_loopRepo){
            destRepo.add(new LoopInfo(li));
        }
        // shuffle?
        if (bShuffle){
            Collections.shuffle(destRepo);
        }
    }

    /**
     * Fill internal loop repository
     */
    private static void InitLoopRepo() {
        // init only when necessary
        if (!s_loopRepo.isEmpty()) {
            return;
        }

        /*
        * First distinguish between cases WITH loop variables and WITHOUT them
        * 
        * cases WITHOUT loop variables can differ in command (3), and any combination
        * of internal of external loop commands (2^3 * 2^6 = 512).
        * As there is no loop var testing, it should not make the slightest difference
        * whether for, do or while is used.
        * Therefore, we choose to implement these 512 loops and use for/do/dowhile-
        * commands randomly.
        * 
        * cases WITH loop variables are harder, because there are so many different
        * possibilities. We use orthogonal arrays to make the lot containable.
        * 
        */
        
        // part 1: without loop vars
        InitLoopRepo_PartWithoutLoopVars();

        // part 2: with loop vars
        InitLoopRepo_PartWithLoopVars();

        // create variable expressions
        MakeLoopVarExpressions();
    }
    
    private static void InitLoopRepo_PartWithoutLoopVars(){
        // make all different combinations and add them to the repo
        for (int c=0; c<512; ++c){
            var loop = new LoopInfo();
            loop.m_bILC_UseContinue = ((c & 1) > 0);
            loop.m_bILC_UseGotoBegin = ((c & 2) > 0);
            loop.m_bILC_UseGotoEnd = ((c & 4) > 0);
            loop.m_bELC_UseBreak = ((c & 8) > 0);
            loop.m_bELC_UseExit = ((c & 16) > 0);
            loop.m_bELC_UseReturn = ((c & 32) > 0);
            loop.m_bELC_UseGotoDirectlyAfterThisLoop = ((c & 64) > 0);
            loop.m_bELC_UseGotoFurtherFromThisLoop = ((c & 128) > 0);
            loop.m_bELC_BreakOutNestedLoops = ((c & 256) > 0);
            s_loopRepo.add(loop);
        }
        // distribute for/do/dowhile rather randomly
        // first, make about 1/3 while loops
        for (int n=0; n< (s_loopRepo.size()/3); ++n){
            do{
                var loop = s_loopRepo.get(Misc.rnd.nextInt(0, s_loopRepo.size()));
                if (loop.m_loopCommand == s_defaultLoopCommand){
                    loop.m_loopCommand = ELoopCommands.WHILE;
                    break;
                }
            } while (true);
        }
        // then, make about 1/3 do-while
        for (int n=0; n< (s_loopRepo.size()/3); ++n){
            do{
                var loop = s_loopRepo.get(Misc.rnd.nextInt(0, s_loopRepo.size()));
                if (loop.m_loopCommand == s_defaultLoopCommand){
                    loop.m_loopCommand = ELoopCommands.DOWHILE;
                    break;
                }
            } while (true);
        }
    }

    private static void InitLoopRepo_PartWithLoopVars(){
        // use orthogonal arrays
        // factors: 8 update dir/type
        //          4 loop var test
        //     (9x) 2 control flow setting
        //          2 var type

        // get orthogonal array
        final int [] LEVELS = {8, 4, 2, 2,2,2, 2,2,2, 2,2,2};
        final int RUNS = 32;
        final int STRENGTH = 2;
        OrthogonalArray oa;
        try {
            oa = new OrthogonalArray(LEVELS, RUNS, STRENGTH);
        }
        catch (Exception e){
            System.out.println(e.toString());
            return;
        }

        final int COL_UPDATE = 0;
        final int COL_TEST = 1;
        final int COL_VAR_TYPE = 2;
        final int COL_CONTINUE = 3;
        final int COL_GOTO_I1 = 4;
        final int COL_GOTO_I2 = 5;
        final int COL_BREAK = 6;
        final int COL_EXIT = 7;
        final int COL_RETURN = 8;
        final int COL_GOTO_E1 = 9;
        final int COL_GOTO_E2 = 10;
        final int COL_GOTO_E3 = 11;

        // the big setup-loop
        for (int run=0; run < oa.iNRuns() ; ++run){
            // setup new loop and shorthand for loop var object
            var loop = new LoopInfo();
            loop.m_loopVar = new LoopVariable();
            var lv = loop.m_loopVar;
            // set several loop variable type
            switch (oa.iValuePerRunPerColumn(run, COL_VAR_TYPE)){
                case 0 -> lv.eVarType = ELoopVarTypes.INT;
                case 1 -> lv.eVarType = ELoopVarTypes.FLOAT;
            }
            // set update method
            lv.eUpdateType = ELoopVarUpdateTypes.intToType(oa.iValuePerRunPerColumn(run, COL_UPDATE));

            // set test method
            lv.eTestType = ELoopVarTestTypes.intToType(oa.iValuePerRunPerColumn(run, COL_TEST), lv.eUpdateType);

            // set control flow properties
            loop.m_bILC_UseContinue =                   (oa.iValuePerRunPerColumn(run, COL_CONTINUE) == 1);
            loop.m_bILC_UseGotoBegin =                  (oa.iValuePerRunPerColumn(run, COL_GOTO_I1) == 1);
            loop.m_bILC_UseGotoEnd =                    (oa.iValuePerRunPerColumn(run, COL_GOTO_I2) == 1);
            loop.m_bELC_UseBreak =                      (oa.iValuePerRunPerColumn(run, COL_BREAK) == 1);
            loop.m_bELC_UseExit =                       (oa.iValuePerRunPerColumn(run, COL_EXIT) == 1);
            loop.m_bELC_UseReturn =                     (oa.iValuePerRunPerColumn(run, COL_RETURN) == 1);
            loop.m_bELC_UseGotoDirectlyAfterThisLoop =  (oa.iValuePerRunPerColumn(run, COL_GOTO_E1) == 1);
            loop.m_bELC_UseGotoFurtherFromThisLoop =    (oa.iValuePerRunPerColumn(run, COL_GOTO_E2) == 1);
            loop.m_bELC_BreakOutNestedLoops =           (oa.iValuePerRunPerColumn(run, COL_GOTO_E3) == 1);
            

            // add loop to repo
            s_loopRepo.add(loop);
            // and also add other loop types (not that many, so we can combine everything with everything)
            loop = new LoopInfo(loop);
            loop.m_loopCommand = ELoopCommands.WHILE;
            s_loopRepo.add(loop);
            loop = new LoopInfo(loop);
            loop.m_loopCommand = ELoopCommands.DOWHILE;
            s_loopRepo.add(loop);
        }
    }

    // object construction
    //////////////////////

    /**
     * Default constructor, sets up default loop and initializes ID
     */
    public LoopInfo(){
        // always create new ID
        SetID();
    }

    /**
     * Copy constructor. All objects get a deep copy.
     * @param rhs  source object
     */
    public LoopInfo(LoopInfo rhs){
        if (rhs!=null) {
            m_loopCommand = rhs.m_loopCommand;
            m_bILC_UseContinue = rhs.m_bILC_UseContinue;
            m_bILC_UseGotoBegin = rhs.m_bILC_UseGotoBegin;
            m_bILC_UseGotoEnd = rhs.m_bILC_UseGotoEnd;
            m_bELC_UseBreak = rhs.m_bELC_UseBreak;
            m_bELC_UseExit = rhs.m_bELC_UseExit;
            m_bELC_UseReturn = rhs.m_bELC_UseReturn;
            m_bELC_UseGotoDirectlyAfterThisLoop = rhs.m_bELC_UseGotoDirectlyAfterThisLoop;
            m_bELC_UseGotoFurtherFromThisLoop = rhs.m_bELC_UseGotoFurtherFromThisLoop;
            m_bELC_BreakOutNestedLoops = rhs.m_bELC_BreakOutNestedLoops;
            if (rhs.m_loopVar != null) {
                m_loopVar = new LoopVariable(rhs.m_loopVar);
            }
            m_iNumberOfImplementations = rhs.m_iNumberOfImplementations;
            m_iCurrentNestingLevel = rhs.m_iCurrentNestingLevel;
            m_strVariablePrefix = rhs.m_strVariablePrefix;
        }

        // always create new ID
        SetID();
    }

    /**
     * Set ID for this loop object, keeping track of ID's for uniqueness
     */
    private void SetID(){
        m_lngLoopID = s_lngNextUsedID++;
    }

    // object access
    // -------------
    //
    // part A: straightforward getters/setters
    public boolean bGetILC_UseContinue() {
        return m_bILC_UseContinue;
    }
    public boolean bGetILC_UseGotoBegin() {
        return m_bILC_UseGotoBegin;
    }
    public boolean bGetILC_UseGotoEnd() {
        return m_bILC_UseGotoEnd;
    }
    public boolean bGetELC_UseBreak() {
        return m_bELC_UseBreak;
    }
    public boolean bGetELC_UseExit() {
        return m_bELC_UseExit;
    }
    public boolean bGetELC_UseReturn() {
        return m_bELC_UseReturn;
    }
    public boolean bGetELC_UseGotoDirectlyAfterThisLoop() {
        return m_bELC_UseGotoDirectlyAfterThisLoop;
    }
    public boolean bGetELC_UseGotoFurtherFromThisLoop() {
        return m_bELC_UseGotoFurtherFromThisLoop;
    }
    public boolean bGetELC_BreakOutNestedLoops() {
        return m_bELC_BreakOutNestedLoops;
    }
    public LoopVariable getLoopVar() {
        return m_loopVar;
    }
    public ELoopCommands getLoopCommand() {
        return m_loopCommand;
    }
    public void setVariablePrefix(String strPrefix){
        m_strVariablePrefix = strPrefix;
    }

    // part B:
    // getters that need calculating

    /**
     * Get the loop finitude. Determines whether this loop certainly be infinite
     * or probably be finite.
     * @return  TIL = truly infinite, PFL = probably finite
     */
    public ELoopFinitude getLoopFinitude() {
        boolean bTIL = false;           // default: PFL
        if (!bAnyExternalFlowControlUsed()){
            // if any external loop flow control is used, the loop is probably finite (which is this methods default
            // answer). So, only explore loop without external loop flow control
            if (m_loopVar == null){
                // no loop var used, so there are no exit options - truly infinite
                bTIL = true;
            }
            else {
                // loop var used, so only a possibility of finality when it is actually tested
                bTIL = (m_loopVar.eTestType == ELoopVarTestTypes.UNUSED);
            }
        }
        // convert bool to enum
        if (bTIL) {
            return ELoopFinitude.TIL;
        }
        else {
            return ELoopFinitude.PFL;
        }
    }

    /**
     * Determine whether any of the external loop flow commands are used
     * @return true if one or more external loop flow commands are used
     */
    public boolean bAnyExternalFlowControlUsed(){
        return  m_bELC_UseBreak ||
                m_bELC_UseExit ||
                m_bELC_UseReturn ||
                m_bELC_UseGotoDirectlyAfterThisLoop ||
                m_bELC_UseGotoFurtherFromThisLoop ||
                m_bELC_BreakOutNestedLoops;
    }

    /**
     * Determine which loop expressions are available, depending
     * on the use of loop variables and (if the loop var is used)
     * whether or not it is actually tested
     * @return enum stating which loop expressions are available
     */
    public ELoopExpressions getLoopExpressions() {
        if (m_loopVar==null){
            // no loop var, no expressions
            return ELoopExpressions.NONE;
        }
        // any loop var is only useful when (a) initialized and (b) updated. Without update, it
        // is indistinguishable from any other variable. It is very bad practice not to initialize
        // in any way before update, so the only difference is: test or no test?
        if (m_loopVar.eTestType != ELoopVarTestTypes.UNUSED){
            return ELoopExpressions.ALL;
        }
        return ELoopExpressions.INIT_UPDATE;
    }

    // part C: access to obtain code
    ////////////////////////////////

    /**
     * Get the statement to initialize loops before the loop statement.
     * @return  depends on loop type and use of loop var. If a for is used,
     *          no separate init is returned (only a comment stating so). Otherwise,
     *          if no loop variable is used, a comment is return stating so.
     *          Otherwise: return a variable declaration and initialization statement.
     *          Something like: int x=100;
     */
    public String strGetLoopInit(){
        if (m_loopCommand == ELoopCommands.FOR){
            // for init is in the statement itself
            return "/* for is not initialized separately */";
        }
        if (m_loopVar!=null){
            // do/while: only init a loop var when it is used
            return strGetCompleteLoopInitExpression() + "; /* loop var init */";
        }
        return "/* no loop var used, so no init */";
    }

    /**
     * Get the complete loop command: do {, while (...) { or for (...;...;...)
     * @return   complete loop command
     */
    public String strGetLoopCommand(){
        switch (m_loopCommand) {
            case FOR -> {
                var out = new StringBuilder();
                // if no init expression is available, declare the variable before the loop,
                // so that it may be updated anyway
                if ( (m_loopVar!=null) && (!getLoopExpressions().bInitAvailable())){
                    out.append(m_loopVar.eVarType.strGetCKeyword()).append(" ").append(strGetLoopVarName()).append("; ");
                }
                out.append("for (");
                if (getLoopExpressions().bInitAvailable()){
                    out.append(strGetCompleteLoopInitExpression());
                }
                out.append(";");
                if (getLoopExpressions().bTestAvailable()){
                    out.append(strGetCompleteLoopTestExpression());
                }
                out.append(";");
                if (getLoopExpressions().bUpdateAvailable()){
                    out.append(strGetCompleteLoopUpdateExpression());
                }
                out.append(") {");
                return out.toString();
            }
            case DOWHILE -> {
                return "do {";
            }
            case WHILE -> {
                if (getLoopExpressions().bTestAvailable()) {
                    return "while (" + strGetCompleteLoopTestExpression() + ") {";
                }
                else{
                    return "while (true) {";
                }
            }
        }
        return "/* unknown loop command */";
    }

    /**
     * Get the loop trailer, depending on the type of loop
     * @return  "}" for for's and while's, "} while ()" for do-while's
     */
    public String strGetLoopTrailer(){
        if (m_loopCommand == ELoopCommands.DOWHILE){
            // do: close block and add while command
            if (getLoopExpressions().bTestAvailable()) {
                return "} while (" + strGetCompleteLoopTestExpression() + ");";
            }
            else {
                return "} while (true)";
            }
        }
        else {
            // for/while: just close block
            return "}";
        }
    }

    /**
     * Get the loop variable update expression, something like 'x++'
     * @return   loop variable update expression, or a comment stating there
     *           is no loop variable and thus no update
     */
    public String strGetCompleteLoopUpdateExpression(){
        if (m_loopVar!=null){
            return strGetLoopVarName() + m_loopVar.strUpdateExpression;
        }
        return "/* no loop variable, so no update */";
    }

    /**
     * Get the complete loop variable name, including the prefix
     * @return      loop variable name
     */
    public String strGetLoopVarName(){
        return m_strVariablePrefix +  "_LV" + m_lngLoopID;
    }

    public CodeMarker getStartMarker(){
        var out = new CodeMarker();
        out.setProperty(STRLOOPIDPROPERTY, "" + m_lngLoopID);
        out.setProperty(ELoopMarkerTypes.STRPROPERTYNAME, ELoopMarkerTypes.BEFORE.strPropertyValue());
        out.setProperty(ELoopCommands.STRPROPERTYNAME, m_loopCommand.strPropertyValue());
        out.setProperty(ELoopFinitude.STRPROPERTYNAME, getLoopFinitude().strPropertyValue());
        if (m_loopVar!=null) {
            out.setProperty(ELoopVarTestTypes.STRPROPERTYNAME, m_loopVar.eTestType.strPropertyValue());
            out.setProperty(ELoopVarUpdateTypes.STRPROPERTYNAME, m_loopVar.eUpdateType.strPropertyValue());
        }
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








    private static void MakeLoopVarExpressions(){
        for (var loop : s_loopRepo) {
            if (loop.getLoopExpressions().bInitAvailable()){
                int low = ILOOPVARLOWVALUELOWBOUND;
                int high = ILOOPVARLOWVALUEHIGHBOUND;
                if (loop.m_loopVar.eUpdateType.bIsDecreasing()){
                    low = ILOOPVARHIGHVALUELOWBOUND;
                    high = ILOOPVARHIGHVALUEHIGHBOUND;
                }
                loop.m_loopVar.strInitExpression = "" + Misc.rnd.nextInt(low, high);
            }
            if (loop.getLoopExpressions().bUpdateAvailable()){
                loop.m_loopVar.strUpdateExpression = loop.m_loopVar.eUpdateType.strGetUpdateExpression();
            }
            if (loop.getLoopExpressions().bTestAvailable()){
                // only return test expression when wanted
                int low = ILOOPVARHIGHVALUELOWBOUND;
                int high = ILOOPVARHIGHVALUEHIGHBOUND;
                if (loop.m_loopVar.eUpdateType.bIsDecreasing()){
                    low = ILOOPVARLOWVALUELOWBOUND;
                    high = ILOOPVARLOWVALUEHIGHBOUND;
                }
                loop.m_loopVar.strTestExpression = loop.m_loopVar.eTestType.strCOperator() + Misc.rnd.nextInt(low,high);
            }
        }
    }






    private String strGetCompleteLoopInitExpression(){
        if (m_loopVar!=null){
            // only init a loop var when it is used
            if (m_loopVar.strInitExpression.isEmpty()){
                return m_loopVar.eVarType.strGetCKeyword() + " " + strGetLoopVarName();
            }
            else {
                return m_loopVar.eVarType.strGetCKeyword() + " " + strGetLoopVarName() + "=" + m_loopVar.strInitExpression;
            }
        }
        return "";
    }

    private String strGetCompleteLoopTestExpression(){
        if (getLoopExpressions().bTestAvailable()){
            return strGetLoopVarName() + m_loopVar.strTestExpression;
        }
        return "/* no loop test expression */";
    }





















    // Object information for output

    public static String strToStringHeader(){
        return "I/F TYPE    IN UP TS  IC IB IE  EB EE ER ED EN EF  LV VT LU LT";
    }
    public String toString(){
        StringBuilder out;
        out = new StringBuilder(getLoopFinitude() + " ");
        out.append(m_loopCommand);
        while (out.length() < 12) { out.append(" "); }
        out.append(cBooleanToChar(getLoopExpressions().bInitAvailable())).append("  ");
        out.append(cBooleanToChar(getLoopExpressions().bUpdateAvailable())).append("  ");
        out.append(cBooleanToChar(getLoopExpressions().bTestAvailable())).append("   ");

        out.append(cBooleanToChar(m_bILC_UseContinue)).append("  ");
        out.append(cBooleanToChar(m_bILC_UseGotoBegin)).append("  ");
        out.append(cBooleanToChar(m_bILC_UseGotoEnd)).append("   ");

        out.append(cBooleanToChar(m_bELC_UseBreak)).append("  ");
        out.append(cBooleanToChar(m_bELC_UseExit)).append("  ");
        out.append(cBooleanToChar(m_bELC_UseReturn)).append("  ");
        out.append(cBooleanToChar(m_bELC_UseGotoDirectlyAfterThisLoop)).append("  ");
        out.append(cBooleanToChar(m_bELC_BreakOutNestedLoops)).append("  ");
        out.append(cBooleanToChar(m_bELC_UseGotoFurtherFromThisLoop)).append("   ");

        out.append(cBooleanToChar(m_loopVar != null)).append("  ");
        if (m_loopVar == null){
            out.append("        ");
        }
        else {
            out.append(m_loopVar.eVarType.strShortCode()).append("  ");
            out.append(m_loopVar.eUpdateType.strShortCode()).append(" ");
            out.append(m_loopVar.eTestType.strCOperator()).append(" "); if (m_loopVar.eTestType.strCOperator().length()==1){out.append(" ");}

        }

        return out.toString();
    }
}