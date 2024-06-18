package nl.ou.debm.common.feature5;

import nl.ou.debm.assessor.CountTestResult;
import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.assessor.SchoolTestResult;
import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.F15BaseCListener;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static nl.ou.debm.common.Misc.dblSafeDiv;
import static nl.ou.debm.common.feature5.IndirectionsProducer.ICASEINDEXFORDEFAULTBRANCH;
import static nl.ou.debm.common.feature5.IndirectionsProducer.ICASEINDEXFORNOTSETYET;

public class IndirectionCListener extends F15BaseCListener {

    //////////////////
    // class constants
    //////////////////
    /** this switch's ID had not been determined yet */         private final static int ISWITCHIDNOTIDENTIFIEDYET = -1;
    /** this switch contains branches from multiple sources */  private final static int ISWITCHIDNOTCONSISTENT = -2;

    /////////////////
    // helper classes
    /////////////////


    private static class SwitchQualityScore{
        public double dblA_switchPresentInBinary = 0.0;         // TODO NIY
        public double dblB_correctNumberOfCases = 0.0;          // TODO NIY
        public double dblC_caseIDCorrectness = 0.0;             // TODO NIY
        public double dblD_defaultBranchCorrectness = 0.0;      // TODO NIY
        public double dblE_caseStartPointCorrectness = 0.0;     // TODO NIY
        public double dblF_noCaseDuplications = 0.0;            // TODO NIY
        public double dblG_noGotos = 0.0;                       // TODO NIY
        public double dblTotalScore(){
            return dblA_switchPresentInBinary == 0.0 ? 0.0 :
                           (dblA_switchPresentInBinary +
                            dblB_correctNumberOfCases +
                            dblC_caseIDCorrectness +
                            dblD_defaultBranchCorrectness +
                            dblE_caseStartPointCorrectness +
                            dblF_noCaseDuplications +
                            dblG_noGotos);
        }
        public String toString() {
            return "t:" + dblTotalScore() +
                    "|A:" + dblA_switchPresentInBinary +
                    "|B:" + dblB_correctNumberOfCases +
                    "|C:" + dblC_caseIDCorrectness +
                    "|D:" + dblD_defaultBranchCorrectness +
                    "|E:" + dblE_caseStartPointCorrectness +
                    "|F:" + dblF_noCaseDuplications +
                    "|G:" + dblG_noGotos;
        }
    }

    /**
     * This private class keeps running information on the selection level of the C-code.
     * The level is increased every time a selection statement (if, switch) is entered
     * and decreased when exited.
     * It's used as a struct in a stack.
     */
    private static class SelectionLevelInfo{
        /** true if this level a switch body (rather than an if/else body) */   public boolean bSwitchBody = false;
        /** may only be valid for switches; switch ID */                        public long lngSwitchID = ISWITCHIDNOTIDENTIFIEDYET;
        /** the expression to enter yhe body; if (boolean) or switch (int) */   public CParser.ExpressionContext expression = null;
        /** all the branches found in a switch body */                          public final List<FoundCaseInfo> fci_list = new ArrayList<>();
        /** the case info for the current branch */                             public FoundCaseInfo current_fci = null;
        @Override
        public String toString(){
            return "SW=" + bSwitchBody + ";SE=" + (expression == null ? "null" : expression.getText())  + ";FCI=" + fci_list;
        }
    }

    /**
     * This struct class collects information about switch branches in the C-code.
     */
    private static class FoundCaseInfo implements Comparable<FoundCaseInfo>{
        /** the case ID for this case*/                         public long lngCaseIDInCode;
        /** the case begin code marker */                       public IndirectionsCodeMarker caseBeginCM = null;
        /** true if the case was a first degree child case */   public boolean bFirstDegreeChild = true;
        /** true if the case is reachable without disturbance*/ public boolean bPathToCaseOk = true;
        /** true if the case is empty */                        public boolean bEmptyBranch = true;
        /** next switch ID in code, valid when branch is empty*/public long lngNextCaseIDInCode = ICASEINDEXFORNOTSETYET;
        /** if valid, only contains a jump to this label (no:)*/public String strContainsOnlyThisJump = null;
        @Override
        public String toString(){
            return "CID=" + lngCaseIDInCode + ";E=" + bEmptyBranch + ";NXID=" + lngNextCaseIDInCode +
                    ";POK=" + bPathToCaseOk + ";FDC=" + bFirstDegreeChild +
                    (strContainsOnlyThisJump!=null ? ";JM=" + strContainsOnlyThisJump : "") +
                    ";CM=" + ((caseBeginCM == null) ? "null" : caseBeginCM.strDebugOutput());
        }

        @Override
        public int compareTo(@NotNull IndirectionCListener.FoundCaseInfo o) {
            int out = (int)Math.signum(this.lngCaseIDInCode - o.lngCaseIDInCode);
            if (out == 0) {
                if ((this.caseBeginCM!=null) && (o.caseBeginCM!=null)){
                    out = (int)Math.signum(this.caseBeginCM.lngGetID() - o.caseBeginCM.lngGetID());
                }
                else if (this.caseBeginCM!=null){
                    out = -1;
                }
                else if (o.caseBeginCM!=null){
                    out = 1;
                }
            }
            return out;
        }
    }

    /**
     * This struct class collects info on switches found in the C-code
     * It also provides specific support for translating branch ID's found in the C
     * code, to those found in the LLVM.
     */
    private static class FoundSwitchInfo{
        /** switch iD */                                        public long lngSwitchID = ISWITCHIDNOTIDENTIFIEDYET;
        /** all cases */                                        public List<FoundCaseInfo> fci = null;
        /** translate: code -> org, org = a x code + b */       public long a = 1;
        /** translate: code -> org, org = a x code + b */       public long b = 0;
        public String toString(){
            return "FSI:ID=" + lngSwitchID + ";CI=" + fci + ";a=" + a + ";b=" + b;
        }

        /**
         * Compare the case indices found in case info to the case indices in the code markers
         * and try to work out the translation factors
         */
        public void calculateTranslationFactors(List<SwitchInfo.LLVMCaseInfo> llvmCI){
            // anything to do?
            if (fci==null){
                return;
            }
            if (llvmCI.isEmpty()){
                return;
            }

            // make a sorted list of LLVM-indices
            List<Long> sortedLLVMIDs = new ArrayList<>(llvmCI.size());
            for (var q : llvmCI){
                if (q.m_lngBranchValue!=ICASEINDEXFORDEFAULTBRANCH) {
                    sortedLLVMIDs.add(q.m_lngBranchValue);
                }
            }
            Collections.sort(sortedLLVMIDs);
            // make a sorted list of C-indices
            List<Long> sortedCIDs = new ArrayList<>(fci.size());
            for (var q : fci){
                sortedCIDs.add(q.lngCaseIDInCode);
            }
            Collections.sort(sortedCIDs);

            // check sizes
            if (llvmCI.size()<fci.size()){
                // alright... something seriously strange is going on,
                // there are more branches in the decompiler output than in
                // the LLVM-code...
                // we return true, so the calling routing thinks we've done something useful
                // and reset a- and b-factors.
                this.a=1; this.b=0;
                return;
            }

            // try to find a simple translation,
            // 1x + b ---> find b factor
            if (bSimpleBTranslationFound(sortedLLVMIDs, sortedCIDs)){
                return;
            }
            // try to find a more challenging translation
            bFactorABTranslationFound(sortedLLVMIDs, sortedCIDs);
        }

        private boolean bSimpleBTranslationFound(List<Long> sortedLLVMIDs, List<Long> sortedCIDs) {
            return bSimpleBTranslationFound(sortedLLVMIDs, sortedCIDs, 1);
        }

        private boolean bFactorABTranslationFound(List<Long> sortedLLVMIDs, List<Long> sortedCIDs){
            // suppose you have two lists:
            // LLVM 11 16 21 31 41 51 56 61
            // C     1  2  6  8
            // now, you can align the first element in (8-4)+1 = 5 positions
            // so, we have 5 possible offsets: n-10, n-15, n-20, n-30, n-40
            // n is NOT 1. n is 1 x [lowest difference between two numbers in the LLVW]
            //
            long lngLowestDifferenceInLLVM = sortedLLVMIDs.get(sortedLLVMIDs.size()-1) - sortedLLVMIDs.get(0);
            for (int i=0 ; i<sortedLLVMIDs.size()-1 ; i++){
                long diff = sortedLLVMIDs.get(i+1) - sortedLLVMIDs.get(i);
                if (lngLowestDifferenceInLLVM>diff){
                    lngLowestDifferenceInLLVM=diff;
                }
            }
            if (lngLowestDifferenceInLLVM==1){
                // we've already done 1, so leave it...
                this.a = 1;
                this.b = 0;
                return true;
            }

            return bSimpleBTranslationFound(sortedLLVMIDs, sortedCIDs, lngLowestDifferenceInLLVM);
        }

        /**
         * Do the hard word of trying to find a- and b-factors for translating branch ID's
         * @param sortedLLVMIDs list of branch ID's in the LLVM
         * @param sortedCIDs list of brand ID's in the decompiler output
         * @param lngFactor possible a-value
         * @return true when successful
         */
        private boolean bSimpleBTranslationFound(List<Long> sortedLLVMIDs, List<Long> sortedCIDs, long lngFactor){
            for (int iFirstOffset = 0; iFirstOffset < (sortedLLVMIDs.size() - sortedCIDs.size() + 1); iFirstOffset++){
                // calculate assumed offset
                long lngOffset = (sortedCIDs.get(0) * lngFactor) - sortedLLVMIDs.get(iFirstOffset);
                // try to match all C-elements to the LLVM-elements
                int iCPtr;
                int iLPtr=0;
                boolean bAllFound = true;
                // loop over C-elements
                for (iCPtr = 1; iCPtr < sortedCIDs.size() ; iCPtr++){
                    // increase L-ptr, because it is still at the last match
                    iLPtr++;
                    // value to find
                    long lngCElement = sortedCIDs.get(iCPtr) * lngFactor;
                    // find match
                    boolean bMatch = false;
                    while (iLPtr<sortedLLVMIDs.size()) {
                        if ((lngCElement - lngOffset) == sortedLLVMIDs.get(iLPtr)){
                            // match found!
                            bMatch = true;
                            break;
                        }
                        iLPtr++;
                    }
                    if (!bMatch){
                        bAllFound = false;
                        break;
                    }
                    // match found, so try next one
                }
                if (bAllFound){
                    this.a = lngFactor;
                    this.b = -lngOffset;
                    return true;
                }
            }
            return false;
        }
    }

    ////////////////////
    // object attributes
    ////////////////////
    // info for or from the outside world
    /** info on all switches found in the LLVM, key=switch ID*/                     private final Map<Long, SwitchInfo> m_LLVMSwitchInfo;
    /** indirection score for jump table switches */                                private final CountTestResult m_indirectionRawJumpTable = new CountTestResult();
    /** indirection score for calculation switches */                               private final CountTestResult m_indirectionRawCalculation = new CountTestResult();
    /** overall switch quality */                                                   private final SchoolTestResult m_switchQualityAllSwitches = new SchoolTestResult();
    /** switch quality; no indirections only */                                     private final SchoolTestResult m_switchQualityNoIndirection = new SchoolTestResult();
    /** switch quality; indirections only */                                        private final SchoolTestResult m_switchQualityIndirectionOnly = new SchoolTestResult();
    /** all metrics in one array (easier access) */                                 private final List<IAssessor.TestResult> m_allTestResults = new ArrayList<>();
    /** input reference */                                                          private final IAssessor.CodeInfo m_ci;

    // info for the listening process
    /** keep track of the compound level following a label */                       private int m_iLabelCompoundLevel = 0;
    /** current label before a code block */                                        private String m_strCurrentLabel = null;
    /** key = label, value = begin-case-code-marker in that labeled block */        private final Map<String, IndirectionsCodeMarker> m_mapLabelToICM = new HashMap<>();
    /** info per selection level */                                                 private final Stack<SelectionLevelInfo> m_sli = new Stack<>();
    /** info per switch (only our switches), mapped by switch ID */                 private final Map<Long, FoundSwitchInfo> m_fsi = new HashMap<>();
    /** set of all the switch/branch ID's */                                        private final Set<String> m_branchIDIDs = new TreeSet<>();
    /** key = switch ID, value = quality score */                                   private final Map<Long, SwitchQualityScore> m_SQS = new HashMap<>();
    /** set of all switchID's found in all switch code markers */                   private final Set<Long> m_switchIDSet = new TreeSet<>();

    ///////////////
    // construction
    ///////////////
    public IndirectionCListener(IAssessor.CodeInfo ci, Map<Long, SwitchInfo> llvmSwitchInfo){
        // call super constructor; the f15-class takes care of a general stuff for finding code markers
        super();

        // keep parameters
        m_LLVMSwitchInfo = llvmSwitchInfo;
        m_ci = ci;

        // setup test result classes
        m_indirectionRawCalculation.setTargetMode(CountTestResult.ETargetMode.HIGHBOUND);
        m_indirectionRawCalculation.setWhichTest(ETestCategories.FEATURE5_RAW_INDIRECTIONSCORE_CALCULATION);
        m_indirectionRawJumpTable.setTargetMode(CountTestResult.ETargetMode.HIGHBOUND);
        m_indirectionRawJumpTable.setWhichTest(ETestCategories.FEATURE5_RAW_INDIRECTIONSCORE_JUMPTABLE);
        m_switchQualityAllSwitches.setWhichTest(ETestCategories.FEATURE5_SWITCH_QUALITY_GENERAL);
        m_switchQualityNoIndirection.setWhichTest(ETestCategories.FEATURE5_SWITCH_QUALITY_NO_INDIRECTION_ONLY);
        m_switchQualityIndirectionOnly.setWhichTest(ETestCategories.FEATURE5_SWITCH_QUALITY_INDIRECTION_ONLY);

        // add all classes to the list
        m_allTestResults.add(m_indirectionRawJumpTable);
        m_allTestResults.add(m_indirectionRawCalculation);
        m_allTestResults.add(m_switchQualityAllSwitches);
        m_allTestResults.add(m_switchQualityNoIndirection);
        m_allTestResults.add(m_switchQualityIndirectionOnly);

        // set all compiler configs
        for (var tr: m_allTestResults) {
            tr.setCompilerConfig(ci.compilerConfig);
        }

        // setup SQS-map
        for (var switchID : m_LLVMSwitchInfo.keySet()){
            m_SQS.put(switchID, new SwitchQualityScore());
        }
    }

    ///////////////////////////////////////
    // communication with the outside world
    ///////////////////////////////////////

    /**
     * After the walk, this routine hands back the results
     * @return the results from the analysis
     */
    public List<IAssessor.TestResult> getTestResults(){
        final List<IAssessor.TestResult> out = new ArrayList<>();
        for (var tr: m_allTestResults) {
            // add only those test result classes that truly returned something
            if (tr instanceof CountTestResult ctr) {
                if (ctr.dblGetHighBound() > 0) {
                    out.add(tr);
                }
            }
            else if (tr instanceof SchoolTestResult str){
                if (str.iGetNumberOfTests() > 0) {
                    out.add(tr);
                }
            }
            else if (tr!=null){
                throw new RuntimeException("Implementation error in test returning");
            }
        }


//        int ni=0,nj=0,nc=0,nd=0;
//        for (var q : m_LLVMSwitchInfo.values()){
//            if (q.getImplementationType()== SwitchInfo.SwitchImplementationType.IF_STATEMENTS){
//                ni++;
//            }
//            else if (q.getImplementationType() == SwitchInfo.SwitchImplementationType.JUMP_TABLE){
//                nj++;
//            }
//            else if (q.getImplementationType() == SwitchInfo.SwitchImplementationType.DIRECT_CALCULATED_JUMP){
//                nc++;
//            }
//            else {
//                nd++;
//            }
//        }
//        pr(m_ci.strDecompiledCFilename + " --- ni=" + ni + ", nj=" + nj + ", nc=" + nc + ", nd=" +nd);

        return out;
    }

    ///////////////////////////////////
    // making up metrics after the walk
    ///////////////////////////////////

    @Override
    public void exitCompilationUnit(CParser.CompilationUnitContext ctx) {
        super.exitCompilationUnit(ctx);

        // hard work is done, do the post-processing

        // first, try to work out translation factors
        for (var fsi : m_fsi.values()) {
            var lswi = m_LLVMSwitchInfo.get(fsi.lngSwitchID);
            if (lswi!=null) {
                fsi.calculateTranslationFactors(lswi.LLVMCaseInfo());
            }
        }

        // raw(est) scores
        processRawScores(m_indirectionRawJumpTable,   SwitchInfo.SwitchImplementationType.JUMP_TABLE);
        processRawScores(m_indirectionRawCalculation, SwitchInfo.SwitchImplementationType.DIRECT_CALCULATED_JUMP);

        // quality scores
        calculateQualityScores();
    }

    private void processRawScores(CountTestResult ctr, SwitchInfo.SwitchImplementationType whichIndirection){
        // loop over all switches in LLVM
        for (var llvmInfo : m_LLVMSwitchInfo.values()){
            // select only those with appropriate type
            if (llvmInfo.getImplementationType() == whichIndirection) {
                // get switch ID
                long lngSwitchID = llvmInfo.lngGetSwitchID();
                // loop over all branches in the LLVM switches
                for (var branch : llvmInfo.LLVMCaseInfo()) {
                    if (branch.m_lngBranchValue != ICASEINDEXFORDEFAULTBRANCH) {
                        // increase possible number of cases in the metric
                        ctr.increaseHighBound();
                        String strFindCode = IndirectionsCodeMarker.strGetValueForTreeSet(lngSwitchID, branch.m_lngBranchValue);
                        if (m_branchIDIDs.contains(strFindCode)) {
                            // found the easy way, done
                            ctr.increaseActualValue(); // increase the number of actually found cases
                        }
                        else {
                            // not found the easy way, so we need to try it the hard way
                            processSibling(lngSwitchID, branch, ctr);
                        }
                    }
                }
            }
        }
    }

    private void processSibling(long lngSwitchID, SwitchInfo.LLVMCaseInfo branch, CountTestResult ctr){
        // test sibling branches
        for (var sibling : branch.m_otherBranchValues){
            String strFindCode = IndirectionsCodeMarker.strGetValueForTreeSet(lngSwitchID, sibling);
            if (m_branchIDIDs.contains(strFindCode)) {
                // good, so now we found the sibling branch that does have a code marker
                // and this code marker is in the code. We now only need to make sure
                // that the code marker is reachable for this specific case
                //
                // we therefore loop over the cases found in the code, as case, and try
                // to determine that this specific case leads to a non-empty case with the
                // correct code marker
                var switchInfoInCode = m_fsi.get(lngSwitchID);
                if (switchInfoInCode!=null) {

                    // look for this case in the list of cases
                    FoundCaseInfo caseInfoInCode = null;
                    for (var cii : switchInfoInCode.fci){
                        if (cii.lngCaseIDInCode == branch.m_lngBranchValue){
                            caseInfoInCode=cii;
                            break;
                        }
                    }
                    if (caseInfoInCode==null){
                        // not found, try a second way
                        for (var cii : switchInfoInCode.fci){
                            long lngTranslatedCaseID = (cii.lngCaseIDInCode * switchInfoInCode.a) + switchInfoInCode.b;
                            if (lngTranslatedCaseID == branch.m_lngBranchValue){
                                caseInfoInCode=cii;
                                break;
                            }
                        }
                    }
                    // when found...
                    if (caseInfoInCode!=null){
                        // check if empty. if so, find next case. Repeat.
                        while (caseInfoInCode.bEmptyBranch){
                            // valid next branch?
                            if (caseInfoInCode.lngNextCaseIDInCode==ICASEINDEXFORNOTSETYET){
                                break;
                            }
                            // look for next branch's ID
                            caseInfoInCode = getNextBranchsFoundCaseInfo(switchInfoInCode, caseInfoInCode);
                        }
                        // check code marker
                        if (caseInfoInCode.caseBeginCM!=null){
                            if (m_branchIDIDs.contains(caseInfoInCode.caseBeginCM.strGetValueForTreeSet())){
                                ctr.increaseActualValue(); // increase the number of actually found cases
                            }
                        }
                    }
                }
                break;
            }
        }

    }

    private static @NotNull FoundCaseInfo getNextBranchsFoundCaseInfo(FoundSwitchInfo switchInfoInCode, FoundCaseInfo caseInfoInCode) {
        FoundCaseInfo nbi = null;
        for (var q : switchInfoInCode.fci){
            if (q.lngCaseIDInCode == caseInfoInCode.lngNextCaseIDInCode){
                nbi = q;
                break;
            }
        }
        // null should never be the result
        if (nbi==null){
            throw new RuntimeException("link error in empty cases chain");
        }
        return nbi;
    }

    private void calculateQualityScores(){
        switchQSA();

        double dblSumAll = 0, dblSumIndirection = 0, dblSumNoIndirection = 0;
        long lngNAll = 0, lngNIndirection = 0, lngNNoIndirection = 0;
        for (var sqs : m_SQS.entrySet()){
            long lngSwitchID = sqs.getKey();
            double score = sqs.getValue().dblTotalScore();
            // general count
            dblSumAll+=score;
            lngNAll++;
            // count per switch type
            var tpe = m_LLVMSwitchInfo.get(lngSwitchID).getImplementationType();
            if ((tpe == SwitchInfo.SwitchImplementationType.DIRECT_CALCULATED_JUMP) ||
                    (tpe == SwitchInfo.SwitchImplementationType.JUMP_TABLE)){
                dblSumIndirection+=score;
                lngNIndirection++;
            }
            else {
                dblSumNoIndirection+=score;
                lngNNoIndirection++;
            }
        }

        // put data in appropriate test classes
        setSingleQualityScore(m_switchQualityAllSwitches,     dblSumAll,           lngNAll);
        setSingleQualityScore(m_switchQualityNoIndirection,   dblSumNoIndirection, lngNNoIndirection);
        setSingleQualityScore(m_switchQualityIndirectionOnly, dblSumIndirection,   lngNIndirection);
    }

    private void setSingleQualityScore(SchoolTestResult str, double dblSum, long lngCount){
        if (lngCount==0){
            // if no tests are present, remove this test from the set
            m_allTestResults.remove(str);
        }
        else {
            // otherwise: set score
            str.setScore(dblSafeDiv(dblSum, (double) lngCount));
        }
    }

    private void switchQSA(){
        // find code markers

        // loop over all switches in the LLVM
        for (var llvm : m_LLVMSwitchInfo.values()){
            // and score whenever it's ID was found in at least 1 code marker
            if (m_switchIDSet.contains(llvm.lngGetSwitchID())){
                m_SQS.get(llvm.lngGetSwitchID()).dblA_switchPresentInBinary = 1;
            }
        }
    }

    ////////////////////////////////////////
    // Basic methods called during the walk:
    // enter 6 different statements
    // (labeled, compound, selection, jump
    // expression, iteration)
    //
    // followed by some exit methods
    ////////////////////////////////////////

    @Override
    public void enterLabeledStatement(CParser.LabeledStatementContext ctx) {
        super.enterLabeledStatement(ctx);

        // only cases and defaults
        if (ctx.Case() != null) {
            ProcessCaseStatement(ctx);
        }
        if (ctx.Default() != null) {
            //
        }
        if (ctx.Identifier()!=null){
            ProcessLabeledStatement(ctx);
        }
    }

    private void ProcessLabeledStatement(CParser.LabeledStatementContext ctx){
        // labeled statement
        //

        // remember label
        m_strCurrentLabel = ctx.Identifier().getText();
        m_iLabelCompoundLevel = 0;
    }

    private void ProcessCaseStatement(CParser.LabeledStatementContext ctx) {
        // case-statement
        // --------------
        //
        // any current statement object is already added to the list, so we don't have to do that now
        // we can simply create a new one (later this function)

        // case must always be in a switch compound statement...
        assert m_sli.peek().bSwitchBody : "case found outside switch body";

        // keep current case
        var oldCaseInfo = m_sli.peek().current_fci;

        // determine the value of the expression
        // we expect an integer value, possibly in hex, possibly negative
        String strCaseIndex = ctx.constantExpression().getText();
        var x = new Misc.ConvertCNumeral(strCaseIndex);
        if (!x.bIsInteger()){
            // in theory, symbolic constants or constant calculations could be emitted by the decompiler,
            // but we consider the chance negligible and therefore, we do not try to work with those
            m_sli.peek().current_fci = null;
            return;
        }

        // make new current case object + set case index
        var fci = new FoundCaseInfo();
        fci.lngCaseIDInCode = x.LngGetIntegerLikeValue();

        // now we add it
        m_sli.peek().fci_list.add(fci);

        // and we keep it as current
        m_sli.peek().current_fci = fci;

        // when there is a previous empty case, link it to this one
        if (oldCaseInfo!=null){
            if (oldCaseInfo.bEmptyBranch) {
                oldCaseInfo.lngNextCaseIDInCode = fci.lngCaseIDInCode;
            }
        }
    }

    @Override
    public void enterExpressionStatement(CParser.ExpressionStatementContext ctx) {
        super.enterExpressionStatement(ctx);

        // when in a switch branch, mark this branch as not empty
        safelyMarkCurrentBranchAsNotEmpty();
        // when in a switch branch, mark this branch as not goto-only
        safelySetCurrentBranchGotoOnlyStatus(null);
    }

    private void safelyMarkCurrentBranchAsNotEmpty(){
        if (!m_sli.isEmpty()){
            var fci = m_sli.peek().current_fci;
            if (fci!=null){
                fci.bEmptyBranch = false;
            }
        }
    }

    private void safelySetCurrentBranchGotoOnlyStatus(String strJump){
        if (!m_sli.isEmpty()){
            var fci = m_sli.peek().current_fci;
            if (fci!=null){
                fci.strContainsOnlyThisJump = strJump;
            }
        }
    }

    @Override
    public void enterCompoundStatement(CParser.CompoundStatementContext ctx) {
        super.enterCompoundStatement(ctx);
        if (m_strCurrentLabel!=null) {
            m_iLabelCompoundLevel++;
        }
    }
    @Override
    public void enterSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.enterSelectionStatement(ctx);

        // when in a switch branch, mark this branch as not empty
        safelyMarkCurrentBranchAsNotEmpty();
        // when in a switch branch, mark this branch as not goto-only
        safelySetCurrentBranchGotoOnlyStatus(null);

        // add new level to stack
        var sli = new SelectionLevelInfo();
        m_sli.push(sli);
        sli.bSwitchBody=(ctx.Switch()!=null);
        sli.expression = ctx.expression();
    }

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        super.enterIterationStatement(ctx);

        // when in a switch branch, mark this branch as not empty
        safelyMarkCurrentBranchAsNotEmpty();
        // when in a switch branch, mark this branch as not goto-only
        safelySetCurrentBranchGotoOnlyStatus(null);
    }

    @Override
    public void enterJumpStatement(CParser.JumpStatementContext ctx) {
        super.enterJumpStatement(ctx);

        // if the jump statement is a goto and if the branch was
        // empty up to now, we mark it as a goto-only branch
        if (ctx.Goto()!=null) {
            // yep, goto in code
            if (!m_sli.isEmpty()) {
                var fci = m_sli.peek().current_fci;
                if (fci != null) {
                    // we are currently in a branch!
                    if (fci.bEmptyBranch) {
                        safelySetCurrentBranchGotoOnlyStatus(ctx.Identifier().getText());
                    }
                }
            }
            // no more label searching
            m_strCurrentLabel = null;
            m_iLabelCompoundLevel = 0;
        }

        // when in a switch branch, mark this branch as not empty
        safelyMarkCurrentBranchAsNotEmpty();
    }

    @Override
    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.exitSelectionStatement(ctx);

        // process stack
        var curLev = m_sli.pop();

        // done when no switch body was closed
        if (!curLev.bSwitchBody) {
            return;
        }

        // switch ID
        long lngSwitchID = curLev.lngSwitchID;
        if (lngSwitchID==-1){
            // not one of our switches
            return;
        }

        // should the cases be propagated to a higher switch?
        // --------------------------------------------------
        //
        // why would we want to do that?
        // well, we found patterns like these:
        // switch (a) {                 // <-- A
        //    case 1:
        //    case 2:
        //    default:
        //       if (a==5) {
        //       } else {
        //           switch (a) {       // <-- B
        //              case 3:
        //              case 4:
        //              default:
        //                 switch (b){  // <-- C
        //                    case 6:
        //                    case 7:
        //                 }
        //           }
        //       }
        // }
        // all these cases belong to 1 switch, but they were emitted less readable
        // we can punish the decompiler for readability, but not for not finding cases 3 and 4.
        // - cases 1 and 2 are first degree children of switch A
        // - cases 3 and 4 are first degree children of switch B
        // - cases 6 and 7 are first degree children of switch C, but they do not belong to the A/B family,
        //   as their switch expression is different
        // - cases 3 and 4 may only be propagated when
        //      - the previous switch had the same expression
        //      - the cases in the previous switch have the same switch ID
        //
        // so, we propagate cases 3 and 4 to the first switch and consider them one big switch
        boolean bMerged = false;
        boolean bPathOK = true;
        for (int lev = m_sli.size()-1 ; lev >= 0; --lev){
            var highLev = m_sli.get(lev);
            if (highLev.bSwitchBody){
                // parent switch found
                if (highLev.lngSwitchID == lngSwitchID){
                    // higher switch found with the same ID, so add these cases to the other's,
                    // but remove FirstChild flag
                    for (var ci : curLev.fci_list){
                        ci.bFirstDegreeChild=false;
                    }
                    // check switch expressions
                    if (!bExpressionsAreAlike(curLev.expression, highLev.expression)){
                        bPathOK = false;
                    }
                    // process path status
                    if (!bPathOK){
                        for (var ci : curLev.fci_list){
                            ci.bPathToCaseOk = false;
                        }
                    }
                    highLev.fci_list.addAll(curLev.fci_list);
                    bMerged = true;
                    // no further propagating
                    // a grandparent of the just closed level, is a parent of the found higher level, so no need
                    // to look further now
                    break;
                }
                else {
                    // continue looking for a parent, but mark path as interrupted by other switch (or if)
                    bPathOK = false;
                }
            }
            else{
                // if found
                // check if path is ok, by comparing the switch expression to the if expression
                if (!bExpressionsAreAlike(curLev.expression, highLev.expression)){
                    bPathOK = false;
                }
            }
        }

        // if there was a merge, we do nothing, because we process the parent switch
        // no merge? Than this was the highest switch!
        if (!bMerged){
            // make new switch info object
            var fsi = new FoundSwitchInfo();
            fsi.lngSwitchID=lngSwitchID;
            fsi.fci = curLev.fci_list;

            // put it in the map
            m_fsi.put(lngSwitchID, fsi);
        }
    }


    ////////////////////
    // other enter/exits
    ////////////////////

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        super.enterFunctionDefinition(ctx);
        m_mapLabelToICM.clear();
    }

    @Override
    public void exitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        super.exitFunctionDefinition(ctx);
        // try adding code markers to cases
        for (var sw : m_fsi.values()){
            for (var ci : sw.fci){
                if ((ci.strContainsOnlyThisJump != null) && (ci.caseBeginCM==null)){
                    ci.caseBeginCM = m_mapLabelToICM.get(ci.strContainsOnlyThisJump);
                }
            }
        }
    }

    @Override
    public void exitCompoundStatement(CParser.CompoundStatementContext ctx) {
        super.exitCompoundStatement(ctx);
        if (m_iLabelCompoundLevel>0) {
            m_iLabelCompoundLevel--;
        }
        else if (m_iLabelCompoundLevel == 0){
            m_strCurrentLabel = null;
        }
    }

    /**
     * Compare two expressions. The expressions may be switch expressions or if expressions.
     * They are deemed alike when: <br>
     * - there is at least one common identifier
     * @param e1 expression1
     * @param e2 expression2
     * @return true if criteria are met
     */
    private boolean bExpressionsAreAlike(CParser.ExpressionContext e1, CParser.ExpressionContext e2){
        var nodes1 = Misc.getAllTerminalNodes(e1);
        var nodes2 = Misc.getAllTerminalNodes(e2);

        for (var n1 : nodes1){
            for (var n2: nodes2){
                if ((n1.iTokenID== CLexer.Identifier) &&
                        (n2.iTokenID == CLexer.Identifier) &&
                        (n1.strText.equals(n2.strText))){
                    return true;
                }
            }
        }
        return false;
    }

    ////////////////////////////////
    // f15-base-class-implementation
    ////////////////////////////////

    @Override
    public EFeaturePrefix getCodeMarkerFeature() {
        return EFeaturePrefix.INDIRECTIONSFEATURE;
    }

    @Override
    public void resetCodeMarkerBuffersOnEnterPostfixExpression() {
    }
    @Override
    public void resetCodeMarkerBuffersOnEnterInitDeclarator() {
    }

    @Override
    public void processWantedCodeMarker(@NotNull CodeMarker cm) {
        if (cm instanceof IndirectionsCodeMarker icm) {     // this should always be true, but it keeps the compiler happy

            // if the marker is a case begin marker, store the combination of switchID and caseID to show
            // its existence
            if (icm.getCodeMarkerLocation()==EIndirectionMarkerLocationTypes.CASEBEGIN){
                m_branchIDIDs.add(icm.strGetValueForTreeSet());
            }

            // store the switch ID for the SQS-A-score
            m_switchIDSet.add(icm.lngGetSwitchID());

            // match a marker to a case
            if (!m_sli.isEmpty()) {
                var cur_sli = m_sli.peek();
                var cur_fci = cur_sli.current_fci;
                if (cur_fci != null) {
                    // we are in a case...
                    if (icm.getCodeMarkerLocation() == EIndirectionMarkerLocationTypes.CASEBEGIN) {
                        // store first begin code marker
                        if (cur_fci.caseBeginCM == null) {
                            cur_fci.caseBeginCM = icm;
                        }
                        // process switch ID
                        // if none were determined yet --> set it
                        // else: check it is the same ID
                        if (cur_sli.lngSwitchID == ISWITCHIDNOTIDENTIFIEDYET){
                            cur_sli.lngSwitchID = icm.lngGetSwitchID();
                        }
                        else if (cur_sli.lngSwitchID != ISWITCHIDNOTCONSISTENT) {
                            if (cur_sli.lngSwitchID != icm.lngGetSwitchID()){
                                cur_sli.lngSwitchID = ISWITCHIDNOTCONSISTENT;
                            }
                        }
                    }
                }
            }

            // check if marker is a begin-case marker within a labeled-block
            if (icm.getCodeMarkerLocation() == EIndirectionMarkerLocationTypes.CASEBEGIN) {
                if (m_strCurrentLabel != null) {
                    if (!m_mapLabelToICM.containsKey(m_strCurrentLabel)){
                        m_mapLabelToICM.put(m_strCurrentLabel, icm);
                    }
                }
            }
        }
    }
}
