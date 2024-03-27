package nl.ou.debm.common;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class CommandLineUtils {

    static public class ParameterDefinition{
        public String strParameterDescriptionHeader="";
        public String strParameterDescription="";
        final public List<String> lPrefix= new ArrayList<>();
        public char cCardinality='1';

        public ParameterDefinition() {};
        public ParameterDefinition(String strParameterDescriptionHeader, String strParameterDescription, String strPrefix, char cCardinality){
            this.strParameterDescriptionHeader = strParameterDescriptionHeader;
            this.strParameterDescription = strParameterDescription;
            this.lPrefix.add(strPrefix);
            this.cCardinality = cCardinality;
        }
    }

    static public class ParsedCommandLineParameter {
        public String strParameterDescription = "";
        public String strValue = "";

        public ParsedCommandLineParameter(String strParameterDescription, String strValue){
            this.strParameterDescription = strParameterDescription;
            this.strValue = strValue;
        }

        @Override
        public String toString(){
            return strParameterDescription + ":" + strValue;
        }
    }


    private String m_strProgramName = "";
    private String m_strCopyRight = "";
    private final  List<ParameterDefinition> m_pmd = new ArrayList<>();

    public CommandLineUtils(String strProgramName, String strCopyRight){
        m_strProgramName = strProgramName;
        m_strCopyRight = strCopyRight;
    }

    public CommandLineUtils(String strProgramName, String strCopyRight, List<ParameterDefinition> pmd){
        m_strProgramName = strProgramName;
        m_strCopyRight = strCopyRight;
        setParameterDefinitions(pmd);
    }

    public void setParameterDefinitions(List<ParameterDefinition> pmd){
        m_pmd.clear();;
        m_pmd.addAll(pmd);
    }

    public void printProgramHeader(){
        System.out.println(m_strProgramName);
        StringBuilder sb = new StringBuilder(m_strProgramName);
        for (int i=0; i<sb.length(); ++i){
            sb.setCharAt(i, '=');
        }
        System.out.println(sb);
        System.out.println(m_strCopyRight);
    }

    public void printHelp(){
        System.out.println("HELP TEXT");
    }

    private void printError(String strError) {
        printError(strError, 1);
    }
    private void printError(String strError, int iErrorNumber){
        printProgramHeader();
        System.out.println();
        System.out.println("*** Error: " + strError);
        System.out.println();
        printHelp();
        exit(iErrorNumber);
    }

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
                printError("no parameters given.");
            }
            printProgramHeader();
            return out;
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
        while (!pars.isEmpty()){
            boolean bFound = false;
            for (var pm : m_pmd) {
                for (var pf : pm.lPrefix) {
                    if (pars.get(0).startsWith(pf)) {
                        // found parameter!
                        bFound = true;
                        // copy to parsed list
                        out.add(new ParsedCommandLineParameter(pm.lPrefix.get(0), pars.get(0).substring(pf.length())));
                    }
                }
            }
            if (!bFound){
                printError("Parameter starts with unknown option code: " + pars.get(0),1);
            }
            pars.remove(0);
        }


        return out;
    }


}
