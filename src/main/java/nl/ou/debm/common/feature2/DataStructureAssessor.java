package nl.ou.debm.common.feature2;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;

import java.util.ArrayList;
import java.util.List;

public class DataStructureAssessor implements IAssessor {
    @Override
    public List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci) {
        //ci.cparser_dec.functionDefinition()
        var test = new CountTestResult(0, 777, 1234);
        test.setWhichTest(ETestCategories.FEATURE2_BLABLA);
        test.setCompilerConfig(ci.compilerConfig);
        var ret = new ArrayList<TestResult>();
        ret.add(test);
        return ret;
    }
}
