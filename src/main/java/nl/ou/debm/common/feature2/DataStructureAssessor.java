package nl.ou.debm.common.feature2;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.feature3.Feature3CVisitor;

import java.util.ArrayList;
import java.util.List;

public class DataStructureAssessor implements IAssessor {
    @Override
    public List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci) {
        var ret = new ArrayList<TestResult>();
        //todo test
        var test = new CountTestResult(0, 777, 1234);
        test.setWhichTest(ETestCategories.FEATURE2_BLABLA);
        test.setCompilerConfig(ci.compilerConfig);
        ret.add(test);

        //todo test with c parser
        System.out.println("hij gaat visiteren");
        //var visitor = new DataStructureCVisitor();
        var visitor = new DataStructureCVisitor();
        visitor.visit(ci.cparser_dec.compilationUnit());
        System.out.println("klaar met visiteren");

        //var test2 = new CountTestResult(0, visitor.codemarkersFound, 1000);
        var test2 = new CountTestResult(0, 100, 1000);
        test2.setWhichTest(ETestCategories.FEATURE2_CODEMARKERS);
        test2.setCompilerConfig(ci.compilerConfig);
        ret.add(test2);

        return ret;
    }
}
