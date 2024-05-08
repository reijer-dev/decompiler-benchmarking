package nl.ou.debm.common;


import nl.ou.debm.common.antlr.CLexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

import static java.lang.Math.abs;

/**
 * Class containing all kinds of miscellaneous functions
 */


public class Misc {
    /** buffered software locations, key=program name (no path) */ private final static Map<String, String> s_softwareLocations = new HashMap<>();

    /**
     * Convert an integer to a string with a fixed length, using zero as
     * pre-character. strGetNumberWithPrefixZeros(23, 4) results in "0023"
     * Negative numbers are inverted to positive numbers. If more digits
     * are needed, because of the height of the value, a longer string will
     * be returned. strGetNumberWithPrefixZeros(123456, 4) will still return "123456"
     * @param lngValue  Value to be converted to a string. Negative numbers will be
     *                inverted
     * @param iLength Number of digits requested.
     * @return        String of the requested length (or longer, if iValue is too big),
     *                using "0" as prefix char.
     */
    public static String strGetNumberWithPrefixZeros(long lngValue, int iLength){
        if(iLength == 0)
            iLength = 1;
        // avoid negative input
        return String.format(Locale.ROOT, "%1$" + iLength + "s", abs(lngValue)).replace(' ', '0');
    }
    public static String strGetHexNumberWithPrefixZeros(long lngValue, int iLength){
        if(iLength == 0)
            iLength = 1;
        // avoid negative input
        return String.format(Locale.ROOT, "%1$" + iLength + "X", abs(lngValue)).replace(' ', '0');
    }

    private static boolean bRunsOnWindows(){
        return (File.separatorChar == '\\');
    }
    private static boolean bRunsOnLinux(){
        return (File.separatorChar == '/');
    }
    public static String strGetExternalSoftwareLocation(String softwareName) {
        // try looking up
        String softwareLocation;
        synchronized (s_softwareLocations){
            softwareLocation= s_softwareLocations.get(softwareName);
        }
        if (softwareLocation!=null){
            return softwareLocation;
        }

        // no joy, so find on system
        String command;
        if (bRunsOnWindows()){
            command = "where";
        }
        else if (bRunsOnLinux()){
            command = "which";
        }
        else {
            throw new RuntimeException("Cannot determine Windows or Linux");
        }
        try {
            var whereSoftware = new ProcessBuilder(command, softwareName).redirectErrorStream(true).start();
            softwareLocation = new BufferedReader(new InputStreamReader(whereSoftware.getInputStream())).readLine();
        }
        catch (Exception ignore) {}
        if (softwareLocation == null){
            throw new RuntimeException("Cannot find requested software: " + softwareName);
        }
        synchronized (s_softwareLocations) {
            s_softwareLocations.put(softwareName, softwareLocation);
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
        return String.format(Locale.ROOT, "%.2f", 100 * v1);
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
        // check higher bound is higher or equal than lower bound
        if ((dblLowBound!=null) && (dblHighBound!=null) && (dblHighBound<dblLowBound)){
            throw new RuntimeException("high bound (" + dblHighBound + ") is lower than low bound (" + dblLowBound + ")");
        }

        // return 1 if actual = target
        if (dblActualValue.equals(dblTargetValue)){
            return 1.0;
        }

        // check if the actual value exceeds bounds
        if ((dblHighBound!=null) && (dblActualValue>dblHighBound)){
            // only accept if target=highbound
            if (!dblTargetValue.equals(dblHighBound)){
                throw new RuntimeException("actual value (" + dblActualValue + ") exceeds high bound (" + dblHighBound + "), target!=high bound");
            }
            // recalculate value, only possible if low bound is set
            if (dblLowBound==null){
                throw new RuntimeException("actual value (" + dblActualValue + ") exceeds high bound (" + dblHighBound + "), no lower bound set");
            }
            dblActualValue = dblHighBound - (dblActualValue - dblHighBound);
            // make sure not below lower bound
            if (dblActualValue < dblLowBound){
                dblActualValue = dblLowBound;
            }
        }
        if ((dblLowBound!=null) && (dblActualValue<dblLowBound)){
            // only accept if target=low bound
            if (!dblTargetValue.equals(dblLowBound)){
                throw new RuntimeException("actual value (" + dblActualValue + ") below low bound (" + dblLowBound + "), target!=low bound");
            }
            // recalculate value, only possible if high bound is set
            if (dblHighBound==null){
                throw new RuntimeException("actual value (" + dblActualValue + ") below low bound (" + dblLowBound + "), no high bound set");
            }
            dblActualValue = dblLowBound + (dblLowBound - dblActualValue);
            // make sure not above lower bound
            if (dblActualValue > dblHighBound){
                dblActualValue = dblHighBound;
            }
        }

        // return null if actual > target and no upper bound is set
        //             if actual < target and no lower bound is set
        // in these cases, we have no margin to relate to
        if (((dblActualValue>dblTargetValue) && (dblHighBound == null)) ||
            ((dblActualValue<dblTargetValue) && (dblLowBound == null))) {
            return null;
        }

        // by now, we have a value that is always either between lower and target of between target and higher
        double margin = 0.0;
        double diff = 0.0;
        if (dblActualValue < dblTargetValue){
            diff = dblActualValue - dblLowBound;
            margin = dblTargetValue - dblLowBound;
        }
        else if (dblActualValue > dblTargetValue) {
            diff = dblHighBound - dblActualValue;
            margin = dblHighBound - dblTargetValue;
        }
        // no equal, because equality was filtered out way before
        // however, if we do not use the else-if, then the compiler complains that dblHighbound might be null
        // which is annoying (and wrong)

        // We want a 100% score in the table to be errorless
        // sometimes the margin is so big, that rounded up a non-100%-score gets to be displayed
        // as 100%. We make sure that doesn't happen.
        var res = diff / margin;
        return ((res>0.9999) && (res<1.0)) ? 0.9999 : res;
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
            if (!bExpression){ System.err.println(strErrorMessage); throw new AssertionError(strErrorMessage); }
        }
    }

    public static class DummyAssertion implements IAssertion{
        @Override
        public void sure(boolean bExpression) {}
        @Override
        public void sure(boolean bExpression, String strErrorMessage) {}
    }

    /**
     * Class to redirect output into nothingness
     * <a href="https://stackoverflow.com/questions/4799006/in-java-how-can-i-redirect-system-out-to-null-then-back-to-stdout-again">Code was found
     * at stackoverflow</a>
     */
    public static class NullPrintStream extends PrintStream {
        public NullPrintStream() {
            super(new NullByteArrayOutputStream());
        }

        private static class NullByteArrayOutputStream extends ByteArrayOutputStream {

            @Override
            public void write(int b) {
                // do nothing
            }

            @Override
            public void write(byte[] b, int off, int len) {
                // do nothing
            }

            @Override
            public void writeTo(OutputStream out) throws IOException {
                // do nothing
            }
        }
    }

    /**
     * Class to check if a string contains a numeral value in any of the C formats
     */
    public static class ConvertCNumeral {
        /** is the converted number an int-like (int or long) */        private boolean m_bIsInteger = false;
        /** is the converted number a float-like (float of double) */   private boolean m_bIsFloat = false;
        /** current int-link value */                                   private long m_lngIntegerValue = 0;
        /** current float -link value */                                private double m_dblFloatingValue = 0.0;
        /** most recent input */                                        private String m_strInput = "";

        /**
         * default constructor
         */
        public ConvertCNumeral(){}

        /**
         * convert string constructor
         * @param strInput string to convert
         */
        public ConvertCNumeral(String strInput){
            setInput(strInput);
        }

        /**
         * check if input is numeral
         * @return true if input is numeral
         */
        public boolean bIsNumeral(){
            return m_bIsInteger || m_bIsFloat;
        }
        /**
         * check if input is integer-like (int, long, char, byte etc.) value
         * @return true if input is integer-like
         */
        public boolean bIsInteger(){
            return m_bIsInteger;
        }
        /**
         * check if input is float-like (double, float etc.) value
         * @return true if input is float-like
         */
        public boolean bIsFloat(){
            return m_bIsFloat;
        }

        /**
         * Compare two object's values
         * @param rhs object to compare to
         * @return -1, 0 or 1 for this&lt;rhs, this==rhs, this&gt;rhs
         */
        public int compareIgnoringValueType(ConvertCNumeral rhs){
            assert rhs!=null;
            assert rhs.bIsNumeral();
            assert this.bIsNumeral();
            if (this.m_bIsInteger && rhs.m_bIsInteger){
                return (int)Math.signum(m_lngIntegerValue - rhs.m_lngIntegerValue);
            }
            if (this.m_bIsInteger && rhs.m_bIsFloat){
                return (int)Math.signum(m_lngIntegerValue - rhs.m_dblFloatingValue);
            }
            if (this.m_bIsFloat && rhs.m_bIsFloat){
                return (int)Math.signum(m_dblFloatingValue - rhs.m_dblFloatingValue);
            }
            return (int)Math.signum(m_dblFloatingValue - rhs.m_lngIntegerValue);
        }

        /**
         * Compare two object's values. Will cause no problem if rhs==null, but return false.
         * @param rhs object to compare to
         * @return true if, and only if, values are equal
         */
        public boolean equalsIgnoringValueTypes(ConvertCNumeral rhs){
            boolean out = false;
            try {
                out = (compareIgnoringValueType(rhs)==0);
            }
            catch (Throwable ignore){}
            return out;
        }

        /**
         * return integer-like conversion result
         * @return decimal value for input, null if not present
         */
        public Long LngGetIntegerLikeValue(){
            if (m_bIsInteger){
                return m_lngIntegerValue;
            }
            return null;
        }
        /**
         * return float-like conversion result
         * @return decimal value for input, null if not present
         */
        public Double DblGetFloatLikeValue(){
            if (m_bIsFloat){
                return m_dblFloatingValue;
            }
            return null;
        }

        /**
         * set conversion input (and convert directly)
         * @param strInput candidate numeral value
         */
        public ConvertCNumeral setInput(String strInput) {
            // reset internals
            m_bIsInteger = false;
            m_bIsFloat = false;
            // copy input
            if (strInput==null){
                m_strInput="";
                return this;
            }
            m_strInput = strInput;
            // get 1-char suffix and 2-char suffix
            String strSuf1 = "", strSuf2 = "";
            if (strInput.length()>1){
                strSuf1 = strInput.substring(strInput.length()-1);
            }
            if (strInput.length()>2){
                strSuf2 = strInput.substring(strInput.length()-2);
            }
            // check suffixes
            boolean bFloatWanted = false, bIntWanted = false;
            if (strSuf2.equalsIgnoreCase("ul")){
                bIntWanted=true;
                strInput = strInput.substring(0,strInput.length()-2);
            }
            else {
                if ((strSuf1.equalsIgnoreCase("l")) ||
                    (strSuf1.equalsIgnoreCase("u"))) {
                    bIntWanted = true;
                    strInput = strInput.substring(0, strInput.length() - 1);
                }
                if (strSuf1.equalsIgnoreCase("f")) {
                    bFloatWanted = true;
                    strInput = strInput.substring(0, strInput.length() - 1);
                }
            }
            // try to convert, first try integer values
            boolean bOK = true;
            try {
                m_lngIntegerValue = Long.decode(strInput);
            }
            catch (Exception e) {
                bOK = false;
            }
            if (bOK){
                // conversion ok? Nice... but only store if an int
                // was explicitly (or implicitly) wanted
                if (!bFloatWanted){
                    m_bIsInteger = true;
                    return this;
                }
            }
            // try converting to a float
            bOK = true;
            try {
                m_dblFloatingValue = Double.parseDouble(strInput);
            }
            catch (Exception e) {
                bOK = false;
            }
            if (bOK){
                // conversion ok? Nice, but only store if float
                // was explicitly or implicitly wanted
                if (!bIntWanted){
                    m_bIsFloat = true;
                    return this;
                }
            }
            // no positive conversion result, so we leave it at this
            return this;
        }

        /**
         * Increase value by 1
         * @return  the increased object
         */
        public ConvertCNumeral increaseByOne(){
            assert bIsNumeral();
            if (m_bIsFloat){
                m_dblFloatingValue++;
            }
            else{
                m_lngIntegerValue++;
            }
            return this;
        }

        /**
         * Decrease value by 1
         * @return  the decreased object
         */
        public ConvertCNumeral decreaseByOne(){
            assert bIsNumeral();
            if (m_bIsFloat){
                m_dblFloatingValue--;
            }
            else{
                m_lngIntegerValue--;
            }
            return this;
        }

        @Override
        public String toString() {
            return m_strInput + "-->" + (!bIsNumeral() ? "X" : (m_bIsFloat ? "F=" + m_dblFloatingValue : "I=" + m_lngIntegerValue));
        }
    }

    /**
     * return the first 'len' chars of a string, but safely, so no null pointer
     * exceptions and no out of bounds exceptions
     * @param strInput input string, may be null (in which case an empty string is returned)
     * @param len max number of characters (can be more than the input's length)
     * @return the requested string
     */
    public static String strSafeLeftString(String strInput, int len){
        if (strInput==null){
            return "";
        }
        if (len>strInput.length()){
            len=strInput.length();
        }
        return strInput.substring(0,len);
    }

    /**
     * return the last 'len' chars of a string, but safely, so no null pointer
     * exceptions and no out of bounds exceptions
     * @param strInput input string, may be null (in which case an empty string is returned)
     * @param len max number of characters (can be more than the input's length)
     * @return the requested string
     */
    public static String strSafeRightString(String strInput, int len){
        if (strInput==null){
            return "";
        }
        int p=strInput.length()-len;
        if (p<0){
            p=0;
        }
        return strInput.substring(p);
    }

    /**
     * Class to store data on ANTLR-elements in (parsed data)
     */
    public static class ANTLRParsedElement{
        /** text of the element/token */                public String strText;
        /** token type ID */                            public int iTokenID;
        ANTLRParsedElement(String strText, int iTokenID){
            this.strText = strText;
            this.iTokenID = iTokenID;
        }
        @Override
        public String toString(){
            return Misc.strGetNumberWithPrefixZeros(iTokenID, 4) + " " + strText;
        }
    }

    /**
     * Return all tokens from a parse tree, work recursively
     * @param prc the subtree to walk
     * @return a list of all tokens (typeID, text)
     */
    public static List<ANTLRParsedElement> getAllTerminalNodes(ParserRuleContext prc) {
        return getAllTerminalNodes(prc, false);
    }
    /**
     * Return all tokens from a parse tree, work recursively
     * @param prc the subtree to walk
     * @param bConcatenateStringLiterals if true, consecutive string literals will be concatenated as if they were one node in the first place
     * @return a list of all tokens (typeID, text)
     */
    public static List<ANTLRParsedElement> getAllTerminalNodes(ParserRuleContext prc, boolean bConcatenateStringLiterals){
        final List<ANTLRParsedElement> out = new ArrayList<>();
        // make a list of all terminal nodes, using recursion
        getAllTerminalNodes_recurse(prc, out);
        // concatenate consecutive string literals
        if (bConcatenateStringLiterals) {
            for (int i=0; i<(out.size()-1); ++i){
                if ((out.get(i).iTokenID == CLexer.StringLiteral) && (out.get(i+1).iTokenID == CLexer.StringLiteral)){
                    // two consecutive strings
                    //
                    // merge and lose double quote in the middle
                    out.set(i, new ANTLRParsedElement(out.get(i).strText.substring(0,out.get(i).strText.length()-1) + out.get(i+1).strText.substring(1), CLexer.StringLiteral));
                    // remove surplus
                    out.remove(i+1);
                    // make sure to check the rest
                    --i;
                }
            }
        }
        return out;
    }

    /**
     * Recursively get all the terminal nodes from a parse tree
     * @param tree the tree to traverse
     * @param list the list to add the terminal nodes to
     */
    private static void getAllTerminalNodes_recurse(ParseTree tree, List<ANTLRParsedElement> list){
        for (int ch = 0; ch <tree.getChildCount(); ch++){
            ParseTree pt = tree.getChild(ch);
            if (pt instanceof TerminalNode node){
                list.add(new ANTLRParsedElement(node.getSymbol().getText(), node.getSymbol().getType()));
            }
            else {
                getAllTerminalNodes_recurse(pt, list);
            }
        }
    }
}
