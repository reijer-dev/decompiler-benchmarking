package nl.ou.debm.common.feature3;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.EOptimize;
import nl.ou.debm.common.antlr.CParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionAssessor implements IAssessor {

    List<BooleanScore> foundFunctionsScores = new ArrayList<>();
    List<BooleanScore> functionStartScores = new ArrayList<>();
    List<NumericScore> functionPrologueStatementsRate = new ArrayList<>();
    List<NumericScore> functionEpilogueStatementsRate = new ArrayList<>();
    List<BooleanScore> functionEndScores = new ArrayList<>();
    List<NumericScore> returnScores = new ArrayList<>();
    List<BooleanScore> perfectBoundariesScores = new ArrayList<>();
    List<BooleanScore> unreachableFunctionsScores = new ArrayList<>();
    List<NumericScore> functionTotalCallScores = new ArrayList<>();
    List<NumericScore> functionCallScores = new ArrayList<>();
    List<BooleanScore> variadicScores = new ArrayList<>();

    HashMap<CParser, Feature3CVisitor> cachedSourceVisitors = new HashMap<>();

    private HashMap<ETestCategories, List<BooleanScore>> booleanScores = new HashMap<>(){
        { put(ETestCategories.FEATURE3_FUNCTION_IDENTIFICATION, foundFunctionsScores); }
        { put(ETestCategories.FEATURE3_FUNCTION_START, functionStartScores); }
        { put(ETestCategories.FEATURE3_FUNCTION_END, functionEndScores); }
        { put(ETestCategories.FEATURE3_PERFECT_BOUNDARIES, perfectBoundariesScores); }
        { put(ETestCategories.FEATURE3_UNREACHABLE_FUNCTION, unreachableFunctionsScores); }
        { put(ETestCategories.FEATURE3_VARIADIC_FUNCTION, variadicScores); }
    };
    private HashMap<ETestCategories, List<NumericScore>> numericScores = new HashMap<>() {
        { put(ETestCategories.FEATURE3_TOTAL_FUNCTION_CALLS, functionTotalCallScores); }
        { put(ETestCategories.FEATURE3_FUNCTION_CALLS, functionCallScores); }
        { put(ETestCategories.FEATURE3_FUNCTION_PROLOGUE_RATE, functionPrologueStatementsRate); }
        { put(ETestCategories.FEATURE3_FUNCTION_EPILOGUE_RATE, functionEpilogueStatementsRate); }
        { put(ETestCategories.FEATURE3_RETURN, returnScores); }
    };

    @Override
    public List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci) {
        // define possible output
        final List<TestResult> out = new ArrayList<>();
        //We skip optimized code, because it confuses our function start and end markers
        if (ci.compilerConfig.optimization == EOptimize.OPTIMIZE){
            out.add(new CountTestResult(ETestCategories.FEATURE3_AGGREGATED, ci.compilerConfig, true));
            return out;
        }

        var result = new CountTestResult();
        //We increase this on every check, and increase dblActualValue on every check pass
        result.setHighBound(0);

        var sourceIsInCache = cachedSourceVisitors.containsKey(ci.cparser_org);
        var sourceCVisitor = cachedSourceVisitors.getOrDefault(ci.cparser_org, new Feature3CVisitor(true));
        var decompiledCVisitor = new Feature3CVisitor(false);

        if(!sourceIsInCache)
            sourceCVisitor.visit(ci.cparser_org.compilationUnit());
        decompiledCVisitor.visit(ci.cparser_dec.compilationUnit());
        if(!sourceIsInCache)
            cachedSourceVisitors.put(ci.cparser_org, sourceCVisitor);

        var decFunctionsNamesByStartMarkerName = new HashMap<String, String>();
        var startMarkerNamesByDecompiledFunctionName = new HashMap<String, String>();
        for (var decFunction : decompiledCVisitor.functions.values()) {
            if (decFunction.getMarkers().size() == 0)
                continue;
            var startMarker = decFunction.getMarkers().get(0);
            decFunctionsNamesByStartMarkerName.put(startMarker.getFunctionName(), decFunction.getName());
            startMarkerNamesByDecompiledFunctionName.put(decFunction.getName(), startMarker.getFunctionName());
        }

        for (var sourceFunction : sourceCVisitor.functions.values()) {
            FoundFunction decompiledFunction;
            FunctionCodeMarker decompiledMarker;
            var functionName = sourceFunction.getName();
            var startMarker = sourceFunction.getMarkers().get(0);

            //Find the decompiled marker
            decompiledMarker = decompiledCVisitor.markersById.getOrDefault(startMarker.lngGetID(), null);

            //Find the decompiled function
            decompiledFunction = decompiledMarker == null ? null : decompiledCVisitor.functions.get(decompiledMarker.functionId);
            if(decompiledMarker != null && !decompiledMarker.getFunctionName().equals(functionName))
                decompiledFunction = null;
            isTrue(functionName, ci.compilerConfig.architecture, foundFunctionsScores, decompiledFunction != null);

            //8.3.2. CHECKING UNREACHABLE FUNCTIONS
            checkUnreachableFunctions(ci, sourceFunction, decompiledFunction);

            //From now on, the checks can be sure the decompiled function is found
            if (decompiledFunction == null)
                continue;

            //8.3.1. CHECKING FUNCTION BOUNDARIES
            checkFunctionBoundaries(ci, sourceFunction, decompiledFunction);

            //8.3.1. CHECKING RETURN STATEMENTS
            checkReturnStatements(ci, sourceFunction, decompiledFunction);

            //8.3.3. CHECKING VARIADIC FUNCTIONS
            checkVariadicFunctions(ci, sourceFunction, decompiledFunction);

            //8.3.4. CHECKING NORMAL FUNCTION CALLS
            checkNormalFunctionCalls(ci, decFunctionsNamesByStartMarkerName, startMarkerNamesByDecompiledFunctionName, sourceFunction, decompiledFunction);
        }
        for(var category : booleanScores.entrySet())
            cumulateBooleanResults(ci.compilerConfig, category, out);
        for(var category : numericScores.entrySet())
            cumulateNumericResults(ci.compilerConfig, category, out);
        return out;
    }

    private void checkReturnStatements(CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        compare(sourceFunction.getName(), ci.compilerConfig.architecture, returnScores, sourceFunction.getNumberOfReturnStatements(), decompiledFunction.getNumberOfReturnStatements());
    }

    private void checkVariadicFunctions(CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        if(sourceFunction.isVariadic())
            isTrue(sourceFunction.getName(), ci.compilerConfig.architecture, variadicScores, decompiledFunction.isVariadic());
    }

    private void checkUnreachableFunctions(CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        //Update unreachable functions score when relevant
        if (sourceFunction.getCalledFromFunctions().size() == 0)
            isTrue(sourceFunction.getName(), ci.compilerConfig.architecture, unreachableFunctionsScores, decompiledFunction != null);
    }

    private void checkFunctionBoundaries(CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        var functionName = sourceFunction.getName();
        //Check start marker
        var decStartMarker = decompiledFunction.getMarkers().get(0);
        isTrue(functionName, ci.compilerConfig.architecture, functionStartScores, decStartMarker.isAtFunctionStart);

        compare(functionName, ci.compilerConfig.architecture, functionPrologueStatementsRate, sourceFunction.getNumberOfPrologueStatements(), decompiledFunction.getNumberOfPrologueStatements());
        compare(functionName, ci.compilerConfig.architecture, functionEpilogueStatementsRate, sourceFunction.getNumberOfEpilogueStatements(), decompiledFunction.getNumberOfEpilogueStatements());

        //Check end marker
        var decEndMarker = decompiledFunction.getMarkers().get(decompiledFunction.getMarkers().size() - 1);
        isTrue(functionName, ci.compilerConfig.architecture, functionEndScores, decEndMarker.isAtFunctionEnd);

        isTrue(functionName, ci.compilerConfig.architecture, perfectBoundariesScores, decompiledFunction.getMarkers().size() == 2);
    }

    private void checkNormalFunctionCalls(CodeInfo ci, HashMap<String, String> decFunctionsNamesByStartMarkerName, HashMap<String, String> startMarkerNamesByDecompiledFunctionName, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        //Checking whether function is called the same amount of times
        var totalCalled = sourceFunction.getCalledFromFunctions().values().stream().reduce(0, Integer::sum);
        var decompiledTotalCalled = decompiledFunction.getCalledFromFunctions().values().stream().reduce(0, Integer::sum);
        compare(sourceFunction.getName(), ci.compilerConfig.architecture, functionTotalCallScores, totalCalled, decompiledTotalCalled);

        //Checking function call sites per caller function
        var calledFromFunctions = sourceFunction.getCalledFromFunctions();
        var decCalledFromFunctions = decompiledFunction.getCalledFromFunctions();

        //For every source function call,
        //compare expected with decompiled function calls
        for (var calledFromFunction : calledFromFunctions.entrySet()) {
            //We only test calls from functions from our feature, because those functions are known by name, through the code markers
            if (!calledFromFunction.getKey().startsWith(EFeaturePrefix.FUNCTIONFEATURE + "_"))
                continue;
            var decFunctionName = decFunctionsNamesByStartMarkerName.getOrDefault(calledFromFunction.getKey(), null);
            var decCalledFromFunction = decFunctionName == null ? 0 : decCalledFromFunctions.getOrDefault(decFunctionName, 0);
            compare(sourceFunction.getName(), ci.compilerConfig.architecture, functionCallScores, calledFromFunction.getValue(), decCalledFromFunction);
        }

        //For every decompiled function call that is not in the source,
        //score on expected 0 against actual value
        for (var decCalledFromFunction : decCalledFromFunctions.entrySet()) {
            var startMarkerName = startMarkerNamesByDecompiledFunctionName.getOrDefault(decCalledFromFunction.getKey(), null);
            if (startMarkerName == null || !calledFromFunctions.containsKey(startMarkerName))
                compare(sourceFunction.getName(), ci.compilerConfig.architecture, functionCallScores, decCalledFromFunction.getValue(), 0);
        }
    }

    private void compare(String name, EArchitecture architecture, List<NumericScore> scores, int highBound, int actual) {
        scores.add(new NumericScore(name, architecture, 0, highBound, actual));
    }

    private void compare(String name, EArchitecture architecture, List<BooleanScore> scores, boolean expected, boolean actual) {
        scores.add(new BooleanScore(name, architecture, expected, actual));
    }

    private void isTrue(String name, EArchitecture architecture, List<BooleanScore> scores, boolean actual) {
        compare(name, architecture, scores, true, actual);
    }

    private void cumulateBooleanResults(CompilerConfig compilerConfig, Map.Entry<ETestCategories, List<BooleanScore>> scores, List<TestResult> out) {
        for (var score : scores.getValue())
            out.add(new CountTestResult(scores.getKey(), compilerConfig, 0, score.actual ? 1 : 0, 1));
    }

    private void cumulateNumericResults(CompilerConfig compilerConfig, Map.Entry<ETestCategories, List<NumericScore>> scores, List<TestResult> out) {
        for (var score : scores.getValue())
            out.add(new CountTestResult(scores.getKey(), compilerConfig, score.lowBound, score.actual, score.highBound));
    }

    private interface Foo {
        void bar();
    }
}