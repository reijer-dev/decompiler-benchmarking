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
        public double dlbLowBound = 0;
        public double dblHighBound = 100;
        public double dblActualValue = 0;
    }

    /**
     * Class (struct) to hand over ANTLR info easily; it stores two lexers and
     * two parsers, for both the (decompiled) c-file and the (decompiled) llvm-file
     */
    public class Codeinfo {
        public CLexer clexer_dec;
        public CParser cparser_dec;
        public CLexer clexer_org;
        public CParser cparser_org;
        public LLVMIRLexer llexer_dec;
        public LLVMIRParser lparser_dec;
        public LLVMIRLexer llexer_org;
        public LLVMIRParser lparser_org;
    }

    SingleTestResult GetSingleTestResult(Codeinfo ci);
}
