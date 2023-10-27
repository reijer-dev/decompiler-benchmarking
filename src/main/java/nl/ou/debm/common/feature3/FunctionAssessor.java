package nl.ou.debm.common.feature3;

import nl.ou.debm.common.IAssessor;

import java.util.HashMap;

public class FunctionAssessor implements IAssessor{

    @Override
    public SingleTestResult GetSingleTestResult(Codeinfo ci) {
        //Gather original information
        var originalCVisitor = new CVisitor();
        originalCVisitor.visit(ci.cparser_org.compilationUnit());

        var originalLlvmVisitor = new LLVMVisitor();
        originalLlvmVisitor.visit(ci.lparser_org.compilationUnit());

        //Gather decompiled information
        var decompiledCVisitor = new CVisitor();
        decompiledCVisitor.visit(ci.cparser_dec.compilationUnit());

        //Find the difference
        return new SingleTestResult();
    }
}
