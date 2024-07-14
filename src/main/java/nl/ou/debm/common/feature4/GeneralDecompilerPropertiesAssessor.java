package nl.ou.debm.common.feature4;

import nl.ou.debm.assessor.CountTestResult;
import nl.ou.debm.assessor.ETestCategories;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.EFeaturePrefix;
import nl.ou.debm.common.IOElements;
import nl.ou.debm.common.antlr.CBaseListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GeneralDecompilerPropertiesAssessor implements IAssessor {
    @Override
    public List<TestResult> GetTestResultsForSingleBinary(CodeInfo ci) {
        final List<TestResult> out = new ArrayList<>();

        // assess syntax (may return null)
        var tr = assessSyntax(ci);
        if (tr!=null){
            out.add(tr);
        }
        return out;
    }

    @Override
    public EFeaturePrefix getPrefix() {
        return EFeaturePrefix.GENERALDECOMPILERPROPERTIES;
    }


    /**
     * Assess the code on parsableness
     * @param ci what code to assess
     * @return a single test result
     */
    private TestResult assessSyntax(CodeInfo ci){

        // make new test result
        var tr = new CountTestResult(ETestCategories.FEATURE4_PARSER_ERRORS, ci.compilerConfig);
        // try to minimize number
        tr.setTargetMode(CountTestResult.ETargetMode.LOWBOUND);

        // redirect stderr
        var defaultStdErr = System.err;
        PrintStream myStdErr = null;
        File stdErrFilename = null;
        try {
            stdErrFilename = Files.createTempFile("ReroutedStdErr", ".txt").toFile();
            myStdErr = new PrintStream(new FileOutputStream(stdErrFilename.getPath()));
            System.setErr(myStdErr);
        }
        catch (Exception ignore) {}

        // on successful redirection:
        if (myStdErr!=null) {
            // walk the whole tree, nothing needs to be done by the listener -- but it may produce errors/
            // warnings on (redirected) stdErr.
            var tree = ci.cparser_dec.compilationUnit();
            var walker = new ParseTreeWalker();
            var listener = new CBaseListener();
            walker.walk(listener, tree);

            // close redirected file
            myStdErr.flush();
            myStdErr.close();

            // undo redirection
            System.setErr(defaultStdErr);

            // count the lines in the decompiled C code
            int cntCLines = IOElements.iGetNumberOfLinesInFile(ci.strDecompiledCFilename);
            tr.setHighBound(cntCLines);

            // count lines in stdErr file
            int cntErr = IOElements.iGetNumberOfLinesInFile(stdErrFilename.getPath());
            if (cntErr > cntCLines) {
                cntErr = cntCLines;
            }
            tr.setActualValue(cntErr);
        }
        else {
            // no joy - no return
            tr = null;
        }

        // remove temp file
        if (stdErrFilename!=null) {
            IOElements.deleteFile(stdErrFilename.getPath());
        }

        // return results (may be null)
        return tr;
    }
}
