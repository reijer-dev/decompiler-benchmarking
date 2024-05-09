package nl.ou.debm.common.feature2;

import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// general utilities for parsing, not specifically tied to decompiler testing
public class Parsing {

    static ArrayList<String> builtinTypeSpecifiers = new ArrayList<>(Arrays.asList(
        "void",
        "char",
        "short",
        "int",
        "long",
        "float",
        "double",
        "signed",
        "unsigned",
        "_Bool"
    ));

    public static CParser makeParser(String code) {
        var lexer = new CLexer(CharStreams.fromString(code));
        return new CParser(new CommonTokenStream(lexer));
    }

    public static void assertNoErrors(CParser parser)
    {
        if (parser.getNumberOfSyntaxErrors() > 0)
            throw new RuntimeException("syntax error encountered");
    }

    // This is an alternative to ctx.getText, which returns code without spaces, which is often problematic.
    /*
    public static String originalCode(ParserRuleContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        Interval interval = new Interval(a,b);
        return ctx.start.getInputStream().getText(interval);
    }
    */

    // Converts a ParseTree to code in a standardized form. It works by concatenating all terminal nodes in the parse tree, separated by spaces. This results in the same code, except that:
    // - comments are gone (because they're not part of the parse tree)
    // - there are no sequences of whitespace. The only kind of whitespace that occurs is a single space.
    // In addition to that:
    // - consecutive string literals are merged.
    public static String normalizedCode(ParseTree tree) {
        var accumulator = new StringBuilder();
        normalizedCodeRecursive(tree, accumulator);
        return accumulator.toString().trim();
    }

    private static void normalizedCodeRecursive(ParseTree tree, StringBuilder accumulator)
    {
        for (int i = 0; i<tree.getChildCount(); i++)
        {
            ParseTree child = tree.getChild(i);

            //If there are consecutive string literals, convert them to a single literal. For example, in C, "a" "b" means the same as "ab".
            if (child instanceof CParser.PrimaryExpressionContext casted) {
                var stringLiterals = casted.StringLiteral();
                if (stringLiterals != null && stringLiterals.size() > 1) {
                    accumulator.append('"');
                    for (var stringLiteral : stringLiterals) {
                        var txt = stringLiteral.getText();
                        var withoutQuotes = txt.substring(1, txt.length() - 1);
                        accumulator.append(withoutQuotes);
                    }
                    accumulator.append('"').append(' ');
                    continue;
                }
            }

            if (child instanceof TerminalNode node) {
                accumulator.append(node.getText()).append(' '); //I separate them all with spaces just to be safe.
            }
            else {
                normalizedCodeRecursive(child, accumulator);
            }
        }
    }

    // This requires a workaround because the C grammar we use doesn't allow for visiting identifiers (or any other types of terminal nodes)
    public static void getIdentifiers(ParseTree tree, List<String> dest)
    {
        for (int i = 0; i<tree.getChildCount(); i++){
            ParseTree child = tree.getChild(i);
            if (child instanceof TerminalNode node) {
                if (node.getSymbol().getType() == CParser.Identifier) {
                    dest.add(node.getText());
                }
            }
            else {
                getIdentifiers(child, dest);
            }
        }
    }

}
