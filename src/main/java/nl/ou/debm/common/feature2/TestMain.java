package nl.ou.debm.common.feature2;

import nl.ou.debm.common.antlr.CLexer;
import nl.ou.debm.common.antlr.CParser;
import nl.ou.debm.common.task.ProcessTask;
import nl.ou.debm.producer.CGenerator;
import nl.ou.debm.producer.DataType;
import nl.ou.debm.producer.Struct;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.ArrayList;
import java.util.function.Consumer;



//een programmatje om dingen gerelateerd aan feature2 te testen tijdens ontwikkeling
public class TestMain {
    public static void myassert(boolean condition) {
        if (!condition) throw new RuntimeException("assert failed");
    }

    public static void main(String[] args) throws Exception
    {
        String functioncall = "DataStructureCodeMarker(\"c5852db2-7acb-cba3-7f81-e7ef3cd1d3b8DS>>expected:struct mijn_struct,ID:2,category:struct,CHECKSUM:4E3E\", ptr, (int64_t)v1, v2, __rax + 0x1);";

        var codemarkerIndex = functioncall.indexOf(DataStructureCodeMarker.characteristic);
        myassert(codemarkerIndex >= 0); // should be larger because the code marker always starts with a quote too
        String fromCodemarker = functioncall.substring(codemarkerIndex);
        System.out.println("fromCodemarker: " + fromCodemarker);

        var codemarkerEndIndex = fromCodemarker.indexOf('\"');
        System.out.println("codemarkerEndIndex: " + codemarkerEndIndex);
        var fromNextParam = fromCodemarker.substring(codemarkerEndIndex + 1);
        System.out.println("fromNextParam: " + fromNextParam);
        fromNextParam = fromNextParam.trim();
        myassert(fromNextParam.charAt(0) == ',');
        fromNextParam = fromNextParam.substring(1);
        fromNextParam = fromNextParam.trim();
        System.out.println("fromNextParam getrimd: " + fromNextParam);

        var lexer = new CLexer(CharStreams.fromString(fromNextParam));
        var parser = new CParser(new CommonTokenStream(lexer));
        var subexprs = parser.expression().assignmentExpression();
        for (var expr : subexprs) {
            String exprText = expr.getText();
            System.out.println("gevonden exprText: " + exprText);
        }

        //
        //  Test parsing with a fixed file
        //

        var lexer1 = new CLexer(CharStreams.fromFileName("C:\\OU\\IB9902, IB9906 - Afstudeerproject\\onderzoek\\c parsen\\binary_x64_cln_opt.exe_retdec.bat.c"));
        var parser1 = new CParser(new CommonTokenStream(lexer1));
        var visitor = new DataStructureCVisitor();
        visitor.visit(parser1.compilationUnit());
    }
}
