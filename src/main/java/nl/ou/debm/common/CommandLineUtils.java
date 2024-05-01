package nl.ou.debm.common;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

/**
 * This class simplifies the process of parsing command line arguments and printing help.
 */
public class CommandLineUtils {

    /**
     * Class to be used as a struct, describing what arguments are expected on the command line
     */
    static public class ParameterDefinition{
        /** very short description of the parameter (preferably in one word) */     public String strParameterDescriptionHeader="";
        /** full description of the option */                                       public String strParameterDescription="";
        /** list of prefixes for this parameter (eg -i=, /i=, i:) */                final public List<String> lPrefix= new ArrayList<>();
        /** cardinality: *=0 or more; ?=0 or 1; 1=exactly 1; +=1 or more      */    public char cCardinality='1';
        /** default value if this argument is not present */                        public String strDefaultValue = "";

        public ParameterDefinition() {};
        public ParameterDefinition(String strParameterDescriptionHeader, String strParameterDescription, String strPrefix, char cCardinality){
            this.strParameterDescriptionHeader = strParameterDescriptionHeader;
            this.strParameterDescription = strParameterDescription;
            this.lPrefix.add(strPrefix);
            this.cCardinality = cCardinality;
        }
        public ParameterDefinition(String strParameterDescriptionHeader, String strParameterDescription, String[] strPrefix, char cCardinality){
            this.strParameterDescriptionHeader = strParameterDescriptionHeader;
            this.strParameterDescription = strParameterDescription;
            this.lPrefix.addAll(List.of(strPrefix));
            this.cCardinality = cCardinality;
        }
        public ParameterDefinition(String strParameterDescriptionHeader, String strParameterDescription, String strPrefix, char cCardinality,
                                   String strDefaultValue){
            this.strParameterDescriptionHeader = strParameterDescriptionHeader;
            this.strParameterDescription = strParameterDescription;
            this.lPrefix.add(strPrefix);
            this.cCardinality = cCardinality;
            this.strDefaultValue = strDefaultValue;
        }
        public ParameterDefinition(String strParameterDescriptionHeader, String strParameterDescription, String[] strPrefix, char cCardinality,
                                   String strDefaultValue){
            this.strParameterDescriptionHeader = strParameterDescriptionHeader;
            this.strParameterDescription = strParameterDescription;
            this.lPrefix.addAll(List.of(strPrefix));
            this.cCardinality = cCardinality;
            this.strDefaultValue = strDefaultValue;
        }
    }

    /**
     * class (struct) that describes the arguments found
     */
    static public class ParsedCommandLineParameter {
        /** prefix for this value (for example: -i=). Always first in the list im ParameterDefinition. */   public String strPrefix = "";
        /** value for this prefix */                                                                        public String strValue = "";

        public ParsedCommandLineParameter(String strPrefix, String strValue){
            this.strPrefix = strPrefix;
            this.strValue = strValue;
        }

        @Override
        public String toString(){
            return strPrefix + ":" + strValue;
        }
    }


    /** program name, used in the header */         private String m_strProgramName = "";
    /** copyright notice */                         private String m_strCopyRight = "";
    /** general help text */                        private String m_strGeneralHelp = "";
    /** description of the expected parameters */   private final  List<ParameterDefinition> m_pmd = new ArrayList<>();

    public CommandLineUtils(String strProgramName, String strCopyRight){
        m_strProgramName = strProgramName;
        m_strCopyRight = strCopyRight;
    }

    public CommandLineUtils(String strProgramName, String strCopyRight, List<ParameterDefinition> pmd){
        m_strProgramName = strProgramName;
        m_strCopyRight = strCopyRight;
        setParameterDefinitions(pmd);
    }

    /**
     * set what arguments are expected by this program
     * @param pmd parameter definitions
     */
    public void setParameterDefinitions(List<ParameterDefinition> pmd){
        m_pmd.clear();;
        m_pmd.addAll(pmd);
    }

    /**
     * Set the text for general help.
     * @param strGeneralHelp New general help text. Text will be auto-wrapped. \n is supported
     */
    public void setGeneralHelp(String strGeneralHelp){
        m_strGeneralHelp=strGeneralHelp;
    }

    /**
     * Print program header: name of the program, double underline and copyright notice
     */
    public void printProgramHeader(){
        System.out.println(m_strProgramName);
        StringBuilder sb = new StringBuilder(m_strProgramName);
        for (int i=0; i<sb.length(); ++i){
            sb.setCharAt(i, '=');
        }
        System.out.println(sb);
        System.out.println(m_strCopyRight);
    }

    /**
     * print help text and details of all the parameters
     */
    public void printHelp(){
        printArrangedText(m_strGeneralHelp, 80, 0);
        System.out.println("\nArguments:");
        for (var pmd : m_pmd){
            for (var itm : pmd.lPrefix) {
                System.out.print(itm + "  ");
            }
            System.out.print(" --> " + pmd.strParameterDescriptionHeader);
            if ((pmd.cCardinality=='1') || (pmd.cCardinality=='+')){
                System.out.print(" (required");
            }
            else {
                System.out.print(" (optional");
            }
            if ((pmd.cCardinality=='+') || (pmd.cCardinality=='*')){
                System.out.print(", repeatable");
            }
            if (pmd.strDefaultValue.isEmpty()) {
                System.out.println(")");
            }
            else {
                System.out.println(", default: " + pmd.strDefaultValue + ")");
            }
            printArrangedText(pmd.strParameterDescription, 75, 5);
        }
    }

    /**
     * Send text to stdout and wrap it appropriately
     * @param strText Text to print
     * @param iWidth line width
     * @param iTab number of spaces left of the text block
     */
    private void printArrangedText(String strText, int iWidth, int iTab){
        var strTab = "                                                     ".substring(0, iTab);
        for (int p1=0; p1 < strText.length(); p1++){
            if (bNonWhiteSpace(strText.charAt(p1))){
                int p2 = p1 + iWidth;
                if (p2>strText.length()){
                    p2=strText.length();
                }
                else {
                    while (bNonWhiteSpace(strText.charAt(p2)) && (p2 > p1)) {
                        p2--;
                    }
                }
                String strLine = strText.substring(p1, p2);
                int p3 = strLine.indexOf('\n');
                if (p3>-1) {
                    strLine=strLine.substring(0,p3);
                }
                System.out.println(strTab + strLine);
                p1 += strLine.length();
            }
        }
    }

    private boolean bNonWhiteSpace(char c){
        return (!((c==' ') || (c=='\n')));
    }


    /**
     * Print header, help, error message and exit program with code 1
     * @param strError error to be printed
     */
    public void printError(String strError) {
        printError(strError, 1);
    }
    /**
     * Print header, help, error message and exit program
     * @param strError error to be printed
     * @param iErrorNumber exit code to be used
     */
    public void printError(String strError, int iErrorNumber){
        printProgramHeader();
        System.out.println();
        printHelp();
        System.out.println();
        System.out.println("*** Error: " + strError);
        exit(iErrorNumber);
    }

    /**
     * Parse command line arguments. Checks cardinality and show the users errors when needed.
     * Whenever errors occur, or help is shown, the program exits using exit().
     * @param args the arguments the JVM passed to main()
     * @return a parsed set of arguments
     */
    public List<ParsedCommandLineParameter> parseCommandLineInput(String[] args){
        assert !m_pmd.isEmpty() : "No parameter definitions initialized";

        // output
        List<ParsedCommandLineParameter> out = new ArrayList<>(args.length);

        // any parameters required?
        boolean bAnyRequired = false;
        for (var pd : m_pmd){
            if ((pd.cCardinality=='1') || (pd.cCardinality=='+')){
                bAnyRequired = true;
                break;
            }
        }
        if (args.length==0){
            if (bAnyRequired) {
                printError("no parameters given.", 6);
            }
        }

        // array to list
        List<String> pars = new ArrayList<>(List.of(args));

        // find help argument
        String[] HELP = { "-h", "-help", "/h", "/help", "-?", "/?"};
        for (var a : pars){
            for (var h : HELP){
                if (a.trim().compareToIgnoreCase(h)==0){
                    printProgramHeader();
                    printHelp();
                    exit(0);
                }
            }
        }

        // do all other parsing
        for (var par : pars){
            boolean bFound = false;
            for (var pm : m_pmd) {
                for (var pf : pm.lPrefix) {
                    if (par.startsWith(pf)) {
                        // found parameter!
                        bFound = true;
                        // copy to parsed list
                        out.add(new ParsedCommandLineParameter(pm.lPrefix.get(0), par.substring(pf.length())));
                    }
                }
            }
            if (!bFound){
                printError("Parameter starts with unknown option code: " + par,1);
            }
        }

        // test cardinality and do defaults
        for (var pm : m_pmd){
            int iOptionCount = 0;
            for (var par : out){
                if (par.strPrefix.equals(pm.lPrefix.get(0))){
                    iOptionCount++;
                }
            }
            if ((iOptionCount==0) && (pm.cCardinality=='1')){
                printError("Required parameter missing (single instance): " + pm.lPrefix.get(0), 2);
            }
            if ((iOptionCount==0) && (pm.cCardinality=='+')){
                printError("Required parameter missing (one ore more instances): " + pm.lPrefix.get(0), 3);
            }
            if ((iOptionCount>1) && (pm.cCardinality=='?')){
                printError("Repeated optional parameter: " + pm.lPrefix.get(0), 4);
            }
            if ((iOptionCount>1) && (pm.cCardinality=='1')){
                printError("Repeated required parameter: " + pm.lPrefix.get(0), 5);
            }
            if ((pm.cCardinality == '1') || (pm.cCardinality=='+')){
                assert pm.strDefaultValue.isEmpty() : "default parameter conflicts with cardinality (" + pm.lPrefix.get(0) + ")";
            }
            if ((iOptionCount == 0) & (!pm.strDefaultValue.isEmpty())){
                out.add(new ParsedCommandLineParameter(pm.lPrefix.get(0), pm.strDefaultValue));
            }
        }

        // return the results
        return out;
    }

    /**
     * Get a value from a list of parsed parameters. If more than one occurs, only the first found is returned
     * @param strWhichParameter what parameter must be searched
     * @param args table to search it in
     * @return the value or null when not found
     */
    public static String strGetParameterValue(String strWhichParameter, List<ParsedCommandLineParameter> args){
        for (var item : args){
            if (item.strPrefix.equals(strWhichParameter)){
                return item.strValue;
            }
        }
        return null;
    }

    /**
     * Get a list of values from a list of parsed parameters.
     * @param strWhichParameter what parameter must be searched
     * @param args table to search it in
     * @return the values, empty list when not found
     */
    public static List<String> strGetParameterValues(String strWhichParameter, List<ParsedCommandLineParameter> args){
        List<String> out = new ArrayList<>();
        for (var item : args){
            if (item.strPrefix.equals(strWhichParameter)){
                out.add(item.strValue);
            }
        }
        return out;
    }

}
