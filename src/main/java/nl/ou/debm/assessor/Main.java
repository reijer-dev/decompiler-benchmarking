package nl.ou.debm.assessor;

import nl.ou.debm.common.CommandLineUtils;
import nl.ou.debm.common.Environment;
import nl.ou.debm.common.IOElements;
import nl.ou.debm.common.Misc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

        // do the assessment
        var ass = new Assessor();
        var result = ass.RunTheTests(cli.strContainerSourceLocation, cli.strDecompilerScript, cli.iContainerToBeTested ,false);

        // output results
        var aggregated = IAssessor.TestResult.aggregate(result);
        if (!cli.strHTMLOutput.isEmpty()){
            generateHTMLReport(aggregated, cli.strHTMLOutput, false);
        }
        if (!cli.strXMLOutput.isEmpty()){
            generateXMLReport(null, aggregated, cli.strXMLOutput, false);
        }
        System.out.println("========================================================================================");
        System.out.println("Done!");
        if (!cli.strHTMLOutput.isEmpty()){
            System.out.println("HTML report written as: " + cli.strHTMLOutput);
        }
        if (!cli.strXMLOutput.isEmpty()){
            System.out.println("XML report written as: " + cli.strXMLOutput);
        }

        //The JVM keeps running forever. It is not clear which thread causes this, but a workaround for now is a hard exit.
        System.exit(0);
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

        // set up basic interpretation parameters
        List<CommandLineUtils.ParameterDefinition> pmd = new ArrayList<>();
        pmd.add(new CommandLineUtils.ParameterDefinition(
                "root_containers_folder",
                "location of the root folder where all the test containers are located",
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

        // all is well, thus we can just print our own program header and go on
        me.printProgramHeader();
    }

}
