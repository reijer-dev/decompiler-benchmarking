package nl.ou.debm.common;

import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.LLVMIRLexer;
import nl.ou.debm.common.antlr.LLVMIRParser;

/**
 * Interface to be implemented by every feature class, in order to ensure
 * that the main assessor loop can invoke an assessment session
 */
public interface IAssessor {
    /**
     * Class (struct) to store one single test result, yielding a value and the
     * upper and lower bounds the value may have.
     */
    public class SingleTestResult{
        public SingleTestResult(){}
        public SingleTestResult(boolean skipped){
            this.skipped = skipped;
        }
        public boolean skipped = false;
        public double dlbLowBound = 0;          // lowest possible test value
        public double dblHighBound = 100;       // highest possible test value
        public double dblActualValue = 0;       // found test value
        public String strTestedItem = "Item description";   // what was tested?
        public String strTestUnit = "units found";          // what unit was tested?
    }

    /**
     * Class (struct) to hand over ANTLR info easily; it stores three lexers and
     * three parsers, one each for the original C, the original LLVM and the decompiled C.
     */
    public class CodeInfo {
        public CLexer clexer_dec;           // lexer of decompiled C
        public CParser cparser_dec;         // parser of decompiled C
        public CLexer clexer_org;           // lexer of original C
        public CParser cparser_org;         // parser of original C
        public LLVMIRLexer llexer_org;      // lexer of original LLVM-IR
        public LLVMIRParser lparser_org;    // parser of original LLVM-IR
        public EArchitecture architecture;  // current architecture
        public EOptimize optimizationLevel; // optimization level
    }

    SingleTestResult GetSingleTestResult(CodeInfo ci);
}
