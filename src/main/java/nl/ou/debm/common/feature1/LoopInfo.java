package nl.ou.debm.common.feature1;

import java.util.ArrayList;
import java.util.List;

import static nl.ou.debm.common.Misc.cBooleanToChar;

public class LoopInfo {
    // loop root type
    public ELoopCommands loopCommand;               // do/for/while
    public ELoopFinitude loopFinitude;              // TrulyInfiniteLoop / Probably Finite Loop
    public ELoopExpressions loopExpressions;        // init/update/test available?
    // internal loop control
    public boolean bILC_UseContinue = false;        // put continue statement in loop
    public boolean bILC_UseGotoBegin = false;       // put goto begin in loop
    public boolean bILC_UseGotoEnd = false;         // put goto begin in loop
    public boolean bILC_UseGotoAnywhere = false;    // go anywhere in loop
    // external loop control
    public boolean bELC_UseBreak = false;           // put break statement in loop
    public boolean bELC_UseExit = false;            // put exit call in loop
    public boolean bELC_UseReturn = false;          // put return statement in loop
    public boolean bELC_UseGotoDirectlyAfterThisLoop = false;   // put goto next-statement-after-loop in loop
    public boolean bELC_UseGotoFurtherFromThisLoop = false;     // goto somewhere further than immediately after loop
    public boolean bELC_BreakOutNestedLoops = false;            // break out nested loops
    // loop variable
    public LoopVariable loopVar = new LoopVariable();           // loop variable details
    // number of implementations
    public int iNumberOfImplementations = 0;

    
    private static List<LoopInfo> loopRepo = new ArrayList<>();

    // default constructor
    public LoopInfo(){
        // empty, but must be available...
    }

    public LoopInfo(ELoopFinitude loopFinitude, ELoopCommands loopCommand, ELoopExpressions loopExpressions){
        this.loopFinitude = loopFinitude;
        this.loopCommand = loopCommand;
        this.loopExpressions = loopExpressions;
    }

    // copy constructor (boring...)
    public LoopInfo(LoopInfo rhs){
        loopCommand = rhs.loopCommand;
        loopFinitude = rhs.loopFinitude;
        loopExpressions = rhs.loopExpressions;
        bILC_UseContinue = rhs.bILC_UseContinue;
        bILC_UseGotoBegin = rhs.bILC_UseGotoBegin;
        bILC_UseGotoEnd = rhs.bILC_UseGotoEnd;
        bILC_UseGotoAnywhere = rhs.bILC_UseGotoAnywhere;
        bELC_UseBreak = rhs.bELC_UseBreak;
        bELC_UseExit = rhs.bELC_UseExit;
        bELC_UseReturn = rhs.bELC_UseReturn;
        bELC_UseGotoDirectlyAfterThisLoop = rhs.bELC_UseGotoDirectlyAfterThisLoop;
        bELC_UseGotoFurtherFromThisLoop = rhs.bELC_UseGotoFurtherFromThisLoop;
        bELC_BreakOutNestedLoops = rhs.bELC_BreakOutNestedLoops;
        loopVar = new LoopVariable(rhs.loopVar);
        iNumberOfImplementations = rhs.iNumberOfImplementations;
    }

    public static void FillLoopRepo(List<LoopInfo> destRepo){
        // init repository
        InitLoopRepo();
        // make deep copy
        destRepo.clear();
        for (var li : loopRepo){
            destRepo.add(new LoopInfo(li));
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
    }

    private static void AddInternalControlFlows() {
        // store reference to current repo
        var repo2 = loopRepo;

        // build new repo: for each loop, add any combination of internal control flow settings
        loopRepo = new ArrayList<>();
        for (var src : repo2) {                 // loop through every item set so far
            for (int i = 0; i < 16; ++i) {         // 4 options T/F = 2^4 = 16 combinations
                var dest = new LoopInfo(src);   // true copy (not just a reference)
                dest.bILC_UseContinue = ((i & 1) != 0);
                dest.bILC_UseGotoAnywhere = ((i & 2) != 0);
                dest.bILC_UseGotoBegin = ((i & 4) != 0);
                dest.bILC_UseGotoEnd = ((i & 8) != 0);
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
        }
    }


    public static String strToStringHeader(){
        return "I/F TYPE  IN UP TS  IC IA IB IE  EB EE ER ED EN EF  LV";
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
        out.append(cBooleanToChar(bILC_UseGotoAnywhere)).append("  ");
        out.append(cBooleanToChar(bILC_UseGotoBegin)).append("  ");
        out.append(cBooleanToChar(bILC_UseGotoEnd)).append("   ");

        out.append(cBooleanToChar(bELC_UseBreak)).append("  ");
        out.append(cBooleanToChar(bELC_UseExit)).append("  ");
        out.append(cBooleanToChar(bELC_UseReturn)).append("  ");
        out.append(cBooleanToChar(bELC_UseGotoDirectlyAfterThisLoop)).append("  ");
        out.append(cBooleanToChar(bELC_BreakOutNestedLoops)).append("  ");
        out.append(cBooleanToChar(bELC_UseGotoFurtherFromThisLoop)).append("   ");

        out.append(cBooleanToChar(loopVar.bUseLoopVariable)).append("  ");
        return out.toString();
    }
}