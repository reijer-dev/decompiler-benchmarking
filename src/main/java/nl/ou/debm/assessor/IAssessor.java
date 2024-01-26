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
    class SingleTestResult implements Comparable<SingleTestResult>, Comparator<SingleTestResult> {
        // public attributes, as it is basically a struct
        public ETestCategories whichTest;                                   // which test
        public final CompilerConfig compilerConfig = new CompilerConfig();  // which compiler configuration
        public boolean skipped = false;                                     // test skipped?
        public double dblLowBound = 0;                                      // lowest possible test value
        public double dblHighBound = 0;                                     // highest possible test value
        public double dblActualValue = 0;                                   // found test value

        // construction
        public SingleTestResult() {
        }

        public SingleTestResult(SingleTestResult rhs){
            this.whichTest = rhs.whichTest;
            this.compilerConfig.copyFrom(rhs.compilerConfig);
            this.dblLowBound = rhs.dblLowBound;
            this.dblActualValue = rhs.dblActualValue;
            this.dblHighBound = rhs.dblHighBound;
            this.skipped = rhs.skipped;
        }

        public SingleTestResult(boolean skipped) {
            this.skipped = skipped;
        }

        public SingleTestResult(double dblLowBound, double dblActualValue, double dblHighBound) {
            this.dblLowBound = dblLowBound;
            this.dblActualValue = dblActualValue;
            this.dblHighBound = dblHighBound;
        }

        public SingleTestResult(ETestCategories whichTest, CompilerConfig compilerConfig) {
            this.whichTest = whichTest;
            this.compilerConfig.copyFrom(compilerConfig);
        }

        public SingleTestResult(ETestCategories whichTest, CompilerConfig compilerConfig, boolean bSkipped) {
            this.whichTest = whichTest;
            this.compilerConfig.copyFrom(compilerConfig);
            this.skipped=bSkipped;
        }

        public SingleTestResult(ETestCategories whichTest, CompilerConfig compilerConfig,
                                double dblLowBound, double dblActualValue, double dblHighBound) {
            this.whichTest = whichTest;
            this.compilerConfig.copyFrom(compilerConfig);
            this.dblLowBound = dblLowBound;
            this.dblActualValue = dblActualValue;
            this.dblHighBound = dblHighBound;
        }

        public SingleTestResult(ETestCategories whichTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize) {
            this.whichTest = whichTest;
            this.compilerConfig.architecture = architecture;
            this.compilerConfig.compiler = compiler;
            this.compilerConfig.optimization = optimize;
        }

        public SingleTestResult(ETestCategories whichTest, EArchitecture architecture, ECompiler compiler, EOptimize optimize,
                                double dblLowBound, double dblActualValue, double dblHighBound) {
            this.whichTest = whichTest;
            this.compilerConfig.architecture = architecture;
            this.compilerConfig.compiler = compiler;
            this.compilerConfig.optimization = optimize;
            this.dblLowBound = dblLowBound;
            this.dblActualValue = dblActualValue;
            this.dblHighBound = dblHighBound;
        }

        // comparison methods
        public static int staticCompare(SingleTestResult o1, SingleTestResult o2){
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
            // two valid SingleTestResult objects
            if (o1.whichTest == o2.whichTest) {
                // compilerConfig is never null, as it is initialized as a final new object during creation
                return o1.compilerConfig.compareTo(o2.compilerConfig);
            }
            if (o1.whichTest == null) {
                return -1;
            }
            if (o2.whichTest == null) {
                return 1;
            }
            return (o1.whichTest.compareTo(o2.whichTest));
        }

        @Override
        public int compare(SingleTestResult o1, SingleTestResult o2) {
            return staticCompare(o1, o2);
        }

        @Override
        public int compareTo(@NotNull IAssessor.SingleTestResult o) {
            return compare(this, o);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SingleTestResult)){
                return false;
            }
            return (compareTo((SingleTestResult) obj) == 0);
        }

        @Override
        public String toString() {
            return this.whichTest.toString() + "|" + this.compilerConfig +
                    dblLowBound + "/" + dblActualValue + "/" + dblHighBound + "/" + strGetPercentage() + "|";
        }

        /**
         * get a string representing the percentage of the possible score
         * @return formatted string
         */
        public String strGetPercentage() {
            return Misc.strGetPercentage(dblLowBound, dblActualValue, dblHighBound);
        }

        /**
         * Aggregate the values of the input object to this object
         * @param rhs  object to be aggregated
         */
        public void addValues(SingleTestResult rhs){
            this.dblLowBound    += rhs.dblLowBound;
            this.dblActualValue += rhs.dblActualValue;
            this.dblHighBound   += rhs.dblHighBound;
        }

        /**
         * aggregate values in a list. The list is sorted by test parameters. Whenever multiple results
         * from the same test parameters (test, arch, comp, opt) is found, these results are added to each other.
         * The original data is not tainted; it's all deep copied.
         * @param input list of the actual test results
         * @return aggregated (and sorted) list
         */
        public static List<SingleTestResult> aggregate(final List<SingleTestResult> input){
            // declare output
            var outList = new ArrayList<SingleTestResult>();
            // make sure input is valid
            if (input!=null) {
                if (!input.isEmpty()) {
                    // to leave the original list alone, we make a copy and sort that
                    var tmpList = new ArrayList<>(input);
                    tmpList.sort(new SingleTestResultComparator());
                    // the first item must be copied anyway
                    outList.add(new SingleTestResult(tmpList.get(0)));
                    int p_in=1;
                    var current_out = outList.get(0);
                    // loop for all next items
                    while (p_in<tmpList.size()) {
                        var current_in  = tmpList.get(p_in);
                        // check whether the same test parameters were used
                        if (current_out.equals(current_in)){
                            // same test parameters: aggregate
                            current_out.addValues(current_in);
                        }
                        else{
                            // different parameters: copy
                            current_out = new SingleTestResult(current_in);
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
        public static List<SingleTestResult> aggregateOverArchitecture(final List<SingleTestResult> input){
            return aggregateOverCertainCategories(input, new AggregateOverArchitecture());
        }

        /**
         * Return an aggregated list that distinguish only test and compiler
         * @param input  list of test results; not changed
         * @return the promised list
         */
        public static List<SingleTestResult> aggregateOverCompiler(final List<SingleTestResult> input){
            return aggregateOverCertainCategories(input, new AggregateOverCompiler());
        }

        /**
         * Return an aggregated list that distinguish only test and optimization
         * @param input  list of test results; not changed
         * @return the promised list
         */
        public static List<SingleTestResult> aggregateOverOptimization(final List<SingleTestResult> input){
            return aggregateOverCertainCategories(input, new AggregateOverOptimization());
        }

        /**
         * Return an aggregated list, with flexible aggregation, by allowing to pass a special object
         * @param input  list of test results; not changed
         * @param aggregator object that can change test parameters; setting one or more to null will result in aggregation
         * @return the promised list
         */
        public static List<SingleTestResult> aggregateOverCertainCategories(final List<SingleTestResult> input, IAggregateWhat aggregator){
            var tempList = new ArrayList<SingleTestResult>(input.size());
            for (var item : input){
                var t = new SingleTestResult(item);
                aggregator.AdaptSingleTestResult(t);
                tempList.add(t);
            }
            return aggregate(tempList);
        }
    }

    /**
     * Comparator class for SingleTestResults, sorting only on test parameters (test/arch/comp/opt)
     */
    class SingleTestResultComparator implements Comparator<SingleTestResult>{
        @Override
        public int compare(SingleTestResult o1, SingleTestResult o2) {
            return SingleTestResult.staticCompare(o1, o2);
        }
    }

    /**
     * Interface to make it easier to aggregate in many different ways
     */
    interface IAggregateWhat{
        void AdaptSingleTestResult(SingleTestResult s);
    }

    class AggregateOverArchitecture implements IAggregateWhat{
        @Override
        public void AdaptSingleTestResult(SingleTestResult s) {
            s.compilerConfig.compiler = null;
            s.compilerConfig.optimization = null;
        }
    }

    class AggregateOverCompiler implements IAggregateWhat{
        @Override
        public void AdaptSingleTestResult(SingleTestResult s) {
            s.compilerConfig.architecture = null;
            s.compilerConfig.optimization = null;
        }
    }

    class AggregateOverOptimization implements IAggregateWhat{
        @Override
        public void AdaptSingleTestResult(SingleTestResult s) {
            s.compilerConfig.architecture = null;
            s.compilerConfig.compiler = null;
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
    List<SingleTestResult> GetTestResultsForSingleBinary(CodeInfo ci);
}
