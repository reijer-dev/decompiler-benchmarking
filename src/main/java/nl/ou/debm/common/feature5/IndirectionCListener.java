package nl.ou.debm.common.feature5;

import nl.ou.debm.assessor.CountTestResult;
import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.assessor.SchoolTestResult;
import nl.ou.debm.common.*;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
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
        private static final DecimalFormat s_formatter = new DecimalFormat("#0.0");
        public double dblA_switchPresentInBinary = 0.0;
        public double dblB_correctNumberOfCases = 0.0;
        public double dblC_caseIDCorrectness = 0.0;             // TODO NIY
        public double dblD_defaultBranchCorrectness = 0.0;
        public double dblE_caseStartPointCorrectness = 0.0;
        public double dblF_noCaseDuplications = 0.0;
        public double dblG_noGotos = 0.0;
        public double dblH_noGrandChildren = 0.0;
        public double dblTotalScore(){
            return dblA_switchPresentInBinary == 0.0 ? 0.0 :
                           (dblA_switchPresentInBinary +
                            dblB_correctNumberOfCases +
                            dblC_caseIDCorrectness +
                            dblD_defaultBranchCorrectness +
                            dblE_caseStartPointCorrectness +
                            dblF_noCaseDuplications +
                            dblG_noGotos +
                            dblH_noGrandChildren);
        }
        public String toString() {
            return "t:" + strQF(dblTotalScore()) +
                    "|A:" + strQF(dblA_switchPresentInBinary) +
                    "|B:" + strQF(dblB_correctNumberOfCases) +
                    "|C:" + strQF(dblC_caseIDCorrectness) +
                    "|D:" + strQF(dblD_defaultBranchCorrectness) +
                    "|E:" + strQF(dblE_caseStartPointCorrectness) +
                    "|F:" + strQF(dblF_noCaseDuplications) +
                    "|G:" + strQF(dblG_noGotos) +
                    "|H:" + strQF(dblH_noGrandChildren);
        }
        private String strQF(double in){
            return s_formatter.format(in);
        }
    }

    /**
     * class to store information on goto blocks
     */
    private static class GotoBlockInfo{
        public String strLabel = "";
        public IndirectionsCodeMarker icm = null;
        public boolean bCodeMarkerIsFirstRealCode = false;
        @Override
        public String toString(){
            return "lab=" + strLabel + ";1st=" + bCodeMarkerIsFirstRealCode + ";icm=" + icm;
        }
        public GotoBlockInfo(String strLabel, IndirectionsCodeMarker icm, boolean bCodeMarkerIsFirstRealCode){
            this.strLabel = strLabel;
            this.icm = icm;
            this.bCodeMarkerIsFirstRealCode = bCodeMarkerIsFirstRealCode;
        }
    }

    /**
     * This private class keeps running information on the selection level of the C-code.
     * The level is increased every time a selection statement (if, switch) is entered
     * and decreased when exited.
     * It's used as a struct in a stack.
     */
    private static class SelectionLevelInfo{
        /** true if this level is a switch body (rather than an if/else body) */    public boolean bSwitchBody = false;
        /** may only be valid for switches; switch ID */                            public long lngSwitchID = ISWITCHIDNOTIDENTIFIEDYET;
        /** the expression to enter the body; if (boolean) or switch (int) */       public CParser.ExpressionContext expression = null;
        /** all the branches found in a switch body */                              public final List<FoundCaseInfo> fci_list = new ArrayList<>();
        /** the case info for the current branch */                                 public FoundCaseInfo current_fci = null;
        /** if there is an ancestor switch: the branch this switch is in */         public FoundCaseInfo ancestor_fci = null;
        /** post-switch default code marker */                                      public IndirectionsCodeMarker icmDefaultCaseAfterSwitch = null;
        @Override
        public String toString(){
            return "SW=" + bSwitchBody +
                    ";ID=" + lngSwitchID +
                    ";curCFI=" + current_fci +
                    ";ancCFI=" + ancestor_fci +
                    ";defAfSw=" + icmDefaultCaseAfterSwitch +
                    ";SE=" + (expression == null ? "null" : expression.getText())  +
                    ";FCI=" + fci_list;
        }
    }

    /**
     * This struct class collects information about switch branches in the C-code.
     */
    private static class FoundCaseInfo implements Comparable<FoundCaseInfo>{
        /** the case ID for this case*/                                 public long lngCaseIDInCode;
        /** the case begin code marker */                               public IndirectionsCodeMarker caseBeginCM = null;
        /** true if the case was a first degree child case */           public boolean bFirstDegreeChild = true;
        /** true if the case is reachable without disturbance*/         public boolean bPathToCaseOk = true;
        /** true if the case is empty */                                public boolean bEmptyBranch = true;
        /** next switch ID in code, valid when branch is empty*/        public long lngNextCaseIDInCode = ICASEINDEXFORNOTSETYET;
        /** if valid, only contains a jump to this label (no:)*/        public String strContainsOnlyThisJump = null;
        /** if true, begin case code marker is first real statement*/   public boolean bCaseBeginCodeMarkerIsFirstStatement = true;
        /** if true, any goto was found on the case level */            public boolean bAnyGotoFound = false;
        @Override
        public String toString(){
            return "CID=" + lngCaseIDInCode + ";E=" + bEmptyBranch + ";NXID=" + lngNextCaseIDInCode +
                    ";POK=" + bPathToCaseOk + ";FDC=" + bFirstDegreeChild +
                    (strContainsOnlyThisJump!=null ? ";JM=" + strContainsOnlyThisJump : "") +
                    ";1st=" + bCaseBeginCodeMarkerIsFirstStatement +
                    ";goto=" + bAnyGotoFound +
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
        /** switch iD */                                                        public long lngSwitchID = ISWITCHIDNOTIDENTIFIEDYET;
        /** all cases */                                                        public List<FoundCaseInfo> fci = null;
        /** translate: code -> org, org = a x code + b */                       public long a = 1;
        /** translate: code -> org, org = a x code + b */                       public long b = 0;
        /** switch is immediately followed by default branch code marker */     public IndirectionsCodeMarker icmSwitchFollowedByThisDefaultCaseCodeMarker = null;
        /** defining icm, the before-switch-code marker */                      public IndirectionsCodeMarker icmBeforeSwitchCodeMarker = null;
        public String toString(){
            return "FSI:ID=" + lngSwitchID + ";a=" + a + ";b=" + b + ";defAfterSw=" + icmSwitchFollowedByThisDefaultCaseCodeMarker + ";CI=" + fci;
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
                if (q.lngCaseIDInCode!=ICASEINDEXFORDEFAULTBRANCH) {
                    sortedCIDs.add(q.lngCaseIDInCode);
                }
            }
            Collections.sort(sortedCIDs);

            // check sizes
            if (sortedLLVMIDs.size()<sortedCIDs.size() || sortedCIDs.isEmpty()){
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

        /**
         * determine the number of branches for the B-score of the SQS.
         * count all branches that are direct children of this switch
         * @return number of direct child branches<br>
         * switch(a){<br>
         *    case 1: // direct child <br>
         *    default:<br>
         *      switch(a){<br>
         *        case 2: // indirect child<br>
         *      }<br>
         * }<br>
         */
        public int iGetNumberOfBScoreCases(){
            int out = 0;
            for (var fci : this.fci){
                if (fci.lngCaseIDInCode!=ICASEINDEXFORDEFAULTBRANCH) {
                    out ++;
                }
            }

            return out;
        }

        /**
         * determine whether there is a true default branch
         * @return true if there is a default branch, containing a default branch code marker
         * and firstDegreeChild==true
         */
        public boolean bHasTrueDefaultBranch() {
            for (var ci : fci){
                if (ci.lngCaseIDInCode==ICASEINDEXFORDEFAULTBRANCH){
                    // default branch
                    if (ci.caseBeginCM != null){
                        // code marker present
                        if (ci.caseBeginCM.lngGetCaseID() == ICASEINDEXFORDEFAULTBRANCH){
                            // and case ID works out fine
                            return true;
                        }
                    }
                    break;
                }
            }
            return false;
        }

        /**
         * determine whether switch has a correct post switch default branch; see
         * explanation in IndirectionAssessor, D-score
         * @return true if:<br>there is a post switch default branch *AND* <br>
         * switch ID's match *AND*<br>
         * no breaks were used in the original code
         */
        public boolean bHasCorrectPostSwitchDefaultBranch(){
            if (icmSwitchFollowedByThisDefaultCaseCodeMarker!=null){
                if (icmSwitchFollowedByThisDefaultCaseCodeMarker.lngGetSwitchID()==lngSwitchID){
                    return icmBeforeSwitchCodeMarker!=null && !icmBeforeSwitchCodeMarker.bGetUseBreaks();
                }
            }
            return false;
        }
    }

    ////////////////////
    // object attributes
    ////////////////////
    // info for or from the outside world
    /** basic info retrieved from the LLVM */                                       private final Map<Long, CodeMarker.CodeMarkerLLVMInfo> m_basicLLVMInfo;
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
    /** key = label, code marker info in that labeled block */                      private final Map<String, GotoBlockInfo> m_mapGotoBlockInfo = new HashMap<>();
    /** info per selection level */                                                 private final Stack<SelectionLevelInfo> m_sli = new Stack<>();
    /** info per switch (only our switches), mapped by switch ID */                 private final Map<Long, FoundSwitchInfo> m_fsi = new HashMap<>();
    /** set of all the switch/branch ID's */                                        private final Set<String> m_branchIDIDs = new TreeSet<>();
    /** key = switch ID, value = quality score */                                   private final Map<Long, SwitchQualityScore> m_SQS = new HashMap<>();
    /** set of all switchID's found in all switch code markers */                   private final Set<Long> m_switchIDSet = new TreeSet<>();
    /** tree containing all the function's selectionLevelInfo's */                  private final SimpleTree<SelectionLevelInfo> m_levelInfoTree = new SimpleTree<>();
    /** current selectionLevelInfo node */                                          private SimpleTree.SimpleTreeNode<SelectionLevelInfo> m_currentTreeNode;
    /** if not null, we look for the first statement in this case */                private FoundCaseInfo m_lookForFirstStatementInThisCase = null;
    /** true when a label is found, falsified on any other than begin case cm */    private boolean m_bNoCodeBetweenLabelAndCodeMarker = false;
    /** number of occurrences per ICM, key = code marker ID, value = # */           private final Map<Long, Integer> m_occurrencePerCodeMarkerInDecompilerOutput = new HashMap<>();
    /** on every sli-pop, this is set, though set to null if non-switch-body was popped */ private SelectionLevelInfo m_lastPoppedSelectionLevelInfoWithSwitchBody = null;

    ///////////////
    // construction
    ///////////////
    public IndirectionCListener(IAssessor.CodeInfo ci, Map<Long, SwitchInfo> llvmSwitchInfo, Map<Long, CodeMarker.CodeMarkerLLVMInfo> basicLLVMInfo){
        // call super constructor; the f15-class takes care of a general stuff for finding code markers
        super();

        // keep parameters
        m_LLVMSwitchInfo = llvmSwitchInfo;
        m_basicLLVMInfo = basicLLVMInfo;
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

        return out;
    }

    /////////////////////////////////////
    // calculating metrics after the walk
    /////////////////////////////////////

    @Override
    public void exitCompilationUnit(CParser.CompilationUnitContext ctx) {
        super.exitCompilationUnit(ctx);

        // hard work is done, do the post-processing
        ////////////////////////////////////////////

        // add defining code-markers to switch info
        for (var li : m_basicLLVMInfo.values()){
            if (li.codeMarker instanceof IndirectionsCodeMarker icm){
                if (icm.getCodeMarkerLocation()==EIndirectionMarkerLocationTypes.BEFORE){
                    var fsi = m_fsi.get(icm.lngGetSwitchID());
                    if (fsi!=null){
                        fsi.icmBeforeSwitchCodeMarker=icm;
                    }
                }
            }
        }

        // try to work out translation factors
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

    private void calculateQualityScores() {
        // calculate all scores
        calculateSubScores();

        // take averages
        calculateAverages();
    }

    private void calculateAverages(){
        // take every switch's score and calculate appropriate averages
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

    private void calculateSubScores(){
        // loop over all switches
        for (var sqs : m_SQS.entrySet()){
            // get basics
            long lngSwitchID=sqs.getKey();
            assert lngSwitchID!=0 : "m_SQS has switch with ID 0";
            var score = sqs.getValue();
            assert score != null : "m_SQS.score has null pointer, lngSwitchID=" + lngSwitchID;
            var LLVM_SI = m_LLVMSwitchInfo.get(lngSwitchID);
            assert LLVM_SI != null : "m_LLVMSwitchInfo map has null object as value";
            var fsi = m_fsi.get(lngSwitchID);
            // no assertion here! This may be null, when the switch is not found in the decompiler output

            // calculate the lot
            calculateSQSA(lngSwitchID, score);
            if (fsi!=null) {
                calculateSQSB(score, LLVM_SI, fsi);
                calculateSQSC(score, LLVM_SI, fsi);
                calculateSQSD(score, LLVM_SI, fsi);
                calculateSQSE(score, LLVM_SI, fsi);
                calculateSQSF(score,          fsi);
                calculateSQSG(score,          fsi);
                calculateSQSH(score,          fsi);
            }
        }
    }

    private void calculateSQSA(long lngSwitchID, SwitchQualityScore score){
        // find code markers
        if (m_switchIDSet.contains(lngSwitchID)){
            score.dblA_switchPresentInBinary = 1;
        }
    }

    private void calculateSQSB(SwitchQualityScore score, SwitchInfo LLVM_SI, FoundSwitchInfo fsi){
        // compare number of branches:
        // equal --> 1.0
        // -1 or <=10% difference --> 0.5
        // else, or when more are found in code than in LLVM: 0.0

        // get branch numbers
        int iNBranchesLLVM = LLVM_SI.iGetNumberOfBSCoreCases();
        int iNBranchesInC = fsi.iGetNumberOfBScoreCases();
        // compare them to score
        if (iNBranchesLLVM == iNBranchesInC) {
            score.dblB_correctNumberOfCases = 1;
        } else if (iNBranchesLLVM > iNBranchesInC) {
            if ((iNBranchesLLVM-1) == iNBranchesInC){
                score.dblB_correctNumberOfCases = 0.5;
            }
            else {
                double pct = (100 * (double) iNBranchesInC) / (double) iNBranchesLLVM;
                if (pct >= 90) {
                    score.dblB_correctNumberOfCases = 0.5;
                }
            }
        }
    }

    private void calculateSQSC(SwitchQualityScore score, SwitchInfo LLVM_SI, FoundSwitchInfo fsi) {
        //    LLVM    decompiler output
        //    0       0
        //    1       1
        //    2
        //            3
        //    4       4
        //    5       5
        //    6
        // in total we have a number of 7 pairs
        // pairs (0,0) (1,1) (4,4) and (5,5) are correct pairs
        // pairs (2,-), (-,3) and (6,-) are 'broken'
        //
        // we iterate over the right list
        // every time we find a right value in the left column, we increase the correct pair count (4)
        // left-only-pair-count = number of left values -/- correct pair count  (6 -/- 4 = 2)
        // right-only-pair-count = number of right values -/- correct pair count (5 -/- 4 = 1)
        // total pair count = correct pair count + left-only-pair-count + right-only-pair-count
        //                  = correct pair count + (number of left values -/- correct pair count) + (number of right values -/- correct pair count) =
        //                  = number of left values + number of right values -/- correct pair count

        double dblNCorrectPairs = 0;
        for (var C_case : fsi.fci){
            if (C_case.lngCaseIDInCode != ICASEINDEXFORDEFAULTBRANCH) {
                long CCaseID = (C_case.lngCaseIDInCode * fsi.a) + fsi.b;
                for (var leftNum : LLVM_SI.LLVMCaseInfo()) {
                    if (leftNum.m_lngBranchValue == CCaseID) {
                        dblNCorrectPairs++;
                        break;
                    }
                }
            }
        }

        double dblNTotalPairs = LLVM_SI.iGetNumberOfBSCoreCases() + fsi.iGetNumberOfBScoreCases() - dblNCorrectPairs;
        if (dblNCorrectPairs == dblNTotalPairs){
            score.dblC_caseIDCorrectness=1.0;
        }
        else {
            if ((dblNCorrectPairs / dblNTotalPairs) >= .7) {
                score.dblC_caseIDCorrectness = 0.5;
            }
        }
        if ((score.dblC_caseIDCorrectness>0) && ((fsi.a!=1) || (fsi.b!=0))){
            score.dblC_caseIDCorrectness-=.1;
        }
    }

    private void calculateSQSD(SwitchQualityScore score, SwitchInfo LLVM_SI, FoundSwitchInfo fsi){
        // compare default branches
        boolean bLLVMDefaultBranch = LLVM_SI.bLLVMHasDefaultBranch();
        boolean bCHasDefaultBranch = fsi.bHasTrueDefaultBranch();
        boolean CDefaultBranchAssessment = (bCHasDefaultBranch || fsi.bHasCorrectPostSwitchDefaultBranch());
        score.dblD_defaultBranchCorrectness = bLLVMDefaultBranch == CDefaultBranchAssessment ? 1 : 0;
    }

    private void calculateSQSE(SwitchQualityScore score, SwitchInfo LLVM_SI, FoundSwitchInfo fsi){
        // get cases from the code marker, so we can determine the case ID in the code marker, later on
        var ICM_cases = LLVM_SI.getICM().getCases();

        // keep track of the score
        double dblTotalCorrectCases=0;

        // loop over all cases (including default) in LLVM (explained in IndirectionAssessor general comment V, start)
        for (var L_case : LLVM_SI.LLVMCaseInfo()){
            // L_case.m_lngBranchValue = branch value in the original LLVM --> case n:
            //
            // look for the case in the list of cases in the code marker and then look for the
            // first non-empty case; keep that case ID
            // (=step 1)
            boolean bLookForFirstNonEmptyBranch = false;
            Integer iCaseIDExpectedInBranchCodeMarker = null;
            for (var I_case : ICM_cases){
                if (I_case.iCaseIndex == L_case.m_lngBranchValue){
                    bLookForFirstNonEmptyBranch = true;
                }
                if (bLookForFirstNonEmptyBranch && I_case.bFillCase){
                    iCaseIDExpectedInBranchCodeMarker = I_case.iCaseIndex;
                    break;
                }
            }
            if (iCaseIDExpectedInBranchCodeMarker!=null){
                // L_case.m_lngBranchValue = branch value in the original LLVM --> case n:
                // iCaseIDExpectedInBranchCodeMarker = branch value that we want to see in the code marker for
                //                                      this case in the emitted output
                // loop over all cases in the decompiler output
                // (=step 2)
                for (var fci : fsi.fci){
                    if (fci.caseBeginCM!=null){
                        if (fci.caseBeginCM.lngGetCaseID() == iCaseIDExpectedInBranchCodeMarker){
                            // we now have a potential match: a branch in the decompiler output
                            // (fci; found case info) that has a begin-code-marker containing the
                            // case ID we are looking for.
                            //
                            // we now try to match the case ID in the decompiler output to the case ID from
                            // the LLVM, using the translation constants a & b in the switch info
                            // (=step 3)
                            long orgCaseID = fsi.a * fci.lngCaseIDInCode + fsi.b;
                            if (orgCaseID == L_case.m_lngBranchValue){
                                // we successfully matched a branch in the LLVM to the branch in the emitted C-code
                                // we score when the begin code marker was the first statement
                                //
                                // (=step 4)
                                if (fci.bCaseBeginCodeMarkerIsFirstStatement){
                                    dblTotalCorrectCases++;
                                }
                            }
                        }
                    }
                }
            }
        }

        // set score
        score.dblE_caseStartPointCorrectness = (3.0 * dblTotalCorrectCases) / (double) LLVM_SI.LLVMCaseInfo().size();
    }

    private void calculateSQSF(SwitchQualityScore score, FoundSwitchInfo fsi) {
        // assume all is well
        score.dblF_noCaseDuplications = 1.0;

        // loop over all cases
        for (var C_case : fsi.fci) {
            var icm = C_case.caseBeginCM;
            if (icm != null) {
                // check the number of occurrences of this code marker in the emitted C-code to the LLVM statistic
                int iNInC = 0;
                long lngNInL = 0;
                try {
                    iNInC = m_occurrencePerCodeMarkerInDecompilerOutput.get(icm.lngGetID());
                    lngNInL = m_basicLLVMInfo.get(icm.lngGetID()).lngNOccurrencesInLLVM;
                } catch (Throwable ignore) {
                }  // ignore possible null-pointer-exceptions
                if (iNInC > lngNInL) {
                    score.dblF_noCaseDuplications = 0.0;
                    break;
                }
            }
        }
    }

    private void calculateSQSG(SwitchQualityScore score, FoundSwitchInfo fsi) {
        // assume all is ok
        score.dblG_noGotos = 1.0;
        // loop over all cases
        for (var C_case : fsi.fci) {
            if (C_case.bAnyGotoFound){
                score.dblG_noGotos = 0.0;
                break;
            }
        }
    }

    private void calculateSQSH(SwitchQualityScore score, FoundSwitchInfo fsi) {
        // assume all is ok
        score.dblH_noGrandChildren = 1.0;
        // loop over all cases
        for (var C_case : fsi.fci) {
            if (!C_case.bFirstDegreeChild){
                score.dblH_noGrandChildren = 0.0;
                break;
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

        if (ctx.Case() != null) {
            ProcessCaseStatement(ctx);
        }
        if (ctx.Default() != null) {
            ProcessDefaultStatement(ctx);
        }
        if (ctx.Identifier()!=null){
            ProcessLabeledStatement(ctx);
        }

        // do not do anything with first code marker search in a case or goto block,
        // because a labeled statement will be followed by a statement and that
        // will be handled.
    }

    private void ProcessDefaultStatement(CParser.LabeledStatementContext ctx){
        // default: found
        //
        // any current case statement object is already added to the list, so we don't have to do that now
        // we can simply create a new one (later this function)

        // case must always be in a switch compound statement...
        assert m_sli.peek().bSwitchBody : "case found outside switch body";

        // we don't have to worry about previous empty cases, such as:
        // switch (a){
        //   case 1:  <code>
        //   case 2:  // empty
        //   default: <code>
        // }
        // because we never produce an empty case before default (would be utter nonsense)
        //

        // process it to the stack and do other things that must be done for case branches too
        commonCaseAndDefaultCode(ICASEINDEXFORDEFAULTBRANCH);
    }

    private void ProcessLabeledStatement(CParser.LabeledStatementContext ctx){
        // labeled statement
        //

        // remember label
        m_strCurrentLabel = ctx.Identifier().getText();
        m_iLabelCompoundLevel = 0;
        m_bNoCodeBetweenLabelAndCodeMarker = true;
    }

    private void ProcessCaseStatement(CParser.LabeledStatementContext ctx) {
        // case-statement
        // --------------
        //
        // any current case statement object is already added to the list, so we don't have to do that now
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

        // process it to the stack and do other things that must be done for default branches too
        long lngCaseID = x.LngGetIntegerLikeValue();
        commonCaseAndDefaultCode(lngCaseID);

        // when there is a previous empty case, link it to this one
        if (oldCaseInfo!=null){
            if (oldCaseInfo.bEmptyBranch) {
                oldCaseInfo.lngNextCaseIDInCode = lngCaseID;
            }
        }
    }

    private void commonCaseAndDefaultCode(long lngCaseID) {
        // make new current case object + set case index
        var fci = new FoundCaseInfo();
        fci.lngCaseIDInCode = lngCaseID;

        // now we add it
        m_sli.peek().fci_list.add(fci);

        // and we keep it as current
        m_sli.peek().current_fci = fci;

        // we need to make sure that the begin case code marker is the first statement in the case
        // (with some by rules, of course).
        // set flag that we are looking for the first statement
        m_lookForFirstStatementInThisCase = fci;
    }

    @Override
    public void enterExpressionStatement(CParser.ExpressionStatementContext ctx) {
        super.enterExpressionStatement(ctx);
        processEnterExpressionOrEnterInitWithExpression(ctx);
    }

    private void processEnterExpressionOrEnterInitWithExpression(ParserRuleContext ctx){
        // we need to roughly check for code marker code
        // we ignore all expressions containing code marker code, because the code marker
        // itself will be handled by enterPostFixExpression (f15 base class)
        // and we found constructs like these (RetDec):
        //        [int] v1 = (int_64*) "code marker text";
        //        __CM_call("same code marker text");
        // we expect the __CM_call() to actually be __CM_call(v1), where v1 was
        // already substituted for the string it represents. We consider this
        // assignment of declaration+assignment to belong to the code marker call itself
        // it may not be very prettily emitted code, but it is clear enough,
        // and it will serve for judging whether 'other' code was inserted before
        // the code marker (which will be relevant to determine if the correct
        // start point has been determined). one could compare it to the
        // prologue statements we accept when assessing function boundaries
        var feature = CodeMarker.isACodeMarkerStringPresentInTheContext(ctx);
        if (feature==null){
            // expression statement without a code marker
            // so, we reset the search
            resetFirstCodeMarkerInCase();
            m_bNoCodeBetweenLabelAndCodeMarker=false;
            // stop looking for a post-switch code marker
            m_lastPoppedSelectionLevelInfoWithSwitchBody = null;
        }

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
        // when in a switch branch, mark this branch as no-code-marker-first
        resetFirstCodeMarkerInCase();
        // mark that the first statement is cannot longer be an expression
        m_bNoCodeBetweenLabelAndCodeMarker = false;
        // stop looking for a post-switch code marker
        m_lastPoppedSelectionLevelInfoWithSwitchBody = null;

        // add new level to stack
        var sli = new SelectionLevelInfo();
        m_sli.push(sli);
        sli.bSwitchBody=(ctx.Switch()!=null);
        sli.expression = ctx.expression();

        // try to find an ancestor branch
        for (int ptr = m_sli.size()-2; ptr>=0; ptr--){
            var anc_info = m_sli.get(ptr);
            if (anc_info!=null){
                if (anc_info.bSwitchBody){
                    sli.ancestor_fci = anc_info.current_fci;
                }
            }
        }

        // add new node to tree
        m_currentTreeNode = m_currentTreeNode.addChild(sli);
    }

    @Override
    public void enterIterationStatement(CParser.IterationStatementContext ctx) {
        super.enterIterationStatement(ctx);

        // when in a switch branch, mark this branch as not empty
        safelyMarkCurrentBranchAsNotEmpty();
        // when in a switch branch, mark this branch as not goto-only
        safelySetCurrentBranchGotoOnlyStatus(null);
        // when in a switch branch, mark this branch as no-code-marker-first
        resetFirstCodeMarkerInCase();
        // mark that the first statement is cannot longer be an expression
        m_bNoCodeBetweenLabelAndCodeMarker = false;
        // stop looking for a post-switch code marker
        m_lastPoppedSelectionLevelInfoWithSwitchBody = null;
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
                    fci.bAnyGotoFound = true;
                }
            }
            // no more label searching
            m_strCurrentLabel = null;
            m_iLabelCompoundLevel = 0;
        }

        // when in a switch branch, mark this branch as not empty
        safelyMarkCurrentBranchAsNotEmpty();
        // when in a switch branch, mark this branch as no-code-marker-first
        resetFirstCodeMarkerInCase();
        // mark that the first statement is cannot longer be an expression
        m_bNoCodeBetweenLabelAndCodeMarker = false;
        // stop looking for a post-switch code marker
        m_lastPoppedSelectionLevelInfoWithSwitchBody = null;
    }

    @Override
    public void exitSelectionStatement(CParser.SelectionStatementContext ctx) {
        super.exitSelectionStatement(ctx);

        // process stack
        //
        // 1. we pop the object, but...
        // 2. we keep it, so we can process a default case code marker we may find after the switch body
        //    (see D-score explanation)
        m_lastPoppedSelectionLevelInfoWithSwitchBody = m_sli.pop();
        if (!m_lastPoppedSelectionLevelInfoWithSwitchBody.bSwitchBody){
            // only search after switch body
            m_lastPoppedSelectionLevelInfoWithSwitchBody=null;
        }
        else{
            // only search when the switch body doesn't contain a default branch
            for (var fci : m_lastPoppedSelectionLevelInfoWithSwitchBody.fci_list){
                if (fci.caseBeginCM!=null){
                    if (fci.caseBeginCM.lngGetCaseID() == ICASEINDEXFORDEFAULTBRANCH){
                        m_lastPoppedSelectionLevelInfoWithSwitchBody=null;
                    }
                }
            }
        }

        // process tree
        m_currentTreeNode = m_currentTreeNode.parent;

        // the assembled tree data is processed later, when the function is
        // exited
    }

    private void resetFirstCodeMarkerInCase(){
        if (m_lookForFirstStatementInThisCase!=null) {
            m_lookForFirstStatementInThisCase.bCaseBeginCodeMarkerIsFirstStatement=false;
        }
        m_lookForFirstStatementInThisCase=null;
    }

    ////////////////////
    // other enter/exits
    ////////////////////

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        super.enterFunctionDefinition(ctx);
        m_mapGotoBlockInfo.clear();
        m_levelInfoTree.clear();
        m_currentTreeNode = m_levelInfoTree.getRoot();
    }

    @Override
    public void exitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        super.exitFunctionDefinition(ctx);

        // try adding code markers to cases
        for (var li : m_levelInfoTree.valuesNoNull()){
            for (var ci : li.fci_list){
                if ((ci.strContainsOnlyThisJump != null) && (ci.caseBeginCM==null)){
                    var gotoBlockInfo = m_mapGotoBlockInfo.get(ci.strContainsOnlyThisJump);
                    if (gotoBlockInfo!=null) {
                        ci.caseBeginCM = gotoBlockInfo.icm;
                        ci.bCaseBeginCodeMarkerIsFirstStatement = gotoBlockInfo.bCodeMarkerIsFirstRealCode;
                    }
                }
            }
        }

        // process the level data
        processSelectionLevelData();
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
    public void resetCodeMarkerBuffersOnEnterInitDeclarator(CParser.InitDeclaratorContext ctx) {
        // this gets called on entering something like this:
        // type var = expression
        // this is functionally equivalent to entering an expression statement
        processEnterExpressionOrEnterInitWithExpression(ctx);
    }

    @Override
    public void processWantedCodeMarker(@NotNull CodeMarker cm) {
        if (cm instanceof IndirectionsCodeMarker icm) {     // this should always be true, but it keeps the compiler happy
            // if the marker is a case begin marker, store the combination of switchID and caseID to show
            // its existence
            if (icm.getCodeMarkerLocation()==EIndirectionMarkerLocationTypes.CASEBEGIN){
                m_branchIDIDs.add(icm.strGetValueForTreeSet());
            }

            // keep track of numbers
            m_occurrencePerCodeMarkerInDecompilerOutput.merge(icm.lngGetID(), 1, Integer::sum);

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
                        // The switch-ID analysis is done later, when all the function's code has been
                        // analysed. Cases may contain single goto statements, leaving code markers
                        // to be found elsewhere (usually: after the switch). So, we wait for all
                        // those constructs to go by and be analysed, before determining switch ID's
                    }
                }
            }

            // other checks for begin code markers
            if (icm.getCodeMarkerLocation() == EIndirectionMarkerLocationTypes.CASEBEGIN) {
                // check if marker is a begin-case marker within a labeled-block
                if (m_strCurrentLabel != null) {
                    if (!m_mapGotoBlockInfo.containsKey(m_strCurrentLabel)){
                        m_mapGotoBlockInfo.put(m_strCurrentLabel,
                                new GotoBlockInfo(m_strCurrentLabel, icm, m_bNoCodeBetweenLabelAndCodeMarker));
                    }
                }
                // no more looking for begin case code markers
                // this means that a true value that may have been established,
                // will be preserved
                m_lookForFirstStatementInThisCase = null;
            }
            else {
                // stop looking after a non-case-begin-marker
                m_strCurrentLabel = null;
                // if we were looking for a first code marker and found something else, reset flag
                resetFirstCodeMarkerInCase();
            }

            // process post-switch-block default case code markers
            if (m_lastPoppedSelectionLevelInfoWithSwitchBody!=null){
                if (icm.getCodeMarkerLocation() == EIndirectionMarkerLocationTypes.CASEBEGIN) {
                    if (icm.lngGetCaseID() == ICASEINDEXFORDEFAULTBRANCH){
                        m_lastPoppedSelectionLevelInfoWithSwitchBody.icmDefaultCaseAfterSwitch = icm;
                    }
                }
            }
            m_lastPoppedSelectionLevelInfoWithSwitchBody=null;
        }
    }

    /**
     * Go through the tree containing all the selection level data (all if's and switches) and
     * filter out switches.
     * The map m_fsi will be filled.
     */
    private void processSelectionLevelData() {
        // try to determine all switch ID's
        for (var sli : m_levelInfoTree.valuesNoNull()){
            if (sli.bSwitchBody) {
                // determine switch ID from cases
                sli.lngSwitchID = ISWITCHIDNOTIDENTIFIEDYET;
                for (var fsi : sli.fci_list) {
                    if (fsi.caseBeginCM != null) {
                        var caseSwitchID = fsi.caseBeginCM.lngGetSwitchID();
                        if (sli.lngSwitchID == ISWITCHIDNOTIDENTIFIEDYET) {
                            // first switch ID found in cases; assume correctness
                            sli.lngSwitchID = caseSwitchID;
                        } else if (sli.lngSwitchID != ISWITCHIDNOTCONSISTENT) {
                            // later case, match previous found switch ID
                            if (caseSwitchID != sli.lngSwitchID) {
                                sli.lngSwitchID = ISWITCHIDNOTCONSISTENT;
                                break;
                            }
                        }
                    }
                }
            }
        }

        // use recursion for propagation of cases
        doLevelTreeNode(m_levelInfoTree.getRoot(), 0);
    }

    /**
     * go through the tree containing all the selection level data (all if's and switches) and
     * filter out switches; do the hard work for this node
     * @param node current node
     * @param iCurrentTreeLevel node level in the tree, 0 = root
     */
    private void doLevelTreeNode(SimpleTree.SimpleTreeNode<SelectionLevelInfo> node, int iCurrentTreeLevel){
        // process all children first
        for (var ch : node.children){
            doLevelTreeNode(ch, iCurrentTreeLevel + 1);
        }

        // all right, all children have been cleared away - now we can process this node
        var curLev = node.data;
        if (curLev==null){
            // we do not use the root node (=function level), so that one is null. Simply ignore,
            // but assert for debug purposes
            assert node.parent==null : "curlev is null, but node has a parent";
            return;
        }

        // done when this is no switch body
        if (!curLev.bSwitchBody) {
            return;
        }

        // get switch ID
        long lngSwitchID = curLev.lngSwitchID;

        // only continue when switch ID could be determined, otherwise assume that it was not
        // one of our switches
        if ((lngSwitchID==ISWITCHIDNOTIDENTIFIEDYET) || (lngSwitchID==ISWITCHIDNOTCONSISTENT)){
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
        //                 switch (a) { // <-- D
        //                    case 8:
        //                    case 9:
        //                    default:
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
        // - cases 8, 9 and the default after 9 asre first degree children of D and belong to the A/B family
        // - cases 3 and 4 may only be propagated when
        //      - the previous switch had the same expression
        //      - the cases in the previous switch have the same switch ID
        //
        // so, we propagate cases 3 and 4 to the first switch and consider them one big switch
        // we also propagate cases 8, 9 and the default to the first switch
        //
        // we only propagate a default branch if the switch is (in)directly in a parent default branch
        // D/default is in B/default, which is in A/default, so in the end, we replace the default in A
        // with the default branch in D
        boolean bMerged = false;
        boolean bPathOK = true;
        var higherNode = node.parent;
        while (higherNode != null) {
            var highLev = higherNode.data;
            if (highLev == null){
                assert higherNode.parent == null : "Null object in selectionLevelInfo, other than at the root node";
                break;
            }
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
                    // check if propagation is in the ancestor's default branch
                    boolean inAncestorsDefault = curLev.ancestor_fci != null && (curLev.ancestor_fci.lngCaseIDInCode == ICASEINDEXFORDEFAULTBRANCH);

                    // remove branch, either from child or from ancestor
                    {
                        List<FoundCaseInfo> defaultCaseRemoveList = null;
                        if (!inAncestorsDefault) {
                            // when child is not in ancestor's default branch:
                            // remove default branch from child switch to prevent propagation
                            defaultCaseRemoveList = curLev.fci_list;
                        } else {
                            // child switch is in ancestor's default branch:
                            // remove default branch from ancestor, for it will be replaced by child's default
                            defaultCaseRemoveList = highLev.fci_list;
                        }
                        FoundCaseInfo defaultBranch = null;
                        for (var ci : defaultCaseRemoveList) {
                            if (ci.lngCaseIDInCode == ICASEINDEXFORDEFAULTBRANCH) {
                                defaultBranch = ci;
                                break;
                            }
                        }
                        if (defaultBranch != null) {
                            defaultCaseRemoveList.remove(defaultBranch);
                        }
                    }
                    // do the propagation
                    highLev.fci_list.addAll(curLev.fci_list);
                    bMerged = true;
                    // no further propagating
                    // a grandparent of the just closed level, is a parent of the found higher level, so no need
                    // to look further now.
                    //
                    // we don't propagate defaults-after-the-switch, because we cannot imagine a construction
                    // where we would encounter nested switches with the same expression **AND** a default-after-the-switch
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
            higherNode = higherNode.parent;
        }

        // if there was a merge, we do nothing, because we process the parent switch
        // no merge? Then this was the highest switch!
        if (!bMerged){
            // make new switch info object
            var fsi = new FoundSwitchInfo();
            fsi.lngSwitchID=lngSwitchID;
            fsi.fci = curLev.fci_list;
            if (curLev.icmDefaultCaseAfterSwitch!=null && curLev.icmDefaultCaseAfterSwitch.lngGetSwitchID()==lngSwitchID){
                fsi.icmSwitchFollowedByThisDefaultCaseCodeMarker = curLev.icmDefaultCaseAfterSwitch;
            }

            // put it in the map
            m_fsi.put(lngSwitchID, fsi);
        }
    }
}
