package nl.ou.debm.common;

import java.util.List;

public class CommandLineUtils {

    static public class ParameterDefinition{
        public String strParameterDescription="";
        public String strPrefix="";
        public char cCardinality='1';
    }

    static public class ParsedCommandLineParameter {
        public String strParameterDescription = "";
        public String strValue = "";
    }

    private String m_strProgramName = "";
    private String m_strCopyRight = "";

    public CommandLineUtils(String strProgramName, String strCopyRight){
        m_strProgramName = strProgramName;
        m_strCopyRight = strCopyRight;
    }

    public List<ParsedCommandLineParameter> parseCommandLineInput(String[] args){
        return null;
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
}
