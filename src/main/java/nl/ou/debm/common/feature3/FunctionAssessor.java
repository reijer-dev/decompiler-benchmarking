package nl.ou.debm.common.feature3;

import nl.ou.debm.common.EOptimize;
import nl.ou.debm.common.IAssessor;
import nl.ou.debm.producer.EFeaturePrefix;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class FunctionAssessor implements IAssessor {

    @Override
    public SingleTestResult GetSingleTestResult(Codeinfo ci) {
        //We skip optimized code, because it confuses our function start and end markers
        if (ci.optimizationLevel == EOptimize.OPTIMIZE)
            return new SingleTestResult(true);

        var result = new SingleTestResult();
        //We increase this on every check, and increase dblActualValue on every check pass
        result.dblHighBound = 0;

        //Gather original information
        var sourceCVisitor = new CVisitor();
        sourceCVisitor.visit(ci.cparser_org.compilationUnit());

        //Gather decompiled information
        var decompiledCVisitor = new CVisitor();
        decompiledCVisitor.visit(ci.cparser_dec.compilationUnit());

        var testableSourceFunctions = sourceCVisitor.functions.values()
                .stream().filter(sourceFunction -> {
                    var functionName = sourceFunction.getName().substring(EFeaturePrefix.FUNCTIONFEATURE.toString().length() + 1);
                    //Skip imperfect functions
                    if (sourceFunction.getMarkers().size() != 2)
                        return false;

                    var startMarker = sourceFunction.getMarkers().get(0);
                    var endMarker = sourceFunction.getMarkers().get(1);
                    if (!sourceFunction.isMarkerAtStart(startMarker))
                        return false;
                    if (!sourceFunction.isMarkerAtEnd(endMarker))
                        return false;
                    if (!startMarker.getFunctionName().equals(functionName))
                        return false;
                    return true;
                }).toList();

        var decFunctionsNamesByStartMarkerName = new HashMap<String, String>();
        var startMarkerNamesByDecompiledFunctionName = new HashMap<String, String>();
        for(var decFunction : decompiledCVisitor.functions.values()){
            if(decFunction.getMarkers().size() == 0)
                continue;
            var startMarker = decFunction.getMarkers().get(0);
            decFunctionsNamesByStartMarkerName.put(startMarker.getFunctionName(), decFunction.getName());
            startMarkerNamesByDecompiledFunctionName.put(decFunction.getName(), startMarker.getFunctionName());
        }

        for (var sourceFunction : testableSourceFunctions) {
            AtomicReference<FoundFunction> decompiledFunction = new AtomicReference<>();
            AtomicReference<FunctionCodeMarker> decompiledMarker = new AtomicReference<>();
            var functionName = sourceFunction.getName().substring(EFeaturePrefix.FUNCTIONFEATURE.toString().length() + 1);
            var startMarker = sourceFunction.getMarkers().get(0);

            var builder = CheckChainBuilder
                    .Check("Find function " + sourceFunction.getName(), result, () -> {
                        //1. Find the decompiled marker
                        decompiledMarker.set(decompiledCVisitor.markersById.getOrDefault(startMarker.getID(), null));
                        return decompiledMarker.get() != null;
                    })
                    .WhenPassed("First marker should be at start", () -> {
                        decompiledFunction.set(decompiledCVisitor.functions.get(decompiledMarker.get().getFunctionId()));
                        //3. Check start marker
                        var decStartMarker = decompiledFunction.get().getMarkers().get(0);
                        if (decStartMarker.getFunctionName().equals(functionName)) {
                            if (decompiledFunction.get().isMarkerAtStart(decStartMarker)) {
                                System.out.println("Function start found at start!");
                                return true;
                            } else {
                                System.out.println("Function start found, but not at the start!");
                                return false;
                            }
                        }
                        return false;
                    })
                    .Then("Last marker should be at end", () -> {
                        //3. Check end marker
                        var decEndMarker = decompiledFunction.get().getMarkers().get(decompiledFunction.get().getMarkers().size() - 1);
                        if (decEndMarker.getFunctionName().equals(functionName)) {
                            if (decompiledFunction.get().isMarkerAtEnd(decEndMarker)) {
                                System.out.println("Function end found at end!");
                                return true;
                            } else {
                                System.out.println("Function end found, but not at the end!");
                                return false;
                            }
                        }
                        return false;
                    })
                    .Then("Only two markers allowed", () -> {
                        //3. Check for perfect match
                        if (decompiledFunction.get().getMarkers().size() == 2) {
                            return true;
                        } else {
                            System.out.println("Weird amount of markers!");
                            return false;
                        }
                    })
                    .Then("Checking whether function is variadic", () -> {
                        return sourceFunction.isVariadic() == decompiledFunction.get().isVariadic();
                    }).Then("Checking whether function is called the same amount of times", () -> {
                        var totalCalled = sourceFunction.getCalledFromFunctions().values().stream().reduce(0, Integer::sum);
                        var decompiledTotalCalled = decompiledFunction.get().getCalledFromFunctions().values().stream().reduce(0, Integer::sum);
                        return totalCalled.equals(decompiledTotalCalled);
                    }).Then("Checking function call sites", () -> {
                        var calledFromFunctions = sourceFunction.getCalledFromFunctions();
                        var decCalledFromFunctions = decompiledFunction.get().getCalledFromFunctions();
                        var totalDiff = 0;
                        for (var calledFromFunction: calledFromFunctions.entrySet()) {
                            var decFunctionName = decFunctionsNamesByStartMarkerName.getOrDefault(calledFromFunction.getKey().substring(3), null);
                            if(decFunctionName == null) {
                                totalDiff += calledFromFunction.getValue();
                            }else {
                                var decCalledFromFunction = decCalledFromFunctions.getOrDefault(decFunctionName, 0);
                                totalDiff += Math.abs(decCalledFromFunction - calledFromFunction.getValue());
                            }
                        }

                        for (var decCalledFromFunction: decCalledFromFunctions.entrySet()) {
                            var startMarkerName = startMarkerNamesByDecompiledFunctionName.getOrDefault(decCalledFromFunction.getKey(), null);
                            if(startMarkerName == null){
                                totalDiff += decCalledFromFunction.getValue();
                            }else if (!calledFromFunctions.containsKey("FF_"+startMarkerName)){
                                totalDiff += decCalledFromFunction.getValue();
                            }
                        }
                        return totalDiff == 0;
                    });
        }

        //Find the difference
        return result;
    }


}

@FunctionalInterface
interface ICheck {
    boolean Check();
}

class CheckChainBuilder {
    public boolean checkPassed;
    public IAssessor.SingleTestResult singleTestResult;

    public static CheckChainBuilder Check(String description, IAssessor.SingleTestResult result, ICheck action) {
        var newInstance = new CheckChainBuilder();
        newInstance.singleTestResult = result;
        newInstance.checkPassed = true;
        return newInstance.WhenPassed(description, action);
    }

    public CheckChainBuilder WhenPassed(String description, ICheck action) {
        if (!checkPassed)
            return this;

        singleTestResult.dblHighBound++;
        this.checkPassed = action.Check();
        if (this.checkPassed)
            singleTestResult.dblActualValue++;
        return this;
    }

    public CheckChainBuilder WhenFailed(String description, ICheck action) {
        if (checkPassed)
            return this;

        singleTestResult.dblHighBound++;
        this.checkPassed = action.Check();
        if (this.checkPassed)
            singleTestResult.dblActualValue++;
        return this;
    }

    public CheckChainBuilder Then(String description, ICheck action) {
        singleTestResult.dblHighBound++;
        this.checkPassed = action.Check();
        if (this.checkPassed)
            singleTestResult.dblActualValue++;
        if (this.checkPassed)
            System.out.println("Check passed: " + description);
        else
            System.out.println("Check failed: " + description);
        return this;
    }
}