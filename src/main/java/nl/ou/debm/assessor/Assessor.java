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
import java.util.List;

import static nl.ou.debm.common.IOElements.*;

/*


    RunTheTests
    -----------
    input: container base folder, decompile script
    output: List<SingleTestResult>
            as RunTheTests uses all the tests in one container, it gets a list of test results for every
            binary in the container. Theses are combined into one list, showing the aggregated values for
            all binaries, based on test/arch/compiler/optimization

    GetTestResultsForSingleBinary
    -----------------------------
    input: CodeInfo object, containing the ANTLR-objects + info about compiler, architecture, optimization
    output: List<SingleTestResult>
            for each test, the result is returned as a SingleTestResult
            a SingleTestResult contains: (1) what was tested, (2) compiler, (3) architecture, (4) optimization,
                                         (5) low bound, (6) high bound, (7) actual value

    Aggregation functions
    ---------------------
    implemented in SingleTestResult. Add the results of tests with the same test parameters. By setting one or
    more of them to null, one can aggregate over categories.
    e.g.: if compiler and optimization are all set to null, aggregation will make sure that for any architecture,
    all values of this architecture are aggregated, regardless of compiler and optimization
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

    public List<IAssessor.TestResult> RunTheTests(final String strContainersBaseFolder, final String strDecompileScript, final boolean allowMissingBinaries) throws Exception {
        var reuseDecompilersOutput = false;

        // create list to be able to aggregate
        final List<List<IAssessor.TestResult>> list = new ArrayList<>();

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
        int size = 0;
        for (var item : list){
            size += item.size();
        }
        var out = new ArrayList<IAssessor.TestResult>(size);
        for (var item : list){
            out.addAll(item);
        }
        IAssessor.TestResult.aggregate(out);

        // remove temporary folder
        bFolderAndAllContentsDeletedOK(tempDir);

        System.out.println("Container " + iContainerNumber);
        System.out.println("Number of tests " + iNumberOfTests);

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


    /**
     * Create a simple HTML-file that contains the data presented in a nicely readable form. No aggregation or
     * other data manipulation is done
     * @param input  list of all the presented test results
     * @param strHTMLOutputFile  target file
     */
    public static void generateReport(List<IAssessor.TestResult> input, String strHTMLOutputFile){
        var sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<table>");
        sb.append("<tr style='text-align:right; font-weight: bold'><th>Description (unit)</th><th>Architecture</th><th>Compiler</th><th>Optimization</th><th>Min score</th><th>Actual score</th><th>Max score</th><th>%</th></tr>");

        for (var item : input){
            sb.append("<tr>");
            sb.append("<td>").append(item.getWhichTest().strTestDescription()).append(" (").append(item.getWhichTest().strTestUnit()).append(")</td>");
            appendCell(sb, item.getArchitecture(), ETextAlign.CENTER, ETextColour.BLACK);
            appendCell(sb, item.getCompiler(), ETextAlign.CENTER, ETextColour.BLACK);
            appendCell(sb, item.getOptimization(), ETextAlign.CENTER, ETextColour.BLACK);
            appendCell(sb, item.dblGetLowBound(), ETextAlign.RIGHT, ETextColour.GREY);
            appendCell(sb, item.dblGetActualValue(), ETextAlign.RIGHT, ETextColour.BLACK);
            appendCell(sb, item.dblGetHighBound(), ETextAlign.RIGHT, ETextColour.GREY);
            appendCell(sb, item.strGetPercentage(), ETextAlign.RIGHT, ETextColour.GREY);
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

    private enum ETextAlign{
        LEFT, CENTER, RIGHT;
        public String strStyleProperty(){
            switch (this) {
                case LEFT -> { return "text-align:left;";
                }
                case CENTER -> { return "text-align:center;";
                }
                case RIGHT -> { return "text-align:right;";
                }
            }
            return "";
        }
    }

    private enum ETextColour{
        BLACK, GREY;
        public String strStyleProperty(){
            switch (this) {
                case BLACK -> { return "color:black;";
                }
                case GREY -> { return "color:grey;";
                }
            }
            return "";
        }
    }

    private static StringBuilder appendCell(StringBuilder sb, Object oWhat, ETextAlign textAlign, ETextColour textColour){
        sb.append("<td style='");
        sb.append(textAlign.strStyleProperty());
        sb.append(textColour.strStyleProperty());
        sb.append("'>");
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
}
