package nl.ou.debm.assessor;

import nl.ou.debm.common.IOElements;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.MyCListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

import static nl.ou.debm.common.IOElements.bFolderExists;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws Exception {
        // set root path
        IOElements.setBasePath(Misc.strGetContainersBaseFolder());

        // get container number
        final int iContainerNumber = iGetContainerNumberToBeAssessed();

        // get number of valid tests within container
        final int iNumberOfTests = iNumberOfValidTestsInContainer(iContainerNumber);
        if (iNumberOfTests<1){ throw new Exception("No valid tests in selected container (" + iContainerNumber + ")."); }


        System.out.println("Container " + iContainerNumber);
        System.out.println("Number of tests " +iNumberOfTests);
    }

    /**
     * Get the number of the container that is to be tested/assessed
     * @return  ID, ranging 1...200
     */
    static int iGetContainerNumberToBeAssessed(){
        // TODO: Implement getting a container number from anywhere
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
                --iTestNumber;
                return iTestNumber;
            }

        }

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