package nl.ou.debm.common.feature1;

import nl.ou.debm.common.Misc;
import nl.ou.debm.producer.*;

import java.util.ArrayList;
import java.util.List;

import static nl.ou.debm.common.Misc.cBooleanToChar;

public class ControlFlowProducer implements IFeature, IStatementGenerator  {

    // attributes
    // ----------

    // what work has been done jet?
    private int m_iNForLoops = 0;               // count number of for loops introduced
    private int m_iDeepestNestingLevel = 0;     // deepest number of nested for loops

    final CGenerator generator;                 // see note in IFeature.java

    // different loop types
    private enum EBasicLoopTypes{
        TIL_DO, TIL_WHILE, TIL_FOR_NONE, TIL_FOR_INIT, TIL_FOR_UPDATE, TIL_FOR_INIT_PLUS_UPDATE,
        PFL_DO, PFL_WHILE, PFL_FOR
    }
    private enum ELoopCategory{
        TIL, PFL
    }
    static private class LoopInfo {
        // loop root type
        public ELoopCategory loopCategory;
        // loop definition statement
        public EBasicLoopTypes basicLoopType;
        // internal loop control
        public boolean bILC_UseContinue = false;
        public boolean bILC_UseGotoBegin = false;
        public boolean bILC_UseGotoEnd = false;
        public boolean bILC_UseGotoAnywhere = false;
        // external loop control
        public boolean bELC_UseBreak = false;
        public boolean bELC_UseExit = false;
        public boolean bELC_UseReturn = false;
        public boolean bELC_UseGotoDirectlyAfterThisLoop = false;
        public boolean bELC_UseGotoFurtherFromThisLoop = false;
        public boolean bELC_BreakOutNestedLoops = false;
        // number of implementations
        public int iNumberOfImplementations = 0;

        public String toString(){
            StringBuilder out;
            out = new StringBuilder(loopCategory + " ");
            out.append(basicLoopType);
            while (out.length() < 31) { out.append(" "); }
            out.append("I/C=").append(cBooleanToChar(bILC_UseContinue));
            out.append(", I/A=").append(cBooleanToChar(bILC_UseGotoAnywhere));
            out.append(", I/B=").append(cBooleanToChar(bILC_UseGotoBegin));
            out.append(", I/E=").append(cBooleanToChar(bILC_UseGotoEnd));
            return out.toString();
        }

        // default constructor
        public LoopInfo(){
            // empty, but must be available...
        }

        // copy constructor (boring...)
        public LoopInfo(LoopInfo rhs){
            basicLoopType = rhs.basicLoopType;
            loopCategory = rhs.loopCategory;
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
            iNumberOfImplementations = rhs.iNumberOfImplementations;
        }

    }
    // basic edition of all possible loops
    private static ArrayList<LoopInfo> loop_repository = new ArrayList<>();
    // object copy of all possible loops
    private final ArrayList<LoopInfo> my_loop_repo = new ArrayList<>();


    // construction
    public ControlFlowProducer(CGenerator generator){
        // set pointer to generator
        this.generator = generator;
        // setup all sorts of loops
        InitStaticLoopRepo();
        // make deep copy of the static loop repo
        CopyStaticLoopRepo();
    }

    private void CopyStaticLoopRepo(){
        // make deep copy of all possible loops
        my_loop_repo.clear();
        for (var li : loop_repository){
            my_loop_repo.add(new LoopInfo(li));
        }
    }

    private static void InitStaticLoopRepo(){
        // only do something when necessary
        if (!loop_repository.isEmpty()){
            // loop repo has already been initialized
            return;
        }

        //////////////////////////////////////////////
        // add all basic loop types to loop repository
        //////////////////////////////////////////////
        for (var lp : EBasicLoopTypes.values()){
            // new loop info object
            var li = new LoopInfo();
            // set basic loop type (for / do / while, differ between infinite or finite definitions)
            li.basicLoopType = lp;
            // any loop is by default probably finite, even the TIL's
            // that is because any TIL is added twice, as a true TIL (see switch below)
            // and a PFL, because a TIL definition can contain a LES and become a PFL
            li.loopCategory = ELoopCategory.PFL;
            loop_repository.add(li);
            // make sure all TIL's are also added as TILL
            switch (lp){
                case PFL_DO, PFL_FOR, PFL_WHILE -> {
                    // less PFL-cases, so easier to write ;-)
                }
                default -> {
                    // add extra edition for TIL-loops
                    var li2 = new LoopInfo(li);
                    li2.loopCategory = ELoopCategory.TIL;
                    loop_repository.add(li2);
                }
            }
        }

        /////////////////////////////////
        // add all internal control flows
        /////////////////////////////////

        // store current repo
        var repo2 = loop_repository;

        // build new repo: for each loop, add any combination of internal control flow settings
        loop_repository = new ArrayList<>();
        for (var src : repo2) {
            for (int i=0 ; i<16 ; ++i){
                var dest = new LoopInfo(src);
                dest.bILC_UseContinue     = ((i & 1)!=0);
                dest.bILC_UseGotoAnywhere = ((i & 2)!=0);
                dest.bILC_UseGotoBegin    = ((i & 4)!=0);
                dest.bILC_UseGotoEnd      = ((i & 8)!=0);
                loop_repository.add(dest);
            }
        }

        /////////////////////////////////
        // add all external control flows
        /////////////////////////////////

        // store current repo (throwing away the current backup)
        repo2 = loop_repository;

        // build new repo: for each loop, add any combination of internal control flow settings
        loop_repository = new ArrayList<>();
        for (var src : repo2) {
            int min, max;
            if (src.loopCategory==ELoopCategory.TIL){
                // truly infinite loop, so no LES's
                min = 0; max = 0;
            }
            else if ((src.basicLoopType == EBasicLoopTypes.PFL_FOR) ||
                     (src.basicLoopType == EBasicLoopTypes.PFL_DO) ||
                     (src.basicLoopType == EBasicLoopTypes.PFL_WHILE) ){
                // PFL loop, so LES's are optional
                min = 0; max = 64;
            }
            else{
                // TIL loop statement, but PFL wanted, so LES required
                min = 1; max = 64;
            }
            for (int i=min ; i<max ; ++i){
                var dest = new LoopInfo(src);
                dest.bELC_UseBreak                     = ((i &  1)!=0);
                dest.bELC_UseExit                      = ((i &  2)!=0);
                dest.bELC_UseReturn                    = ((i &  4)!=0);
                dest.bELC_UseGotoDirectlyAfterThisLoop = ((i &  8)!=0);
                dest.bELC_BreakOutNestedLoops          = ((i & 16)!=0);
                dest.bELC_UseGotoFurtherFromThisLoop   = ((i & 32)!=0);
                loop_repository.add(dest);
            }
        }

    }


    public void ShowMeLoops(){
        System.out.println("# loops: " + my_loop_repo.size());
        int cnt = 0;
        for (var li : my_loop_repo){
            System.out.print(Misc.strGetNumberWithPrefixZeros(cnt++,4) + ": ");
            System.out.println(li);
        }
    }





    @Override
    public boolean isSatisfied() {
        return (m_iNForLoops >= 10) ;
    }

    @Override
    public String getPrefix() {
        return EFeaturePrefix.CONTROLFLOWFEATURE.toString();
    }

    @Override
    public List<String> getIncludes() {
        return new ArrayList<>(){
            { add("<stdio.h>");
              add("<stdbool.h>"); }
        };
    }

    @Override
    public List<String> getNewStatements() {
        return getNewStatements(null);
    }

    @Override
    public List<String> getNewStatements(StatementPrefs prefs) {
        // check prefs object
        if (prefs == null){
            prefs = new StatementPrefs(null);
        }

        // create list
        var list = new ArrayList<String>();

        // can we oblige?
        if (prefs.loop == EStatementPref.NOT_WANTED){
            // no, as we only produce loops
            return list;
        }
        if (prefs.numberOfStatements == ENumberOfStatementsPref.SINGLE){
            // no, as we only produce multiple statements
            return list;
        }
        if (prefs.expression == EStatementPref.REQUIRED){
            // no, as we do not do expressions
            return list;
        }
        if (prefs.assignment == EStatementPref.REQUIRED){
            // no, as we do not do assignments
            return list;
        }
        if (prefs.compoundStatement == EStatementPref.NOT_WANTED){
            // no, as we use a compound statement as loop body
            return list;
        }

        // we have now asserted that:
        // - loops, multiple statements and compound statements are  allowed or required
        // - expressions and assignments are not required


        // still a stub but make something of it
        var forloop = new ForLoop("int c=0", "c<10", "++c");

        list.add("printf(\"" + forloop.toCodeMarker() + "\");");
        list.add(forloop.strGetForStatement(true));
        list.add("   printf(\"loopVar = %d;\", c);");
        list.add("}");

        m_iNForLoops++;
        return list;
    }
}
