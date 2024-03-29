package nl.ou.debm.common;


import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Collection;
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
        return iRobustStringToInt(strInput, 0);
    }

    /**
     * Easy string-to-int conversion with error checks, if input is null or empty or otherwise
     * non-parsable, it simply returns the default value
     * @param strInput  string input to be parsed to an int
     * @param iDefault defaulf value
     * @return parse result
     */
    public static int iRobustStringToInt(String strInput, int iDefault){
        int out = iDefault;
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
        return lngRobustStringToLong(strInput, 0);
    }

    /**
     * Easy string-to-long conversion with error checks, if input is null or empty or otherwise
     * non-parsable, it simply returns the default value
     * @param strInput  string input to be parsed to a long
     * @param lngDefault value to be return on non-parsableness
     * @return parse result
     */
    public static long lngRobustStringToLong(String strInput, long lngDefault) {
        long out = lngDefault;
        try {
            out = Long.decode(strInput);
        } catch (Exception ignore) {
        }
        return out;
    }

    /**
     * Easy string-to-double conversion with error checks, if input is null or empty or otherwise
     * non-parsable, it simply returns 0
     * @param strInput  string input to be parsed to a double
     * @return parse result
     */
    public static double dblRobustStringToDouble(String strInput) {
        return dblRobustStringToDouble(strInput, 0);
    }

    /**
     * Easy string-to-double conversion with error checks, if input is null or empty or otherwise
     * non-parsable, it simply returns the default value
     * @param strInput  string input to be parsed to a double
     * @param dblDefault value to be return on non-parsableness
     * @return parse result
     */
    public static double dblRobustStringToDouble(String strInput, long dblDefault) {
        double out = dblDefault;
        try {
            out = Double.parseDouble(strInput);
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
     * @param dblTargetValue  target value
     * @return  the promised nicely formatted percentage string
     */
    public static String strGetPercentage(Double dblLowBound, Double dblActualValue, Double dblHighBound, Double dblTargetValue){
        Double v1 = dblGetFraction(dblLowBound, dblActualValue, dblHighBound, dblTargetValue);
        if (v1==null){
            return "";
        }
        return String.format("%.2f", 100 * v1);
    }

    /**
     * Calculate fraction. Return 1 if actual = target. Return 0 if actual is low bound or high bound.
     * @param dblLowBound     lowest possible value (may be null)
     * @param dblActualValue  actual value (may be null or NaN)
     * @param dblHighBound    highest possible value (may be null)
     * @param dblTargetValue  target value (may be null)
     * @return  fraction, null when calculation fails (NaN input on dblActualValue, missing bounds, etc.)
     */
    public static Double dblGetFraction(Double dblLowBound, Double dblActualValue, Double dblHighBound, Double dblTargetValue){
        // check nulls in input
        if (dblActualValue==null){
            // no actual value -- be done
            return null;
        }
        if (dblActualValue.isNaN()){
            // actual value is not a number -- be done
            return null;
        }
        if (dblTargetValue==null){
            // no target value -- be done
            return null;
        }
        // return 1 if all is well
        if (dblActualValue.equals(dblTargetValue)){
            return 1.0;
        }

        // determine to compare up or down
        if (dblActualValue > dblTargetValue){
            // find pct in range target-upper bound
            if (dblHighBound==null){
                // no higher bound, done
                return null;
            }
            assert dblActualValue <= dblHighBound : "Actual value is greater than high bound";
            var margin = dblHighBound - dblTargetValue;
            var diff = dblHighBound - dblActualValue;
            if (margin == 0){
                return null;
            }
            else {
                return diff / margin;
            }
        }
        else {
            // find pct in range upper bound-target
            if (dblLowBound==null){
                // no lower bound, done
                return null;
            }
            assert dblLowBound <= dblActualValue : "Actual value is smaller than low bound";
            var margin = dblTargetValue - dblLowBound;
            var diff = dblActualValue - dblLowBound;
            if (margin == 0){
                return null;
            }
            else {
                return diff / margin;
            }
        }
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

    /**
     * safe division<br>
     * x / null -> 0<br>
     * null / x -> 0<br>
     * x / 0 -> 0<br>
     * otherwise: usual division outcome
     * @param aboveLine number to be divided
     * @param belowLine number to divide by
     * @return safe division
     */
    public static double dblSafeDiv(Double aboveLine, Double belowLine){
        if ((aboveLine==null) || (belowLine==null)){
            return 0.0;
        }
        if (belowLine==0){
            return 0.0;
        }
        return aboveLine/belowLine;
    }

    public static double calculateStandardDeviation(Collection<Double> array) {

        // get the sum of array
        var sum = 0.0;
        for (var i : array) {
            sum += i;
        }

        // get the mean of array
        int length = array.size();
        double mean = sum / length;

        // calculate the standard deviation
        double standardDeviation = 0.0;
        for (double num : array) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / (length - 1));
    }

    /*
        the assert keyword is very useful in Java, but it may be switched off with compiler options
        as an alternative, we can use make.sure(), which does pretty much the same, but is controlled by this
        static
     */
    static {
        // use true is assertions should take place, and false if not
        if (true){
            make = new RealAssertion();
        }
        else{
            make = new DummyAssertion();
        }
    }

    /**
     * interface for assertion class, so we can make a real class that actually does something, or a fake class
     * to throw it all away
     */
    public interface IAssertion{
        /**
         * throw runtime exception when expression is false
         * @param bExpression expression to be tested, must be true to have the program continue, when false: throws
         *                    runtime exception
         */
        void sure(boolean bExpression);
        /**
         * throw runtime exception when expression is false
         * @param bExpression expression to be tested, must be true to have the program continue, when false: throws
         *                    runtime exception
         * @param strErrorMessage error message to be included in exception
         */
        void sure(boolean bExpression, String strErrorMessage);
    }

    /**
     * static assert object
     */
    public static IAssertion make;

    public static class RealAssertion implements IAssertion{
        @Override
        public void sure(boolean bExpression) {
            sure(bExpression, "");
        }
        @Override
        public void sure(boolean bExpression, String strErrorMessage) {
            if (!bExpression){ throw new AssertionError(strErrorMessage); }
        }
    }

    public static class DummyAssertion implements IAssertion{
        @Override
        public void sure(boolean bExpression) {}
        @Override
        public void sure(boolean bExpression, String strErrorMessage) {}
    }
}
