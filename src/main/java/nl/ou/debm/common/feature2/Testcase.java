package nl.ou.debm.common.feature2;

public class Testcase {
    enum Status {
        ok, variableNotFound
    }
    Status status;
    DataStructureCodeMarker codemarker;
    String variableAddressExpr; //variable address expression created by the decompiler. This is the second argument of a DataStructureCodeMarker function call.
    NameInfo.VariableInfo varInfo;
}