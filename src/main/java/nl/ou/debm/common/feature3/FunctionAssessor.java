package nl.ou.debm.common.feature3;

import nl.ou.debm.common.EOptimize;
import nl.ou.debm.common.IAssessor;
import nl.ou.debm.producer.EFeaturePrefix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class FunctionAssessor implements IAssessor {

    List<BooleanScore> foundFunctionsScores = new ArrayList<>();
    List<BooleanScore> functionStartScores = new ArrayList<>();
    List<BooleanScore> functionEndScores = new ArrayList<>();
    List<BooleanScore> perfectBoundariesScores = new ArrayList<>();
    List<BooleanScore> unreachableFunctionsScores = new ArrayList<>();
    List<NumericScore> functionTotalCallScores = new ArrayList<>();
    List<NumericScore> functionCallScores = new ArrayList<>();
    List<BooleanScore> tailCallScores = new ArrayList<>();
    List<BooleanScore> variadicScores = new ArrayList<>();

    @Override
    public SingleTestResult GetSingleTestResult(CodeInfo ci) {
        //We skip optimized code, because it confuses our function start and end markers
        if (ci.optimizationLevel == EOptimize.OPTIMIZE)
            return new SingleTestResult(true);

        var result = new SingleTestResult();
        //We increase this on every check, and increase dblActualValue on every check pass
        result.dblHighBound = 0;

        var sourceCVisitor = new Feature3CVisitor();
        var decompiledCVisitor = new Feature3CVisitor();

        //Visit C trees at the same time
        var tasks = new ArrayList<Callable<Object>>();
        tasks.add(() -> sourceCVisitor.visit(ci.cparser_org.compilationUnit()));
        tasks.add(() -> decompiledCVisitor.visit(ci.cparser_dec.compilationUnit()));
        try {
            Executors.newCachedThreadPool().invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

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
        for (var decFunction : decompiledCVisitor.functions.values()) {
            if (decFunction.getMarkers().size() == 0)
                continue;
            var startMarker = decFunction.getMarkers().get(0);
            decFunctionsNamesByStartMarkerName.put(startMarker.getFunctionName(), decFunction.getName());
            startMarkerNamesByDecompiledFunctionName.put(decFunction.getName(), startMarker.getFunctionName());
        }

        for (var sourceFunction : testableSourceFunctions) {
            FoundFunction decompiledFunction = null;
            FunctionCodeMarker decompiledMarker = null;
            var functionName = sourceFunction.getName().substring(EFeaturePrefix.FUNCTIONFEATURE.toString().length() + 1);
            var startMarker = sourceFunction.getMarkers().get(0);

            //Find the decompiled marker
            decompiledMarker = decompiledCVisitor.markersById.getOrDefault(startMarker.rawMarker.getID(), null);

            //Find the decompiled function
            decompiledFunction = decompiledMarker == null ? null : decompiledCVisitor.functions.get(decompiledMarker.functionId);
            isTrue(foundFunctionsScores, decompiledFunction != null);

            //Update unreachable functions score when relevant
            if (sourceFunction.getCalledFromFunctions().size() == 0)
                isTrue(unreachableFunctionsScores, decompiledFunction != null);

            //From now on, the checks can be sure the decompiled function is found
            if (decompiledFunction == null)
                continue;

            //Check start marker
            var decStartMarker = decompiledFunction.getMarkers().get(0);
            if (decStartMarker.getFunctionName().equals(functionName))
                isTrue(functionStartScores, decompiledFunction.isMarkerAtStart(decStartMarker));

            //Check end marker
            var decEndMarker = decompiledFunction.getMarkers().get(decompiledFunction.getMarkers().size() - 1);
            isTrue(functionEndScores, decompiledFunction.isMarkerAtEnd(decEndMarker));

            isTrue(perfectBoundariesScores, decompiledFunction.getMarkers().size() == 2);

            //Checking whether function is variadic
            compare(variadicScores, sourceFunction.isVariadic(), decompiledFunction.isVariadic());

            //Checking whether function is called the same amount of times
            var totalCalled = sourceFunction.getCalledFromFunctions().values().stream().reduce(0, Integer::sum);
            var decompiledTotalCalled = decompiledFunction.getCalledFromFunctions().values().stream().reduce(0, Integer::sum);
            compare(functionTotalCallScores, 0, totalCalled, decompiledTotalCalled);

            //Checking function call sites per caller function
            var calledFromFunctions = sourceFunction.getCalledFromFunctions();
            var decCalledFromFunctions = decompiledFunction.getCalledFromFunctions();

            //For every source function call,
            //compare expected with decompiled function calls
            for (var calledFromFunction : calledFromFunctions.entrySet()) {
                var decFunctionName = decFunctionsNamesByStartMarkerName.getOrDefault(calledFromFunction.getKey().substring(3), null);
                var decCalledFromFunction = decFunctionName == null ? 0 : decCalledFromFunctions.getOrDefault(decFunctionName, 0);
                compare(functionCallScores, 0, calledFromFunction.getValue(), decCalledFromFunction);
            }

            //For every decompiled function call that is not in the source,
            //score on expected 0 against actual value
            for (var decCalledFromFunction : decCalledFromFunctions.entrySet()) {
                var startMarkerName = startMarkerNamesByDecompiledFunctionName.getOrDefault(decCalledFromFunction.getKey(), null);
                if (startMarkerName == null || !calledFromFunctions.containsKey("FF_" + startMarkerName))
                    compare(functionCallScores, 0, decCalledFromFunction.getValue(), 0);
            }
        }

        return result;
    }

    private void compare(List<NumericScore> scores, int lowBound, int highBound, int actual) {
        scores.add(new NumericScore(lowBound, highBound, actual));
    }

    private void compare(List<BooleanScore> scores, boolean expected, boolean actual) {
        scores.add(new BooleanScore(expected, actual));
    }

    private void isTrue(List<BooleanScore> scores, boolean actual) {
        compare(scores, true, actual);
    }
}