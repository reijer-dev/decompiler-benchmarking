package nl.ou.debm.common;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 * Class containing all kinds of miscellaneous functions
 */


public class Misc {
    /**
     * Convert an integer to a string with a fixed length, using zero as
     * pre-character. strGetNumberWithPrefixZeros(23, 4) results in "0023"
     * Negative numbers are inverted to positive numbers. If more digits
     * are needed, because of the height of the value, a longer string will
     * be returened. strGetNumberWithPrefixZeros(123456, 4) will still return "123456"
     * @param iValue  Value to be converted to a string. Negative numbers will be
     *                inverted
     * @param iLength Number of digits requested.
     * @return        String of the requested length (or longer, if iValue is too big),
     *                using "0" as prefix char.
     */
    public static String strGetNumberWithPrefixZeros(int iValue, int iLength){
        // avoid negative input
        iValue = abs(iValue);
        // calculate length
        iLength = max(("" + iValue).length(), iLength);
        // initial conversion:
        // ten zeros are enough, considering the maximum value of an integer
        String strTmp = "000000000" + iValue;
        // ...but, hey, if a user wants a 20-digit string, who are we to defy?
        while ((strTmp.length()-iLength)<0) {
            // this will result in a zero-adding loop, causing some garbage issues
            // however, since this part of the program will quite likely not be
            // called very often and since it adds ten digits per loop, the
            // overhead is acceptable.
            strTmp = "0000000000" + strTmp;
        }
        // return the proper result
        return strTmp.substring(strTmp.length()-iLength);
    }

    /**
     * Get the base-folder for the containers, based on Linux-arch (Jaaps PC) or
     * Windows-arch (Reijers PC/Kesava's PC)
     * @return  base folder for containers
     * TODO: implement Kesava's PC
     */
    public static String strGetContainersBaseFolder(){
        // TODO: Find a nicer debug-solution

        if (File.separatorChar == '/'){
            // assume: Jaap
            return "/home/jaap/VAF/containers/";
        }
        else{
            return "C:\\Users\\reije\\OneDrive\\Documenten\\Development\\c-program\\containers\\";
        }
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
}
