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
}