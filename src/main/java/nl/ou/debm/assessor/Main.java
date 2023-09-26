package nl.ou.debm.assessor;

import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.antlr.MyCListener;
import nl.ou.debm.common.lib;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        System.out.println("Dit is de assessor");
        System.out.println("Gebruik van common: ");
        System.out.println(lib.inc(1));
        ANTLRTest();
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