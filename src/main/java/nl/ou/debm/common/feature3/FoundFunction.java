package nl.ou.debm.common.feature3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FoundFunction {
    private String name;
    private boolean isVariadic;
    private List<FunctionCodeMarker> containingMarkers = new ArrayList<>();
    private HashMap<String, Integer> calledFromFunctions = new HashMap<>();
    private int numberOfStatements;
    private int numberOfReturnStatements;
    private int numberOfPrologueStatements;
    private int numberOfEpilogueStatements;
    private int variableDeclarationsBeforeStartMarker;
    private int registerHomingBeforeStartMarker;
    public void addMarker(FunctionCodeMarker marker){
        containingMarkers.add(marker);
    }
    public List<FunctionCodeMarker> getMarkers(){ return containingMarkers; }
    public void addCalledFromFunction(String functionName){

        if(!calledFromFunctions.containsKey(functionName))
            calledFromFunctions.put(functionName, 1);
        else
            calledFromFunctions.put(functionName, calledFromFunctions.get(functionName)+1);
    }

    public HashMap<String, Integer> getCalledFromFunctions() {
        return calledFromFunctions;
    }

    public String getName(){ return name; }
    public void setName(String name) { this.name = name; }

    public boolean isVariadic() {
        return isVariadic;
    }

    public void setIsVariadic(boolean isVariadic) {
        this.isVariadic = isVariadic;
    }

    public int getNumberOfStatements() {
        return numberOfStatements;
    }

    public void setNumberOfStatements(int numberOfStatements) {
        this.numberOfStatements = numberOfStatements;
    }

    public int getNumberOfPrologueStatements() {
        return numberOfPrologueStatements;
    }

    public void setNumberOfPrologueStatements(int numberOfPrologueStatements) {
        this.numberOfPrologueStatements = numberOfPrologueStatements;
    }

    public int getNumberOfEpilogueStatements() {
        return numberOfEpilogueStatements;
    }

    public void setNumberOfEpilogueStatements(int numberOfEpilogueStatements) {
        this.numberOfEpilogueStatements = numberOfEpilogueStatements;
    }

    public int getNumberOfReturnStatements() {
        return numberOfReturnStatements;
    }

    public void registerReturnStatement() {
        this.numberOfReturnStatements++;
    }

    public int getVariableDeclarationsBeforeStartMarker() {
        return variableDeclarationsBeforeStartMarker;
    }

    public void setVariableDeclarationsBeforeStartMarker(int variableDeclarationsBeforeStartMarker) {
        this.variableDeclarationsBeforeStartMarker = variableDeclarationsBeforeStartMarker;
    }

    public int getRegisterHomingBeforeStartMarker() {
        return registerHomingBeforeStartMarker;
    }

    public void setRegisterHomingBeforeStartMarker(int registerHomingBeforeStartMarker) {
        this.registerHomingBeforeStartMarker = registerHomingBeforeStartMarker;
    }
}
