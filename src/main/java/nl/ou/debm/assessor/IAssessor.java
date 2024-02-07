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
     * Class (struct) to store one single test (test conditions + result (a value and the
     * upper and lower bounds the value may have)).
     */
    abstract class TestResult implements Comparable<TestResult>, Comparator<TestResult> {
        // private attributes
        protected ETestCategories m_whichTest;                                    // which test
        protected final CompilerConfig m_compilerConfig = new CompilerConfig();   // which compiler configuration
        protected boolean m_bTestSkipped = false;                                 // test skipped?
        protected int m_iNTests = 1;                                              // number of tests involved

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
        public void setSkippedValue(boolean bSkipped){
            m_bTestSkipped=bSkipped;
        }
        public void setTestSkippedFlag(){
            setSkippedValue(true);
        }
        public boolean getSkipped(){
            return m_bTestSkipped;
        }
        public int iGetNumberOfTests(){
            return m_iNTests;
        }

        // access functions to be implemented in children
        public abstract double dblGetLowBound();
        public abstract double dblGetActualValue();
        public abstract double dblGetHighBound();
        public abstract double dblGetTarget();
        public abstract int iGetNumberOfDecimalsToBePrinted();

        // copy mechanism
        protected void copyFrom(TestResult rhs){
            m_whichTest = rhs.m_whichTest;
            m_compilerConfig.copyFrom(rhs.m_compilerConfig);
            m_bTestSkipped = rhs.m_bTestSkipped;
            m_iNTests = rhs.m_iNTests;
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
        protected void aggregateValues(TestResult rhs){
            m_iNTests += rhs.m_iNTests;
        }

        /**
         * Compare two TestResults on whichTest and compilerConfig. Accepts null input.
         * @param o1 first TestResult to be compared
         * @param o2 second TestResult to be compared
         * @return first smaller than second: -1, equal 0, first greater than second: 1
         */
        public static int staticCompare(TestResult o1, TestResult o2){
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
            // check class names
            int r = o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
            if (r!=0) {
                return r;
            }
            // check tests
            if (o1.m_whichTest == o2.m_whichTest) {
                // compilerConfig is never null, as it is initialized as a final new object during creation
                return o1.m_compilerConfig.compareTo(o2.m_compilerConfig);
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
            return staticCompare(o1, o2);
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
            return this.m_whichTest.toString() + "|" + this.m_compilerConfig +
                    dblGetLowBound() + "/" + dblGetActualValue() + "/" + dblGetHighBound() + "/" + strGetPercentage() + "|" +
                    "N=" + m_iNTests + "|";
        }

        /**
         * get a string representing the percentage of the possible score
         * @return formatted string
         */
        public String strGetPercentage() {
            return Misc.strGetPercentage(dblGetLowBound(), dblGetActualValue(), dblGetHighBound());
        }

        /**
         * get the fractioned result; return 0 if high=low
         * @return (actual-low) / (high/low)
         */
        public double dblGetFraction(){
            return Misc.dblGetFraction(dblGetLowBound(), dblGetActualValue(), dblGetHighBound());
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
            if (input!=null) {
                if (!input.isEmpty()) {
                    // to leave the original list alone, we make a copy and sort that
                    var tmpList = new ArrayList<>(input);
                    tmpList.sort(new SingleTestResultComparator());
                    // the first item must be copied anyway
                    outList.add(tmpList.get(0).makeCopy());
                    int p_in=1;
                    var current_out = outList.get(0);
                    // loop for all next items
                    while (p_in<tmpList.size()) {
                        var current_in  = tmpList.get(p_in);
                        // check whether the same test parameters were used
                        if (current_out.equals(current_in)){
                            // same test parameters: aggregate
                            current_out.aggregateValues(current_in);
                        }
                        else{
                            // different parameters: copy
                            current_out = current_in.makeCopy();
                            outList.add(current_out);
                        }
                        p_in++;
                    }
                }
            }
            return outList;
        }

        /**
         * Return an aggregated list that distinguish only test and architecture
         * @param input  list of test results; not changed
         * @return the promised list
         */
        public static List<TestResult> aggregateOverArchitecture(final List<TestResult> input){
            return aggregateOverCertainCategories(input, new AggregateOverArchitecture());
        }

        /**
         * Return an aggregated list that distinguish only test and compiler
         * @param input  list of test results; not changed
         * @return the promised list
         */
        public static List<TestResult> aggregateOverCompiler(final List<TestResult> input){
            return aggregateOverCertainCategories(input, new AggregateOverCompiler());
        }

        /**
         * Return an aggregated list that distinguish only test and optimization
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
                aggregator.AdaptSingleTestResult(t);
                tempList.add(t);
            }
            return aggregate(tempList);
        }
    }

    class CountTestResult extends TestResult{

        private double m_dblLowBound = 0;
        private double m_dblActualValue = 0;
        private double m_dblHighBound = 0;


        public CountTestResult(){
        }
        public CountTestResult(CountTestResult rhs){
            copyFrom(rhs);
        }
        public CountTestResult(double dblLowBound, double dblActualValue, double dblHighBound) {
            m_dblLowBound = dblLowBound;
            m_dblActualValue = dblActualValue;
            m_dblHighBound = dblHighBound;
        }

        public CountTestResult(ETestCategories whichTest, CompilerConfig compilerConfig) {
            m_whichTest = whichTest;
            m_compilerConfig.copyFrom(compilerConfig);
        }
    
        public CountTestResult(ETestCategories whichTest, CompilerConfig compilerConfig, boolean bSkipped) {
            m_whichTest = whichTest;
            m_compilerConfig.copyFrom(compilerConfig);
            m_bTestSkipped=bSkipped;
        }
    
        public CountTestResult(ETestCategories whichTest, CompilerConfig compilerConfig,
                          double dblLowBound, double dblActualValue, double dblHighBound) {
            m_whichTest = whichTest;
            m_compilerConfig.copyFrom(compilerConfig);
            m_dblLowBound = dblLowBound;
            m_dblActualValue = dblActualValue;
            m_dblHighBound = dblHighBound;
        }
    
        public CountTestResult(ETestCategories whichTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize) {
            m_whichTest = whichTest;
            m_compilerConfig.architecture = architecture;
            m_compilerConfig.compiler = compiler;
            m_compilerConfig.optimization = optimize;
        }
    
        public CountTestResult(ETestCategories whichTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize,
                          double dblLowBound, double dblActualValue, double dblHighBound) {
            m_whichTest = whichTest;
            m_compilerConfig.architecture = architecture;
            m_compilerConfig.compiler = compiler;
            m_compilerConfig.optimization = optimize;
            m_dblLowBound = dblLowBound;
            m_dblActualValue = dblActualValue;
            m_dblHighBound = dblHighBound;
        }

        public void copyFrom(CountTestResult rhs){
            super.copyFrom(rhs);
            m_dblLowBound = rhs.m_dblLowBound;
            m_dblActualValue = rhs.m_dblActualValue;
            m_dblHighBound = rhs.m_dblHighBound;
        }

        @Override
        public double dblGetLowBound() {
            return m_dblLowBound;
        }

        @Override
        public double dblGetActualValue() {
            return m_dblActualValue;
        }

        @Override
        public double dblGetHighBound() {
            return m_dblHighBound;
        }

        @Override
        public double dblGetTarget() {
            return m_dblHighBound;
        }

        public void setLowBound(double dblLowBound){
            m_dblLowBound=dblLowBound;
        }
        public void setActualValue(double dblActualValue){
            m_dblActualValue=dblActualValue;
        }
        public void setHighBound(double dblHighBound){
            m_dblHighBound=dblHighBound;
        }

        public void increaseLowBound(){
            m_dblLowBound++;
        }
        public void increaseActualValue(){
            m_dblActualValue++;
        }
        public void increaseHighBound(){
            m_dblHighBound++;
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
            if (rhs instanceof CountTestResult rh){
                super.aggregateValues(rhs);
                m_dblLowBound += rh.m_dblLowBound;
                m_dblActualValue += rh.m_dblActualValue;
                m_dblHighBound += rh.m_dblHighBound;
            }
        }
    }

    /**
     * Comparator class for SingleTestResults, sorting only on test parameters (test/arch/comp/opt)
     */
    class SingleTestResultComparator implements Comparator<TestResult>{
        @Override
        public int compare(TestResult o1, TestResult o2) {
            return TestResult.staticCompare(o1, o2);
        }
    }

    /**
     * Interface to make it easier to aggregate in many different ways
     */
    interface IAggregateWhat{
        void AdaptSingleTestResult(TestResult s);
    }

    class AggregateOverArchitecture implements IAggregateWhat{
        @Override
        public void AdaptSingleTestResult(TestResult s) {
            s.m_compilerConfig.compiler = null;
            s.m_compilerConfig.optimization = null;
        }
    }

    class AggregateOverCompiler implements IAggregateWhat{
        @Override
        public void AdaptSingleTestResult(TestResult s) {
            s.m_compilerConfig.architecture = null;
            s.m_compilerConfig.optimization = null;
        }
    }

    class AggregateOverOptimization implements IAggregateWhat{
        @Override
        public void AdaptSingleTestResult(TestResult s) {
            s.m_compilerConfig.architecture = null;
            s.m_compilerConfig.compiler = null;
        }
    }

    /**
     * Class (struct) to hand over ANTLR info easily; it stores three lexers and
     * three parsers, one each for the original C, the original LLVM and the decompiled C.
     */
    class CodeInfo {
        public CLexer clexer_dec;           // lexer of decompiled C
        public CParser cparser_dec;         // parser of decompiled C
        public CLexer clexer_org;           // lexer of original C
        public CParser cparser_org;         // parser of original C
        public LLVMIRLexer llexer_org;      // lexer of original LLVM-IR
        public LLVMIRParser lparser_org;    // parser of original LLVM-IR
        final public CompilerConfig compilerConfig = new CompilerConfig();   // compiler, optimization. architecture
    }

    /**
     * The main entry point for the feature-assessor-implementation
     * @param ci all the necessary data on the presented binary and source
     * @return  the test results
     */
    List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci);
}
