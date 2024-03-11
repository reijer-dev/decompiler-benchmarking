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
    void TestConversions(){
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

        assertEquals(99, iRobustStringToInt("99"));
        assertEquals(9*16+9, iRobustStringToInt("0x99"));
        assertEquals(99, lngRobustStringToLong("99"));
        assertEquals(9*16+9, lngRobustStringToLong("0x99"));
        assertEquals(9*16+9, lngRobustHexStringToLong("99"));
        assertEquals(9*16+9, lngRobustHexStringToLong("0x99"));
        assertEquals(9*16+9, lngRobustHexStringToLong("0X99"));
    }

    @Test
    public void CRC16Test(){
        assertEquals(40797, iCalcCRC16("dec'm rulez!"));
        assertEquals(26577, iCalcCRC16("JBC/JSC inc."));
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
    }

}