package nl.ou.debm.assessor;

import nl.ou.debm.common.*;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.LLVMIRLexer;
import nl.ou.debm.common.antlr.LLVMIRParser;
import nl.ou.debm.common.feature1.LoopAssessor;
import nl.ou.debm.common.feature3.FunctionAssessor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.ou.debm.common.IOElements.*;

/*




    RunTheTests
    -----------
    input: container base folder, decompile script
    output: Map<String, SingleTestResult>
            as RunTheTests uses all the tests in one container, it gets a set of test results, one set for every test
            it aggregates these over all the tests

    GetTestResultsForSingleBinary
    -----------------------------
    input: CodeInfo object, containing the ANTLR-objects + info about compiler, architecture, optimization
    output: Map<String, SingleTestResult>
            for each test, the result is returned as a SingleTestResult
            a test parameter contains (1) what was tested, (2) compiler, (3) architecture, (4) optimization
            a SingleTestResult contains: low bound, high bound, actual value


    AggregateOverCompiler/Architecture/Optimization/Test
    ----------------------------------------------------
    input:  Map<String, SingleTestResult>
    output: Map<String, SingleTestResult>
            aggregation over compiler, architecture or optimization or test


 */



public class Assessor {

    private final ArrayList<IAssessor> feature = new ArrayList<>();      // array containing all assessor classes

    /**
     * constructor
     */
    public Assessor(){
        // add all features to array
        feature.add(new LoopAssessor());
        //feature.add(new DataStructuresFeature());
        feature.add(new FunctionAssessor());
    }

    public Map<String, IAssessor.SingleTestResult> RunTheTests(final String strContainersBaseFolder, final String strDecompileScript, final boolean allowMissingBinaries) throws Exception {
        // create list to be able to aggregate
        final List<Map<String, IAssessor.SingleTestResult>> list = new ArrayList<>();

        var reuseDecompilersOutput = false;

        // set root path, to be used program-wide (as it is a static)
        Environment.containerBasePath = strContainersBaseFolder;

        // get name of the decompiler-script and test its existence & executableness
        if (!Files.isExecutable(Paths.get(strDecompileScript))) {
            throw new Exception("Decompilation script (" + strDecompileScript + ") does not exist or is not executable.");
        }

        // get container number
        final int iContainerNumber = iGetContainerNumberToBeAssessed();

        // get number of valid tests within container
        final int iNumberOfTests = iNumberOfValidTestsInContainer(iContainerNumber);
        if (iNumberOfTests < 1) {
            throw new Exception("No valid tests in selected container (" + iContainerNumber + ").");
        }

        // setup temporary folder to receive the decompiler output
        var tempDir = Files.createTempDirectory("debm");

        // run all tests in container
        final String STRCDECOMP = "cdecomp.txt";
        // create new variable set
        var codeinfo = new IAssessor.CodeInfo();
        for (int iTestNumber = 0; iTestNumber < iNumberOfTests; ++iTestNumber) {
            // read original C
            codeinfo.clexer_org = new CLexer(CharStreams.fromFileName(strCSourceFullFilename(iContainerNumber, iTestNumber)));
            codeinfo.cparser_org = new CParser((new CommonTokenStream(codeinfo.clexer_org)));
            // invoke decompiler for every binary
            for (var compiler : ECompiler.values()) {
                for (var architecture : EArchitecture.values()) {
                    for (var opt : EOptimize.values()) {
                        // setup values
                        var strBinary = strBinaryFullFileName(iContainerNumber, iTestNumber, architecture, compiler, opt);
                        if(allowMissingBinaries && !Files.exists(Paths.get(strBinary)))
                            continue;
                        var strCDest = Paths.get(tempDir.toString(), STRCDECOMP).toAbsolutePath().toString();

                        var existingCDest = Path.of(strBinary.replace(".exe", ".c"));
                        if(reuseDecompilersOutput && Files.exists(existingCDest)){
                            Files.copy(existingCDest, Path.of(strCDest), StandardCopyOption.REPLACE_EXISTING);
                        }else {

                            // setup new process
                            var decompileProcessBuilder = new ProcessBuilder(
                                    strDecompileScript,
                                    strBinary,
                                    strCDest
                            );
                            decompileProcessBuilder.redirectErrorStream(true);
                            // remove old files
                            deleteFile(strCDest);
                            // start new process
                            System.out.println("Invoking decompiler for: " + strBinary);
                            var decompileProcess = decompileProcessBuilder.start();
                            // make sure output is processed
                            var reader = new BufferedReader(new InputStreamReader(decompileProcess.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                // read output just to get it out of any pipe
                                System.out.println(line);
                            }
                            // wait for script to end = decompilation to finish
                            decompileProcess.waitFor();
                        }
                        // continue when decompiler output files are found
                        if (bFileExists(strCDest)) {
                            if(reuseDecompilersOutput)
                                Files.copy(Path.of(strCDest), Path.of(strBinary.replace(".exe", ".c")), StandardCopyOption.REPLACE_EXISTING);
                            codeinfo.compilerConfig.architecture = architecture;
                            codeinfo.compilerConfig.optimization = opt;
                            codeinfo.compilerConfig.compiler = compiler;
                            // read decompiled C
                            codeinfo.clexer_dec = new CLexer(CharStreams.fromFileName(strCDest));
                            codeinfo.cparser_dec = new CParser(new CommonTokenStream(codeinfo.clexer_dec));
                            // read compiler LLVM output
                            codeinfo.llexer_org = new LLVMIRLexer(CharStreams.fromFileName(strLLVMFullFileName(iContainerNumber, iTestNumber, architecture, compiler, opt)));
                            codeinfo.lparser_org = new LLVMIRParser(new CommonTokenStream(codeinfo.llexer_org));
                            // invoke all features
                            for (var f : feature){
                                var testResult = f.GetTestResultsForSingleBinary(codeinfo);
                                list.add(testResult);
                            }
                            // no need to delete decompilation files here, as they as deleted before
                            // decompilation script is run. The last decompilation files will be
                            // deleted when the temp dir is deleted
                        }
                    }
                }
            }
        }

        // aggregate over test sources
        var out = aggregateOverTestSources(list);

        for (var f : feature){
            if(f instanceof FunctionAssessor functionAssessor)
                functionAssessor.generateReport();
        }

        // remove temporary folder
        bFolderAndAllContentsDeletedOK(tempDir);

        System.out.println("Container " + iContainerNumber);
        System.out.println("Number of tests " + iNumberOfTests);

        return out;
    }

    /**
     * Aggregate test results from a set of sources and compute a single test result per TestParameter
     * @param list  set of maps containing the result for a set of sources
     * @return  aggregated results over all the sources
     */
    private Map<String, IAssessor.SingleTestResult> aggregateOverTestSources(List<Map<String, IAssessor.SingleTestResult>> list){
        final Map<String, IAssessor.SingleTestResult> out = new HashMap<>();

        // loop over all the results of the C-sources
        for (var map : list){
            // loop over all the results of a single C-source
            for (var item : map.entrySet()) {
                // determine whether the specific test shows up in the aggregated results
                var result = out.get(item.getKey());
                if (result!=null){
                    // yes it does, so aggregate
                    result.dblLowBound+=item.getValue().dblLowBound;
                    result.dblHighBound+=item.getValue().dblHighBound;
                    result.dblActualValue+=item.getValue().dblActualValue;
                }
                else{
                    // no, it doesn't yet, so just copy this result
                    out.put(item.getKey(), item.getValue());
                }
            }
        }

        return out;
    }

    /**
     * Get the number of the container that is to be tested/assessed
     * @return  ID, ranging 0...199
     */
    int iGetContainerNumberToBeAssessed(){
        // TODO: Implement getting a container number from anywhere
        //       (command line input, random something, whatever)
        //       for now: just return 0 for test purposes
        return 0;
    }

    /**
     * Determine the number of valid tests in a container. It checks: <br>
     *  - the existence of the container folder <br>
     *  - the existence of test container(s) <br>
     *  - per test container: <br>
     *    * existence of the c-sourcefile <br>
     *    * existence of the binaries & llvms <br>
     * @param iContainerNumber  Container to be tested
     * @return  highest valid test in the container
     */
    int iNumberOfValidTestsInContainer(int iContainerNumber){
        int iTestNumber=-1;

        while (true) {
            // try next folder
            ++iTestNumber;
            String strTestPath = IOElements.strTestFullPath(iContainerNumber, iTestNumber);
            if (!bFolderExists(strTestPath)){
                // folder does not exist -- be done with it
                break;
            }

            // check test contents
            // -------------------
            //
            // check all binaries & llvm's
            for (var compiler : ECompiler.values()){
                for (var architecture : EArchitecture.values()){
                    for (var optimize : EOptimize.values()){
                        if (!bFileExists(strBinaryFullFileName(iContainerNumber, iTestNumber,
                                architecture, compiler, optimize))){
                            break;
                        }
                        if (!bFileExists(strLLVMFullFileName(iContainerNumber, iTestNumber,
                                architecture, compiler, optimize))){
                            break;
                        }
                    }
                }
            }
            // check c-source
            if (!bFileExists(strCSourceFullFilename(iContainerNumber, iTestNumber))){
                break;
            }
        }
        return iTestNumber;
    }

    public static Map<String, IAssessor.SingleTestResult> AggregateOverArchitecture(Map<String, IAssessor.SingleTestResult> input){
        return aggregate(input, new AggregateOverArchitectureClass());
    }
    public static Map<String, IAssessor.SingleTestResult> AggregateOverCompiler(Map<String, IAssessor.SingleTestResult> input){
        return aggregate(input, new AggregateOverCompilerClass());
    }
    public static Map<String, IAssessor.SingleTestResult> AggregateOverOptimization(Map<String, IAssessor.SingleTestResult> input){
        return aggregate(input, new AggregateOverOptimizationClass());
    }
    public static Map<String, IAssessor.SingleTestResult> AggregateOverTest(Map<String, IAssessor.SingleTestResult> input){
        return aggregate(input, new AggregateOverTestClass());
    }

    private static Map<String, IAssessor.SingleTestResult> aggregate(Map<String, IAssessor.SingleTestResult> map, IAssessor.IAggregateKeys aggregateFunction){
        final Map<String, IAssessor.SingleTestResult> out = new HashMap<>();
        for (var item : map.entrySet()){
//            IAssessor.TestParameters newKey = aggregateFunction.oldKeyToNewKey(item.getKey());
//            var newVal = out.get(newKey);
//            if (newVal == null){
//                out.put(newKey, item.getValue());
//            }
//            else{
//                newVal.dblLowBound    += item.getValue().dblLowBound;
//                newVal.dblActualValue += item.getValue().dblActualValue;
//                newVal.dblHighBound   += item.getValue().dblHighBound;
//            }
        }
        return out;
    }

    static private class AggregateOverArchitectureClass implements IAssessor.IAggregateKeys {
        @Override
        public IAssessor.TestParameters oldKeyToNewKey(IAssessor.TestParameters key) {
            return new IAssessor.TestParameters(key.whichTest, new CompilerConfig(key.compilerConfig.architecture, null, null));
        }
    }
    static private class AggregateOverCompilerClass implements IAssessor.IAggregateKeys {
        @Override
        public IAssessor.TestParameters oldKeyToNewKey(IAssessor.TestParameters key) {
            return new IAssessor.TestParameters(key.whichTest, new CompilerConfig(null, key.compilerConfig.compiler, null));
        }
    }
    static private class AggregateOverOptimizationClass implements IAssessor.IAggregateKeys {
        @Override
        public IAssessor.TestParameters oldKeyToNewKey(IAssessor.TestParameters key) {
            return new IAssessor.TestParameters(key.whichTest, new CompilerConfig(null, null, key.compilerConfig.optimization));
        }
    }
    static private class AggregateOverTestClass implements IAssessor.IAggregateKeys {
        @Override
        public IAssessor.TestParameters oldKeyToNewKey(IAssessor.TestParameters key) {
            return new IAssessor.TestParameters(key.whichTest, new CompilerConfig(null, null, null));
        }
    }

    public static void generateReport(Map<String, IAssessor.SingleTestResult> input, String strHTMLOutputFile){

        /*
            make a really simple table

            TODO: categorize & layout
         */

        var sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<table>");
        sb.append("<tr><th>Description (unit)</th><th>Architecture</th><th>Compiler</th><th>Optimization</th><th>Score</th><th>Max score</th><th style='text-align:right'>%</th></tr>");

        for (var item : input.entrySet()){
            sb.append("<tr>");
//            sb.append("<td>").append(item.getKey().whichTest.strTestDescription()).append(" (").append(item.getKey().whichTest.strTestUnit()).append(")</td>");
//            appendCell(sb, item.getKey().compilerConfig.architecture);
//            appendCell(sb, item.getKey().compilerConfig.compiler);
//            appendCell(sb, item.getKey().compilerConfig.optimization);
            sb.append("<td style='text-align:right'>").append(item.getValue().dblActualValue).append("</td>");
            sb.append("<td style='text-align:right'>").append(item.getValue().dblHighBound).append("</td>");
            sb.append("<td style='text-align:right'>").append(String.format("%.2f", getPercentage(item.getValue()))).append("%</td>");
            sb.append("</tr>");
        }

        sb.append("</table></body></html>");
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(strHTMLOutputFile));
            writer.write(sb.toString());
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static StringBuilder appendCell(StringBuilder sb, Object oWhat){
        sb.append("<td>");
        String strWhat = null;
        if (oWhat instanceof String){
            strWhat = (String)oWhat;
        }
        else if (oWhat instanceof EArchitecture){
            strWhat = ((EArchitecture) oWhat).strTableCode();
        }
        else if (oWhat instanceof ECompiler){
            strWhat = ((ECompiler) oWhat).strTableCode();
        }
        else if (oWhat instanceof EOptimize){
            strWhat = ((EOptimize) oWhat).strTableCode();
        }
        if (strWhat != null){
            sb.append(strWhat);
        }
        sb.append("</td>");
        return sb;
    }

    private static double getPercentage(IAssessor.SingleTestResult testResult){
        var margin = testResult.dblHighBound - testResult.dblLowBound;
        if(margin == 0)
            margin = 100;
        return 100 * testResult.dblActualValue / margin;
    }
}
