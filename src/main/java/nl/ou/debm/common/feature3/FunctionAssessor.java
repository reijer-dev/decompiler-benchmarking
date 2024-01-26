package nl.ou.debm.common.feature3;

import nl.ou.debm.common.EArchitecture;
import nl.ou.debm.common.EOptimize;
import nl.ou.debm.common.IAssessor;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.antlr.CParser;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FunctionAssessor implements IAssessor {

    List<BooleanScore> foundFunctionsScores = new ArrayList<>();
    List<BooleanScore> functionStartScores = new ArrayList<>();
    List<NumericScore> functionPrologueStatementsRate = new ArrayList<>();
    List<BooleanScore> functionEndScores = new ArrayList<>();
    List<NumericScore> returnScores = new ArrayList<>();
    List<BooleanScore> perfectBoundariesScores = new ArrayList<>();
    List<BooleanScore> unreachableFunctionsScores = new ArrayList<>();
    List<NumericScore> functionTotalCallScores = new ArrayList<>();
    List<NumericScore> functionCallScores = new ArrayList<>();
    List<BooleanScore> tailCallScores = new ArrayList<>();
    List<BooleanScore> variadicScores = new ArrayList<>();

    HashMap<CParser, Feature3CVisitor> cachedSourceVisitors = new HashMap<>();

    private HashMap<String, List<BooleanScore>> booleanScores = new HashMap<>(){
        { put("1. Found functions", foundFunctionsScores); }
        { put("2. Function starts", functionStartScores); }
        { put("3. Function ends", functionEndScores); }
        { put("4. Perfect boundaries", perfectBoundariesScores); }
        { put("5. Return statements", perfectBoundariesScores); }
        { put("6. Unreachable functions", unreachableFunctionsScores); }
        { put("7. Tail calls", tailCallScores); }
        { put("8. Variadic functions", variadicScores); }
    };
    private HashMap<String, List<NumericScore>> numericScores = new HashMap<>() {
        { put("8. Function calls (1 - Total)", functionTotalCallScores); }
        { put("9. Function calls (2 - Per function)", functionCallScores); }
        { put("10. % prologue statements", functionPrologueStatementsRate); }
    };

    @Override
    public SingleTestResult GetSingleTestResult(CodeInfo ci) {
        //We skip optimized code, because it confuses our function start and end markers
        if (ci.optimizationLevel == EOptimize.OPTIMIZE)
            return new SingleTestResult(true);

        var result = new SingleTestResult();
        //We increase this on every check, and increase dblActualValue on every check pass
        result.dblHighBound = 0;

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
            isTrue(functionName, ci.architecture, foundFunctionsScores, decompiledFunction != null);

            //8.3.2. CHECKING UNREACHABLE FUNCTIONS
            checkUnreachableFunctions(ci, sourceFunction, decompiledFunction);

            //From now on, the checks can be sure the decompiled function is found
            if (decompiledFunction == null)
                continue;

            //8.3.1. CHECKING FUNCTION BOUNDARIES
            checkFunctionBoundaries(ci, decompiledFunction, functionName);

            //8.3.1. CHECKING RETURN STATEMENTS
            checkReturnStatements(ci, sourceFunction, decompiledFunction);

            //8.3.3. CHECKING VARIADIC FUNCTIONS
            checkVariadicFunctions(ci, sourceFunction, decompiledFunction);

            //8.3.4. CHECKING NORMAL FUNCTION CALLS
            checkNormalFunctionCalls(ci, decFunctionsNamesByStartMarkerName, startMarkerNamesByDecompiledFunctionName, sourceFunction, decompiledFunction);
        }

        return result;
    }

    private void checkReturnStatements(CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        compare(sourceFunction.getName(), ci.architecture, returnScores, sourceFunction.getNumberOfReturnStatements(), decompiledFunction.getNumberOfReturnStatements());
    }

    public void generateReport(){
        var sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>Function feature</h2>");
        sb.append("<table>");
        sb.append("<tr><th>Description</th><th>Architecture</th><th>Score</th><th>Max score</th><th style='text-align:right'>%</th></tr>");

        for(var score : booleanScores.entrySet()) {
            var testResultForScore = new SingleTestResult();
            cumulateBooleanResults(score.getValue(), testResultForScore);

            sb.append("<tr><td>");
            sb.append(score.getKey());
            sb.append("</td><td></td><td style='text-align:right'>");
            sb.append(testResultForScore.dblActualValue);
            sb.append("</td><td style='text-align:right'>");
            sb.append(testResultForScore.dblHighBound);
            sb.append("</td><td style='text-align:right'>");
            sb.append(String.format("%.2f", getPercentage(testResultForScore)));
            sb.append("%</td></tr>");

            var archs = score.getValue().stream().map(x -> x.architecture).distinct().toArray();
            for (var arch : archs) {
                var testResultForArch = new SingleTestResult();
                var fails = cumulateBooleanResults(score.getValue().stream().filter(x -> x.architecture == arch).toList(), testResultForArch);
                sb.append("<tr><td></td><td>");
                sb.append(arch);
                if(fails.size() > 0){
                    sb.append("<br /><details><summary>Fouten:</summary><ul>");
                    for(var fail : fails){
                        sb.append("<li>");
                        sb.append(fail);
                        sb.append("</li>");
                    }
                    sb.append("</details>");
                }
                sb.append("</td><td style='text-align:right'>");
                sb.append(testResultForArch.dblActualValue);
                sb.append("</td><td style='text-align:right'>");
                sb.append(testResultForArch.dblHighBound);
                sb.append("</td><td style='text-align:right'>");
                sb.append(String.format("%.2f", getPercentage(testResultForArch)));
                sb.append("%</td>");
                sb.append("</tr>");
            }
        }

        for(var score : numericScores.entrySet()) {
            var testResultForScore = new SingleTestResult();
            cumulateNumericResults(score.getValue(), testResultForScore);

            sb.append("<tr><td>");
            sb.append(score.getKey());
            sb.append("</td><td></td><td style='text-align:right'>");
            sb.append(testResultForScore.dblActualValue);
            sb.append("</td><td style='text-align:right'>");
            sb.append(testResultForScore.dblHighBound);
            sb.append("</td><td style='text-align:right'>");
            sb.append(String.format("%.2f", getPercentage(testResultForScore)));
            sb.append("%</td></tr>");

            var archs = score.getValue().stream().map(x -> x.architecture).distinct().toArray();
            for (var arch : archs) {
                var testResultForArch = new SingleTestResult();
                var fails = cumulateNumericResults(score.getValue().stream().filter(x -> x.architecture == arch).toList(), testResultForArch);
                sb.append("<tr><td></td><td>");
                sb.append(arch);
                if(fails.size() > 0){
                    sb.append("<br /><details><summary>Fouten:</summary><ul>");
                    for(var fail : fails){
                        sb.append("<li>");
                        sb.append(fail);
                        sb.append("</li>");
                    }
                    sb.append("</details>");
                }
                sb.append("</td><td style='text-align:right'>");
                sb.append(testResultForArch.dblActualValue);
                sb.append("</td><td style='text-align:right'>");
                sb.append(testResultForArch.dblHighBound);
                sb.append("</td><td style='text-align:right'>");
                sb.append(String.format("%.2f", getPercentage(testResultForArch)));
                sb.append("%</td>");
                sb.append("</tr>");
            }
        }

        sb.append("</table></body></html>");
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream("C:\\Users\\reije\\OneDrive\\Documenten\\Development\\c-program\\containers\\container_000\\test_000\\report.html"));
            writer.write(sb.toString());
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double getPercentage(SingleTestResult testResult){
        var margin = testResult.dblHighBound - testResult.dblLowBound;
        if(margin == 0)
            margin = 100;
        return 100 * testResult.dblActualValue / margin;
    }

    private void checkVariadicFunctions(CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        if(sourceFunction.isVariadic())
            isTrue(sourceFunction.getName(), ci.architecture, variadicScores, decompiledFunction.isVariadic());
    }

    private void checkUnreachableFunctions(CodeInfo ci, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        //Update unreachable functions score when relevant
        if (sourceFunction.getCalledFromFunctions().size() == 0)
            isTrue(sourceFunction.getName(), ci.architecture, unreachableFunctionsScores, decompiledFunction != null);
    }

    private void checkFunctionBoundaries(CodeInfo ci, FoundFunction decompiledFunction, String functionName) {
        //Check start marker
        var decStartMarker = decompiledFunction.getMarkers().get(0);
        isTrue(functionName, ci.architecture, functionStartScores, decStartMarker.isAtFunctionStart);

        compare(functionName, ci.architecture, functionPrologueStatementsRate, decompiledFunction.getNumberOfStatements(), decompiledFunction.getNumberOfStatements() - decompiledFunction.getNumberOfPrologueStatements());

        //Check end marker
        var decEndMarker = decompiledFunction.getMarkers().get(decompiledFunction.getMarkers().size() - 1);
        isTrue(functionName, ci.architecture, functionEndScores, decEndMarker.isAtFunctionEnd);

        isTrue(functionName, ci.architecture, perfectBoundariesScores, decompiledFunction.getMarkers().size() == 2);
    }

    private void checkNormalFunctionCalls(CodeInfo ci, HashMap<String, String> decFunctionsNamesByStartMarkerName, HashMap<String, String> startMarkerNamesByDecompiledFunctionName, FoundFunction sourceFunction, FoundFunction decompiledFunction) {
        //Checking whether function is called the same amount of times
        var totalCalled = sourceFunction.getCalledFromFunctions().values().stream().reduce(0, Integer::sum);
        var decompiledTotalCalled = decompiledFunction.getCalledFromFunctions().values().stream().reduce(0, Integer::sum);
        compare(sourceFunction.getName(), ci.architecture, functionTotalCallScores, totalCalled, decompiledTotalCalled);

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
            compare(sourceFunction.getName(), ci.architecture, functionCallScores, calledFromFunction.getValue(), decCalledFromFunction);
        }

        //For every decompiled function call that is not in the source,
        //score on expected 0 against actual value
        for (var decCalledFromFunction : decCalledFromFunctions.entrySet()) {
            var startMarkerName = startMarkerNamesByDecompiledFunctionName.getOrDefault(decCalledFromFunction.getKey(), null);
            if (startMarkerName == null || !calledFromFunctions.containsKey(startMarkerName))
                compare(sourceFunction.getName(), ci.architecture, functionCallScores, decCalledFromFunction.getValue(), 0);
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

    private List<String> cumulateBooleanResults(List<BooleanScore> scores, SingleTestResult singleTestResult) {
        var highbound = scores.size();
        var actualValue = scores.stream().filter(x -> x.actual == x.expected).count();
        //Adapt global score
        singleTestResult.dblHighBound += highbound;
        singleTestResult.dblActualValue += actualValue;
        return scores.stream().filter(x -> x.actual != x.expected).map(x -> x.name).toList();
    }

    private List<String> cumulateNumericResults(List<NumericScore> scores, SingleTestResult singleTestResult) {
        var highbound = 0;
        var actualValue = 0;
        for (var score : scores) {
            highbound++;
            if (score.highBound != 0) {
                actualValue += score.actual / (float) score.highBound;
            }else if(score.actual == 0){
                actualValue += 1;
            }
        }
        singleTestResult.dblHighBound += highbound;
        singleTestResult.dblActualValue += actualValue;
        return scores.stream().filter(x -> x.actual != x.highBound).map(x -> x.name).toList();
    }

    private interface Foo {
        void bar();
    }
}