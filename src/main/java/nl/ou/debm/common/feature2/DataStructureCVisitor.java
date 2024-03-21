package nl.ou.debm.common.feature2;

import nl.ou.debm.common.CodeMarker;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.antlr.CBaseVisitor;
import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.ArrayList;
import java.util.List;

public class DataStructureCVisitor extends CBaseVisitor<Object> {
    //public int codemarkersFound = 0; //todo om te testen

    static class Testcase {
        DataStructureCodeMarker codeMarker;
        String result; //variable address expression created by the decompiler
    }


    // recover DataStructureCodeMarkers from any C code
    //
    // Every DataStructureCodeMarker created by the producer is represented in the original C code as a function call with two parameters:
    // 1. the string representation of the code marker
    // 2. the address of the variable, of which we want to know if the decompiler can determine its type correctly
    //
    // After decompilation, some differences may occur. For example, the decompiled function call may have extra parameters. We want to have some tolerance for such mistakes, so we make as few assumptions about the structure of the C code as possible. What is assumed is the following:
    // - Code marker strings may occur anywhere. They are easily recognized by their characteristic prefix, which is highly unlikely to occur in code naturally.
    // - After a code marker string there is a comma (the separator for function call parameters) followed by an expression. This expression is the memory address of the variable being tested.
    public  List<Testcase> findTestcases(String code) {
        var ret = new ArrayList<Testcase>();

        while (true) {
            // parse code marker
            var codemarkerStartIndex = code.indexOf(DataStructureCodeMarker.characteristic);
            if (codemarkerStartIndex == -1) break;
            var codemarkerEndIndex = code.indexOf('\"', codemarkerStartIndex); //assumes quotes don't occur inside code marker strings!
            var codemarkerString = code.substring(codemarkerStartIndex, codemarkerEndIndex);
            System.out.println("gevonden codemarker string: " + codemarkerString);
            var codemarker = new DataStructureCodeMarker(codemarkerString);

            // parse variable address expression
            code = code.substring(codemarkerEndIndex + 1); //+1 skips the quote
            code = code.trim();
            if (code.charAt(0) != ',') {
                System.out.println("codemarker: " + codemarkerString);
                System.out.println("remaining code: : " + code);
                throw new RuntimeException("comma expected"); //todo afhandelen?
            }
            code = code.substring(1); //removes the comma

            //Use the C parser to parse the remaining code as an expression. Parsing stops when the expression ends, so it doesn't matter if there is more code. One special case of this is important though: if the decompiler has created too many function parameters, which are separated by commas, all parameters are interpreted as one big comma-separated compound expression, of which we need only the first element. If at any point parsing fails, the parser stops parsing, but the successfully parsed parts are preserved, and we only need the first one, so this should work reliably.
            var lexer = new CLexer(CharStreams.fromString(code));
            var parser = new CParser(new CommonTokenStream(lexer));
            var subexprs = parser.expression().assignmentExpression();
            assert subexprs != null; //todo afhandelen?
            assert subexprs.size() > 0;
            String exprText = subexprs.get(0).getText();

            var testcase = new Testcase();
            testcase.codeMarker = codemarker;
            testcase.result = exprText;
            ret.add(testcase);
        }

        return ret;
    }
    //todo: gaat dit goed om met onverwachte situaties?

    public Object visitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        System.out.println("in the visit function defeintion visitor");
        /*
        Ghidra creates empty structs, with even incorrect C code.
        ANTLR sees these lines as function definitions.
        Therefore, we return when no function body is found
        */
        if (ctx.compoundStatement() == null || ctx.compoundStatement().blockItemList() == null)
            return null;

        var statements = ctx.compoundStatement().blockItemList().blockItem();

        for (var statement : statements) {
            var statementText = statement.getText();
            if (statementText.contains(DataStructureCodeMarker.characteristic)) {
                System.out.println("codemarker gevonden: " + statementText);

                var testcases = findTestcases(statementText);
                System.out.println("de testcases:");
                for (var t : testcases) {
                    System.out.println(t.codeMarker + "  en variable: " + t.result);
                }
            }
            else {
                System.out.println("is geen codemarker: " + statementText);
            }
        }

        return null;
    }
}
