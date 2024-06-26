package nl.ou.debm.assessor;

import nl.ou.debm.common.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;
import static nl.ou.debm.assessor.Assessor.generateHTMLReport;
import static nl.ou.debm.assessor.Assessor.generateXMLReport;
import static nl.ou.debm.common.CommandLineUtils.strGetParameterValue;
import static nl.ou.debm.common.CommandLineUtils.strGetParameterValues;
import static nl.ou.debm.common.Misc.strSafeRightString;

public class Main {

    public static void main(String[] args) throws Exception {
        // handle args
        var cli = new AssessorCLIParameters();
        handleCLIParameters(args, cli);

        // tikz support
        var plotLines = new HashMap<String, List<IAssessor.TestResult>>();

        // loop over all scripts
        int scriptIndex = 0;
        for (var strDecompilerScript : cli.decompilerScripts) {
            System.out.println("Containers folder:    " + cli.strContainerSourceLocation);
            System.out.println("Decompilation script: " + strDecompilerScript);
            System.out.print("Requested container:  ");
            if (cli.iContainerToBeTested >= 0) {
                System.out.println(cli.iContainerToBeTested);
            } else {
                System.out.println("randomly selected one");
            }
            System.out.println("Operating mode:       " + cli.workMode.strOutput());
            if (!cli.strAggregate.isEmpty()) {
                System.out.println("Aggregation mode:     " + cli.strAggregate);
            }

            // do the assessment
            var ass = new Assessor(cli.featureList);
            var result = ass.RunTheTests(cli.strContainerSourceLocation, strDecompilerScript, cli.iContainerToBeTested,
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

            // remember data for tikz
            plotLines.put(strStripDecompilerPath(strDecompilerScript), aggregated);

            // generate report
            if (cli.workMode.bAssessingDone()) {
                if (!cli.strHTMLOutput.isEmpty()) {
                    final Map<String, String> prop = new HashMap<>();
                    prop.put("Decompiler name", strStripDecompilerPath(strDecompilerScript));
                    generateHTMLReport(prop, aggregated, IOElements.strAddFileIndex(cli.strHTMLOutput, scriptIndex, cli.decompilerScripts.size()), false);
                }
                if (!cli.strXMLOutput.isEmpty()) {
                    generateXMLReport(null, aggregated, IOElements.strAddFileIndex(cli.strXMLOutput, scriptIndex, cli.decompilerScripts.size()), false);
                }
            }

            // show work is done
            if (cli.workMode.bAssessingDone()) {
                if (!cli.strHTMLOutput.isEmpty()) {
                    System.out.println("HTML report written:  " + IOElements.strAddFileIndex(cli.strHTMLOutput, scriptIndex, cli.decompilerScripts.size()));
                }
                if (!cli.strXMLOutput.isEmpty()) {
                    System.out.println("XML report written:   " + IOElements.strAddFileIndex(cli.strXMLOutput, scriptIndex, cli.decompilerScripts.size()));
                }
            }
            else {
                System.out.println("Decompilation done.");
            }
            System.out.println("========================================================================================");

            // keep track of all the scripts
            scriptIndex++;
        }

        // tikz?
        if (!cli.strTIKZOutput.isEmpty()){
            var tikzPicture = Assessor.generateTikzPicture(plotLines);
            IOElements.writeToFile(tikzPicture, cli.strTIKZOutput);
            System.out.println("tikz-file written:    " + cli.strTIKZOutput);
            System.out.println("========================================================================================");
        }
        // table?
        if (!cli.strTableOutput.isEmpty()){
            var tableData = Assessor.generateLatexTable(cli, plotLines);
            IOElements.writeToFile(tableData, cli.strTableOutput);
            System.out.println("latex-table written:    " + cli.strTableOutput);
            System.out.println("========================================================================================");
        }

        //The JVM keeps running forever. It is not clear which thread causes this, but a workaround for now is a hard exit.
        exit(0);
    }

    /**
     * extract decompiler name from a full decompiler script name<br>
     * - throw away the path<br>
     * - throw away the extension<br>
     * - throw away run- prefix<br>
     * - throw away -online postfix<br>
     * @param strInput full path to a decompiler (script)
     * @return simple name
     */
    private static String strStripDecompilerPath(String strInput){
        // isolate filename
        var strFilename = Paths.get(strInput).getFileName().toString();
        // strip extension
        int p = strFilename.lastIndexOf('.');
        if (p>-1){
            strFilename = strFilename.substring(0,p);
        }
        // strip '-online'
        if (strSafeRightString(strFilename,7).equals("-online")){
            strFilename = strFilename.substring(0, strFilename.length()-7);
        }
        // strip 'run'
        if (strFilename.startsWith("run-")){
            strFilename = strFilename.substring(4);
        }
        return strFilename;
    }

    /**
     * Class to exchange command line data
     */
    public static class AssessorCLIParameters{
        public String strContainerSourceLocation = "";
        public final List<String> decompilerScripts = new ArrayList<>();
        public String strHTMLOutput = "";
        public String strXMLOutput = "";
        public String strTIKZOutput = "";
        public String strTableOutput = "";
        public int iContainerToBeTested = -1;
        public EAssessorWorkModes workMode = EAssessorWorkModes.DECOMPILE_AND_ASSESS;
        public boolean bShowDecompilerOutput = false;
        public List<IAssessor> featureList = null;
        public String strAggregate = "";
        public int iNThreads = -1;
    }

    /**
     * Handle command line arguments. When completely empty: do defaults. Show help is requested.
     * Exits program when errors are encountered or help is requested.
     * @param args command line argument array as provided to main()-function
     * @param cli output
     */
    public static void handleCLIParameters(String[] args, AssessorCLIParameters cli){
        // options
        final String STRROOTCONTAINEROPTION = "-c=";
        final String STRSCRIPTROOTFOLDER = "-srf=";
        final String STRDECOMPILERSCRIPTOPTION = "-s=";
        final String STRCONTAINERINDEXOPTION = "-i=";
        final String STRHTMLOPTION = "-html=";
        final String STRXMLOPTION = "-xml=";
        final String STRTIKZOPTION = "-tikz=";
        final String STRTABLEOPTION = "-table=";
        final String STRWORKMODE = "-wm=";
        final String STRSHOWDECOMPILEROUTPUT = "-shd=";
        final String STRWHICHFEATURES = "-f=";
        final String STRAGGREGATEOUTPUT = "-ao=";
        final String STRTHREADS = "-th=";

        // set up basic interpretation parameters
        List<CommandLineUtils.ParameterDefinition> pmd = new ArrayList<>();
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "root_containers_folder",
                "location of the root folder where all the test containers are located. " +
                        "If you use *, the default setting from the class Environment is used.",
                new String[]{STRROOTCONTAINEROPTION, "/c="}, '1'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "decompiler_script_folder",
                "path to a folder containing your decompiler script(s). You may omit this " +
                        "option and enter the full path at the decompiler_script option, but if you run more " +
                        "decompilers in one go, this may be useful.",
                new String[]{STRSCRIPTROOTFOLDER, "/srf="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "decompiler_script",
                "path to a script to invoke the decompiler. The script must accept two " +
                        "parameters, the first being the binary to be decompiled, the second being " +
                        "the file that must contain the C output after decompilation. If specified, " +
                        "the decompiler_script_folder will be inserted before the decompiler script. " +
                        "It is possible to set more decompiler scripts by using this option more than once.",
                new String[]{STRDECOMPILERSCRIPTOPTION, "/s="}, '+'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "container_to_be_tested",
                "container index to be tested. If omitted, a random container is selected. " +
                        "Integer value expected, decimal number",
                new String[]{STRCONTAINERINDEXOPTION, "/i="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "html_output",
                "the assessor's results will be written in html to this file. If all " +
                        "output options are omitted, a default html filename will be used.",
                new String[]{STRHTMLOPTION, "/html="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "xml_output",
                "the assessor's results will be written in xml to this file.",
                new String[]{STRXMLOPTION, "/xml="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "tikz_output",
                "the assessor's results will be written in tikz to this file.",
                new String[]{STRTIKZOPTION, "/tikz="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "table_output",
                "the assessor's results will be written in a LaTex table to this file.",
                new String[]{STRTABLEOPTION, "/table="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "work_mode",
                "the assessor normally takes two steps: (1) it invokes the decompiler script and " +
                        "(2) it analyses the results. The decompiler outputs are always stored in the container.\n" +
                        "-wm=d use this default mode (also used when this parameter is omitted)\n" +
                        "-wm=p only do step 1, so no analysing\n" +
                        "-wm=n only do step 1, but only when no earlier emitted decompiler output exists\n" +
                        "-wm=a assess only, use the decompiled files that the decompiler emitted earlier.",
                new String[]{STRWORKMODE, "/wm="}, '?', "d"
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "show_decompiler_output",
                "if set to anything other than n or no (case insensitive), all decompiler" +
                        "output will be printed in stead of suppressed.",
                new String[]{STRSHOWDECOMPILEROUTPUT, "/shd="}, '?', "no"
        ));
        String strDesc = "when omitted or set to 'all': test all features, otherwise test only the features " +
                "set. Every character in the string represents a feature to be tested. Any combination is allowed. " +
                "Possibilities:";
        for (var item : EFeaturePrefix.values()){
            if (item.strGetCLIDescription() != null) {
                strDesc += "\n   " + item.charFeatureLetterForCLIOptions() + " : " + item.strGetCLIDescription();
            }
        }
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "test_which_features",
                strDesc,
                new String[]{STRWHICHFEATURES, "/f="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "aggregate_output",
                "aggregate test results over compiler settings, use 'a' for architecture, 'c' for " +
                        "compiler and 'o' for optimization. These may be combined. The selected settings will no longer " +
                        "be distinguished in the output.",
                new String[]{STRAGGREGATEOUTPUT, "/ao="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "threads_used",
                "binaries are assessed using parallel threads. The default number of threads used " +
                        "is the number of processors available. This number may be set to any numeral value, but will " +
                        "be capped at the number of processors available.",
                new String[]{STRTHREADS, "/th="}, '?'
        ));
        // set up info
        var me = new CommandLineUtils("deb'm assessor",
                "(c) 2023/2024 Jaap van den Bos, Kesava van Gelder, Reijer Klaasse",
                pmd);
        me.setGeneralHelp("This program assesses decompiled C code and scores on a number of aspects. It emits " +
                "its output in HTML, XML and/or tikz format.");
        // parse command line parameters (errors or requested help will stop the program by using exit(), so
        // it only returns if all is well)
        var a = me.parseCommandLineInput(args);

        // and use parsed data
        String strValue;

        // container base folder
        ////////////////////////
        strValue = strGetParameterValue(STRROOTCONTAINEROPTION, a);
        assert strValue!=null;         // should not be a problem, as the parser should hold if this required option is omitted
        if (strValue.equals("*")){
            cli.strContainerSourceLocation = Environment.containerBasePath;
        }
        else {
            // use argument
            cli.strContainerSourceLocation = IOElements.strAdaptPathToMatchFileSystemAndAddSeparator(strValue);
        }
        if (!IOElements.bFolderExists(cli.strContainerSourceLocation)){
            me.printError("Container base folder does not exist.", 101);
        }

        // decompiler base folder
        /////////////////////////
        strValue = strGetParameterValue(STRSCRIPTROOTFOLDER, a);
        String strScriptBaseFolder = "";
        if (strValue!=null){
            if (strValue.equals("*")){
                strScriptBaseFolder = Environment.decompilerPath;
            }
            else {
                strScriptBaseFolder = strValue;
            }
        }

        // container to be assessed
        ///////////////////////////
        strValue = strGetParameterValue(STRCONTAINERINDEXOPTION, a);
        if (strValue==null){
            // omitted, use default
            cli.iContainerToBeTested = -1; // a random container will be selected later on in the program
        }
        else {
            // try to interpret
            long val = Misc.lngRobustStringToLong(strValue, Long.MIN_VALUE);
            if (val == Long.MIN_VALUE){
                me.printError("Conversion to number failed for container to be tested (" + strValue + ")" );
            }
            if (val < 0 ){
                me.printError("Negative container number is not allowed (" + strValue + "); omit arg if you want a random container");
            }
            cli.iContainerToBeTested = (int)val;
        }

        // html/xml output
        //////////////////
        strValue = strGetParameterValue(STRHTMLOPTION, a);
        boolean bAnyOutput = false;
        if (strValue!=null){
            cli.strHTMLOutput = strValue;
            bAnyOutput = true;
        }
        strValue = strGetParameterValue(STRXMLOPTION, a);
        if (strValue!=null){
            cli.strXMLOutput = strValue;
            bAnyOutput = true;
        }
        strValue = strGetParameterValue(STRTIKZOPTION, a);
        if (strValue!=null){
            cli.strTIKZOutput = strValue;
            bAnyOutput = true;
        }
        strValue = strGetParameterValue(STRTABLEOPTION, a);
        if (strValue!=null){
            cli.strTableOutput = strValue;
            bAnyOutput = true;
        }
        if (!bAnyOutput){
            cli.strHTMLOutput = Path.of(cli.strContainerSourceLocation, "report.html").toString();
        }

        // work mode
        ////////////
        strValue = strGetParameterValue(STRWORKMODE, a);
        assert strValue != null;    // will always work, as this has a default value, but keep the compiler happy
        switch (strValue) {
            case "d" -> cli.workMode = EAssessorWorkModes.DECOMPILE_AND_ASSESS;
            case "a" -> cli.workMode = EAssessorWorkModes.ASSESS_ONLY;
            case "p" -> cli.workMode = EAssessorWorkModes.DECOMPILE_ONLY;
            case "n" -> cli.workMode = EAssessorWorkModes.DECOMPILE_WHEN_NECESSARY;
            default -> me.printError("Illegal work mode: " + strValue);
        }

        // decompiler script(s)
        ///////////////////////
        var scriptList = strGetParameterValues(STRDECOMPILERSCRIPTOPTION, a);
        assert !scriptList.isEmpty();       // should not be a problem, as cardinality is set to +
        for (var strCS : scriptList) {
            String strFullScript = Path.of(strScriptBaseFolder, strCS).toString();
            if (cli.workMode.bDecompilationPossible()) {
                if (!IOElements.bFileExists(strFullScript)) {
                    me.printError("Decompiler script (" + strFullScript + ") does not exist.", 102);
                }
            }
            cli.decompilerScripts.add(strFullScript);
        }

        // decompiler output
        ////////////////////
        strValue = strGetParameterValue(STRSHOWDECOMPILEROUTPUT, a);
        assert strValue != null;    // will always work, as this has a default value, but keep the compiler happy
        cli.bShowDecompilerOutput = true;
        if ((strValue.equalsIgnoreCase("n")) || (strValue.equalsIgnoreCase("no"))) {
            cli.bShowDecompilerOutput=false;
        }

        // features
        ///////////
        strValue = strGetParameterValue(STRWHICHFEATURES, a);
        if (strValue!=null){
            if (!strValue.equalsIgnoreCase("all")){
                cli.featureList = new ArrayList<>();
                for (int p=0;p<strValue.length();++p){
                    boolean bOK = false;
                    for (var item : EFeaturePrefix.values()){
                        if (strValue.charAt(p) == item.charFeatureLetterForCLIOptions()){
                            var new_instance = item.getAppropriateIAssessorClass();
                            if (new_instance!=null) {
                                cli.featureList.add(new_instance);
                                bOK = true;
                                break;
                            }
                        }
                    }
                    if (!bOK){
                        me.printError("Illegal feature code: " + strValue.charAt(p));
                    }
                }
            }
        }

        // aggregation
        //////////////
        strValue = strGetParameterValue(STRAGGREGATEOUTPUT, a);
        if (strValue!=null) {
            for (int p=0; p<strValue.length();++p){
                switch (strValue.charAt(p)){
                    case 'a', 'A', 'c', 'C', 'o', 'O' -> {;}
                    default -> me.printError("Illegal aggregation code: " + strValue.charAt(p));
                }
            }
            cli.strAggregate = strValue;
        }

        // thread count
        ///////////////
        strValue = strGetParameterValue(STRTHREADS, a);
        if (strValue!=null) {
            cli.iNThreads = Misc.iRobustStringToInt(strValue);
        }

        // all is well, thus we can just print our own program header and go on
        me.printProgramHeader();
    }

}
