package nl.ou.debm.common;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Random;

import static java.lang.Math.abs;

/**
 * Class containing all kinds of miscellaneous functions
 */


public class Misc {
    /**
     * Convert an integer to a string with a fixed length, using zero as
     * pre-character. strGetNumberWithPrefixZeros(23, 4) results in "0023"
     * Negative numbers are inverted to positive numbers. If more digits
     * are needed, because of the height of the value, a longer string will
     * be returned. strGetNumberWithPrefixZeros(123456, 4) will still return "123456"
     * @param iValue  Value to be converted to a string. Negative numbers will be
     *                inverted
     * @param iLength Number of digits requested.
     * @return        String of the requested length (or longer, if iValue is too big),
     *                using "0" as prefix char.
     */
    public static String strGetNumberWithPrefixZeros(int iValue, int iLength){
        if(iLength == 0)
            iLength = 1;
        // avoid negative input
        return String.format("%1$" + iLength + "s", abs(iValue)).replace(' ', '0');
    }

    private static boolean bRunsOnWindows(){
        return (File.separatorChar == '\\');
    }
    private static boolean bRunsOnLinux(){
        return (File.separatorChar == '/');
    }
    public static String strGetExternalSoftwareLocation(String softwareName) throws Exception {
        String command;
        if (bRunsOnWindows()){
            command = "where";
        }
        else if (bRunsOnLinux()){
            command = "which";
        }
        else {
            throw new Exception("Cannot determine Windows or Linux");
        }
        var whereSoftware = new ProcessBuilder(command, softwareName).redirectErrorStream(true).start();
        var softwareLocation = new BufferedReader(new InputStreamReader(whereSoftware.getInputStream())).readLine();
        if (softwareLocation == null){
            throw new Exception("Cannot find requested software: " + softwareName);
        }
        return softwareLocation;
    }

    /**
     * Easy short print helper for booleans
     * @param b     boolean to be printed
     * @return      'T' when b = true, otherwise 'F'
     */
    public static char cBooleanToChar(boolean b){
        if (b){
            return 'T';
        }
        return 'F';
    }

    /**
     * Test string for 'True' value (T in short). null strings
     * return false in stead of exception
     * @param s input string
     * @return true if and only if string equals "T"
     */
    public static boolean bIsTrue(String s){
        return s!=null && s.equals("T");
    }

    /**
     * Test char for 'T' value (true)
     * @param c char to be tested
     * @return true if and only if c=='T'
     */
    public static boolean bIsTrue(char c){
        return c=='T';
    }

    // make one single project wide random generator available
    public static final Random rnd = new Random();

    /**
     * Easy string-to-int conversion with error checks, if input is null or empty or otherwise
     * non-parsable, it simply returns 0
     * @param strInput  string input to be parsed to an int
     * @return parse result
     */
    public static int iRobustStringToInt(String strInput){
        int out = 0;
        try {
            out = Integer.parseInt(strInput);
        }
        catch (Exception ignore){}
        return out;
    }

    /**
     * Easy string-to-long conversion with error checks, if input is null or empty or otherwise
     * non-parsable, it simply returns 0
     * @param strInput  string input to be parsed to an int
     * @return parse result
     */
    public static long lngRobustStringToInt(String strInput){
        long out = 0;
        try {
            out = Long.parseLong(strInput);
        }
        catch (Exception ignore){}
        return out;
    }
}
