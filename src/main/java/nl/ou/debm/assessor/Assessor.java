package nl.ou.debm.assessor;

import nl.ou.debm.common.*;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.LLVMIRLexer;
import nl.ou.debm.common.antlr.LLVMIRParser;
import nl.ou.debm.common.feature1.LoopAssessor;
import nl.ou.debm.common.feature3.FunctionAssessor;
import nl.ou.debm.common.feature4.GeneralDecompilerPropertiesAssessor;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    output: List<TestResult>
            for each test, the result is returned as a TestResult (abstract)
            a TestResult contains the necessary values/accessors, implemented by child classes

    Aggregation functions
    ---------------------
    implemented in TestResult. Add the results of tests with the same test parameters. By setting one or
    more of them to null, one can aggregate over categories.
    e.g.: if compiler and optimization are all set to null, aggregation will make sure that for any architecture,
    all values of this architecture are aggregated, regardless of compiler and optimization




    NOTES ON DECOMPILER RUBBISH
    ===========================

    In the happy flow, a decompiler will produce a file containing decompiled C and this decompiled C is
    parsable by ANTLR.
    So, 8 binaries should result in 8 decompiled C files. We test them all and calculate our metrics
    using all 8.

    (A)
    Now say, 1 of those binaries is not processable by the decompiler; it fails to emit a C file.
    Do we:
    (1) simply ignore this and calculate our metrics using 7 files or
    (2) still calculate our metrics using 8 files, using a dummy one as 8th, so it will score 0 on that
    We choose (1). We want to reward the decompiler for being able to say: I can't make anything of this, sorry guys.

    (B)
    Now say, of the 7 C files produced by the decompiler, two are so crappy, ANTLR cannot parse them at all
    (generating exceptions). We have the same two choices, but we now choose (2): the decompiler thinks it's good,
    but it's not, and it should show in the statistics.

 */



public class Assessor {

    /** lock object for main synchronisation*/
    static private final Object lockObj = new Object();

    /**
     * class to handle a progress bar
     */
    private static class ProgressValues{
        /** current value */                    public int iCurrent=0;
        /** max value*/                         public int iMax=0;
        /** bar width*/                         private int m_iWidth=0;
        /** StringBuilder repres of the bar*/   private final StringBuilder m_sbBar = new StringBuilder();
        /** set bar width */
        public void SetWidth(int iWidth){
            if (iWidth>0) {
                m_iWidth = iWidth;
            }
            m_sbBar.setLength(iWidth);
        }
        /** default constructor */
        public ProgressValues(){
            SetWidth(70);
        }

        /**
         * get current progressbar
         * @return a progressbar, based on object values
         */
        public String strProgressBar() {
            // inspired by https://medium.com/javarevisited/how-to-display-progressbar-on-the-standard-console-using-java-18f01d52b30e
            double d1 = ((double) iCurrent / (double) iMax) * m_iWidth;
            int i1 = (int) d1;
            if (i1<0) { i1=0;}
            if (i1>m_iWidth) {i1=m_iWidth;}
            for (int c=0; c<m_iWidth; c++){
                if (c<i1){
                    m_sbBar.setCharAt(c, Character.toChars(0x2588)[0]);
                }
                else {
                    m_sbBar.setCharAt(c, Character.toChars(0x2591)[0]);
                }
            }
            return m_sbBar.toString();
        }
    }
    /** progress bar, also serves as sync object for progress */
    private final ProgressValues m_prv = new ProgressValues();

    static public final String s_strDummyDecompiledCFile =
        "/* This is a dummy file, automatically generated by the assessor.\n" +
        "   It was generated, because the decompiler was unable to produce\n" +
        "   any decompilation file. So, we feed our assessing feature classes\n" +
        "   this dummy file, which makes sure they don't find anything useful,\n" +
        "   but also makes sure that the test is fair. Further details: see\n" +
        "   comments in the assessor code. */\n" +
        "\n" +
        "int main(int argc, char *argv[])\n" +
        "{\n" +
        "   return 0;\n" +
        "}\n";


    private final List<IAssessor> feature = new ArrayList<>();      // array containing all assessor classes

    /**
     * constructor
     */
    public Assessor(){
        feature.addAll(getFeatureDefaults());
    }
    public Assessor(List<IAssessor> featuresToBeTested){
        // add features given by user
        if (featuresToBeTested==null){
            featuresToBeTested = getFeatureDefaults();
        }
        assert !featuresToBeTested.isEmpty() : "At least one feature must be tested!";
        feature.addAll(featuresToBeTested);
        // make sure no two features are the same
        for (int i = 0; i < feature.size() - 1; ++i) {
            for (int j = i + 1; j < feature.size(); ++j) {
                if (feature.get(i).getClass().getSimpleName().equals(feature.get(j).getClass().getSimpleName())) {
                    feature.remove(j);
                    --j;    // by lowering j, we ensure that the next j-round will start at, effectively, the newly
                            // shifted object
                }
            }
        }
    }

    private List<IAssessor> getFeatureDefaults(){
        final List<IAssessor> f = new ArrayList<>();      // array containing all assessor classes
        // add all features to array
        f.add(new LoopAssessor());
        //f.add(new DataStructuresFeature());
        f.add(new FunctionAssessor());
        f.add(new GeneralDecompilerPropertiesAssessor());
        return f;
    }

    /**
     * Main entry point for assessing a binary
     * @param strContainersBaseFolder       where are all the containers
     * @param strDecompileScript            what decompiler script to use
     * @param iRequestedContainerNumber     what container to use (if below zero, select random)
     * @param allowMissingBinaries          allow binaries not present
     * @param workMode                      determines decompilation and/or assessing
     * @param showDecompilerOutput          do or don't show decompiler output
     * @param iNThreads                     number of threads to be used, capped at maximum = number of processors, below
     *                                      1 means number of processors is used
     * @return                              list of test results
     */
    public List<IAssessor.TestResult> RunTheTests(final String strContainersBaseFolder, final String strDecompileScript,
                                                  final int iRequestedContainerNumber,
                                                  final boolean allowMissingBinaries,
                                                  EAssessorWorkModes workMode,
                                                  boolean showDecompilerOutput,
                                                  int iNThreads) throws Exception {

        // create list to be able to aggregate
        final List<IAssessor.TestResult> list = Collections.synchronizedList(new ArrayList<>());

        // set root path, to be used program-wide (as it is a static)
        Environment.containerBasePath = strContainersBaseFolder;

        // get name of the decompiler-script and test its existence & executableness
        if (!Files.isExecutable(Paths.get(strDecompileScript))) {
            throw new Exception("Decompilation script (" + strDecompileScript + ") does not exist or is not executable.");
        }

        // get container number
        String tmp = strGetContainerNumberToBeAssessed(iRequestedContainerNumber);
        final int iContainerNumber = Math.abs(Integer.parseInt(tmp));         // avoid negative number
        System.out.print("Selected container:   " + iContainerNumber);
        if (tmp.startsWith("-")){
            System.out.println("  (randomly selected; requested container does not exist)");
        }
        else{
            System.out.println();
        }

        // get number of valid tests within container
        final int iNumberOfTests = iNumberOfValidTestsInContainer(iContainerNumber);
        if (iNumberOfTests < 1) {
            throw new Exception("No valid tests in selected container (" + iContainerNumber + ").");
        }
        System.out.println("Number of tests:      " + iNumberOfTests);

        // setup temporary folder to receive the decompiler output
        var tempDir = Files.createTempDirectory("debm");

        // determine number of threads and setup thread pool
        int hardwareThreads = Runtime.getRuntime().availableProcessors();
        if ((iNThreads < 1) || (iNThreads > hardwareThreads)){
            iNThreads = hardwareThreads;
        }
        var EXEC = Executors.newFixedThreadPool(iNThreads);
        System.out.println("Threads used:         " + iNThreads);

        var tasks = new ArrayList<Callable<Object>>();
        int iBinaryIndex = 0;
        final int iTotalBinaries = iNumberOfTests * CompilerConfig.configs.size();
        m_prv.iCurrent=0;
        m_prv.iMax = iTotalBinaries;
        final boolean showDecompilerOutputLambda = (workMode==EAssessorWorkModes.ASSESS_ONLY) ? false : showDecompilerOutput;
        System.out.println("Number of binaries:   " + iTotalBinaries);
        showProgress(false);

        // remember start time
        var startTime = Instant.now();

        // run the tests
        for (int iTestNumber = 0; iTestNumber < iNumberOfTests; ++iTestNumber) {
            // read original C
            var clexer_org = new CLexer(CharStreams.fromFileName(strCSourceFullFilename(iContainerNumber, iTestNumber)));
            var cparser_org = new CParser((new CommonTokenStream(clexer_org)));
            var lines = Files.lines(Path.of(strCSourceFullFilename(iContainerNumber, iTestNumber)));
            var versionMarker = lines.map(x -> CodeMarker.findInStatement(EFeaturePrefix.METADATA, x)).filter(x -> x != null).findFirst();
            if(versionMarker.isEmpty())
                throw new RuntimeException("No version code marker found in source");
            var generatorVersion = versionMarker.get().strPropertyValue("version");
            if(!generatorVersion.equals(MetaData.Version))
                throw new RuntimeException("Version of source does not match Assessor version");
            // process all the binaries
            for(var config : CompilerConfig.configs){
                int finalITestNumber = iTestNumber;
                iBinaryIndex++;
                int finalBinaryNumber = iBinaryIndex;
                tasks.add(() -> {
                    // keep user informed
                    if (showDecompilerOutputLambda) {
                        System.out.println("Started working on binary " + finalBinaryNumber + "/" + iTotalBinaries);
                    }
                    // setup values
                    var codeinfo = new IAssessor.CodeInfo();
                    codeinfo.cparser_org = cparser_org;
                    codeinfo.clexer_org = clexer_org;
                    var strBinary = strBinaryFullFileName(iContainerNumber, finalITestNumber, config.architecture, config.compiler, config.optimization);
                    codeinfo.strAssemblyFilename = strBinary.replace("binary_", "assembly_").replace(".exe", ".s");
                    if (allowMissingBinaries && !Files.exists(Paths.get(strBinary)))
                        return null;
                    var strCDest = Paths.get(tempDir.toString(), UUID.randomUUID() + ".txt").toAbsolutePath().toString();
                    var decompilerName = Path.of(strDecompileScript).getFileName().toString();
                    decompilerName = decompilerName.substring(0, decompilerName.lastIndexOf('.'));
                    var decompilationSavePath = Path.of(strBinary.replace(".exe", "-" + decompilerName + ".c"));

                    try {
                        // invoke decompiler or copy previously produced code
                        if ((workMode == EAssessorWorkModes.ASSESS_ONLY) && Files.exists(decompilationSavePath)) {
                            // decompilation is not explicitly requested (test 1),
                            // and the previous output is available (test 2)
                            // in which case: use the previous result
                            Files.copy(decompilationSavePath, Path.of(strCDest), StandardCopyOption.REPLACE_EXISTING);
                        } else if (workMode != EAssessorWorkModes.ASSESS_ONLY) {
                            // setup new process
                            var decompileProcessBuilder = new ProcessBuilder(
                                    strDecompileScript,
                                    strBinary,
                                    strCDest
                            );
                            decompileProcessBuilder.redirectErrorStream(true);
                            // start new process
                            if (showDecompilerOutputLambda) {
                                System.out.println("Invoking decompiler for: " + strBinary);
                            }
                            var decompileProcess = decompileProcessBuilder.start();
                            var strHexPID = Misc.strGetHexNumberWithPrefixZeros(decompileProcess.pid(), 8);
                            // make sure output is processed
                            var reader = new BufferedReader(new InputStreamReader(decompileProcess.getInputStream()));
                            String line;
                            long lp = 0;
                            while ((line = reader.readLine()) != null) {
                                // read output just to get it out of any pipe
                                //
                                if (showDecompilerOutputLambda) {
                                    // only show when wanted
                                    // add process ID and line number, so output could be filtered/ sorted to make sense
                                    System.out.println(strHexPID + ", " + Misc.strGetHexNumberWithPrefixZeros(lp++, 4) + ": " + line);
                                }
                            }
                            // wait for script to end = decompilation to finish
                            decompileProcess.waitFor();
                            if (workMode == EAssessorWorkModes.DECOMPILE_ONLY) {
                                Files.copy(Path.of(strCDest), decompilationSavePath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                        // continue when
                        //   -- decompiler output files are found AND
                        //   -- assessing is requested
                        if (workMode != EAssessorWorkModes.DECOMPILE_ONLY) {
                            synchronized (lockObj) {
                                // assessing is requested
                                //
                                // set-up first basic feature-4-test: does the decompiler actually manage to produce a file
                                // this test cannot be stowed away in a feature class, as feature classes operate under
                                // the assumption that a decompiler result is available.
                                var fileProducedTest = new CountTestResult(ETestCategories.FEATURE4_DECOMPILED_FILES_PRODUCED, config);
                                fileProducedTest.setTargetMode(CountTestResult.ETargetMode.HIGHBOUND);
                                fileProducedTest.setLowBound(0);
                                fileProducedTest.setHighBound(1);
                                fileProducedTest.setTestNumber(finalITestNumber);
                                list.add(fileProducedTest);
                                //
                                // check if the file to assess exists
                                if (bFileExists(strCDest)) {
                                    // mark file as produced
                                    fileProducedTest.setActualValue(1);
                                    // now we know the file is produced, we set up a second core feature-4-class, recording
                                    // whether ANTLR crashes. This too is something we cannot stow away in a feature class,
                                    // for the same reason: a feature class operates under the assumption that an input
                                    // is non-ANTLR-crashing c code
                                    // we only add the test now, because we want a metric that shows ANTLR crashes relative
                                    // to the number of real cases ANTLR was used (and ANTLR isn't used when no decompiled
                                    // C file is produced by the decompiler)
                                    var ANTLRCrashTest = new CountTestResult(ETestCategories.FEATURE4_ANTLR_CRASHES, config);
                                    ANTLRCrashTest.setTargetMode(CountTestResult.ETargetMode.LOWBOUND);
                                    ANTLRCrashTest.setLowBound(0);
                                    ANTLRCrashTest.setHighBound(1);
                                    ANTLRCrashTest.setTestNumber(finalITestNumber);
                                    list.add(ANTLRCrashTest);
                                    // copy the produced decompiled file to the container
                                    Files.copy(Path.of(strCDest), decompilationSavePath, StandardCopyOption.REPLACE_EXISTING);
                                    codeinfo.compilerConfig.copyFrom(config);
                                    // read decompiled C
                                    codeinfo.clexer_dec = new CLexer(CharStreams.fromFileName(strCDest));
                                    codeinfo.cparser_dec = new CParser(new CommonTokenStream(codeinfo.clexer_dec));
                                    // read compiler LLVM output
                                    codeinfo.llexer_org = new LLVMIRLexer(CharStreams.fromFileName(strLLVMFullFileName(iContainerNumber, finalITestNumber, config.architecture, config.compiler, config.optimization)));
                                    codeinfo.lparser_org = new LLVMIRParser(new CommonTokenStream(codeinfo.llexer_org));
                                    // remember file name (which comes in handy for debugging + is needed for a line count in feature 4)
                                    codeinfo.strDecompiledCFilename = decompilationSavePath.toString();
                                    /////////////////////////
                                    // invoke all features //
                                    /////////////////////////
                                    //
                                    // ANTLR can throw errors if the code to be parsed is really rubbish. When this
                                    // happens, we present all the features with a dummy input, where nothing will be found,
                                    // which will lower the aggregated scores.
                                    //
                                    // two attempts
                                    // on the first attempt: use file input (set above)
                                    // on the second attempt (meaning exception running the first): use dummy input
                                    //
                                    // we collect the results in a temporary list. Suppose ANTLR crashes on feature 2. That
                                    // would mean the feature 1 score was already added to the gross list. Rerunning all the
                                    // features on dummy code would result in a second feature 1 score being added. This is
                                    // unwanted. So, if ANTLR crashes, we throw away the lot; if the code is that rubbish,
                                    // the decompiler deserves no better
                                    final List<IAssessor.TestResult> singleBinaryList = Collections.synchronizedList(new ArrayList<>());
                                    boolean bAllGoneWell;
                                    bAllGoneWell = tryAssessment(false, codeinfo, singleBinaryList, ANTLRCrashTest, finalITestNumber);
                                    if (!bAllGoneWell) {
                                        // we do not need to record ANTLR's crash as the ANTLRCrashTest object is modified by tryAssessment()
                                        singleBinaryList.clear();
                                        tryAssessment(true, codeinfo, singleBinaryList, ANTLRCrashTest, finalITestNumber);
                                    }
                                    list.addAll(singleBinaryList);
                                }
                            }
                            // no else needed for file-exist-check, as the test is already added to the list
                            // and the actual value remains at its originally set value of 0.
                        }
                        // show progress
                        if (showDecompilerOutputLambda) {
                            // if decompiler output is shown, set a prefix to the progress output
                            // so it can be filtered when wanted
                            System.out.print("HHHHHHHH  ");
                        }
                        showProgress(true);
                        if (showDecompilerOutputLambda) {
                            // if decompiler output is shown, new output should be on the next line
                            System.out.println();
                        }
                    }
                    catch (Throwable t){
                        System.out.println("File: " + decompilationSavePath + " caused " + t.toString());
                        t.printStackTrace();
                    }
                    // task done
                    return 0;
                });
            }
        }
        var returns = EXEC.invokeAll(tasks);
        for (Future<Object> r : returns) {
            try {
                Object temp = r.get();
                if (temp != null) {
                    if (!temp.toString().equals("0")) {
                        System.out.println("returned " + temp);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted Exception catch");
                e.printStackTrace();
            } catch (ExecutionException e) {
                System.out.println("ExExp");
                throw new RuntimeException(e);  //// THIS CAUSES HANGUP
            }
        }

        // remove temporary folder
        bFolderAndAllContentsDeletedOK(tempDir);

        // strip feature 4 when not wanted
        // (needs to be done as two of the basic feature 4 tests are performed hard wired in this assessor
        // code)
        boolean bRemoveFeature4 = true;
        for (var itm : feature){
            if (itm instanceof GeneralDecompilerPropertiesAssessor){
                bRemoveFeature4 = false;
                break;
            }
        }
        if (bRemoveFeature4){
            for (int i = list.size()-1; i>=0; --i){
                if (list.get(i).getWhichTest().bIsFeature4Test()){
                    list.remove(i);
                }
            }
        }

        // remember finish time
        var endTime = Instant.now();        // formatting code borrowed from: https://www.geeksforgeeks.org/how-to-format-seconds-in-java/
        long seconds = Duration.between(startTime, endTime).toSeconds();
        System.out.println("Time elapsed:         " + Duration.ofSeconds(seconds).toString()
                                                                                 .substring(2)
                                                                                 .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                                                                                 .toLowerCase());

        // return results
        return list;
    }

    /** show that another test is finished*/
    private void showProgress(boolean bCount){
        synchronized (m_prv){
            if (bCount) {
                m_prv.iCurrent++;
            }
            String out = m_prv.strProgressBar() + "  (" + m_prv.iCurrent + "/" + m_prv.iMax + ")\r";
            System.out.print(out);
        }
    }

    private boolean tryAssessment(boolean useDummy, IAssessor.CodeInfo codeinfo, List<IAssessor.TestResult> list, CountTestResult ANTLRCrashTest, int finalITestNumber) {
        if (useDummy) {
            // set decompiled C to dummy
            codeinfo.clexer_dec = new CLexer(CharStreams.fromString(s_strDummyDecompiledCFile));
            codeinfo.cparser_dec = new CParser(new CommonTokenStream(codeinfo.clexer_dec));
        }
        PrintStream currentStdErr = null;
        // invoke all features
        for (var f : feature) {
            // do the assessing
            List<IAssessor.TestResult> testResult = null;
            try {
                // reset all parsers before invoking the several features
                codeinfo.cparser_org.reset();
                codeinfo.cparser_dec.reset();
                codeinfo.lparser_org.reset();
                // re-rout stderr to null, so we won't see all the ANTLR-stderr-output (it only messes the output up,
                // and we can catch the output in feature 4 anyway)
                currentStdErr = System.err;
                System.setErr(new Misc.NullPrintStream());
                // do feature
                testResult = f.GetTestResultsForSingleBinary(codeinfo);
                // all went well, thus restore stdErr
                System.setErr(currentStdErr);
            } catch (RecognitionException e) {
                // something went wrong...
                // mark this file as an ANTLR-crash
                ANTLRCrashTest.setActualValue(1);
                // restore stderr
                restoreStdErr(currentStdErr);
                return false;
            } catch(Exception e){
                var stackTrace = e.getStackTrace();
                if(stackTrace.length > 0 && stackTrace[0].getClassName().startsWith("org.antlr")){
                    // something went wrong...
                    // mark this file as an ANTLR-crash
                    ANTLRCrashTest.setActualValue(1);
                    restoreStdErr(currentStdErr);
                    return false;
                }else {
                    restoreStdErr(currentStdErr);
                    throw e;
                }
            }
            // add a test serial number
            for (var item : testResult)
                item.setTestNumber(finalITestNumber);
            // add results to the complete list of test results
            list.addAll(testResult);
        }
        return true;
    }

    private void restoreStdErr(PrintStream oldStdErr){
        if (oldStdErr!=null) {
            if (System.err!=oldStdErr){
                System.setErr(oldStdErr);
            }
        }
    }

    private String strGetContainerNumberToBeAssessed(int iInput){
        // make sure root folder exists
        assert IOElements.bFolderExists(Environment.containerBasePath) : "Container root folder (" + Environment.containerBasePath + ") does not exist";

        // specific input wanted & present?
        if ((iInput>=0) && (IOElements.bFolderExists(IOElements.strContainerFullPath(iInput)))) {
            return iInput + "";
        }

        // get random container
        // --------------------
        // make list of all the container numbers that are present
        // select a random element
        List<Integer> containerIndex = new ArrayList<>(1000);
        for (int ci = 0; ci<1000; ci++){
            if (IOElements.bFolderExists(IOElements.strContainerFullPath(ci))){
                containerIndex.add(ci);
            }
        }
        assert !containerIndex.isEmpty() : "Container root folder (" + Environment.containerBasePath + ") has no containers";

        // return random index
        if (iInput>=0){
            // make negative to indicate that a random container was used
            return "-" + containerIndex.get(Misc.rnd.nextInt(containerIndex.size()));
        }
        else{
            return "" + containerIndex.get(Misc.rnd.nextInt(containerIndex.size()));
        }
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
    public static void generateHTMLReport(List<IAssessor.TestResult> input, String strHTMLOutputFile, boolean bAddTestColumns) {
        generateHTMLReport(new HashMap<String, String>(), input, strHTMLOutputFile, bAddTestColumns);
    }

    /**
     * Get html code to put before one or more tables and behind the (set of) table(s), so a complete
     * html file is created
     * @param sb_header will be erased and will contain header after function call
     * @param sb_footer will be erased and will contain footer after function call
     */
    public static void getHTMLHeaderAndFooter(StringBuilder sb_header, StringBuilder sb_footer){
        // header
        assert sb_header!=null;
        sb_header.setLength(0);
        sb_header.append("<html>");
        sb_header.append("<head>");
        sb_header.append("<style>");
        sb_header.append("table, th, td {border: 1px solid black; border-collapse: collapse;padding: 3px;}");
        sb_header.append("</style>");
        sb_header.append("</head>");
        sb_header.append("<body>");

        // footer
        assert sb_footer!=null;
        sb_footer.setLength(0);
        sb_footer.append("</body></html>");
    }

    public static void getXMLHeaderAndFooter(StringBuilder sb_header, StringBuilder sb_footer){
        // header
        assert sb_header!=null;
        sb_header.setLength(0);
        sb_header.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb_header.append("<!DOCTYPE html PUBLIC \"-//DEBM//EN\" \"https://raw.githubusercontent.com/reijer-dev/decompiler-benchmarking/master/xml/output.dtd\">");
        sb_header.append("<debm xmlns=\"https://raw.githubusercontent.com/reijer-dev/decompiler-benchmarking/master/xml/output.dtd\">");

        // footer
        assert sb_footer!=null;
        sb_footer.setLength(0);
        sb_footer.append("</debm>");
    }

    /**
     * Create a simple HTML-file that contains the data presented in a nicely readable form. No aggregation or
     * other data manipulation is done.
     * @param input  list of all the presented test results
     * @param pars   map of custom parameter list to be added as info before data table
     * @param bSortOutput if true, output is sorted per test/arch/compiler/opt
     * @param strHTMLOutputFile the file to which the output must be written
     */
    public static void generateHTMLReport(Map<String, String> pars, List<IAssessor.TestResult> input, String strHTMLOutputFile, boolean bSortOutput, boolean bAddTestColumns){
        StringBuilder sb_t = generateHTMLReport(pars, input, bSortOutput, bAddTestColumns);
        StringBuilder sb_h = new StringBuilder(), sb_f = new StringBuilder();
        getHTMLHeaderAndFooter(sb_h, sb_f);
        sb_h.append(sb_t).append(sb_f);
        IOElements.writeToFile(sb_h, strHTMLOutputFile);
    }

    /**
         * Create a simple HTML-file that contains the data presented in a nicely readable form. No aggregation or
         * other data manipulation is done. Test results are sorted in ascending order: test, arch, compiler, optimization.
         * @param input  list of all the presented test results
         * @param pars   map of custom parameter list to be added as info before data table
         * @param strHTMLOutputFile  target file
         */
    public static void generateHTMLReport(Map<String, String> pars, List<IAssessor.TestResult> input, String strHTMLOutputFile, boolean bAddTestColumns){
        generateHTMLReport(pars, input, strHTMLOutputFile, true, bAddTestColumns);
    }

    /**
     * Create a simple HTML-table (or two, if a parameter set is given)
     * that contains the data presented in a nicely readable form. No aggregation or
     * other data manipulation is done. Test results are sorted in ascending order: test, arch, compiler, optimization.
     * @param input  list of all the presented test results
     * @param pars   map of custom parameter list to be added as info before data table
     * @return HTML-table
     */
    public static StringBuilder generateHTMLReport(Map<String, String> pars, List<IAssessor.TestResult> input, boolean bAddTestColumns){
        return generateHTMLReport(pars, input, true, bAddTestColumns);
    }

    /**
     * Create a simple HTML-table (or two, if a parameter set is given)
     * that contains the data presented in a nicely readable form. No aggregation or
     * other data manipulation is done.
     * @param input  list of all the presented test results
     * @param pars   map of custom parameter list to be added as info before data table
     * @param bSortOutput if true, output is sorted per test/arch/compiler/opt
     * @param bAddTestColumns if true, add column for every single test case and the std deviation
     * @return HTML-table
     */
    public static StringBuilder generateHTMLReport(Map<String, String> pars, List<IAssessor.TestResult> input,
                                                   boolean bSortOutput, boolean bAddTestColumns){
        // sort the lot?
        List<IAssessor.TestResult> adaptedInput;
        if (bSortOutput){
            adaptedInput = new ArrayList<>(input);
            input.sort(new IAssessor.TestResultComparatorWithTestNumber());
        }
        else{
            adaptedInput = input;
        }

        // initialize output
        var sb = new StringBuilder();

        // parameter table
        if (pars!=null) {
            if (!pars.isEmpty()) {
                sb.append("<table>");
                sb.append("<tr style='text-align:center; font-weight: bold'><th>Description</th><th>Value</th></tr>");
                for (var item : pars.entrySet()) {
                    sb.append("<tr>");
                    sb.append("<td>").append(item.getKey()).append("</td>");
                    sb.append("<td>").append(item.getValue()).append("</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");
            }
        }

        // data table initialization
        sb.append("<table>");
        sb.append("<tr style='text-align:center; font-weight: bold'><th>Description (unit)</th><th>Architecture</th><th>Compiler</th><th>Optimization</th><th>Min score</th><th>Actual score</th><th>Max score</th><th>Target score</th><th>% min/max</th><th># tests</th>");
        var maxTests = 0;
        if (bAddTestColumns) {
            maxTests = adaptedInput.stream().map(x -> x.getScoresPerTest().size()).max(Comparator.comparingInt(x -> x)).orElse(0);
            for (var i = 0; i < maxTests; i++)
                sb.append("<th>Test ").append(i + 1).append("</th>");
            sb.append("<th>Standard deviation</th></tr>");
        }

        // fill data table
        var evenRow = true;
        ETestCategories currentTestCategory = null;
        for (var item : adaptedInput){
            sb.append("<tr><td>");
            if(currentTestCategory != item.getWhichTest())
                sb.append(item.getWhichTest().strTestDescription()).append(" (").append(item.getWhichTest().strTestUnit()).append(")");
            sb.append("</td>");
            appendCell(sb, evenRow, item.getArchitecture(), ETextAlign.CENTER, ETextColour.BLACK, false, 0);
            appendCell(sb, evenRow, item.getCompiler(), ETextAlign.CENTER, ETextColour.BLACK,false, 0 );
            appendCell(sb, evenRow, item.getOptimization(), ETextAlign.CENTER, ETextColour.BLACK, false, 0);
            appendCell(sb, evenRow, item.dblGetLowBound(), ETextAlign.RIGHT, ETextColour.GREY, false, item.iGetNumberOfDecimalsToBePrinted());
            appendCell(sb, evenRow, item.dblGetActualValue(), ETextAlign.RIGHT, ETextColour.BLACK, false, item.iGetNumberOfDecimalsToBePrinted());
            appendCell(sb, evenRow, item.dblGetHighBound(), ETextAlign.RIGHT, ETextColour.GREY, false, item.iGetNumberOfDecimalsToBePrinted());
            appendCell(sb, evenRow, item.dblGetTarget(), ETextAlign.RIGHT, ETextColour.GREY, false, item.iGetNumberOfDecimalsToBePrinted());
            appendCell(sb, evenRow, item.strGetPercentage(), ETextAlign.RIGHT, ETextColour.BLACK, true, -1);
            appendCell(sb, evenRow, item.iGetNumberOfTests(), ETextAlign.RIGHT, ETextColour.GREY, false, 0);
            if (bAddTestColumns) {
                for (var i = 0; i < maxTests; i++) {
                    if (i < item.getScoresPerTest().size())
                        appendCell(sb, evenRow, item.getScoresPerTest().get(i), ETextAlign.RIGHT, ETextColour.GREY, false, 2);
                    else
                        appendCell(sb, evenRow, "-", ETextAlign.LEFT, ETextColour.GREY, false, 2);
                }
                appendCell(sb, evenRow, item.dblGetStandardDeviation(), ETextAlign.RIGHT, ETextColour.BLACK, false, 2);
            }
            sb.append("</tr>");
            currentTestCategory = item.getWhichTest();
            evenRow = !evenRow;
        }

        // finalize output
        sb.append("</table>");
        return sb;
    }

    public static void generateXMLReport(Map<String, String> pars, List<IAssessor.TestResult> input, String strXMLOutputFile, boolean bSortOutput){
        StringBuilder sb_t = generateXMLReport(pars, input, bSortOutput);
        StringBuilder sb_h = new StringBuilder(), sb_f = new StringBuilder();
        getHTMLHeaderAndFooter(sb_h, sb_f);
        sb_h.append(sb_t).append(sb_f);
        IOElements.writeToFile(sb_h, strXMLOutputFile);
    }

    public static StringBuilder generateXMLReport(Map<String, String> pars, List<IAssessor.TestResult> input, boolean bSortOutput){
        final String STRTABLEPROPERTY = "tableproperty";
        final String STRPROPNAME = "propname";
        final String STRPROPVALUE = "propvalue";
        final String STRTESTRESULT = "testresult";
        final String STRTESTID = "testid";
        final String STRTESTDESC = "testdescription";
        final String STRTESTUNIT = "testunit";
        final String STRARCH = "architecture";
        final String STRCOMP = "compiler";
        final String STROPT = "optimization";
        final String STRMIN = "minvalue";
        final String STRACT = "actualvalue";
        final String STRMAX = "maxvalue";
        final String STRTAR = "targetvalue";
        final String STRCNT = "testcount";

        // sort the lot?
        List<IAssessor.TestResult> adaptedInput;
        if (bSortOutput){
            adaptedInput = new ArrayList<>(input);
            input.sort(new IAssessor.TestResultComparatorWithTestNumber());
        }
        else{
            adaptedInput = input;
        }

        // initialize output
        var sb = new StringBuilder();

        // new table
        sb.append("<table>");

        // parameter table
        if (pars!=null){
            if (!pars.isEmpty()) {
                for (var item : pars.entrySet()) {
                    appendXMLStartTag(sb, STRTABLEPROPERTY);
                    appendXMLSingleValue(sb, STRPROPNAME, item.getKey(), 0);
                    appendXMLSingleValue(sb, STRPROPVALUE, item.getValue(), 0);
                    appendXMLEndTag(sb, STRTABLEPROPERTY);
                }
            }
        }

        // fill data table
        for (var item : adaptedInput) {
            appendXMLStartTag(sb, STRTESTRESULT);
            appendXMLSingleValue(sb, STRTESTID, item.getWhichTest().lngUniversalIdentifier(), 0);
            appendXMLSingleValue(sb, STRTESTDESC, item.getWhichTest().strTestDescription(), 0);
            appendXMLSingleValue(sb, STRTESTUNIT, item.getWhichTest().strTestUnit(), 0);
            appendXMLSingleValue(sb, STRARCH, item.getArchitecture(),0);
            appendXMLSingleValue(sb, STRCOMP, item.getCompiler(), 0);
            appendXMLSingleValue(sb, STROPT, item.getOptimization(),0 );
            appendXMLSingleValue(sb, STRMIN, item.dblGetLowBound(), item.iGetNumberOfDecimalsToBePrinted());
            appendXMLSingleValue(sb, STRMAX, item.dblGetHighBound(), item.iGetNumberOfDecimalsToBePrinted());
            appendXMLSingleValue(sb, STRACT, item.dblGetActualValue(), item.iGetNumberOfDecimalsToBePrinted());
            appendXMLSingleValue(sb, STRTAR, item.dblGetTarget(), item.iGetNumberOfDecimalsToBePrinted());
            appendXMLSingleValue(sb, STRCNT, item.iGetNumberOfTests(), 0);
            appendXMLEndTag(sb, STRTESTRESULT);
        }
        // finalize output
        sb.append("</table>");
        return sb;
    }

    private static void appendXMLStartTag(StringBuilder sb, String strTag){
        sb.append("<").append(strTag).append(">");
    }

    private static void appendXMLEndTag(StringBuilder sb, String strTag){
        sb.append("</").append(strTag).append(">");
    }

    private static void appendXMLSingleValue(StringBuilder sb, String strTag, Object oValue, int iNumberOfDecimals){
        appendXMLStartTag(sb, strTag);
        sb.append(strCellValue(oValue, iNumberOfDecimals));
        appendXMLEndTag(sb, strTag);
    }

    /** enum to store html cell text align values */
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

    /** enum to store html text colors */
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

    /** enum to store html text colors */
    private enum EBackgroundColour{
        WHITE, GREY;
        public String strStyleProperty(){
            switch (this) {
                case WHITE -> { return "";
                }
                case GREY -> { return "background-color:#ccc;";
                }
            }
            return "";
        }
    }

    /**
     * Append a value as a cell to a StringBuilder object
     * @param sb StringBuilder to use
     * @param oWhat  what is to be added
     * @param textAlign text alignment
     * @param textColour text color
     * @param evenRow true if row is even (make shading pattern possible)
     * @param iNumberOfDecimals only used when printing a decimal value; number of decimals to be printed
     */
    private static void appendCell(StringBuilder sb, Boolean evenRow, Object oWhat, ETextAlign textAlign, ETextColour textColour, boolean bBold, int iNumberOfDecimals){
        sb.append("<td style='");
        sb.append(textAlign.strStyleProperty());
        sb.append(textColour.strStyleProperty());
        sb.append((evenRow ? EBackgroundColour.WHITE : EBackgroundColour.GREY).strStyleProperty());
        sb.append("'>");
        if (bBold){
            sb.append("<b>");
        }
        sb.append(strCellValue(oWhat, iNumberOfDecimals));
        if (bBold){
            sb.append("</b>");
        }
        sb.append("</td>");
    }

    /**
     * Get cell value
     * @param oWhat the value to process
     * @param iNumberOfDecimals number of decimals to use if it's a number
     * @return neat string value; may be empty if oWhat is not recognized
     */
    private static String strCellValue(Object oWhat, int iNumberOfDecimals){
        String strWhat = null;
        if (oWhat!=null) {
            if (bIsNumeric(oWhat)) {
                double val = 0;
                if (oWhat instanceof Double) {
                    val = (Double) oWhat;
                } else if (oWhat instanceof Float) {
                    val = (double) ((Float) oWhat);
                } else if (oWhat instanceof Integer) {
                    val = (double) ((Integer) oWhat);
                } else if (oWhat instanceof Long) {
                    val = (double) ((Long) oWhat);
                }
                String strFormat = "%." + iNumberOfDecimals + "f";
                strWhat=String.format(Locale.ROOT, strFormat, val);
            } else if (oWhat instanceof String) {
                strWhat = (String) oWhat;
            } else if (oWhat instanceof EArchitecture) {
                strWhat = ((EArchitecture) oWhat).strTableCode();
            } else if (oWhat instanceof ECompiler) {
                strWhat = ((ECompiler) oWhat).strTableCode();
            } else if (oWhat instanceof EOptimize) {
                strWhat = ((EOptimize) oWhat).strTableCode();
            }
        }
        return strWhat == null ? "" : strWhat;
    }

    private static boolean bIsNumeric(Object oWhat){
        return (oWhat instanceof Integer) ||
               (oWhat instanceof Long) ||
               (oWhat instanceof Double) ||
               (oWhat instanceof Float);
    }

    public static String generateTikzPicture(HashMap<String, List<IAssessor.TestResult>> input){
        var sb = new StringBuilder();
        var metrics = input.values().stream().findFirst().get().stream().map(x -> x.getWhichTest()).toList();

        line(sb,"\\pgfplotstableread[row sep=\\\\,col sep=&]{");
        line(sb,"    metric   & " + String.join(" & ", input.keySet()) + " \\\\");
        for(var metric : metrics){
            sb.append(metric.strTestDescription());
            for(var value : input.values().stream().map(x -> x.stream().filter(y -> y.getWhichTest() == metric).findFirst()).toList()){
                sb.append(" & ");
                if(value.isPresent())
                    sb.append(value.get().dblGetFraction() * 100d);
                else
                    sb.append(0);
            }
            line(sb," \\\\");
        }
        line(sb,"}\\mydata");
        line(sb,"");

        var labels = String.join(",", metrics.stream().map(x -> x.strTestDescription()).toList());
        line(sb,"\\begin{tikzpicture}");
        line(sb,"    \\begin{axis}[");
        line(sb,"        ybar,");
        line(sb,"                bar width=.5cm,");
        line(sb,"                width=\\textwidth,");
        line(sb,"                height=.5\\textwidth,");
        line(sb,"                legend style={at={(0.5,1)},");
        line(sb,"        anchor=north,legend columns=-1},");
        line(sb,"        symbolic x coords={"+labels+"},");
        line(sb,"                x tick label style={font=\\small,text width=1.7cm,align=center},");
        line(sb,"                xtick=data,");
        line(sb,"                nodes near coords,");
        line(sb,"        nodes near coords align={vertical},");
        line(sb,"                ymin=0,ymax=100,");
        line(sb,"                ylabel={\\%},");
        line(sb,"        ]");
        for(var test : input.keySet())
            line(sb,"        \\addplot table[x=metric,y="+test+"]{\\mydata};");
        line(sb,"        \\legend{"+String.join(", ", input.keySet())+"}");
        line(sb,"    \\end{axis}");
        line(sb,"\\end{tikzpicture}");

        return sb.toString();
    }

    private static void line(StringBuilder sb, String str){
        sb.append(str + "\r\n");
    }
}
