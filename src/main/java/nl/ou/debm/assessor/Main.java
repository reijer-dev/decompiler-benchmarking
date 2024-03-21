package nl.ou.debm.assessor;

import nl.ou.debm.common.Environment;
import nl.ou.debm.common.IOElements;
import nl.ou.debm.common.Misc;

import java.nio.file.Path;

import static java.lang.System.exit;
import static nl.ou.debm.assessor.Assessor.generateReport;

public class Main {

    public static void main(String[] args) throws Exception {
        // handle args
        var cli = new AssessorCLIParameters();
        handleCLIParameters(args, cli);
        printProgramHeader();
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
        generateReport(aggregated, Path.of(cli.strContainerSourceLocation, "report.html").toString(), false);
        System.out.println("========================================================================================");
        System.out.println("Done! Report in html written to " + Path.of(cli.strContainerSourceLocation, "report.html").toString());

        //The JVM keeps running forever. It is not clear which thread causes this, but a workaround for now is a hard exit.
        System.exit(0);
    }

    /**
     * Class to exchange command line data
     */
    private static class AssessorCLIParameters{
        public String strContainerSourceLocation = "";
        public String strDecompilerScript = "";
        public int iContainerToBeTested = -1;
    }

    /**
     * Handle command line arguments. When completely empty: do defaults. Show help is requested.
     * Exits program when errors are encountered or help is requested.
     * @param args command line argument array as provided to main()-function
     * @param cli output
     */
    private static void handleCLIParameters(String[] args, AssessorCLIParameters cli){
        // show help if requested
        String[] HELP = { "-h", "-help", "/h", "/help", "-?", "/?"};
        for (var a : args){
            for (var h : HELP){
                if (a.trim().compareToIgnoreCase(h)==0){
                    printHelp();
                    exit(0);
                }
            }
        }

        final int ERROR = 0;

        // check number of parameters
        if (args.length>3){
            printHelp();
            System.err.println("Error: too many parameters (" + args.length + ")");
            for (int p=0; p<args.length; p++){
                System.err.println(p + ":" + args[p]);
            }
            exit(ERROR);
        }

        // check container folder
        if (args.length<1){
            // omitted --> error
            printHelp();
            System.err.println("Error: container folder omitted, but is a required parameter.");
            exit(ERROR);
        }
        else {
            // use default?
            if (args[0].equals("*")){
                cli.strContainerSourceLocation = Environment.containerBasePath;
            }
            else {
                // use argument
                cli.strContainerSourceLocation = IOElements.strAdaptPathToMatchFileSystemAndAddSeparator(args[0]);
            }
            if (!IOElements.bFolderExists(cli.strContainerSourceLocation)){
                printHelp();
                System.err.println("Error: container folder does not exist.");
                exit(ERROR);
            }
        }

        // check decompiler script
        if (args.length<2){
            // omitted --> error
            printHelp();
            System.err.println("Error: decompiler script omitted, but is a required parameter.");
            exit(ERROR);
        }
        else {
            if (!IOElements.bFileExists(args[1])){
                printHelp();
                System.err.println("Error: decompiler script not found.");
                exit(ERROR);
            }
            cli.strDecompilerScript = args[1];
        }

        // which container is to be checked
        if (args.length<3){
            // omitted, use default
            cli.iContainerToBeTested = -1; // a random container will be selected later on in the programm
        }
        else {
            // try to interpret
            long val = Misc.lngRobustStringToLong(args[2], Long.MIN_VALUE);
            if (val == Long.MIN_VALUE){
                printHelp();
                System.err.println("Error: conversion to number failed for container to be tested (" + args[2] + ")" );
                exit(ERROR);
            }
            if (val < 0 ){
                printHelp();
                System.err.println("Error: negative container number is not allowed (" + args[2] + "); omit arg if you want a random container");
                exit(ERROR);
            }
            cli.iContainerToBeTested = (int)val;
        }

    }

    /**
     * Dump help text to stdout
     */
    private static void printHelp(){
        // show help and be done.
        printProgramHeader();
        System.out.println(
                "This software takes up to three parameters:\n" +
                        "container_root_folder  -- location of the root folder where all the test containers are found\n" +
                        "                          required parameter.\n" +
                        "decompiler_script      -- full path to a script to invoke the decompiler. The script must accept two\n" +
                        "                          parameters, the first being the binary to be decompiled, the second being\n" +
                        "                          the file that must contain the C output after decompilation\n" +
                        "                          required parameter.\n" +
                        "container_to_be_tested -- container number to be tested. If omitted, a random container is selected\n" +
                        "                          integer value expected, decimal number, optional parameter\n" +
                        "\n" +
                        "running with any of the following in the argument list will produce this help:\n" +
                        "-h, -help, -?, /h, /help, /?, all case insensitive"
                // we do not show the hidden * option for container folder in the help screen, as it is
                // developer specific
        );
    }

    private static void printProgramHeader(){
        System.out.println(
                        "deb'm assessor\n" +
                        "==============\n" +
                        "\n" +
                        "(c) 2023/2024 Jaap van den Bos, Kesava van Gelder, Reijer Klaasse\n");

    }



}
