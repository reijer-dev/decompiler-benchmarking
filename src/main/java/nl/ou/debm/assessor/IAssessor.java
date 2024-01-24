package nl.ou.debm.assessor;

import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.LLVMIRLexer;
import nl.ou.debm.common.antlr.LLVMIRParser;

import java.util.Map;

/**
 * Interface to be implemented by every feature class, in order to ensure
 * that the main assessor loop can invoke an assessment session
 */
public interface IAssessor {
    public class TestParameters{
        public TestParameters(){}
        public TestParameters(ETestCategories whichTest, CompilerConfig compilerConfig){
            this.whichTest = whichTest;
            this.compilerConfig.copyFrom(compilerConfig);
        }
        public ETestCategories whichTest;
        public final CompilerConfig compilerConfig = new CompilerConfig();

        @Override
        public boolean equals(Object rhs) {
            if (!(rhs instanceof TestParameters other)){
                return false;
            }
            return (whichTest == other.whichTest) &&
                   (compilerConfig.equals(other.compilerConfig));
        }
    }

    /**
     * Class (struct) to store one single test result, yielding a value and the
     * upper and lower bounds the value may have.
     */
    class SingleTestResult{
        public SingleTestResult(){}
        public SingleTestResult(boolean skipped){
            this.skipped = skipped;
        }
        final public TestParameters testParameters = new TestParameters();
        public boolean skipped = false;
        public double dblLowBound = 0;          // lowest possible test value
        public double dblHighBound = 0;       // highest possible test value
        public double dblActualValue = 0;       // found test value
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

    interface IAggregateKeys{
        TestParameters oldKeyToNewKey(TestParameters key);
    }

    Map<TestParameters, SingleTestResult> GetTestResultsForSingleBinary(CodeInfo ci);
}
