package nl.ou.debm.common.feature2;

import nl.ou.debm.common.CodeMarker;

public class Testcase {
    enum Status {
        ok, variableNotFound, unparseableType
    }
    Status status;
    CodeMarker codemarker;
    NameInfo.VariableInfo varInfo;
}