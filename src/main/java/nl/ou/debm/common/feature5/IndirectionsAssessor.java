package nl.ou.debm.common.feature5;

import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.feature3.Feature3CVisitor;

import java.util.HashMap;
import java.util.List;

public class IndirectionsAssessor implements IAssessor {
    HashMap<CParser, Feature3CVisitor> cachedSourceVisitors = new HashMap<>();
    @Override
    public List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci) {
        return null;
    }
}
