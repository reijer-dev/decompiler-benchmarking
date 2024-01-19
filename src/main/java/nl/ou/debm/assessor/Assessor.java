package nl.ou.debm.assessor;

import nl.ou.debm.common.*;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.LLVMIRLexer;
import nl.ou.debm.common.antlr.LLVMIRParser;
import nl.ou.debm.common.feature1.LoopAssessor;
import nl.ou.debm.common.feature3.FunctionAssessor;
import nl.ou.debm.common.feature3.FunctionCodeMarker;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;

import static nl.ou.debm.common.IOElements.*;

public class Assessor {

    private final ArrayList<IAssessor> feature = new ArrayList<IAssessor>();      // array containing all assessor classes
    public final ArrayList<IAssessor.SingleTestResult> testResults = new ArrayList<>();

    /**
     * constructor
     */
    public Assessor(){
        // add all features to array
        //feature.add(new DataStructuresFeature());
        feature.add(new FunctionAssessor());
    }

    public void RunTheTests(final String strContainersBaseFolder, final String strDecompileScript, final boolean allowMissingBinaries) throws Exception {
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
                    for (var opt : Arrays.stream(EOptimize.values()).filter(x -> x == EOptimize.NO_OPTIMIZE).toList()) {
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
                            codeinfo.architecture = architecture;
                            codeinfo.optimizationLevel = opt;
                            // read decompiled C
                            codeinfo.clexer_dec = new CLexer(CharStreams.fromFileName(strCDest));
                            codeinfo.cparser_dec = new CParser(new CommonTokenStream(codeinfo.clexer_dec));
                            // read compiler LLVM output
                            codeinfo.llexer_org = new LLVMIRLexer(CharStreams.fromFileName(strLLVMFullFileName(iContainerNumber, iTestNumber, architecture, compiler, opt)));
                            codeinfo.lparser_org = new LLVMIRParser(new CommonTokenStream(codeinfo.llexer_org));
                            // invoke all features
                            for (var f : feature){
                                var testResult = f.GetSingleTestResult(codeinfo);
                                if(!testResult.skipped)
                                    testResults.add(testResult);
                            }
                            // no need to delete decompilation files here, as they as deleted before
                            // decompilation script is run. The last decompilation files will be
                            // deleted when the temp dir is deleted
                        }
                    }
                }
            }
        }

        for (var f : feature){
            if(f instanceof FunctionAssessor functionAssessor)
                functionAssessor.generateReport();
        }

        // remove temporary folder
        bFolderAndAllContentsDeletedOK(tempDir);

        System.out.println("Container " + iContainerNumber);
        System.out.println("Number of tests " + iNumberOfTests);
    }

    /**
     * Get the number of the container that is to be tested/assessed
     * @return  ID, ranging 0...199
     */
    int iGetContainerNumberToBeAssessed(){
        // TODO: Implement getting a container number from anywhere
        //       (command line input, random something, whatever)
        //       for now: just return 1 for test purposes
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
}
