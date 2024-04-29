package nl.ou.debm.test;

import nl.ou.debm.common.IOElements;
import nl.ou.debm.common.Misc;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static nl.ou.debm.common.Misc.*;
import static org.junit.jupiter.api.Assertions.*;

class MiscTest {

    @org.junit.jupiter.api.Test
    void strGetNumberWithPrefixZerosTest() {
        int len = 0;
        assertEquals("0", strGetNumberWithPrefixZeros(0,len));
        assertEquals("1", strGetNumberWithPrefixZeros(1,len));
        assertEquals("9", strGetNumberWithPrefixZeros(9,len));
        assertEquals("10", strGetNumberWithPrefixZeros(10,len));
        assertEquals("15", strGetNumberWithPrefixZeros(15,len));
        assertEquals("23", strGetNumberWithPrefixZeros(23,len));
        assertEquals("99", strGetNumberWithPrefixZeros(99,len));
        assertEquals("100", strGetNumberWithPrefixZeros(100,len));
        
        len = 1;
        assertEquals("0", strGetNumberWithPrefixZeros(0,len));
        assertEquals("1", strGetNumberWithPrefixZeros(1,len));
        assertEquals("9", strGetNumberWithPrefixZeros(9,len));
        assertEquals("10", strGetNumberWithPrefixZeros(10,len));
        assertEquals("15", strGetNumberWithPrefixZeros(15,len));
        assertEquals("23", strGetNumberWithPrefixZeros(23,len));
        assertEquals("99", strGetNumberWithPrefixZeros(99,len));
        assertEquals("100", strGetNumberWithPrefixZeros(100,len));

        len = 2;
        assertEquals("00", strGetNumberWithPrefixZeros(0,len));
        assertEquals("01", strGetNumberWithPrefixZeros(1,len));
        assertEquals("09", strGetNumberWithPrefixZeros(9,len));
        assertEquals("10", strGetNumberWithPrefixZeros(10,len));
        assertEquals("15", strGetNumberWithPrefixZeros(15,len));
        assertEquals("23", strGetNumberWithPrefixZeros(23,len));
        assertEquals("99", strGetNumberWithPrefixZeros(99,len));
        assertEquals("100", strGetNumberWithPrefixZeros(100,len));

        len = 3;
        assertEquals("000", strGetNumberWithPrefixZeros(0,len));
        assertEquals("001", strGetNumberWithPrefixZeros(1,len));
        assertEquals("009", strGetNumberWithPrefixZeros(9,len));
        assertEquals("010", strGetNumberWithPrefixZeros(10,len));
        assertEquals("015", strGetNumberWithPrefixZeros(15,len));
        assertEquals("023", strGetNumberWithPrefixZeros(23,len));
        assertEquals("099", strGetNumberWithPrefixZeros(99,len));
        assertEquals("100", strGetNumberWithPrefixZeros(100,len));

        len = 29;
        assertEquals("00000000000000000000000000000", strGetNumberWithPrefixZeros(0,len));
        assertEquals("00000000000000000000000000001", strGetNumberWithPrefixZeros(1,len));
        assertEquals("00000000000000000000000000009", strGetNumberWithPrefixZeros(9,len));
        assertEquals("00000000000000000000000000010", strGetNumberWithPrefixZeros(10,len));
        assertEquals("00000000000000000000000000015", strGetNumberWithPrefixZeros(15,len));
        assertEquals("00000000000000000000000000023", strGetNumberWithPrefixZeros(23,len));
        assertEquals("00000000000000000000000000099", strGetNumberWithPrefixZeros(99,len));
        assertEquals("00000000000000000000000000100", strGetNumberWithPrefixZeros(100,len));

        len = 30;
        assertEquals("000000000000000000000000000000", strGetNumberWithPrefixZeros(0,len));
        assertEquals("000000000000000000000000000001", strGetNumberWithPrefixZeros(1,len));
        assertEquals("000000000000000000000000000009", strGetNumberWithPrefixZeros(9,len));
        assertEquals("000000000000000000000000000010", strGetNumberWithPrefixZeros(10,len));
        assertEquals("000000000000000000000000000015", strGetNumberWithPrefixZeros(15,len));
        assertEquals("000000000000000000000000000023", strGetNumberWithPrefixZeros(23,len));
        assertEquals("000000000000000000000000000099", strGetNumberWithPrefixZeros(99,len));
        assertEquals("000000000000000000000000000100", strGetNumberWithPrefixZeros(100,len));

        len = 31;
        assertEquals("0000000000000000000000000000000", strGetNumberWithPrefixZeros(0,len));
        assertEquals("0000000000000000000000000000001", strGetNumberWithPrefixZeros(1,len));
        assertEquals("0000000000000000000000000000009", strGetNumberWithPrefixZeros(9,len));
        assertEquals("0000000000000000000000000000010", strGetNumberWithPrefixZeros(10,len));
        assertEquals("0000000000000000000000000000015", strGetNumberWithPrefixZeros(15,len));
        assertEquals("0000000000000000000000000000023", strGetNumberWithPrefixZeros(23,len));
        assertEquals("0000000000000000000000000000099", strGetNumberWithPrefixZeros(99,len));
        assertEquals("0000000000000000000000000000100", strGetNumberWithPrefixZeros(100,len));
    }

    @org.junit.jupiter.api.Test
    void strGetHexNumberWithPrefixZerosTest() {
        int len = 0;
        assertEquals("0", strGetHexNumberWithPrefixZeros(0, len));
        assertEquals("1", strGetHexNumberWithPrefixZeros(1, len));
        assertEquals("9", strGetHexNumberWithPrefixZeros(9, len));
        assertEquals("A", strGetHexNumberWithPrefixZeros(10, len));
        assertEquals("F", strGetHexNumberWithPrefixZeros(15, len));
        assertEquals("17", strGetHexNumberWithPrefixZeros(23, len));
        assertEquals("63", strGetHexNumberWithPrefixZeros(99, len));
        assertEquals("64", strGetHexNumberWithPrefixZeros(100, len));

        len = 1;
        assertEquals("0", strGetHexNumberWithPrefixZeros(0, len));
        assertEquals("1", strGetHexNumberWithPrefixZeros(1, len));
        assertEquals("9", strGetHexNumberWithPrefixZeros(9, len));
        assertEquals("A", strGetHexNumberWithPrefixZeros(10, len));
        assertEquals("F", strGetHexNumberWithPrefixZeros(15, len));
        assertEquals("17", strGetHexNumberWithPrefixZeros(23, len));
        assertEquals("63", strGetHexNumberWithPrefixZeros(99, len));
        assertEquals("64", strGetHexNumberWithPrefixZeros(100, len));

        len = 2;
        assertEquals("00", strGetHexNumberWithPrefixZeros(0, len));
        assertEquals("01", strGetHexNumberWithPrefixZeros(1, len));
        assertEquals("09", strGetHexNumberWithPrefixZeros(9, len));
        assertEquals("0A", strGetHexNumberWithPrefixZeros(10, len));
        assertEquals("0F", strGetHexNumberWithPrefixZeros(15, len));
        assertEquals("17", strGetHexNumberWithPrefixZeros(23, len));
        assertEquals("63", strGetHexNumberWithPrefixZeros(99, len));
        assertEquals("64", strGetHexNumberWithPrefixZeros(100, len));

    }

    @Test
    void strGetExternalSoftwareLocationTest() {
        assertThrows(Exception.class, () -> {
            var location = Misc.strGetExternalSoftwareLocation("dezesoftwarebestaatniet");
        });
        assertDoesNotThrow(() -> {
            var location = Misc.strGetExternalSoftwareLocation("clang");
        });
    }

    @Test
    void booleanPrintValTest() {
        assertEquals('T', cBooleanToChar(true));
        assertEquals('F', cBooleanToChar(false));
    }

    @Test
    void bIsTrueTest(){
        assertTrue(Misc.bIsTrue('T'));
        assertFalse(Misc.bIsTrue('t'));
        assertTrue(Misc.bIsTrue("T"));
        assertFalse(Misc.bIsTrue("t"));
        assertFalse(Misc.bIsTrue(null));
        assertFalse(Misc.bIsTrue("HI"));
        assertFalse(Misc.bIsTrue('f'));
        assertFalse(Misc.bIsTrue("F"));
    }

    @Test
    void TestRobustRoutines(){
        assertDoesNotThrow(() -> {
            Misc.iRobustStringToInt(null);
            Misc.iRobustStringToInt("");
            Misc.iRobustStringToInt("some idiot input");
            Misc.lngRobustStringToLong(null);
            Misc.lngRobustStringToLong("");
            Misc.lngRobustStringToLong("some idiot input");
            Misc.lngRobustHexStringToLong(null);
            Misc.lngRobustHexStringToLong("");
            Misc.lngRobustHexStringToLong("some idiot input");
            Misc.dblRobustStringToDouble(null);
            Misc.dblRobustStringToDouble("");
            Misc.dblRobustStringToDouble("some idiot input");
            Misc.iRobustStringToInt(null,10);
            Misc.iRobustStringToInt("",10);
            Misc.iRobustStringToInt("some idiot input",10);
            Misc.lngRobustStringToLong(null,10);
            Misc.lngRobustStringToLong("",10);
            Misc.lngRobustStringToLong("some idiot input",10);
            Misc.dblRobustStringToDouble(null,10);
            Misc.dblRobustStringToDouble("",10);
            Misc.dblRobustStringToDouble("some idiot input",10);
        });

        assertEquals(0, Misc.iRobustStringToInt(null));
        assertEquals(0, Misc.iRobustStringToInt(""));
        assertEquals(0, Misc.iRobustStringToInt("blablablabla"));

        assertEquals(0, Misc.lngRobustStringToLong(null));
        assertEquals(0, Misc.lngRobustStringToLong(""));
        assertEquals(0, Misc.lngRobustStringToLong("blablablabla"));

        assertEquals(0, Misc.lngRobustHexStringToLong(null));
        assertEquals(0, Misc.lngRobustHexStringToLong(""));
        assertEquals(0, Misc.lngRobustHexStringToLong("blablablabla"));

        assertEquals(0, Misc.dblRobustStringToDouble(null));
        assertEquals(0, Misc.dblRobustStringToDouble(""));
        assertEquals(0, Misc.dblRobustStringToDouble("blablablabla"));

        int defVal= rnd.nextInt(1923);
        assertEquals(defVal, Misc.iRobustStringToInt(null, defVal));
        assertEquals(defVal, Misc.iRobustStringToInt("", defVal));
        assertEquals(defVal, Misc.iRobustStringToInt("blablablabla", defVal));

        assertEquals(defVal, Misc.lngRobustStringToLong(null, defVal));
        assertEquals(defVal, Misc.lngRobustStringToLong("", defVal));
        assertEquals(defVal, Misc.lngRobustStringToLong("blablablabla", defVal));
        assertEquals(defVal, Misc.dblRobustStringToDouble(null), defVal);
        assertEquals(defVal, Misc.dblRobustStringToDouble("", defVal));
        assertEquals(defVal, Misc.dblRobustStringToDouble("blablablabla"), defVal);


        assertEquals(99, iRobustStringToInt("99"));
        assertEquals(9*16+9, iRobustStringToInt("0x99"));
        assertEquals(99, lngRobustStringToLong("99"));
        assertEquals(9*16+9, lngRobustStringToLong("0x99"));
        assertEquals(9*16+9, lngRobustHexStringToLong("99"));
        assertEquals(9*16+9, lngRobustHexStringToLong("0x99"));
        assertEquals(9*16+9, lngRobustHexStringToLong("0X99"));

        assertEquals(2.2, dblRobustStringToDouble("2.2"));
    }


    @Test
    void testTrimRight(){
        for (int i=0; i<10000; i++){
            var v1 = strGetWhiteSpace();
            var v2 = strGetNonWhiteSpace();
            var v3 = strGetWhiteSpace();

            if (v2.isEmpty()){
                assertEquals("", strTrimRight(v1 + v2 + v3));
            }
            else{
                assertEquals(v1 + v2, strTrimRight(v1 + v2 + v3));
            }
        }
    }

    private String strGetWhiteSpace() {
        var sb = new StringBuilder();
        for (int i=rnd.nextInt(0,10); i>0; --i){
            while (true){
                char c = (char)rnd.nextInt(0,256);
                if (Character.isWhitespace(c)){
                    sb.append(c); break;
                }
            }
        }
        return sb.toString();
    }
    private String strGetNonWhiteSpace(){
        return UUID.randomUUID().toString().substring(0, Misc.rnd.nextInt(0,10));
    }

    @Test
    public void CRC16Test(){
        assertEquals(40797, iCalcCRC16("dec'm rulez!"));
        assertEquals(26577, iCalcCRC16("JBC/JSC inc."));
    }

    @Test
    void TestFloatTrailer(){
        assertEquals("", Misc.strFloatTrailer(false));
        for (int i=0; i<100; ++i){
            var tr = Misc.strFloatTrailer(true);
            assertEquals(3, tr.length());
            assertEquals('.', tr.charAt(0));
            assertTrue(tr.charAt(1)>='0' && tr.charAt(1)<='9');
            assertTrue(tr.charAt(2)>='0' && tr.charAt(2)<='9');
        }
    }

    @Test
    void TestSafeString(){
        class NULL_CLASS{
            public String toString(){
                return null;
            }
        }

        var q = new NULL_CLASS();

        final String ts = "IONOWEINJDOPIEWNJFIOEJN";

        assertEquals("", Misc.strSafeToString(q));
        assertEquals("", Misc.strSafeToString(null));
        assertEquals(ts, Misc.strSafeToString(q, ts));
        assertEquals(ts, Misc.strSafeToString(null, ts));
    }

    @Test
    public void FractionTest(){
        double low=10, high=30, target=25;

        assertThrows(AssertionError.class, () -> { Misc.dblGetFraction(low, 9.0, high, target);  });
        assertThrows(AssertionError.class, () -> { Misc.dblGetFraction(low, 31.0, high, target);  });
        assertThrows(AssertionError.class, () -> { Misc.dblGetFraction(high, 15.0, low, target);  });

        assertEquals(1, Misc.dblGetFraction(low, 25.0, high, target));
        assertEquals((10.0/15.0), Misc.dblGetFraction(low, 20.0, high, target));
        assertEquals((5.0/15.0), Misc.dblGetFraction(low, 15.0, high, target));
        assertEquals((0.0/15.0), Misc.dblGetFraction(low, 10.0, high, target));
        assertEquals((0.0/5.0), Misc.dblGetFraction(low, 30.0, high, target));
        assertEquals((1.0/5.0), Misc.dblGetFraction(low, 29.0, high, target));
        assertEquals((2.0/5.0), Misc.dblGetFraction(low, 28.0, high, target));
        assertEquals((3.0/5.0), Misc.dblGetFraction(low, 27.0, high, target));
        assertEquals((4.0/5.0), Misc.dblGetFraction(low, 26.0, high, target));

        assertNull(Misc.dblGetFraction(low,null, high, target));
        assertNull(Misc.dblGetFraction(low,15.0, high, null));
        assertNull(Misc.dblGetFraction(low,null, high, null));
        assertNull(Misc.dblGetFraction(null,15.0, high, target));
        assertNull(Misc.dblGetFraction(low,27.0, null, target));

        assertEquals("", Misc.strGetPercentage(low,null, high, target));
        assertEquals("", Misc.strGetPercentage(low,15.0, high, null));
        assertEquals("", Misc.strGetPercentage(low,null, high, null));
        assertEquals("", Misc.strGetPercentage(null,15.0, high, target));
        assertEquals("", Misc.strGetPercentage(low,27.0, null, target));
    }

    @Test
    void SafeCompareTest(){
        Double lv = -10.0, hv = 31.32, sv = 31.32;
        assertEquals(-1, iSafeCompare(lv,hv));
        assertEquals(1, iSafeCompare(hv, lv));
        assertEquals(0, iSafeCompare(hv, sv));
        assertEquals(-1, iSafeCompare(null, hv));
        assertEquals(1, iSafeCompare(hv, null));
        assertEquals(0, iSafeCompare(null, null));
    }

    @Test
    void SafeDiv(){
        assertEquals(.5, dblSafeDiv(1.0, 2.0));
        assertEquals(0, dblSafeDiv(0.0, 2.0));
        assertEquals(0, dblSafeDiv(null, 2.0));
        assertEquals(0, dblSafeDiv(10.0, 0.0));
        assertEquals(0, dblSafeDiv(10.0, null));
    }

    @Test
    void ConvertCNumbers(){

        var q = new Misc.ConvertCNumeral("0x123UL");
        assertTrue(q.bIsInteger());assertEquals(291,q.LngGetIntegerLikeValue());
        q.setInput("0x123U"); assertTrue(q.bIsInteger()); assertEquals(291,q.LngGetIntegerLikeValue()); assertTrue(q.bIsNumeral());
        q.setInput("0x123L"); assertTrue(q.bIsInteger()); assertEquals(291,q.LngGetIntegerLikeValue()); assertTrue(q.bIsNumeral());
        q.setInput("0x123LU"); assertFalse(q.bIsInteger()); assertNull(q.LngGetIntegerLikeValue()); assertFalse(q.bIsNumeral());

        q.setInput("123"); assertEquals(123, q.LngGetIntegerLikeValue()); assertTrue(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("123U"); assertEquals(123, q.LngGetIntegerLikeValue()); assertTrue(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("123UL"); assertEquals(123, q.LngGetIntegerLikeValue()); assertTrue(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("123L"); assertEquals(123, q.LngGetIntegerLikeValue()); assertTrue(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("123LU"); assertNull(q.LngGetIntegerLikeValue()); assertFalse(q.bIsInteger()); assertFalse(q.bIsNumeral());

        q.setInput("123F"); assertNull(q.LngGetIntegerLikeValue());  assertFalse(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("123F"); assertEquals(123, q.DblGetFloatLikeValue()); assertFalse(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("123.");assertEquals(123, q.DblGetFloatLikeValue()); assertFalse(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("123.12");assertEquals(123.12, q.DblGetFloatLikeValue()); assertFalse(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("123.14");assertEquals(123.14, q.DblGetFloatLikeValue()); assertFalse(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("123.15");assertEquals(123.15, q.DblGetFloatLikeValue()); assertFalse(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("12.A3F"); assertNull(q.DblGetFloatLikeValue()); assertFalse(q.bIsInteger()); assertFalse(q.bIsNumeral());

        q.setInput("0123"); assertEquals(83, q.LngGetIntegerLikeValue()); assertTrue(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("0123U"); assertEquals(83, q.LngGetIntegerLikeValue()); assertTrue(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("0123UL"); assertEquals(83, q.LngGetIntegerLikeValue()); assertTrue(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("0123L"); assertEquals(83, q.LngGetIntegerLikeValue()); assertTrue(q.bIsInteger()); assertTrue(q.bIsNumeral());
        q.setInput("0123LU"); assertNull(q.LngGetIntegerLikeValue()); assertFalse(q.bIsInteger()); assertFalse(q.bIsNumeral());

        q.setInput("blubber"); assertNull(q.LngGetIntegerLikeValue()); assertNull(q.DblGetFloatLikeValue());

        var p=new Misc.ConvertCNumeral();
        assertNull(p.LngGetIntegerLikeValue()); assertNull(p.DblGetFloatLikeValue()); assertFalse(p.bIsNumeral());

        p.setInput("1.0");  q.setInput("12.0"); assertEquals(-1,p.compareIgnoringValueType(q));
        p.setInput("12.0"); q.setInput("12.0"); assertEquals(0,p.compareIgnoringValueType(q));
        p.setInput("24.0"); q.setInput("12.0"); assertEquals(1,p.compareIgnoringValueType(q));
        p.setInput("1  ");  q.setInput("12.0"); assertEquals(-1,p.compareIgnoringValueType(q));
        p.setInput("12  "); q.setInput("12.0"); assertEquals(0,p.compareIgnoringValueType(q));
        p.setInput("24  "); q.setInput("12.0"); assertEquals(1,p.compareIgnoringValueType(q));
        p.setInput("1.0");  q.setInput("12  "); assertEquals(-1,p.compareIgnoringValueType(q));
        p.setInput("12.0"); q.setInput("12  "); assertEquals(0,p.compareIgnoringValueType(q));
        p.setInput("24.0"); q.setInput("12  "); assertEquals(1,p.compareIgnoringValueType(q));
        p.setInput("1  ");  q.setInput("12  "); assertEquals(-1,p.compareIgnoringValueType(q));
        p.setInput("12  "); q.setInput("12  "); assertEquals(0,p.compareIgnoringValueType(q));
        p.setInput("24  "); q.setInput("12  "); assertEquals(1,p.compareIgnoringValueType(q));

        p.setInput("24"); p.increaseByOne(); assertEquals(25, p.LngGetIntegerLikeValue());
        p.setInput("24"); assertEquals(25, p.increaseByOne().LngGetIntegerLikeValue());
        p.setInput("24"); p.decreaseByOne(); assertEquals(23, p.LngGetIntegerLikeValue());
        p.setInput("24"); assertEquals(23, p.decreaseByOne().LngGetIntegerLikeValue());
        p.setInput("24.2"); p.increaseByOne(); assertEquals(25.2, p.DblGetFloatLikeValue());
        p.setInput("24.3"); assertEquals(25.3, p.increaseByOne().DblGetFloatLikeValue());
        p.setInput("24.4"); p.decreaseByOne(); assertEquals(23.4, p.DblGetFloatLikeValue());
        p.setInput("24.5"); assertEquals(23.5, p.decreaseByOne().DblGetFloatLikeValue());
    }

    @Test
    void safeLeftAndRight(){
        String TEST = "0123456789";
        assertEquals("", Misc.strSafeLeftString(null, 10));
        assertEquals("", Misc.strSafeLeftString(TEST, 0));
        assertEquals("0", Misc.strSafeLeftString(TEST, 1));
        assertEquals("01", Misc.strSafeLeftString(TEST, 2));
        assertEquals("012", Misc.strSafeLeftString(TEST, 3));
        assertEquals("0123", Misc.strSafeLeftString(TEST, 4));
        assertEquals("01234", Misc.strSafeLeftString(TEST, 5));
        assertEquals("012345", Misc.strSafeLeftString(TEST, 6));
        assertEquals("0123456", Misc.strSafeLeftString(TEST, 7));
        assertEquals("01234567", Misc.strSafeLeftString(TEST, 8));
        assertEquals("012345678", Misc.strSafeLeftString(TEST, 9));
        assertEquals("0123456789", Misc.strSafeLeftString(TEST, 10));
        assertEquals("0123456789", Misc.strSafeLeftString(TEST, 11));

        assertEquals("", Misc.strSafeRightString(null, 10));
        assertEquals("", Misc.strSafeRightString(TEST, 0));
        assertEquals("9", Misc.strSafeRightString(TEST, 1));
        assertEquals("89", Misc.strSafeRightString(TEST, 2));
        assertEquals("789", Misc.strSafeRightString(TEST, 3));
        assertEquals("6789", Misc.strSafeRightString(TEST, 4));
        assertEquals("56789", Misc.strSafeRightString(TEST, 5));
        assertEquals("456789", Misc.strSafeRightString(TEST, 6));
        assertEquals("3456789", Misc.strSafeRightString(TEST, 7));
        assertEquals("23456789", Misc.strSafeRightString(TEST, 8));
        assertEquals("123456789", Misc.strSafeRightString(TEST, 9));
        assertEquals("0123456789", Misc.strSafeRightString(TEST, 10));
        assertEquals("0123456789", Misc.strSafeRightString(TEST, 11));

    }

    @Test
    void FileIndices(){
        assertEquals("", IOElements.strAddFileIndex(null, 0, 0));
        assertEquals("", IOElements.strAddFileIndex("", 0, 0));
        assertEquals("/foo/bar", IOElements.strAddFileIndex("/foo/bar", 0, 1));
        assertEquals("/foo/bar10", IOElements.strAddFileIndex("/foo/bar", 10, 1));
        assertEquals("/foo/bar10.exe", IOElements.strAddFileIndex("/foo/bar.exe", 10, 1));
        assertEquals("/foo/bar.hoi10.exe", IOElements.strAddFileIndex("/foo/bar.hoi.exe", 10, 1));
    }
}