package nl.ou.debm.common;


import org.jetbrains.annotations.NotNull;

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
    public static String strGetHexNumberWithPrefixZeros(int iValue, int iLength){
        if(iLength == 0)
            iLength = 1;
        // avoid negative input
        return String.format("%1$" + iLength + "X", abs(iValue)).replace(' ', '0');
    }
//    private void setID(long lngID){
//        propMap.put(STRIDFIELD, Long.toHexString(lngID));
//    }

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
     * return false instead of exception
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
            out = Integer.decode(strInput);
        }
        catch (Exception ignore){}
        return out;
    }

    /**
     * Easy string-to-long conversion with error checks, if input is null or empty or otherwise
     * non-parsable, it simply returns 0
     * @param strInput  string input to be parsed to a long
     * @return parse result
     */
    public static long lngRobustStringToLong(String strInput) {
        long out = 0;
        try {
            out = Long.decode(strInput);
        } catch (Exception ignore) {
        }
        return out;
    }

    /**
     * Easy hex string-to-long conversion with error checks, if input is null or empty or otherwise
     * non-parsable, it simply returns 0.
     * @param strHexInput  hex string input to be parsed to a long, may be preceded by "0x" or "0X", but
     *                     not necessarily
     * @return parse result
     */
    public static long lngRobustHexStringToLong(String strHexInput){
        long out = 0;
        if (strHexInput==null){
            return out;
        }
        if (strHexInput.startsWith("0x") || strHexInput.startsWith("0X")) {
            return lngRobustStringToLong(strHexInput);
        }
        return lngRobustStringToLong("0x"+strHexInput);
    }

    /**
     * Remove white space from right side of string
     * @param strInput string to be stripped
     * @return  string without trailing whitespace, may be null if input is null
     */
    public static String strTrimRight(String strInput){
        // check input
        if (strInput == null){
            return null;
        }
        // remove trailing whitespace
        int p;
        for (p=strInput.length()-1; p>=0; --p){
            if (!Character.isWhitespace(strInput.charAt(p))){
                break;
            }
        }
        return strInput.substring(0, p+1);
    }

    /**
     * Return trailer for numeric value. If it is a float,
     * return ".##", otherwise return ""
     * @param bIsFloat trailer for float wanted?
     * @return ".##" of "", # begin a random digit
     */
    public static String strFloatTrailer(boolean bIsFloat){
        return bIsFloat ? "." + strGetNumberWithPrefixZeros(Misc.rnd.nextInt(0, 100),2) : "";
    }

    /**
     * Perform toString method to given object, returning "" is the object is null
     * @param obj object to be toString 'ed
     * @return  obj.toString(), or "" if obj==null.
     */
    public static String strSafeToString(Object obj){
        return strSafeToString(obj, "");
    }

    /**
     * Perform toString method to given object, returning strTextIfNull is the object is null
     * @param obj object to be toString 'ed
     * @param strTextIfNull text to be returned if the object is null
     * @return  obj.toString(), or strTextIfNull if obj==null.
     */
    public static String strSafeToString(Object obj, @NotNull String strTextIfNull){
        if (obj==null){
            return strTextIfNull;
        }
        else {
            var string = obj.toString();
            if (string==null){
                return strTextIfNull;
            }
            else {
                return string;
            }
        }
    }

    /**
     * Get a nice percentage, formatted as 2 decimals number
     * @param dblLowBound     lowest possible value
     * @param dblActualValue  actual value
     * @param dblHighBound    highest possible value
     * @return  the promised nicely formatted percentage string
     */
    public static String strGetPercentage(double dblLowBound, double dblActualValue, double dblHighBound){
        return String.format("%.2f", 100 * dblGetFraction(dblLowBound, dblActualValue, dblHighBound));
    }

    /**
     * Calculate fraction: (actual - low) / (high - low). Returns 0 if high=low.
     * @param dblLowBound     lowest possible value
     * @param dblActualValue  actual value
     * @param dblHighBound    highest possible value
     * @return  fraction
     */
    public static double dblGetFraction(double dblLowBound, double dblActualValue, double dblHighBound){
        var margin = dblHighBound - dblLowBound;
        if(margin == 0) {
            margin = 100;
        }
        return (dblActualValue - dblLowBound) / margin;
    }

    /**
     * Compare to objects, but able to deal with null input
     * @param o1  first object
     * @param o2  second object
     * @return   - o1 and o2 both null: 0<br>
     *           - o1 null and o2 not null: -1 <br>
     *           - o1 not null and o2 null: 1 <br>
     *           - o1 and o2 both not null: result of o1.compareTo(o2)
     * @param <T>  The type of class to be compared
     */
    public static <T> int iSafeCompare(Comparable<T> o1, T o2){
        if (o1==o2){
            return 0;
        }
        if (o1==null){
            return -1;
        }
        if (o2==null){
            return 1;
        }
        return o1.compareTo(o2);
    }

    /**
     * Calculate CRC16 for a string
     * @param strInput Input for CRC calculation
     * @return CRC16
     */
    public static int iCalcCRC16(String strInput){
        // adapted from:
        // D00001275 Flexible & Interoperable Data Transfer (FIT) Protocol Rev 2.3.pdf
        //
        // available at the garmin.com website
        //
        if (strInput==null){
            strInput="";
        }
        int crc = 0;
        for (int p=0; p<strInput.length(); ++p) {
            crc = iGetNextCRC16(crc,strInput.charAt(p));
        }
        return crc;
    }

    private static final int[] s_crcTable = {0x0000, 0xCC01, 0xD801, 0x1400, 0xF001, 0x3C00, 0x2800, 0xE401,
                                             0xA001, 0x6C00, 0x7800, 0xB401, 0x5000, 0x9C01, 0x8801, 0x4400};

    /**
     * Get next CRC-calculation
     * @param crc_in last CRC-16
     * @param c character to be processed
     * @return new CRC-16
     */
    private static int iGetNextCRC16(int crc_in, char c){
        int tmp = s_crcTable[crc_in &0xF];
        crc_in = (crc_in >> 4) & 0xFFF;
        crc_in = crc_in ^ tmp ^ s_crcTable[c & 0xF];
        tmp = s_crcTable[crc_in & 0xF];
        crc_in = (crc_in >> 4) & 0xFFF;
        crc_in = crc_in ^ tmp ^ s_crcTable[(c>>4) & 0xF];
        return crc_in;
    }
}
