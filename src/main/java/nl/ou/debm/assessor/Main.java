package nl.ou.debm.assessor;

import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.MyCListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

import static nl.ou.debm.common.IOElements.strAdaptPathToMatchFileSystemAndAddSeparator;
import static nl.ou.debm.common.Misc.strGetContainersBaseFolder;

public class Main {

    public static void main(String[] args) throws Exception {
        var ass = new Assessor();
        ass.RunTheTests(strGetContainersBaseFolder(),
                strAdaptPathToMatchFileSystemAndAddSeparator(strGetContainersBaseFolder()) + "decompile.sh");
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
