package nl.ou.debm.assessor;

import nl.ou.debm.common.*;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.LLVMIRLexer;
import nl.ou.debm.common.antlr.LLVMIRParser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Interface to be implemented by every feature class, in order to ensure
 * that the main assessor loop can invoke an assessment session
 */
public interface IAssessor {
    /**
     *
     * TestResult is an abstract base class for test result storage. It contains and implements a few
     * features that need to be processed for any test:<br>
     * - what test (ETestCategories)<br>
     * - what test conditions (CompilerConfig)<br>
     * - how many tests the result is based upon (normally 1, but this rises on aggregation)<br>
     * <br>
     * The class requires its children to implement five access functions, returning lower/higher bound,
     * actual value, target value and the number of decimals to be printed in a table.
     *
     */
    abstract class TestResult implements Comparable<TestResult>, Comparator<TestResult> {
        // private attributes
        /** which test is stored */
        protected ETestCategories m_whichTest;
        /** what compiler config was used for the binary */
        protected final CompilerConfig m_compilerConfig = new CompilerConfig();
        /** number of tests involved in determining this TestResult's value */
        protected int m_iNTests = 1;
        /** score belongs to which test (test being one tested C source) */
        private int m_TestNumber;
        /** standard deviation over this test category, calculates over test suites **/
        private double m_dblStandardDeviation;
        private List<Double> m_scoresPerTest = new ArrayList<>();

        // basic accessor functions
        public void setWhichTest(ETestCategories whichTest){
            m_whichTest=whichTest;
        }
        public ETestCategories getWhichTest(){
            return m_whichTest;
        }
        public void setCompilerConfig(CompilerConfig compilerConfig){
            m_compilerConfig.copyFrom(compilerConfig);
        }
        public CompilerConfig getCompilerConfig(){
            return m_compilerConfig;
        }
        public EArchitecture getArchitecture(){
            return m_compilerConfig.architecture;
        }
        public ECompiler getCompiler(){
            return m_compilerConfig.compiler;
        }
        public EOptimize getOptimization(){
            return m_compilerConfig.optimization;
        }
        public int iGetNumberOfTests(){
            return m_iNTests;
        }

        // access functions to be implemented in children
        /**
         * @return the lowest possible test metric value, may be null if no lower bound is available
         */
        public abstract Double dblGetLowBound();
        /**
         * @return the test metric, may be null if no actual value could be determined
         */
        public abstract Double dblGetActualValue();
        /**
         * @return the highest possible test metric value, may be null if no higher bound is available
         */
        public abstract Double dblGetHighBound();
        /**
         * @return the target test metric (the optimal value), may be null if no target is available
         */
        public abstract Double dblGetTarget();

        /**
         * Determine the number of decimals to be printed when the low bound/ high bound/ actual value/ target values
         * are printed in a table
         * @return Number of decimals; 0 = print whole number, 1= print tenths etc,
         */
        public abstract int iGetNumberOfDecimalsToBePrinted();

        /**
         * Copy the class attributes from sibling
         * Start with calling copyAbstractValues
         * @param rhs source object
         */
        public abstract void copyFrom(TestResult rhs);

        /**
         * Copy the abstract class attributes from sibling
         * @param rhs source object
         */
        protected void copyAbstractValues(TestResult rhs){
            m_whichTest = rhs.m_whichTest;
            m_compilerConfig.copyFrom(rhs.m_compilerConfig);
            m_iNTests = rhs.m_iNTests;
            m_TestNumber = rhs.m_TestNumber;
        }

        /**
         * get a new instance of the same child type
         * @return new instance of same child type
         */
        public abstract TestResult getNewInstance();
        /**
         * get a new instance of the same child type and copy values
         * @return new instance of same child type
         */
        public abstract TestResult makeCopy();

        // aggregate functions to be implemented in children
        /**
         * Aggregate the given test results to the test results in this instance. In the implementation,
         * aggregateAbstractValues(rhs) must be called.
         * @param rhs test results to be aggregated to this instance.
         */
        public abstract void aggregateValues(TestResult rhs);

        /**
         * aggregate abstract test attributes
         * @param rhs input to be aggregated
         */
        protected void aggregateAbstractValues(TestResult rhs){
            m_iNTests += rhs.m_iNTests;
        }

        /**
         * Compare two TestResults on whichTest and compilerConfig. Accepts null input.
         * @param o1 first TestResult to be compared
         * @param o2 second TestResult to be compared
         * @return first smaller than second: -1, equal 0, first greater than second: 1
         */
        public static int staticCompare(TestResult o1, TestResult o2, boolean alsoCompareTestNumber){
            // check object validity
            if ((o1 == null) && (o2 == null)) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            // two valid TestResult objects
            // ----------------------------
            // check tests
            if (o1.m_whichTest == o2.m_whichTest) {
                if(alsoCompareTestNumber && o1.m_compilerConfig.equals(o2.m_compilerConfig))
                    return o1.m_TestNumber - o2.m_TestNumber;
                // compilerConfig is never null, as it is initialized as a final new object during creation
                int r = o1.m_compilerConfig.compareTo(o2.m_compilerConfig);
                if (r != 0){
                    return r;
                }

                // check class names
                r = o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
                return r;
            }
            if (o1.m_whichTest == null) {
                return -1;
            }
            if (o2.m_whichTest == null) {
                return 1;
            }
            return (o1.m_whichTest.compareTo(o2.m_whichTest));
        }

        /**
         * Compare two TestResults on whichTest and compilerConfig. Accepts null input.
         * @param o1 first TestResult to be compared
         * @param o2 second TestResult to be compared
         * @return first smaller than second: -1, equal 0, first greater than second: 1
         */
        @Override
        public int compare(TestResult o1, TestResult o2) {
            return staticCompare(o1, o2, false);
        }

        /**
         * Compare this TestResult with another on whichTest and compilerConfig. Accepts null input.
         * @param o second TestResult to be compared
         * @return this smaller than other: -1, equal 0, this greater than other: 1
         */
        @Override
        public int compareTo(@NotNull IAssessor.TestResult o) {
            return compare(this, o);
        }

        /**
         * Compare two objects
         * @param obj   the reference object with which to compare.
         * @return true if, and only if, other object is a TestResult with an equal test and equal compiler config
         */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TestResult)){
                return false;
            }
            return (compareTo((TestResult) obj) == 0);
        }

        @Override
        public String toString() {
            String out = "";
            try {
                out = this.m_whichTest.toString() + "|" + this.m_compilerConfig +
                        dblGetLowBound() + "/" + dblGetActualValue() + "/" + dblGetHighBound() + "/" + strGetPercentage() + "|" +
                        "N=" + m_iNTests + "|";
            }
            catch (Throwable ignore) {}
            return out;
        }

        /**
         * get a string representing the percentage of the possible score
         * @return formatted string
         */
        public String strGetPercentage() {
            return Misc.strGetPercentage(dblGetLowBound(), dblGetActualValue(), dblGetHighBound(), dblGetTarget());
        }

        /**
         * get the fractionated result; return 0 if high=low
         * @return (actual-low) / (high/low)
         */
        public Double dblGetFraction(){
            return Misc.dblGetFraction(dblGetLowBound(), dblGetActualValue(), dblGetHighBound(), dblGetTarget());
        }

        /**
         * aggregate values in a list. The list is sorted by test parameters. Whenever multiple results
         * from the same test parameters (test, arch, comp, opt) is found, these results are added to each other.
         * The original data is not tainted; it's all deep copied.
         * @param input list of the actual test results
         * @return aggregated (and sorted) list
         */
        public static List<TestResult> aggregate(final List<TestResult> input){
            // declare output
            var outList = new ArrayList<TestResult>();
            // make sure input is valid
            if (input==null) {
                return outList;
            }
            if (input.isEmpty()) {
                return outList;
            }

            // to leave the original list alone, we make a copy and sort that
            var tmpList = new ArrayList<>(input);
            tmpList.sort(new TestResultComparatorWithoutTestNumber());
            // the first item must be copied anyway
            outList.add(tmpList.get(0).makeCopy());
            int p_in=1;
            var current_out = outList.get(0);
            var currentTestNumber = 0;
            // loop for all next items
            while (p_in<tmpList.size()) {
                var current_in  = tmpList.get(p_in);
                // check whether the same test parameters were used
                if (current_out.equals(current_in)){
                    // same test parameters: aggregate
                    current_out.aggregateValues(current_in);
                    var fraction = current_out.dblGetFraction();
                    if(current_in.m_TestNumber != currentTestNumber) {
                        current_out.getScoresPerTest().add(fraction == null ? 0.0 : fraction);
                        currentTestNumber = current_in.m_TestNumber;
                    }
                }
                else{
                    //Calculate standard deviation
                    if(current_out.getScoresPerTest().size() > 0) {
                        current_out.setStandardDeviation(Misc.calculateStandardDeviation(current_out.getScoresPerTest()));
                    }
                    // different parameters: copy
                    current_out = current_in.makeCopy();
                    outList.add(current_out);
                }
                p_in++;
            }
            return outList;
        }

        /**
         * Return an aggregated list that distinguishes only test and architecture
         * @param input  list of test results; not changed
         * @return the promised list
         */
        public static List<TestResult> aggregateOverArchitecture(final List<TestResult> input){
            return aggregateOverCertainCategories(input, new AggregateOverArchitecture());
        }

        /**
         * Return an aggregated list that distinguishes only test and compiler
         * @param input  list of test results; not changed
         * @return the promised list
         */
        public static List<TestResult> aggregateOverCompiler(final List<TestResult> input){
            return aggregateOverCertainCategories(input, new AggregateOverCompiler());
        }

        /**
         * Return an aggregated list that distinguishes only test and optimization
         * @param input  list of test results; not changed
         * @return the promised list
         */
        public static List<TestResult> aggregateOverOptimization(final List<TestResult> input){
            return aggregateOverCertainCategories(input, new AggregateOverOptimization());
        }

        /**
         * Return an aggregated list, with flexible aggregation, by allowing to pass a special object
         * @param input  list of test results; not changed
         * @param aggregator object that can change test parameters; setting one or more to null will result in aggregation
         * @return the promised list
         */
        public static List<TestResult> aggregateOverCertainCategories(final List<TestResult> input, IAggregateWhat aggregator){
            var tempList = new ArrayList<TestResult>(input.size());
            for (var item : input){
                var t = item.makeCopy();
                aggregator.AdaptTestResult(t);
                tempList.add(t);
            }
            return aggregate(tempList);
        }

        public void setTestNumber(int testNumber) {
            this.m_TestNumber = testNumber;
        }

        public double dblGetStandardDeviation() {
            return m_dblStandardDeviation;
        }

        public void setStandardDeviation(double standardDeviation) {
            this.m_dblStandardDeviation = standardDeviation;
        }

        public List<Double> getScoresPerTest() {
            return m_scoresPerTest;
        }
    }

    /**
     * Implementation class for test results. This class is used for results that use simple counting, for
     * example: we count the number of loops in de decoded c-code in relation to the number of loops
     * introduced in the original code.<br>
     * As we only count, we use longs instead of doubles. Target can be high bound or low bound,
     * depending on the tsargetmode
     *
     */
    class CountTestResult extends TestResult{
        public enum ETargetMode{
            HIGHBOUND, LOWBOUND;
        }

        /** low bound for the metric */
        protected long m_lngLowBound=0;
        /** actual value for the metric */
        protected long m_lngActualValue = 0;
        /** high bound for the metric */
        protected long m_lngHighBound = 0;
        /** target mode, by default: high bound */
        protected ETargetMode m_targetMode = ETargetMode.HIGHBOUND;

        public CountTestResult(){        }
        public CountTestResult(CountTestResult rhs){
            copyFrom(rhs);
        }
        public CountTestResult(ETestCategories whichTest){
            m_whichTest = whichTest;
        }
        public CountTestResult(long lngLowBound, long lngActualValue, long lngHighBound) {
            m_lngLowBound = lngLowBound;
            m_lngActualValue = lngActualValue;
            m_lngHighBound = lngHighBound;
        }

        public CountTestResult(ETestCategories whichTest, CompilerConfig compilerConfig,
                               long lngLowBound, long lngActualValue, long lngHighBound) {
            m_whichTest = whichTest;
            m_compilerConfig.copyFrom(compilerConfig);
            m_lngLowBound = lngLowBound;
            m_lngActualValue = lngActualValue;
            m_lngHighBound = lngHighBound;
        }

        public CountTestResult(ETestCategories whichTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize) {
            m_whichTest = whichTest;
            m_compilerConfig.architecture = architecture;
            m_compilerConfig.compiler = compiler;
            m_compilerConfig.optimization = optimize;
        }

        public CountTestResult(ETestCategories whichTest, CompilerConfig config) {
            m_whichTest = whichTest;
            m_compilerConfig.copyFrom(config);
        }

        public CountTestResult(ETestCategories whichTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize,
                               long lngLowBound, long lngActualValue, long lngHighBound) {
            m_whichTest = whichTest;
            m_compilerConfig.architecture = architecture;
            m_compilerConfig.compiler = compiler;
            m_compilerConfig.optimization = optimize;
            m_lngLowBound = lngLowBound;
            m_lngActualValue = lngActualValue;
            m_lngHighBound = lngHighBound;
        }

        public void copyFrom(TestResult rhs){
            assert rhs instanceof CountTestResult;
            var rhss = (CountTestResult) rhs;
            super.copyAbstractValues(rhs);
            m_lngLowBound = rhss.m_lngLowBound;
            m_lngActualValue = rhss.m_lngActualValue;
            m_lngHighBound = rhss.m_lngHighBound;
            m_targetMode = rhss.m_targetMode;
        }

        @Override
        public Double dblGetLowBound() {
            return (double)m_lngLowBound;
        }

        @Override
        public Double dblGetActualValue() {
            return (double)m_lngActualValue;
        }

        @Override
        public Double dblGetHighBound() {
            return (double)m_lngHighBound;
        }

        @Override
        public Double dblGetTarget() {
            if (m_targetMode == ETargetMode.HIGHBOUND) {
                return (double) m_lngHighBound;
            }
            else {
                return (double) m_lngLowBound;
            }
        }

        public void setLowBound(long lngLowBound){
            m_lngLowBound = lngLowBound;
        }
        public void setActualValue(long lngActualValue){
            m_lngActualValue=lngActualValue;
        }
        public void setHighBound(long lngHighBound){
            m_lngHighBound=lngHighBound;
        }
        public void setTargetMode(ETargetMode mode){
            m_targetMode = mode;
        }
        public ETargetMode getTargetMode(){ return m_targetMode; }

        public void increaseActualValue(){
            m_lngActualValue++;
        }
        public void increaseHighBound(){
            m_lngHighBound++;
        }

        @Override
        public int iGetNumberOfDecimalsToBePrinted() {
            return 0;
        }

        @Override
        public TestResult getNewInstance() {
            return new CountTestResult();
        }

        @Override
        public TestResult makeCopy() {
            return new CountTestResult(this);
        }

        @Override
        public void aggregateValues(TestResult rhs) {
            assert rhs instanceof CountTestResult : "aggregating different types of test results";
            super.aggregateAbstractValues(rhs);
            var rh = (CountTestResult) rhs;
            assert rh.m_targetMode==this.m_targetMode : "aggregating different target modes";
            m_lngLowBound += rh.m_lngLowBound;
            m_lngActualValue += rh.m_lngActualValue;
            m_lngHighBound += rh.m_lngHighBound;
        }
    }

    /**
     * Comparator class for SingleTestResults, sorting only on test parameters (test/arch/comp/opt)
     */
    class TestResultComparatorWithTestNumber implements Comparator<TestResult>{
        @Override
        public int compare(TestResult o1, TestResult o2) {
            return TestResult.staticCompare(o1, o2, true);
        }
    }

    /**
     * Comparator class for SingleTestResults, sorting only on test parameters (test/arch/comp/opt)
     */
    class TestResultComparatorWithoutTestNumber implements Comparator<TestResult>{
        @Override
        public int compare(TestResult o1, TestResult o2) {
            return TestResult.staticCompare(o1, o2, false);
        }
    }

    /**
     * Interface to make it easier to aggregate in many different ways
     */
    interface IAggregateWhat{
        void AdaptTestResult(TestResult s);
    }

    class AggregateOverArchitecture implements IAggregateWhat{
        @Override
        public void AdaptTestResult(TestResult s) {
            s.m_compilerConfig.compiler = null;
            s.m_compilerConfig.optimization = null;
        }
    }

    class AggregateOverCompiler implements IAggregateWhat{
        @Override
        public void AdaptTestResult(TestResult s) {
            s.m_compilerConfig.architecture = null;
            s.m_compilerConfig.optimization = null;
        }
    }

    class AggregateOverOptimization implements IAggregateWhat{
        @Override
        public void AdaptTestResult(TestResult s) {
            s.m_compilerConfig.architecture = null;
            s.m_compilerConfig.compiler = null;
        }
    }

    /**
     * Class (struct) to hand over ANTLR info easily; it stores three lexers and
     * three parsers, one each for the original C, the original LLVM and the decompiled C.
     */
    class CodeInfo {
        /** lexer of decompiled C */                    public CLexer clexer_dec;
        /** parser of decompiled C */                   public CParser cparser_dec;
        /** lexer of original C */                      public CLexer clexer_org;
        /**parser of original C */                      public CParser cparser_org;
        /** lexer of original LLVM-IR */                public LLVMIRLexer llexer_org;
        /** parser of original LLVM-IR */               public LLVMIRParser lparser_org;
        /** compiler, optimization. architecture */     final public CompilerConfig compilerConfig = new CompilerConfig();
        /** name of decompiled file */                  public String strDecompiledCFilename;
    }

    /**
     * The main entry point for the feature-assessor-implementation
     * @param ci all the necessary data on the presented binary and source
     * @return  the test results
     */
    List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci);
}
