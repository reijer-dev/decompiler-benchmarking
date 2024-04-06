package nl.ou.debm.common.feature3;

import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.CompilerConfig;
import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.EOptimize;
import nl.ou.debm.common.antlr.CParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static nl.ou.debm.common.feature3.AsmType.*;

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

        var decompiledCVisitor = new Feature3CVisitor(false, ci.strDecompiledCFilename);
        decompiledCVisitor.visit(ci.cparser_dec.compilationUnit());

        var assemblyPrologues = new HashMap<String, FunctionPrologue>();
        var assemblyEpilogues = new HashMap<String, FunctionEpilogue>();

        synchronized (FunctionAssessor.class){
            var sourceIsInCache = cachedSourceVisitors.containsKey(ci.cparser_org);
            sourceCVisitor = cachedSourceVisitors.getOrDefault(ci.cparser_org, new Feature3CVisitor(true));

            if (!sourceIsInCache) {
                sourceCVisitor.visit(ci.cparser_org.compilationUnit());
                cachedSourceVisitors.put(ci.cparser_org, sourceCVisitor);
            }

            List<String> asmLines = null;
            try {
                asmLines = Files.readAllLines(Paths.get(ci.strAssemblyFilename))
                        .stream()
                        .map(AssemblyHelper::Preprocess)
                        .filter(x -> !x.isEmpty())
                        .toList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String currentFunction = null;
            var firstMarkerFound = false;
            long lineOfLastMarker = 0;
            long lineOfFunctionStart = 0;
            var homedRegisters = new ArrayList<String>();
            var standardPrologueStatements = 0;
            var standardEpilogueStatements = 0;
            var allocatedStackSize = 0;
            var registerMap = new HashMap<String, String>();
            long lineNumber = 0;

            for(var line : asmLines){
                var info = ci.compilerConfig.architecture == EArchitecture.X64ARCH ? AssemblyHelper.getX64LineType(line, homedRegisters, registerMap) : AssemblyHelper.getX86LineType(line, registerMap);

                //Skip in-function labels, because ANTLR puts them together in the next statement
                if(info.type == OtherLabel)
                    continue;

                lineNumber++;
                //Skip all Structured Exception Handling of x64
                if(info.type == Pseudo){
                    if(!firstMarkerFound)
                        standardPrologueStatements++;
                    else
                        standardEpilogueStatements++;
                    continue;
                }

                if(info.type == FunctionLabel){
                    currentFunction = info.value;
                    standardPrologueStatements = 0;
                    standardEpilogueStatements = 0;
                    homedRegisters = new ArrayList<>();
                    firstMarkerFound = false;
                    lineOfLastMarker = 0;
                    lineOfFunctionStart = lineNumber;
                    continue;
                }

                if(currentFunction == null)
                    continue;

                if(info.type == Call && info.value.contains("printf"))
                {
                    lineOfLastMarker = lineNumber;
                    standardEpilogueStatements = 0;
                    //Is it the start marker?
                    if(!firstMarkerFound) {
                        var prologue = new FunctionPrologue();
                        //Minus 2: the printf and its argument pushing the line above
                        prologue.totalLength = (int)(lineNumber - lineOfFunctionStart) - 2;
                        prologue.standardLines = standardPrologueStatements;
                        prologue.registerHomingStatements = homedRegisters.size();
                        prologue.allocatedStackSize = allocatedStackSize;
                        assemblyPrologues.put(currentFunction, prologue);
                        firstMarkerFound = true;
                    }
                    continue;
                }

                if(info.type == Return){
                    //Function returns! Calculate epilogue length
                    var epilogue = new FunctionEpilogue();
                    epilogue.totalLength = (int)(lineNumber - lineOfLastMarker) - 1;
                    epilogue.standardLines = standardEpilogueStatements;
                    assemblyEpilogues.put(currentFunction, epilogue);
                    continue;
                }

                //Check for standard prolog or epilog statements
                if(!firstMarkerFound){
                    if(info.type == NonVolatileRegisterSave || info.type == BaseToStackPointer || info.type == SaveBasePointer)
                        standardPrologueStatements++;
                    if(info.type == StackAllocation) {
                        standardPrologueStatements++;
                        try {
                            allocatedStackSize = Integer.parseInt(info.value);
                        }catch(Exception ex){
                            //Ignore
                        }
                    } else if(info.type == RegisterHoming){
                        homedRegisters.add(info.value);
                    } else if(info.type == RegisterMove){
                        registerMap.remove(info.value2);
                        registerMap.put(info.value2, info.value);
                        //Mark as standard, this can not be decompiled
                        standardPrologueStatements++;
                    }
                }else{
                    if(info.type == NonVolatileRegisterLoad || info.type == StackDeallocation){
                        standardEpilogueStatements++;
                    } else if(info.type == RegisterMove){
                        //Mark as standard, this can not be decompiled
                        standardEpilogueStatements++;
                    }
                }
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

            var isUnreachable = startMarker.strPropertyValue("callable").equals("0");
            if(isUnreachable) {
                //8.3.2. CHECKING UNREACHABLE FUNCTIONS
                checkUnreachableFunctions(result, ci, sourceFunction, decompiledFunction);
                continue;
            }

            isTrue(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_IDENTIFICATION, decompiledFunction != null);

            //From now on, the checks can be sure the decompiled function is found
            if (decompiledFunction == null){
                isTrue(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_IDENTIFICATION, false);
                continue;
            }else{
                var valid = decompiledFunction.getMarkers().size() == 2;
                valid = valid && decompiledFunction.getMarkers().get(0).isStartMarker();
                valid = valid && decompiledFunction.getMarkers().get(1).isEndMarker();
                isTrue(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_IDENTIFICATION, valid);
                if(!valid)
                    continue;
            }

            //8.3.1. CHECKING FUNCTION BOUNDARIES
            checkPrologueStatements(result, ci, sourceFunction, decompiledFunction, assemblyPrologues);
            //checkEpilogueStatements(result, ci, sourceFunction, decompiledFunction, assemblyEpilogues);

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

    private void checkPrologueStatements(SingleAssessmentResult result, CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction, HashMap<String, FunctionPrologue> assemblyPrologueStatements) {
        var functionName = sourceFunction.getName();

        var prologue = assemblyPrologueStatements.getOrDefault(functionName, null);
        if(prologue == null)
            return;

        var unexplainedDecompiledLines = decompiledFunction.getNumberOfPrologueStatements();
        //Voordeel van de twijfel: elk gealloceerd adres mag 1 variabele initialisatie zijn
        var matchingStackAllocationStatements = Math.min(decompiledFunction.getVariableDeclarationsBeforeStartMarker(), prologue.allocatedStackSize);
        unexplainedDecompiledLines -= matchingStackAllocationStatements;

        var matchingRegisterHomingStatements = Math.min(decompiledFunction.getRegisterHomingBeforeStartMarker(), prologue.registerHomingStatements);
        unexplainedDecompiledLines -= matchingRegisterHomingStatements;

        var unexplainedAsmLines = prologue.totalLength - prologue.standardLines;
        var startFound = unexplainedDecompiledLines <= unexplainedAsmLines;
        isTrue(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_START, startFound);

        if(unexplainedAsmLines > 0) {
            var unexplainedStatementsRate = 1 - (unexplainedDecompiledLines / (double) unexplainedAsmLines);
            if(unexplainedStatementsRate < 0)
                unexplainedStatementsRate = 0;
            compare(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_PROLOGUE_RATE, 1.0, unexplainedStatementsRate);
        }else{
            var unexplainedStatementsRate = unexplainedDecompiledLines > 0 ? 0.0 : 1.0;
            compare(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_PROLOGUE_RATE, 1.0, unexplainedStatementsRate);
        }
    }

    /*private void checkEpilogueStatements(SingleAssessmentResult result, CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction, HashMap<String, FunctionEpilogue> assemblyEpilogueStatements) {
        var functionName = sourceFunction.getName();
        var epilogue = assemblyEpilogueStatements.getOrDefault(functionName, null);
        if(epilogue == null)
            return;

        var unexplainedDecompiledLines = decompiledFunction.getNumberOfEpilogueStatements();

        var unexplainedAsmLines = epilogue.totalLength - epilogue.standardLines;
        var endFound = unexplainedDecompiledLines <= unexplainedAsmLines;
        isTrue(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_END, endFound);

        if(unexplainedAsmLines > 0) {
            var unexplainedStatementsRate = 1 - (unexplainedDecompiledLines / (double) unexplainedAsmLines);
            if(unexplainedStatementsRate < 0)
                unexplainedStatementsRate = 0;
            if(functionName.equals("CF_function_88") && ci.strDecompiledCFilename.contains("test_000") && ci.strDecompiledCFilename.contains("x64")){
                System.out.println("TEST");
            }
            compare(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_EPILOGUE_RATE, 1.0, unexplainedStatementsRate);
        }else{
            var unexplainedStatementsRate = unexplainedDecompiledLines > 0 ? 0.0 : 1.0;
            compare(result, functionName, ci.compilerConfig, ETestCategories.FEATURE3_FUNCTION_EPILOGUE_RATE, 1.0, unexplainedStatementsRate);
        }
    }*/

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
            /*{ put(ETestCategories.FEATURE3_FUNCTION_END, functionEndScores); }*/
            { put(ETestCategories.FEATURE3_UNREACHABLE_FUNCTION, unreachableFunctionsScores); }
        };

        private HashMap<ETestCategories, List<F1Score>> f1Scores = new HashMap<>(){
            { put(ETestCategories.FEATURE3_FUNCTION_CALLS, functionCallScores); }
            { put(ETestCategories.FEATURE3_VARIADIC_FUNCTION, variadicScores); }
        };

        private HashMap<ETestCategories, List<NumericScore>> numericScores = new HashMap<>() {
            { put(ETestCategories.FEATURE3_FUNCTION_PROLOGUE_RATE, functionPrologueStatementsRate); }
            /*{ put(ETestCategories.FEATURE3_FUNCTION_EPILOGUE_RATE, functionEpilogueStatementsRate); }*/
            { put(ETestCategories.FEATURE3_RETURN, returnScores); }
        };
    }
}