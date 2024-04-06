package nl.ou.debm.assessor;

import nl.ou.debm.common.CommandLineUtils;
import nl.ou.debm.common.Environment;
import nl.ou.debm.common.IOElements;
import nl.ou.debm.common.Misc;
import nl.ou.debm.common.feature1.LoopAssessor;
import nl.ou.debm.common.feature3.FunctionAssessor;
import nl.ou.debm.common.feature4.GeneralDecompilerPropertiesAssessor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;
import static nl.ou.debm.assessor.Assessor.generateHTMLReport;
import static nl.ou.debm.assessor.Assessor.generateXMLReport;
import static nl.ou.debm.common.CommandLineUtils.strGetParameterValue;

public class Main {

    public static void main(String[] args) throws Exception {
        // handle args
        var cli = new AssessorCLIParameters();
        handleCLIParameters(args, cli);
        System.out.println("Containers folder:    " + cli.strContainerSourceLocation);
        System.out.println("Decompilation script: " + cli.strDecompilerScript);
        System.out.print  ("Requested container:  ");
        if (cli.iContainerToBeTested>=0) {
            System.out.println(cli.iContainerToBeTested);
        }
        else {
            System.out.println("randomly selected one");
        }
        System.out.println("Operating mode:       " + cli.workMode.strOutput());
        if (!cli.strAggregate.isEmpty()){
            System.out.println("Aggregation mode:     " + cli.strAggregate);
        }

        // do the assessment
        var ass = new Assessor(cli.featureList);
        var result = ass.RunTheTests(cli.strContainerSourceLocation, cli.strDecompilerScript, cli.iContainerToBeTested ,
                false, cli.workMode, cli.bShowDecompilerOutput);

        // write results
        var aggregated = IAssessor.TestResult.aggregate(result);
        // aggregate on arch/comp/opt?
        if (!cli.strAggregate.isEmpty()){
            if (cli.strAggregate.contains("a") || cli.strAggregate.contains("A")){
                aggregated = IAssessor.TestResult.aggregateLooseArchitecture(aggregated);
            }
            if (cli.strAggregate.contains("c") || cli.strAggregate.contains("C")){
                aggregated = IAssessor.TestResult.aggregateLooseCompiler(aggregated);
            }
            if (cli.strAggregate.contains("o") || cli.strAggregate.contains("O")){
                aggregated = IAssessor.TestResult.aggregateLooseOptimization(aggregated);
            }
        }

        if (cli.workMode != EAssessorWorkModes.DECOMPILE_ONLY) {
            if (!cli.strHTMLOutput.isEmpty()) {
                generateHTMLReport(aggregated, cli.strHTMLOutput, false);
            }
            if (!cli.strXMLOutput.isEmpty()) {
                generateXMLReport(null, aggregated, cli.strXMLOutput, false);
            }
        }

        // show work is done
        System.out.println("========================================================================================");
        System.out.println("Done!");
        if (cli.workMode != EAssessorWorkModes.DECOMPILE_ONLY) {
            if (!cli.strHTMLOutput.isEmpty()) {
                System.out.println("HTML report written as: " + cli.strHTMLOutput);
            }
            if (!cli.strXMLOutput.isEmpty()) {
                System.out.println("XML report written as: " + cli.strXMLOutput);
            }
        }

        //The JVM keeps running forever. It is not clear which thread causes this, but a workaround for now is a hard exit.
        exit(0);
    }

    /**
     * Class to exchange command line data
     */
    private static class AssessorCLIParameters{
        public String strContainerSourceLocation = "";
        public String strDecompilerScript = "";
        public String strHTMLOutput = "";
        public String strXMLOutput = "";
        public int iContainerToBeTested = -1;
        public EAssessorWorkModes workMode = EAssessorWorkModes.DECOMPILE_AND_ASSESS;
        public boolean bShowDecompilerOutput = false;
        public List<IAssessor> featureList = null;
        public String strAggregate = "";
    }

    /**
     * Handle command line arguments. When completely empty: do defaults. Show help is requested.
     * Exits program when errors are encountered or help is requested.
     * @param args command line argument array as provided to main()-function
     * @param cli output
     */
    private static void handleCLIParameters(String[] args, AssessorCLIParameters cli){
        // options
        final String STRROOTCONTAINEROPTION = "-c=";
        final String STRDECOMPILERSCRIPTOPTION = "-s=";
        final String STRCONTAINERINDEXOPTION = "-i=";
        final String STRHTMLOPTION = "-html=";
        final String STRXMLOPTION = "-xml=";
        final String STRWORKMODE = "-wm=";
        final String STRSHOWDECOMPILEROUTPUT = "-shd=";
        final String STRWHICHFEATURES = "-f=";
        final String STRAGGREGATEOUTPUT = "-ao=";

        // set up basic interpretation parameters
        List<CommandLineUtils.ParameterDefinition> pmd = new ArrayList<>();
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "root_containers_folder",
                "location of the root folder where all the test containers are located. " +
                        "If you use *, the default setting from the class Environment is used.",
                new String[]{STRROOTCONTAINEROPTION, "/c="}, '1'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "decompiler_script",
                "full path to a script to invoke the decompiler. The script must accept two " +
                        "parameters, the first being the binary to be decompiled, the second being " +
                        "the file that must contain the C output after decompilation",
                new String[]{STRDECOMPILERSCRIPTOPTION, "/s="}, '1'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "container_to_be_tested",
                "container index to be tested. If omitted, a random container is selected. " +
                        "Integer value expected, decimal number",
                new String[]{STRCONTAINERINDEXOPTION, "/i="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "html_output",
                "the assessor's results will be written in html to this file. If both html and" +
                        "xml output options are omitted, a default html filename will be used.",
                new String[]{STRHTMLOPTION, "/html="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "xml_output",
                "the assessor's results will be written in xml to this file.",
                new String[]{STRXMLOPTION, "/xml="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "work_mode",
                "the assessor normally takes two steps: (1) it invokes the decompiler script and " +
                        "(2) it analyses the results. The decompiler outputs are always stored in the container.\n" +
                        "-wm=d use this default mode (also used when this parameter is omitted)\n" +
                        "-wm=p only do step 1, so no analysing\n" +
                        "-wm=a assess only, use the decompiled files that the decompiler emitted earlier.",
                new String[]{STRWORKMODE, "/wm="}, '?', "d"
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "show_decompiler_output",
                "if set to anything other than n or no (case insensitive), all decompiler" +
                        "output will be printed in stead of suppressed.",
                new String[]{STRSHOWDECOMPILEROUTPUT, "/shd="}, '?', "no"
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "test_which_features",
                "when omitted or set to 'a': test all features, otherwise test only the features " +
                        "set. So '41' will result in features 1 and 4 only",
                new String[]{STRWHICHFEATURES, "/f="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "test_which_features",
                "when omitted or set to 'a': test all features, otherwise test only the features " +
                        "set. So '41' will result in features 1 and 4 only",
                new String[]{STRWHICHFEATURES, "/f="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "test_which_features",
                "when omitted or set to 'a': test all features, otherwise test only the features " +
                        "set. So '41' will result in features 1 and 4 only",
                new String[]{STRWHICHFEATURES, "/f="}, '?'
        ));
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "aggregate_output",
                "aggregate test results over compiler settings, use 'a' for architecture, 'c' for " +
                        "compiler and 'o' for optimization. These may be combined. The selected settings will no longer " +
                        "be distinguished in the output.",
                new String[]{STRAGGREGATEOUTPUT, "/ao="}, '?'
        ));
        // set up info
        var me = new CommandLineUtils("deb'm assessor",
                "(c) 2023/2024 Jaap van den Bos, Kesava van Gelder, Reijer Klaasse",
                pmd);
        me.setGeneralHelp("This program assesses decompiled C code and scores on a number of aspects. It emits " +
                "its output in HTML and/or XML format.");
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

        // compiler script
        //////////////////
        strValue = strGetParameterValue(STRDECOMPILERSCRIPTOPTION, a);
        assert strValue!=null;         // should not be a problem, as the parser should hold if this required option is omitted
        if (!IOElements.bFileExists(strValue)){
            me.printError("Decompiler script does not exist.", 102);
        }
        cli.strDecompilerScript = strValue;

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
        if (!bAnyOutput){
            cli.strHTMLOutput = Path.of(cli.strContainerSourceLocation, "report.html").toString();
        }

        // work mode
        ////////////
        strValue = strGetParameterValue(STRWORKMODE, a);
        assert strValue != null;    // will always work, as this has a default value, but keep the compiler happy
        if (strValue.equals("d")){
            cli.workMode = EAssessorWorkModes.DECOMPILE_AND_ASSESS;
        }
        else if (strValue.equals("a")){
            cli.workMode = EAssessorWorkModes.ASSESS_ONLY;
        }
        else if (strValue.equals("p")){
            cli.workMode = EAssessorWorkModes.DECOMPILE_ONLY;
        }
        else {
            me.printError("Illegal work mode: " + strValue);
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
            if (!strValue.equalsIgnoreCase("a")){
                cli.featureList = new ArrayList<>();
                for (int p=0;p<strValue.length();++p){
                    switch (strValue.charAt(p)){
                        case '1' -> cli.featureList.add(new LoopAssessor());
                        //case '2' -> cli.featureList.add(new DataStructuresFeature());  // TODO: NIY!!
                        case '3' -> cli.featureList.add(new FunctionAssessor());
                        case '4' -> cli.featureList.add(new GeneralDecompilerPropertiesAssessor());
                        default -> me.printError("Illegal feature code: " + strValue.charAt(p));
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

        // all is well, thus we can just print our own program header and go on
        me.printProgramHeader();
    }

}
