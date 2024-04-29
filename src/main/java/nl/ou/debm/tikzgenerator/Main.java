package nl.ou.debm.tikzgenerator;

import nl.ou.debm.assessor.Assessor;
import nl.ou.debm.assessor.EAssessorWorkModes;
import nl.ou.debm.assessor.IAssessor;
import nl.ou.debm.common.IOElements;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static nl.ou.debm.assessor.Main.handleCLIParameters;

public class Main {
    public static void main(String[] args) throws Exception {
        var cli = new nl.ou.debm.assessor.Main.AssessorCLIParameters();
        handleCLIParameters(args, cli);

        var plotLines = new HashMap<String, List<IAssessor.TestResult>>();
        plotLines.put("Ghidra", extracted(cli, "ghidra"));
        plotLines.put("RetDec", extracted(cli, "retdec"));
        plotLines.put("Hex-Rays", extracted(cli, "hexrays-online"));
        var tikzPicture = Assessor.generateTikzPicture(plotLines);
        IOElements.writeToFile(tikzPicture, cli.strTIKZOutput);
        System.exit(0);
    }

    private static List<IAssessor.TestResult> extracted(nl.ou.debm.assessor.Main.AssessorCLIParameters cli, String decompiler) throws Exception {
        cli.workMode = EAssessorWorkModes.ASSESS_ONLY;
        cli.iContainerToBeTested = 0;
        cli.strAggregate = "a";
        cli.bShowDecompilerOutput = true;
//        cli.strDecompilerScript = "C:\\studie\\decompiler-benchmarking\\scripts\\run-" + decompiler + ".bat";
        cli.strHTMLOutput = Path.of(cli.strContainerSourceLocation, "report-" + decompiler + "-" + 0 + ".html").toString();


        // do the assessment
        var ass = new Assessor(cli.featureList);
        var result = ass.RunTheTests(cli.strContainerSourceLocation, cli.decompilerScripts.get(0), cli.iContainerToBeTested,
                false, cli.workMode, cli.bShowDecompilerOutput, cli.iNThreads);

        // write results
        var aggregated = IAssessor.TestResult.aggregate(result);
        // aggregate on arch/comp/opt?
        if (!cli.strAggregate.isEmpty()) {
            if (cli.strAggregate.contains("a") || cli.strAggregate.contains("A")) {
                aggregated = IAssessor.TestResult.aggregateLooseArchitecture(aggregated);
            }
            if (cli.strAggregate.contains("c") || cli.strAggregate.contains("C")) {
                aggregated = IAssessor.TestResult.aggregateLooseCompiler(aggregated);
            }
            if (cli.strAggregate.contains("o") || cli.strAggregate.contains("O")) {
                aggregated = IAssessor.TestResult.aggregateLooseOptimization(aggregated);
            }
        }

        // show work is done
        System.out.println("========================================================================================");
        System.out.println("Done!");
        return aggregated;
    }
}
