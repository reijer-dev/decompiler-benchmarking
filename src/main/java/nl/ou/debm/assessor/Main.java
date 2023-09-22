package nl.ou.debm.assessor;

import nl.ou.debm.common.TestLexer;
import nl.ou.debm.common.TestParser;
import nl.ou.debm.common.lib;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

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
        TestLexer l=new TestLexer(CharStreams.fromString("hallo John"));
        TestParser p=new TestParser(new CommonTokenStream(l));

        String name = p.prule().name().getText();
        System.out.println("Ik vond een naam: " + name);
    }
}