package nl.ou.debm.common.feature5;

import nl.ou.debm.assessor.IAssessor;

import java.util.List;

public class IndirectionsAssessor implements IAssessor {
    @Override
    public List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci) {

        var llvmVisitor = new IndirectionsLLVMVisitor();
        //Outcommented for speed
        //llvmVisitor.visit(ci.lparser_org.compilationUnit());
        new AssemblySwitchParser().setIndirectionInfo(llvmVisitor.getSwitches(), ci);
        return null;
    }
}
