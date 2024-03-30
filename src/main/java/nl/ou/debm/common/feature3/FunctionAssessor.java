package nl.ou.debm.common.feature3;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.EOptimize;
import nl.ou.debm.common.antlr.CParser;

import java.util.*;

public class FunctionAssessor implements IAssessor {
    HashMap<CParser, Feature3CVisitor> cachedSourceVisitors = new HashMap<>();

    @Override
    public List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci) {
        // define possible output
        final List<TestResult> out = new ArrayList<>();
        var result = new SingleAssessmentResult();
        //We skip optimized code, because it confuses our function start and end markers
        if (ci.compilerConfig.optimization == EOptimize.OPTIMIZE){
            return out;
        }

        Feature3CVisitor sourceCVisitor = null;

        var decompiledCVisitor = new Feature3CVisitor(false);
        decompiledCVisitor.visit(ci.cparser_dec.compilationUnit());

        synchronized (FunctionAssessor.class){
            var sourceIsInCache = cachedSourceVisitors.containsKey(ci.cparser_org);
            sourceCVisitor = cachedSourceVisitors.getOrDefault(ci.cparser_org, new Feature3CVisitor(true));

            if (!sourceIsInCache) {
                sourceCVisitor.visit(ci.cparser_org.compilationUnit());
                cachedSourceVisitors.put(ci.cparser_org, sourceCVisitor);
            }
        }

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
            isTrue(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_IDENTIFICATION, decompiledFunction != null);

            //8.3.2. CHECKING UNREACHABLE FUNCTIONS
            checkUnreachableFunctions(result, ci, sourceFunction, decompiledFunction);

            //From now on, the checks can be sure the decompiled function is found
            if (decompiledFunction == null)
                continue;

            //8.3.1. CHECKING FUNCTION BOUNDARIES
            checkFunctionBoundaries(result, ci, sourceFunction, decompiledFunction);

            //8.3.1. CHECKING RETURN STATEMENTS
            checkReturnStatements(result, ci, sourceFunction, decompiledFunction);

            //8.3.3. CHECKING VARIADIC FUNCTIONS
            checkVariadicFunctions(result, ci, sourceFunction, decompiledFunction);

            //8.3.4. CHECKING NORMAL FUNCTION CALLS
            checkFunctionCalls(result, ci, decFunctionsNamesByStartMarkerName, startMarkerNamesByDecompiledFunctionName, sourceFunction, decompiledFunction);
        }
        out.addAll(result.recallScores.values().stream().flatMap(Collection::stream).toList());
        out.addAll(result.f1Scores.values().stream().flatMap(Collection::stream).toList());
        out.addAll(result.numericScores.values().stream().flatMap(Collection::stream).toList());
        return out;
    }

    private void checkReturnStatements(SingleAssessmentResult result, CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        compare(result, sourceFunction.getName(), ci.compilerConfig, ETestCategories.FEATURE3_RETURN, sourceFunction.getNumberOfReturnStatements(), decompiledFunction.getNumberOfReturnStatements());
    }

    private void checkVariadicFunctions(SingleAssessmentResult result, CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        compare(result, sourceFunction.getName(), ci.compilerConfig, ETestCategories.FEATURE3_VARIADIC_FUNCTION, sourceFunction.isVariadic(), decompiledFunction.isVariadic());
    }

    private void checkUnreachableFunctions(SingleAssessmentResult result, CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        //Update unreachable functions score when relevant
        if (sourceFunction.getCalledFromFunctions().entrySet().stream().allMatch(x -> x.getKey().equals(sourceFunction.getName())))
            isTrue(result, sourceFunction.getName(), ci.compilerConfig, ETestCategories.FEATURE3_UNREACHABLE_FUNCTION, decompiledFunction != null);
    }

    private void checkFunctionBoundaries(SingleAssessmentResult result, CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        var functionName = sourceFunction.getName();
        //Check start marker
        var decStartMarker = decompiledFunction.getMarkers().get(0);
        isTrue(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_START, decStartMarker.isAtFunctionStart);

        var prologueStatementsRate = 1 - (decompiledFunction.getNumberOfPrologueStatements() / (double)(decompiledFunction.getNumberOfStatements() - decompiledFunction.getNumberOfPrologueStatements()));
        compare(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_PROLOGUE_RATE, 1.0, prologueStatementsRate);
        compare(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_EPILOGUE_RATE, sourceFunction.getNumberOfEpilogueStatements(), decompiledFunction.getNumberOfEpilogueStatements());

        //Check end marker
        var decEndMarker = decompiledFunction.getMarkers().get(decompiledFunction.getMarkers().size() - 1);
        isTrue(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_END, decEndMarker.isAtFunctionEnd);
    }

    private void checkFunctionCalls(SingleAssessmentResult result, CodeInfo ci, HashMap<String, String> decFunctionsNamesByStartMarkerName, HashMap<String, String> startMarkerNamesByDecompiledFunctionName, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        //Checking function call sites per caller function
        var calledFromFunctions = sourceFunction.getCalledFromFunctions();
        var decCalledFromFunctions = decompiledFunction.getCalledFromFunctions();

        //For every source function call,
        //compare expected with decompiled function calls
        for (var calledFromFunction : calledFromFunctions.entrySet()) {
            //We only test calls from functions from our feature, because those functions are known by name, through the code markers
            var decFunctionName = decFunctionsNamesByStartMarkerName.getOrDefault(calledFromFunction.getKey(), null);
            var decCalledFromFunction = decFunctionName == null ? 0 : decCalledFromFunctions.getOrDefault(decFunctionName, 0);
            for(int i = 1; i <= calledFromFunction.getValue(); i++)
                compare(result, sourceFunction.getName(), ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_CALLS, true, decCalledFromFunction >= i);
            for(int i = calledFromFunction.getValue() + 1; i <= decCalledFromFunction; i++)
                compare(result, sourceFunction.getName(), ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_CALLS, false, true);
        }

        //For every decompiled function call that is not in the source,
        //score on expected 0 against actual value
        for (var decCalledFromFunction : decCalledFromFunctions.entrySet()) {
            var startMarkerName = startMarkerNamesByDecompiledFunctionName.getOrDefault(decCalledFromFunction.getKey(), null);
            if (startMarkerName == null || !calledFromFunctions.containsKey(startMarkerName))
                for(var i = 1; i <= decCalledFromFunction.getValue(); i++)
                    compare(result, sourceFunction.getName(), ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_CALLS, false, true);
        }
    }

    private void compare(SingleAssessmentResult result, String name, CompilerConfig compilerConfig, ETestCategories category, int highBound, int actual) {
        result.numericScores.get(category).add(new NumericScore(category, compilerConfig, 0, highBound, actual));
    }

    private void compare(SingleAssessmentResult result, String name, CompilerConfig compilerConfig, ETestCategories category, Double highBound, Double actual) {
        result.numericScores.get(category).add(new NumericScore(category, compilerConfig, 0.0, highBound, actual));
    }

    private void compare(SingleAssessmentResult result, String name, CompilerConfig compilerConfig, ETestCategories category, boolean expected, boolean actual) {
        result.f1Scores.get(category).add(new F1Score(category, compilerConfig, expected, actual));
    }

    private void isTrue(SingleAssessmentResult result, String name, CompilerConfig compilerConfig, ETestCategories category, boolean actual) {
        result.recallScores.get(category).add(new RecallScore(category, compilerConfig, actual));
    }

    private interface Foo {
        void bar();
    }

    private class SingleAssessmentResult{
        public List<RecallScore> foundFunctionsScores = new ArrayList<>();
        public List<RecallScore> functionStartScores = new ArrayList<>();
        public List<NumericScore> functionPrologueStatementsRate = new ArrayList<>();
        public List<NumericScore> functionEpilogueStatementsRate = new ArrayList<>();
        public List<RecallScore> functionEndScores = new ArrayList<>();
        public List<NumericScore> returnScores = new ArrayList<>();
        public List<RecallScore> unreachableFunctionsScores = new ArrayList<>();
        public List<F1Score> functionCallScores = new ArrayList<>();
        public List<F1Score> variadicScores = new ArrayList<>();

        private HashMap<ETestCategories, List<RecallScore>> recallScores = new HashMap<>(){
            { put(ETestCategories.FEATURE3_FUNCTION_IDENTIFICATION, foundFunctionsScores); }
            { put(ETestCategories.FEATURE3_FUNCTION_START, functionStartScores); }
            { put(ETestCategories.FEATURE3_FUNCTION_END, functionEndScores); }
            { put(ETestCategories.FEATURE3_UNREACHABLE_FUNCTION, unreachableFunctionsScores); }
        };

        private HashMap<ETestCategories, List<F1Score>> f1Scores = new HashMap<>(){
            { put(ETestCategories.FEATURE3_FUNCTION_CALLS, functionCallScores); }
            { put(ETestCategories.FEATURE3_VARIADIC_FUNCTION, variadicScores); }
        };

        private HashMap<ETestCategories, List<NumericScore>> numericScores = new HashMap<>() {
            { put(ETestCategories.FEATURE3_FUNCTION_PROLOGUE_RATE, functionPrologueStatementsRate); }
            { put(ETestCategories.FEATURE3_FUNCTION_EPILOGUE_RATE, functionEpilogueStatementsRate); }
            { put(ETestCategories.FEATURE3_RETURN, returnScores); }
        };
    }
}