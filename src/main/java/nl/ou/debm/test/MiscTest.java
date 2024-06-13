package nl.ou.debm.test;

import nl.ou.debm.common.Misc;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static nl.ou.debm.common.Misc.*;
import static org.junit.jupiter.api.Assertions.*;

class MiscTest {

    @org.junit.jupiter.api.Test
    void strGetNumberWithPrefixZerosTest() {
        int len = 0;
        assertEquals("0", strGetAbsNumberWithPrefixZeros(0,len));
        assertEquals("1", strGetAbsNumberWithPrefixZeros(1,len));
        assertEquals("9", strGetAbsNumberWithPrefixZeros(9,len));
        assertEquals("10", strGetAbsNumberWithPrefixZeros(10,len));
        assertEquals("15", strGetAbsNumberWithPrefixZeros(15,len));
        assertEquals("23", strGetAbsNumberWithPrefixZeros(23,len));
        assertEquals("99", strGetAbsNumberWithPrefixZeros(99,len));
        assertEquals("100", strGetAbsNumberWithPrefixZeros(100,len));
        
        len = 1;
        assertEquals("0", strGetAbsNumberWithPrefixZeros(0,len));
        assertEquals("1", strGetAbsNumberWithPrefixZeros(1,len));
        assertEquals("9", strGetAbsNumberWithPrefixZeros(9,len));
        assertEquals("10", strGetAbsNumberWithPrefixZeros(10,len));
        assertEquals("15", strGetAbsNumberWithPrefixZeros(15,len));
        assertEquals("23", strGetAbsNumberWithPrefixZeros(23,len));
        assertEquals("99", strGetAbsNumberWithPrefixZeros(99,len));
        assertEquals("100", strGetAbsNumberWithPrefixZeros(100,len));

        len = 2;
        assertEquals("00", strGetAbsNumberWithPrefixZeros(0,len));
        assertEquals("01", strGetAbsNumberWithPrefixZeros(1,len));
        assertEquals("09", strGetAbsNumberWithPrefixZeros(9,len));
        assertEquals("10", strGetAbsNumberWithPrefixZeros(10,len));
        assertEquals("15", strGetAbsNumberWithPrefixZeros(15,len));
        assertEquals("23", strGetAbsNumberWithPrefixZeros(23,len));
        assertEquals("99", strGetAbsNumberWithPrefixZeros(99,len));
        assertEquals("100", strGetAbsNumberWithPrefixZeros(100,len));

        len = 3;
        assertEquals("000", strGetAbsNumberWithPrefixZeros(0,len));
        assertEquals("001", strGetAbsNumberWithPrefixZeros(1,len));
        assertEquals("009", strGetAbsNumberWithPrefixZeros(9,len));
        assertEquals("010", strGetAbsNumberWithPrefixZeros(10,len));
        assertEquals("015", strGetAbsNumberWithPrefixZeros(15,len));
        assertEquals("023", strGetAbsNumberWithPrefixZeros(23,len));
        assertEquals("099", strGetAbsNumberWithPrefixZeros(99,len));
        assertEquals("100", strGetAbsNumberWithPrefixZeros(100,len));

        len = 29;
        assertEquals("00000000000000000000000000000", strGetAbsNumberWithPrefixZeros(0,len));
        assertEquals("00000000000000000000000000001", strGetAbsNumberWithPrefixZeros(1,len));
        assertEquals("00000000000000000000000000009", strGetAbsNumberWithPrefixZeros(9,len));
        assertEquals("00000000000000000000000000010", strGetAbsNumberWithPrefixZeros(10,len));
        assertEquals("00000000000000000000000000015", strGetAbsNumberWithPrefixZeros(15,len));
        assertEquals("00000000000000000000000000023", strGetAbsNumberWithPrefixZeros(23,len));
        assertEquals("00000000000000000000000000099", strGetAbsNumberWithPrefixZeros(99,len));
        assertEquals("00000000000000000000000000100", strGetAbsNumberWithPrefixZeros(100,len));

        len = 30;
        assertEquals("000000000000000000000000000000", strGetAbsNumberWithPrefixZeros(0,len));
        assertEquals("000000000000000000000000000001", strGetAbsNumberWithPrefixZeros(1,len));
        assertEquals("000000000000000000000000000009", strGetAbsNumberWithPrefixZeros(9,len));
        assertEquals("000000000000000000000000000010", strGetAbsNumberWithPrefixZeros(10,len));
        assertEquals("000000000000000000000000000015", strGetAbsNumberWithPrefixZeros(15,len));
        assertEquals("000000000000000000000000000023", strGetAbsNumberWithPrefixZeros(23,len));
        assertEquals("000000000000000000000000000099", strGetAbsNumberWithPrefixZeros(99,len));
        assertEquals("000000000000000000000000000100", strGetAbsNumberWithPrefixZeros(100,len));

        len = 31;
        assertEquals("0000000000000000000000000000000", strGetAbsNumberWithPrefixZeros(0,len));
        assertEquals("0000000000000000000000000000001", strGetAbsNumberWithPrefixZeros(1,len));
        assertEquals("0000000000000000000000000000009", strGetAbsNumberWithPrefixZeros(9,len));
        assertEquals("0000000000000000000000000000010", strGetAbsNumberWithPrefixZeros(10,len));
        assertEquals("0000000000000000000000000000015", strGetAbsNumberWithPrefixZeros(15,len));
        assertEquals("0000000000000000000000000000023", strGetAbsNumberWithPrefixZeros(23,len));
        assertEquals("0000000000000000000000000000099", strGetAbsNumberWithPrefixZeros(99,len));
        assertEquals("0000000000000000000000000000100", strGetAbsNumberWithPrefixZeros(100,len));
    }

    @org.junit.jupiter.api.Test
    void strGetHexNumberWithPrefixZerosTest() {
        int len = 0;
        assertEquals("0", strGetAbsHexNumberWithPrefixZeros(0, len));
        assertEquals("1", strGetAbsHexNumberWithPrefixZeros(1, len));
        assertEquals("9", strGetAbsHexNumberWithPrefixZeros(9, len));
        assertEquals("A", strGetAbsHexNumberWithPrefixZeros(10, len));
        assertEquals("F", strGetAbsHexNumberWithPrefixZeros(15, len));
        assertEquals("17", strGetAbsHexNumberWithPrefixZeros(23, len));
        assertEquals("63", strGetAbsHexNumberWithPrefixZeros(99, len));
        assertEquals("64", strGetAbsHexNumberWithPrefixZeros(100, len));

        len = 1;
        assertEquals("0", strGetAbsHexNumberWithPrefixZeros(0, len));
        assertEquals("1", strGetAbsHexNumberWithPrefixZeros(1, len));
        assertEquals("9", strGetAbsHexNumberWithPrefixZeros(9, len));
        assertEquals("A", strGetAbsHexNumberWithPrefixZeros(10, len));
        assertEquals("F", strGetAbsHexNumberWithPrefixZeros(15, len));
        assertEquals("17", strGetAbsHexNumberWithPrefixZeros(23, len));
        assertEquals("63", strGetAbsHexNumberWithPrefixZeros(99, len));
        assertEquals("64", strGetAbsHexNumberWithPrefixZeros(100, len));

        len = 2;
        assertEquals("00", strGetAbsHexNumberWithPrefixZeros(0, len));
        assertEquals("01", strGetAbsHexNumberWithPrefixZeros(1, len));
        assertEquals("09", strGetAbsHexNumberWithPrefixZeros(9, len));
        assertEquals("0A", strGetAbsHexNumberWithPrefixZeros(10, len));
        assertEquals("0F", strGetAbsHexNumberWithPrefixZeros(15, len));
        assertEquals("17", strGetAbsHexNumberWithPrefixZeros(23, len));
        assertEquals("63", strGetAbsHexNumberWithPrefixZeros(99, len));
        assertEquals("64", strGetAbsHexNumberWithPrefixZeros(100, len));

    }

    @Test
    void strGetExternalSoftwareLocationTest() {
        assertThrows(RuntimeException.class, () -> {
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

        assertThrows(RuntimeException.class, () -> { Misc.dblGetFraction(high, 15.0, low, target);  });
        assertThrows(RuntimeException.class, () -> { Misc.dblGetFraction(high, 9.0, low, target);  });
        assertThrows(RuntimeException.class, () -> { Misc.dblGetFraction(high, 31.0, low, target);  });

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

        double l2=0, t2=6, h2=10;
        assertThrows(RuntimeException.class, () -> { Misc.dblGetFraction(l2, -2.0, h2, t2); });
        assertThrows(RuntimeException.class, () -> { Misc.dblGetFraction(l2, 12.0, h2, t2); });
        assertEquals((0.0/6.0), Misc.dblGetFraction(l2, 0.0, h2, t2));
        assertEquals((2.0/6.0), Misc.dblGetFraction(l2, 2.0, h2, t2));
        assertEquals((1.0), Misc.dblGetFraction(l2, 6.0, h2, t2));
        assertEquals((2.0/4.0), Misc.dblGetFraction(l2, 8.0, h2, t2));
        assertEquals((1.0/4.0), Misc.dblGetFraction(l2, 9.0, h2, t2));
        assertEquals((0.0/4.0), Misc.dblGetFraction(l2, 10.0, h2, t2));

        double l3=0, t3=10, h3=10;
        assertThrows(RuntimeException.class, () -> { Misc.dblGetFraction(l3, -1.0, h3, t3); });
        assertEquals((0.0/10.0), Misc.dblGetFraction(l3, 0.0, h3, t3));
        assertEquals((3.0/10.0), Misc.dblGetFraction(l3, 3.0, h3, t3));
        assertEquals((5.0/10.0), Misc.dblGetFraction(l3, 5.0, h3, t3));
        assertEquals((9.0/10.0), Misc.dblGetFraction(l3, 9.0, h3, t3));
        assertEquals((1.0), Misc.dblGetFraction(l3, 10.0, h3, t3));
        assertEquals((9.0/10.0), Misc.dblGetFraction(l3, 11.0, h3, t3));
        assertEquals((1.0/10.0), Misc.dblGetFraction(l3, 19.0, h3, t3));
        assertEquals((0.0/10.0), Misc.dblGetFraction(l3, 20.0, h3, t3));
        assertEquals((0.0/10.0), Misc.dblGetFraction(l3, 200.0, h3, t3));

        double l4=0, t4=10;
        Double h4 = null;
        assertThrows(RuntimeException.class, () -> { Misc.dblGetFraction(l4, -1.0, h4, t4); });
        assertEquals((0.0/10.0), Misc.dblGetFraction(l4, 0.0, h4, t4));
        assertEquals((3.0/10.0), Misc.dblGetFraction(l4, 3.0, h4, t4));
        assertEquals((5.0/10.0), Misc.dblGetFraction(l4, 5.0, h4, t4));
        assertEquals((9.0/10.0), Misc.dblGetFraction(l4, 9.0, h4, t4));
        assertEquals((1.0), Misc.dblGetFraction(l4, 10.0, h4, t4));
        assertNull(dblGetFraction(l4, 11.0, h4, t4));
        assertNull(dblGetFraction(l4, 19.0, h4, t4));
        assertNull(dblGetFraction(l4, 20.0, h4, t4));
        assertNull(dblGetFraction(l4, 200.0, h4, t4));

        double l5=0, t5=0, h5=10;
        assertThrows(RuntimeException.class, () -> { Misc.dblGetFraction(l5, 11.0, h5, t5); });
        assertEquals((1.0), Misc.dblGetFraction(l5, 0.0, h5, t5));
        assertEquals((7.0/10.0), Misc.dblGetFraction(l5, 3.0, h5, t5));
        assertEquals((5.0/10.0), Misc.dblGetFraction(l5, 5.0, h5, t5));
        assertEquals((1.0/10.0), Misc.dblGetFraction(l5, 9.0, h5, t5));
        assertEquals((0.0/10.0), Misc.dblGetFraction(l5, 10.0, h5, t5));
        assertEquals((9.0/10.0), Misc.dblGetFraction(l5, -1.0, h5, t5));
        assertEquals((1.0/10.0), Misc.dblGetFraction(l5, -9.0, h5, t5));
        assertEquals((0.0/10.0), Misc.dblGetFraction(l5, -10.0, h5, t5));
        assertEquals((0.0/10.0), Misc.dblGetFraction(l5, -200.0, h5, t5));

        double t6=0, h6=10;
        Double l6 = null;
        assertThrows(RuntimeException.class, () -> { Misc.dblGetFraction(l6, 11.0, h6, t6); });
        assertEquals((1.0), Misc.dblGetFraction(l6, 0.0, h6, t6));
        assertEquals((7.0/10.0), Misc.dblGetFraction(l6, 3.0, h6, t6));
        assertEquals((5.0/10.0), Misc.dblGetFraction(l6, 5.0, h6, t6));
        assertEquals((1.0/10.0), Misc.dblGetFraction(l6, 9.0, h6, t6));
        assertEquals((0.0/10.0), Misc.dblGetFraction(l6, 10.0, h6, t6));
        assertNull(dblGetFraction(l6, -1.0, h6, t6));
        assertNull(dblGetFraction(l6, -9.0, h6, t6));
        assertNull(dblGetFraction(l6, -10.0, h6, t6));
        assertNull(dblGetFraction(l6, -200.0, h6, t6));

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

        assertEquals("012345678", Misc.strSafeLeftString(TEST, -1));
        assertEquals("01234567", Misc.strSafeLeftString(TEST, -2));
        assertEquals("0123456", Misc.strSafeLeftString(TEST, -3));
        assertEquals("012345", Misc.strSafeLeftString(TEST, -4));
        assertEquals("01234", Misc.strSafeLeftString(TEST, -5));
        assertEquals("0123", Misc.strSafeLeftString(TEST, -6));
        assertEquals("012", Misc.strSafeLeftString(TEST, -7));
        assertEquals("01", Misc.strSafeLeftString(TEST, -8));
        assertEquals("0", Misc.strSafeLeftString(TEST, -9));
        assertEquals("", Misc.strSafeLeftString(TEST, -10));
        assertEquals("", Misc.strSafeLeftString(TEST, -11));

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

}