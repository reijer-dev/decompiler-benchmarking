package nl.ou.debm.common.feature1;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.ortharray.OrthogonalArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static nl.ou.debm.common.Misc.cBooleanToChar;
import static nl.ou.debm.common.Misc.strFloatTrailer;
import static nl.ou.debm.common.feature1.LoopProducer.*;

/*

    Properties of loops
    ===================

    Loop nesting
    ------------
    A loop may in its body contain another loop. This second one is called a nested (or child) loop. Nesting can have
    different patterns. The nesting level of a loop is the number of parent loops. So, the outermost loop has nesting
    level 0. If a loop has a parent loop that has a parent loop, the loop's nesting level is 2. Loops may have multiple
    children.
    We use all kinds of nesting patterns, implemented by LoopPatternNode.

    Loop command
    ------------
    One of three: do, while and for. We use all three.

    Loop variable
    -------------
    A loop variable is a variable that is updated every time the loop's body is executed. Loop variables are not always
    used. We produce loops that do and do not use loop variables.

    When used, loop variables may have different properties of their own:
        - the variable type (int, long, char...) We use int and float only and consider these representations of
          all sorts of numeric values;
        - the update mode of the variable (++, --, +=[int], -=[int] etc.). We use ++, --, +=?, -=?, *=?, /=?,
          +=getchar() and -=getchar. A '?' stands for a numeric value. We also use the getchar() variants to include loops
          that can never be calculated at compile time;
        - whether or not the loop variable is tested to continue the loop and if tested: in what way. We include
          both tested and non-tested loop variables, and when tested we use !=, >, <, >= and <= as operators;
        - the scope of the variable (global, function, compound statement). We only use as local as possible. Any
          variable used in a for-loop is declared in the for-statement, variables used in do and while loops are
          declared immediately before the do- or while-statement. Every loop has its own loop variable; we do not
          reuse any, to keep the matter simple.
        - whether the variable is part of a struct. We don't use struct variables, as the main difference is
          not in the loop code, but in the variable retrieval/updating code, which is part of another feature's study.

    Loop finitude
    -------------
    A loop can be infinite or not. If a loop has no exit strategy, control is never transferred outside the loop once
    entered (while (true) { ; }). We call these: TIL's - truly infinite loops. We know for sure they will never end.
    The opposite are PFL's: probably finite loops. These are loops that may end (though we're not always certain at
    compile time): for (int a=0; a<10; a++) {} -- this will probably end (unless the loop's body would include a
    statement like a--, which counter effects the a++ in the for command). for (int a=0; a<10; a+=getchar()) may not end
    at all, if getchar() keeps on returning zero or negative values.

    Explicit loop flow control
    --------------------------
    Several commands transfer control flow outside the loop:
    - break
    - exit
    - return
    - goto somewhere-further-after-the-loop
    - goto directly-after-the-outermost-loop (in other languages, this may a break-multiple-loops-statement, that C
      lacks)

    One command transfers control to another point inside the loop:
    - continue
    We make all sorts of combinations of the above, to see what happens when a non-straightforward loop is encountered
    by the decompiler.

    Unrolling
    ---------
    Loops may be unrolled as an optimization. We try to seduce the compiler, when running optimization, to produce both
    normal and unrolled loops. We do this by making loops with a large number of iterations and containing some
    dummy-instruction and child loops; loops like these are not attractive to unroll. On the other hand, we also
    present loops without any fuss that do not contain any explicit loop flow control or dummy-statements and have
    only a limited number of iterations. We check in the LLVM whether a loop is unrolled or not.

 */

/**
 * Every object of the class is a set of loop properties, that can be used to write code.
 * The class itself holds methods for setting up and accessing a repository of LoopInfo object, representing
 * all variations to be tested.
 */
public class LoopInfo {

    // class attributes
    // ----------------
    /** next new object gets this ID */             private static long s_lngNextUsedID = 0;
    /** loop repo, all loops to be implemented */   private static final List<LoopInfo> s_loopRepo = new ArrayList<>();
    /** default loop command */                     private static final ELoopCommands s_defaultLoopCommand = ELoopCommands.FOR;
    /** sync object */                              private static final Object s_syncObj = new Object();

    // constants
    // ---------
    /** number of runs for the main OA */           private static final int INUMBEROFOARUNS = 32;

    // class init
    // ----------
    static {
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
         * then there is the set of loops that we try to lure the compiler into unrolling
         * these will never use any internal or external control flow constructs
         * and they will only use static updates (no +=getchar() or -=getchar())
         */

        // part 1: without loop vars
        initLoopRepo_PartWithoutLoopVars();

        // part 2: with loop vars
        initLoopRepo_PartWithLoopVars();

        // part 3: unroll loops
        initLoopRepo_PartForUnrolling();

        // create variable expressions
        makeLoopVarExpressions();
    }

    // object attributes
    // -----------------
    //
    // part A: all information required to make the loop statements
    // basics
    /** do/for/while */                                         private ELoopCommands m_loopCommand = s_defaultLoopCommand;
    /** loop variable details (null = unused) */                private LoopVariable m_loopVar = null;
    /** OA run number used, -1 = not used */                    private int m_iOARunUsed = -1;

    // internal loop flow control
    /** put continue statement in loop */                       private boolean m_bILC_UseContinue = false;

    // external loop flow control
    /** put break statement in loop */                          private boolean m_bELC_UseBreak = false;
    /** put exit call in loop */                                private boolean m_bELC_UseExit = false;
    /** put return statement in loop */                         private boolean m_bELC_UseReturn = false;
    /** put goto somewhere further than immediately after loop */private boolean m_bELC_UseGotoFurtherFromThisLoop = false;
    /** break out nested loops */                               private boolean m_bELC_BreakOutNestedLoops = false;

    // part B: all information for loop objects
    /** unique loop-object ID */                                private long m_lngLoopID = 0;
    /** number of times this loop is actually in the code */    private int m_iNumberOfImplementations = 0;
    /** prefix for this loop's variable */                      private String m_strVariablePrefix = "";
    /** determine in what way loop unrolling is or is not stimulated */private ELoopUnrollTypes m_unrollMode = ELoopUnrollTypes.NO_ATTEMPT;
    /** number of iterations of possibly unrolled loop; -1 = unused */ private int m_iNumberOfIterations = -1;


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
        synchronized (s_syncObj) {
            // make deep copy
            destRepo.clear();
            for (var li : s_loopRepo) {
                destRepo.add(new LoopInfo(li));
            }
            // shuffle?
            if (bShuffle) {
                Collections.shuffle(destRepo);
            }
        }
    }

    /**
     * Provide a testcase loopInfo.
     * @param iSequence which testcase to retrieve
     * @return LoopInfo object with test settings
     */
    public static LoopInfo GetSingleTestLoopInfo(int iSequence){
        var li = new LoopInfo();

        switch (iSequence) {
            case 1 -> {
//                li.m_loopVar = new LoopVariable();
//                li.m_loopVar.eVarType = ELoopVarTypes.INT;
//                li.m_loopVar.eTestType = ELoopVarTestTypes.GREATER_OR_EQUAL;
//                li.m_loopVar.strInitExpression = "2024";
//                li.m_loopVar.strTestExpression = ">=1976";
//                li.m_loopVar.strUpdateExpression = "=-17";
                li.m_loopCommand = ELoopCommands.FOR;
            }
            default -> {
                li.m_loopVar = new LoopVariable();
                li.m_loopVar.eVarType = ELoopVarTypes.INT;
                li.m_loopVar.eTestType = ELoopVarTestTypes.SMALLER_THAN;
                li.m_loopVar.strInitExpression = "1923";
                li.m_loopVar.strTestExpression = "<2525";
                li.m_loopVar.strUpdateExpression = "++";
                li.m_loopCommand = ELoopCommands.WHILE;
                li.m_bELC_UseBreak = true;
                li.m_bELC_UseExit = true;
                li.m_bELC_UseGotoFurtherFromThisLoop = true;
                li.m_bELC_UseReturn = true;
                li.m_bELC_BreakOutNestedLoops = true;
                li.m_bILC_UseContinue = true;
            }
        }

        return li;
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
            m_bELC_UseBreak = rhs.m_bELC_UseBreak;
            m_bELC_UseExit = rhs.m_bELC_UseExit;
            m_bELC_UseReturn = rhs.m_bELC_UseReturn;
            m_bELC_UseGotoFurtherFromThisLoop = rhs.m_bELC_UseGotoFurtherFromThisLoop;
            m_bELC_BreakOutNestedLoops = rhs.m_bELC_BreakOutNestedLoops;
            if (rhs.m_loopVar != null) {
                m_loopVar = new LoopVariable(rhs.m_loopVar);
            }
            m_iNumberOfImplementations = rhs.m_iNumberOfImplementations;
            m_strVariablePrefix = rhs.m_strVariablePrefix;
            m_unrollMode = rhs.m_unrollMode;
            m_iNumberOfIterations = rhs.m_iNumberOfIterations;
            m_iOARunUsed = rhs.m_iOARunUsed;
        }

        // always create new ID
        SetID();
    }

    /**
     * Construct info object from code marker<br>
     * IMPORTANT: this loop's ID is taken from the marker,
     * so uniqueness is no longer guaranteed!
     */
    public LoopInfo(LoopCodeMarker cm){
        m_loopCommand = cm.getLoopCommand();
        if (!cm.strGetLoopVarName().isEmpty()){
            m_loopVar = new LoopVariable();
            m_loopVar.strInitExpression = cm.strGetInitExpression();
            m_loopVar.strUpdateExpression = cm.strGetUpdateExpression();
            m_loopVar.strTestExpression = cm.strGetTestExpression();
        }
        m_bILC_UseContinue = cm.bGetUseContinue();
        m_bELC_UseBreak = cm.bGetUseBreak();
        m_bELC_UseExit = cm.bGetUseExit();
        m_bELC_UseReturn = cm.bGetUseReturn();
        m_bELC_UseGotoFurtherFromThisLoop = cm.bGetUseGotoFurtherFromThisLoop();
        m_bELC_BreakOutNestedLoops = cm.bGetUseBreakOutNestedLoops();
        m_lngLoopID = cm.lngGetLoopID();
        m_unrollMode = cm.getLoopUnrolling();
    }

    /**
     * Set ID for this loop object, keeping track of ID's for uniqueness
     */
    private void SetID(){
        synchronized (s_syncObj) {
            m_lngLoopID = s_lngNextUsedID++;
        }
    }

    // object access
    // -------------
    //
    // part A: straightforward getters/setters
    public long lngGetLoopID(){
        return m_lngLoopID;
    }
    public boolean bGetILC_UseContinue() {
        return m_bILC_UseContinue;
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
    public ELoopUnrollTypes getUnrolling(){
        return m_unrollMode;
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

    /**
     * Returns true if the loop should be made conditional in the code. Any loop that will not be able
     * to jump directly after the loop, must be conditional, because otherwise code will be optimized
     * away for being unreachable
     * @return Make loop conditional or not.
     */
    public boolean bMakeConditional(){
        // all TIL-loops must be conditional
        if (getLoopFinitude()==ELoopFinitude.TIL){
            return true;
        }
        // left: all PFL's

        // every PFL with a test condition is fine
        if (getLoopVar()!=null){
            if (getLoopVar().eTestType!=ELoopVarTestTypes.UNUSED){
                return false;
            }
        }
        // left: PFL's that have no loop-var test
        //       these *must* have one or more external control flow statements
        //       because that is the only way to terminate the loop

        // if one of them is a break -- all is fine
        if (bGetELC_UseBreak()){
            return false;
        }

        // left: PFL's that have a loop termination statement such as:
        //       - return (no execution past the loop because it returns to the function call)
        //       - exit (no execution past the loop because it terminates the program)
        //       - goto-further-from-loop (because it skips some code)
        //       - break-multiple-loops (because it also skips code)
        return true;
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
                return "} while (true);";
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

    /**
     * Get start-of-loop-code-marker. This marker contains all elementary loop code
     * @return object containing all loop information
     */
    public LoopCodeMarker getStartMarker(int iCurrentNestingLevel, long lngParentLoopID){
        // get default(s) for every loop marker
        var out = getDefaultMarker();
        // set location
        out.setLoopCodeMarkerLocation(ELoopMarkerLocationTypes.BEFORE);
        out.setNestingLevel(iCurrentNestingLevel);
        out.setParentLoopID(lngParentLoopID);
        // set loop properties
        out.setLoopCommand(m_loopCommand);
        out.setLoopFinitude(getLoopFinitude());
        if (m_loopVar!=null) {
            // loop var
            out.setLoopVarName(strGetLoopVarName());
            // loop var type
            out.setLoopVarType(m_loopVar.eVarType);
            // init expression
            out.setInitExpression(m_loopVar.strInitExpression);
            // update expression
            out.setUpdateExpression(m_loopVar.strUpdateExpression);
            // test expression
            if (getLoopExpressions().bTestAvailable()){
                out.setTestExpression(m_loopVar.strTestExpression);
            }
        }
        // flow control
        out.setUseContinue(               m_bILC_UseContinue);
        out.setUseBreak(                  m_bELC_UseBreak);
        out.setUseExit(                   m_bELC_UseExit);
        out.setUseReturn(                 m_bELC_UseReturn);
        out.setUseGotoFurtherFromThisLoop(m_bELC_UseGotoFurtherFromThisLoop);
        out.setUseBreakOutNestedLoops(    m_bELC_BreakOutNestedLoops);
        // unrolling attempt
        out.setLoopUnrolling(m_unrollMode);
        if (m_unrollMode != ELoopUnrollTypes.NO_ATTEMPT) {
            // unrolling iterations
            assert m_iNumberOfIterations >-1 : "something went wrong in determining the number of iterations";
            out.setNumberOfUnrolledIterations(m_iNumberOfIterations);
        }
        // done
        return out;
    }

    /**
     * Get end-of-loop-code-marker
     * @return  object containing minimum loop information for use at end of loop
     */
    public CodeMarker getEndMarker(){
        var out = getDefaultMarker();
        out.setLoopCodeMarkerLocation(ELoopMarkerLocationTypes.AFTER);
        return out;
    }
    /**
     * Get body-of-loop-code-marker
     * @return  object containing minimum loop information for use in loop body
     */
    public CodeMarker getBodyMarker(){
        var out = getDefaultMarker();
        out.setLoopCodeMarkerLocation(ELoopMarkerLocationTypes.BODY);
        return out;
    }

    // Private static methods
    /////////////////////////
    /**
     * add to the default loop repo loops that have no loop variables
     */
    private static void initLoopRepo_PartWithoutLoopVars(){
        // make all different combinations and add them to the repo
        for (int c=0; c<64; ++c){
            var loop = new LoopInfo();
            loop.m_bILC_UseContinue = ((c & 1) > 0);
            loop.m_bELC_UseBreak = ((c & 2) > 0);
            loop.m_bELC_UseExit = ((c & 4) > 0);
            loop.m_bELC_UseReturn = ((c & 8) > 0);
            loop.m_bELC_UseGotoFurtherFromThisLoop = ((c & 16) > 0);
            loop.m_bELC_BreakOutNestedLoops = ((c & 32) > 0);
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

    /**
     * add to the internal loop repo ordinary loops that have loop variables
     */
    private static void initLoopRepo_PartWithLoopVars(){
        // the big setup-loop -- the loop properties are set from OA-info in another method
        for (int run=0; run < INUMBEROFOARUNS ; ++run){
            // setup new loop and shorthand for loop var object
            var loop = new LoopInfo();
            loop.m_loopVar = new LoopVariable();
            // remember this loop was created using orthogonal arrays for the properties
            loop.m_iOARunUsed = run;
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
        // make sure all the loop properties are set using OA's
        refactorOALoopProperties(s_loopRepo);
    }

    /**
     * look for all elements in the repo that were created using and OA for the loop's properties
     * refactor those properties, using another OA
     * @param loopRepo the repo to be refactored
     */
    public static void refactorOALoopProperties(List<LoopInfo> loopRepo){
        // OA properties
        final int [] LEVELS = {8, 4, 2, 2,2,2, 2,2,2};
        final int STRENGTH = 2;
        OrthogonalArray oa = new OrthogonalArray(LEVELS, INUMBEROFOARUNS, STRENGTH);
        // OA columns
        final int COL_UPDATE = 0;
        final int COL_TEST = 1;
        final int COL_VAR_TYPE = 2;
        final int COL_CONTINUE = 3;
        final int COL_BREAK = 4;
        final int COL_EXIT = 5;
        final int COL_RETURN = 6;
        final int COL_GOTO_E1 = 7;
        final int COL_GOTO_E2 = 8;

        // loop all the loops in the repo
        for (var loop : loopRepo){
            // only refactor those who have loop properties based on orthogonal arrays
            if (loop.m_iOARunUsed >=0){
                // easy access to the run index
                int iRun = loop.m_iOARunUsed;

                // access loop var
                var lv = loop.m_loopVar;

                // set the loop variable type
                switch (oa.iValuePerRunPerColumn(iRun, COL_VAR_TYPE)){
                    case 0 -> lv.eVarType = ELoopVarTypes.INT;
                    case 1 -> lv.eVarType = ELoopVarTypes.FLOAT;
                }
                // set update method
                lv.eUpdateType = ELoopVarUpdateTypes.intToType(oa.iValuePerRunPerColumn(iRun, COL_UPDATE));

                // set test method
                lv.eTestType = ELoopVarTestTypes.OAIntToType(oa.iValuePerRunPerColumn(iRun, COL_TEST), lv.eUpdateType);

                // set control flow properties
                loop.m_bILC_UseContinue =                   (oa.iValuePerRunPerColumn(iRun, COL_CONTINUE) == 1);
                loop.m_bELC_UseBreak =                      (oa.iValuePerRunPerColumn(iRun, COL_BREAK) == 1);
                loop.m_bELC_UseExit =                       (oa.iValuePerRunPerColumn(iRun, COL_EXIT) == 1);
                loop.m_bELC_UseReturn =                     (oa.iValuePerRunPerColumn(iRun, COL_RETURN) == 1);
                loop.m_bELC_UseGotoFurtherFromThisLoop =    (oa.iValuePerRunPerColumn(iRun, COL_GOTO_E1) == 1);
                loop.m_bELC_BreakOutNestedLoops =           (oa.iValuePerRunPerColumn(iRun, COL_GOTO_E2) == 1);
            }
        }
    }


    /**
     * add to internal loop repo loops that will quite probably unroll
     */
    private static void initLoopRepo_PartForUnrolling(){
        // unrolling
        // 3 loop commands (for, do while)
        // 4 dir/types (++ -- +=c -=c)
        // 2 var types (int float)
        // 3 tests when int (!=, < or <= when increasing; always test)
        // 2 tests when float (< or <= when increasing; always test, do not use != as it may block unrolling)
        // 0 control flow settings
        // 2 body markers (with or without printing the loop var)
        // total: 3 * 4 * (2 + 3) = 120
        // not that many, we add them all

        for (var updateItem : ELoopVarUpdateTypes.values()) {
        if (updateItem.bIncludeForUnrolling()) {
            for (var loopCommand : ELoopCommands.values()) {
                for (var unrollType : ELoopUnrollTypes.values()){
                if (unrollType != ELoopUnrollTypes.NO_ATTEMPT){
                    for (var varTypeItem : ELoopVarTypes.values()) {
                        // init loop info
                        var loop = new LoopInfo();
                        loop.m_loopVar = new LoopVariable();
                        var lv = loop.m_loopVar;
                        // set easy values
                        loop.m_unrollMode = unrollType;
                        loop.m_loopCommand = loopCommand;
                        lv.eVarType = varTypeItem;
                        lv.eUpdateType = updateItem;
                        // test types:
                        // 1.: !=
                        lv.eTestType = ELoopVarTestTypes.NON_EQUAL;
                        if (lv.eVarType == ELoopVarTypes.INT) {
                            s_loopRepo.add(loop);
                        }
                        // 2., 3.: < and <=
                        if (lv.eUpdateType.bIsIncreasing()) {
                            loop = new LoopInfo(loop);
                            loop.m_loopVar.eTestType = ELoopVarTestTypes.SMALLER_THAN;
                            s_loopRepo.add(loop);
                            loop = new LoopInfo(loop);
                            loop.m_loopVar.eTestType = ELoopVarTestTypes.SMALLER_OR_EQUAL;
                            s_loopRepo.add(loop);
                        }
                        // 4., 5.: > and >=
                        else {
                            loop = new LoopInfo(loop);
                            loop.m_loopVar.eTestType = ELoopVarTestTypes.GREATER_THAN;
                            s_loopRepo.add(loop);
                            loop = new LoopInfo(loop);
                            loop.m_loopVar.eTestType = ELoopVarTestTypes.GREATER_OR_EQUAL;
                            s_loopRepo.add(loop);
                        }
                    }
                }
                }
            }
        }
        }
    }

    private static void makeLoopVarExpressions(){
        for (var loop : s_loopRepo) {
            // depends on unrolling attempt
            if (loop.getUnrolling() != ELoopUnrollTypes.NO_ATTEMPT) {
                // attempted loop unrolling
                //
                // loop var shorthand
                var lv = loop.m_loopVar;
                // this may only happen when all expressions are available
                assert loop.getLoopExpressions().bInitAvailable();
                assert loop.getLoopExpressions().bUpdateAvailable();
                assert loop.getLoopExpressions().bTestAvailable();
                assert lv!=null;
                // determine number of iterations
                loop.m_iNumberOfIterations = Misc.rnd.nextInt(ILOOPMINNUMBEROFITERATIONSFORUNROLLING, ILOOPMAXNUMBEROFITERATIONSFORUNROLLING);
                // determine start point
                int iStartPoint = Misc.rnd.nextInt(ILOOPSTARTMINIMUMFORUNROLLING, ILOOPSTARTMMAXMUMFORUNROLLING);
                // loop update value
                int iLoopUpdate = 1;
                switch (lv.eUpdateType){
                    case INCREASE_BY_ONE -> { ; }
                    case DECREASE_BY_ONE -> iLoopUpdate = -1;
                    case INCREASE_OTHER -> iLoopUpdate = Misc.rnd.nextInt(ILOOPUPDATEIFNOTONELOWBOUND, ILOOPUPDATEIFNOTONEHIGHBOUND);
                    case DECREASE_OTHER -> iLoopUpdate = -Misc.rnd.nextInt(ILOOPUPDATEIFNOTONELOWBOUND, ILOOPUPDATEIFNOTONEHIGHBOUND);
                    default -> {assert false;}
                }
                // set init expression
                lv.strInitExpression = iStartPoint + strFloatTrailer(lv.eVarType==ELoopVarTypes.FLOAT);
                // set update expression
                lv.strUpdateExpression = lv.eUpdateType.strGetUpdateExpressionForUnrolling(lv.eVarType, iLoopUpdate);
                // get final update value to accommodate for float values
                double dblPreciseUpdateValue;
                if (lv.strUpdateExpression.charAt(1) == '='){
                    // += or -=
                    dblPreciseUpdateValue = Misc.dblRobustStringToDouble(lv.strUpdateExpression.substring(2)) * Math.signum(iLoopUpdate);
                    // the sign function is needed, because the -= expression will have a positive operand
                }
                else{
                    // ++ or --
                    dblPreciseUpdateValue = iLoopUpdate;
                }

                // set test expression
                //
                // in float-expressions, we may loose some accuracy, which is why we don't use the unequal-operator
                if (lv.eVarType==ELoopVarTypes.FLOAT) {
                    assert lv.eTestType!=ELoopVarTestTypes.NON_EQUAL : "No unequal operator for floating unrollables";
                    lv.strTestExpression = lv.eTestType.strCOperator() + (iStartPoint + (loop.m_iNumberOfIterations * dblPreciseUpdateValue));
                }
                else {
                    lv.strTestExpression = lv.eTestType.strCOperator() + (long)(iStartPoint + (loop.m_iNumberOfIterations * dblPreciseUpdateValue));
                }
                // compensate for >= and <= --> they will have one more iteration
                if ((lv.eTestType==ELoopVarTestTypes.GREATER_OR_EQUAL) || (lv.eTestType==ELoopVarTestTypes.SMALLER_OR_EQUAL)){
                    loop.m_iNumberOfIterations++;
                }
            }
            else {
                // normal loops
                ///////////////

                // init expression
                if (loop.getLoopExpressions().bInitAvailable()) {
                    // count low to high or high to low
                    int low = ILOOPVARLOWVALUELOWBOUND;
                    int high = ILOOPVARLOWVALUEHIGHBOUND;
                    if (loop.m_loopVar.eUpdateType.bIsDecreasing()) {
                        low = ILOOPVARHIGHVALUELOWBOUND;
                        high = ILOOPVARHIGHVALUEHIGHBOUND;
                    }
                    // random init value
                    loop.m_loopVar.strInitExpression = Misc.rnd.nextInt(low, high) + strFloatTrailer(loop.m_loopVar.eVarType == ELoopVarTypes.FLOAT);
                }
                if (loop.getLoopExpressions().bUpdateAvailable()) {
                    // set update expression
                    loop.m_loopVar.strUpdateExpression = loop.m_loopVar.eUpdateType.strGetUpdateExpression(loop.m_loopVar.eVarType == ELoopVarTypes.FLOAT);
                }
                if (loop.getLoopExpressions().bTestAvailable()) {
                    // only return test expression when wanted
                    int low = ILOOPVARHIGHVALUELOWBOUND;
                    int high = ILOOPVARHIGHVALUEHIGHBOUND;
                    if (loop.m_loopVar.eUpdateType.bIsDecreasing()) {
                        low = ILOOPVARLOWVALUELOWBOUND;
                        high = ILOOPVARLOWVALUEHIGHBOUND;
                    }
                    // random test value, combine with operator
                    loop.m_loopVar.strTestExpression = loop.m_loopVar.eTestType.strCOperator() + Misc.rnd.nextInt(low, high);
                }
            }
        }
    }

    // private non-static methods

    /**
     * get default CodeMarker object
     * @return newly created object, loopID set
     */
    private LoopCodeMarker getDefaultMarker(){
        var out = new LoopCodeMarker();
        out.setLoopID(m_lngLoopID);
        return out;
    }

    /**
     * Get an init expression for the loop var. This may only be a declaration, but when
     * an init expression is used, a declaration-and-init is returned
     * @return something like "int q" or "int q=10"
     */
    private String strGetCompleteLoopInitExpression(){
        if (m_loopVar!=null){
            // only init a loop var when it is really used
            if (m_loopVar.strInitExpression.isEmpty()){
                return m_loopVar.eVarType.strGetCKeyword() + " " + strGetLoopVarName();
            }
            else {
                return m_loopVar.eVarType.strGetCKeyword() + " " + strGetLoopVarName() + "=" + m_loopVar.strInitExpression;
            }
        }
        return "";
    }

    /**
     * Get a full loop var test expression
     * @return something like "a==10"
     */
    private String strGetCompleteLoopTestExpression(){
        if (getLoopExpressions().bTestAvailable()){
            return strGetLoopVarName() + m_loopVar.strTestExpression;
        }
        return "/* no loop test expression */";
    }

    // Object information for console output
    ////////////////////////////////////////

    public static String strToStringHeader(){
        return "I/F TYPE    N/U IN UP TS  IC  EB EE ER EN EF  LV VT LU LT";
    }
    public String toString(){
        StringBuilder out;
        out = new StringBuilder(getLoopFinitude() + " ");
        out.append(m_loopCommand);
        while (out.length() < 12) { out.append(" "); }
        if (m_unrollMode == ELoopUnrollTypes.ATTEMPT_PRINT_LOOP_VAR){
            out.append("UU+ ");
        }
        else if (m_unrollMode == ELoopUnrollTypes.ATTEMPT_DO_NOT_PRINT_LOOP_VAR) {
            out.append("UU- ");
        }
        else {
            out.append("--  ");
        }
        out.append(cBooleanToChar(getLoopExpressions().bInitAvailable())).append("  ");
        out.append(cBooleanToChar(getLoopExpressions().bUpdateAvailable())).append("  ");
        out.append(cBooleanToChar(getLoopExpressions().bTestAvailable())).append("   ");

        out.append(cBooleanToChar(m_bILC_UseContinue)).append("  ");

        out.append(cBooleanToChar(m_bELC_UseBreak)).append("  ");
        out.append(cBooleanToChar(m_bELC_UseExit)).append("  ");
        out.append(cBooleanToChar(m_bELC_UseReturn)).append("  ");
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