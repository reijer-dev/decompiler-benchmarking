package nl.ou.debm.assessor;

import nl.ou.debm.common.*;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.MyCListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import java.io.IOException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static nl.ou.debm.common.IOElements.*;
import static nl.ou.debm.common.Misc.strGetContainersBaseFolder;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws Exception {
        // set root path, to be used program-wide (as it is a static)
        // TODO: get this from a parameter
        IOElements.setBasePath(strGetContainersBaseFolder());

        // get name of the decompiler-script and test its existence & executableness
        // TODO: get this from a parameter
        final String strDecompileScript=strAdaptPathToMatchFileSystemAndAddSeparator(strGetContainersBaseFolder() + "decompile_using_retdec");
        if (!Files.isExecutable(Paths.get(strDecompileScript))){
            throw new Exception("Decompilation script (" + strDecompileScript + ") does not exist or is not executable.");
        }

        // get container number
        final int iContainerNumber = iGetContainerNumberToBeAssessed();

        // get number of valid tests within container
        final int iNumberOfTests = iNumberOfValidTestsInContainer(iContainerNumber);
        if (iNumberOfTests<1){
            throw new Exception("No valid tests in selected container (" + iContainerNumber + ").");
        }

        // select a test within the container
        final int iTestNumber = 1;  // TODO: IMPLEMENT THIS

        // invoke decompiler for every binary
        for (var compiler : ECompiler.values()){
            for (var architecture : EArchitecture.values()){
                for (var opt : EOptimize.values()){
                    //
                }
            }
        }


        System.out.println("Container " + iContainerNumber);
        System.out.println("Number of tests " +iNumberOfTests);
    }

    /**
     * Get the number of the container that is to be tested/assessed
     * @return  ID, ranging 1...200
     */
    static int iGetContainerNumberToBeAssessed(){
        // TODO: Implement getting a container number from anywhere
        //       (command line input, random something, whatever)
        //       for now: just return 1 for test purposes
        return 1;
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
    static int iNumberOfValidTestsInContainer(int iContainerNumber){
        int iTestNumber=0;

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

        --iTestNumber;
        return iTestNumber;
    }


    public static void ANTLRTest(){
    /*    var strCCode = """
                #include <stdio.h>
                void pp(){
                 printf("functie");
                }
                int main(){
                 int a=10;
                 int b=a;
                 printf("hoi");
                 for (int c=0;c<10;++c){printf("hallo!");}
                 pp();
                 return 15;}""";
        System.out.println(strCCode);
        System.out.println("---------------------");*/
        //var lexer = new CLexer(CharStreams.fromString(strCCode));
        CLexer lexer;
        try {
            lexer = new CLexer(CharStreams.fromFileName("/home/jaap/VAF/decompiler-benchmarking/src/main/java/nl/ou/debm/assessor/antlrtest.c"));
        }
        catch (IOException e){
            System.out.println(e.toString());
            return;
        }
        var tokens = new CommonTokenStream(lexer);
        var parser = new CParser(tokens);
        var tree = parser.compilationUnit();

        var listener = new MyCListener();
        var walker = new ParseTreeWalker();
        walker.walk(listener, tree);
    }
}
