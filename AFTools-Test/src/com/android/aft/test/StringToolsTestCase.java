package com.android.aft.test;

import android.test.AndroidTestCase;

import com.android.aft.AFCoreTools.StringTools;

public class StringToolsTestCase extends AndroidTestCase {

    public void testIsAlnumStr() {
        assertEquals("Big string with alpha char", true, StringTools.isAlNum("dioazdazdpazdjpz"));
        assertEquals("Big string with numeric char", true, StringTools.isAlNum("14984513518412"));
        assertEquals("Big string with alpha and numeric char", true, StringTools.isAlNum("ze1581jfez12699oi"));
        assertEquals("String with a not alphanum char '^'", false, StringTools.isAlNum("zejfez12699o^i"));
    }

    public void testIsAlNumChar() {
        assertEquals("Valid char 'a'", true, StringTools.isAlNum('a'));
        assertEquals("Valid char '5'", true, StringTools.isAlNum('5'));
        assertEquals("Valid char 'H'", true, StringTools.isAlNum('H'));
        assertEquals("Unvalid char ';'", false, StringTools.isAlNum(';'));
    }

    public void testIsHex() {
        assertEquals("Valid hex with numeric char", true, StringTools.isHex("12"));
        assertEquals("Valid hex with alpha char", true, StringTools.isHex("af"));
        assertEquals("Valid hex with a mix of numeric and alpha char", true, StringTools.isHex("4589fbac45d"));
        assertEquals("Unvalid hex with the invalid char 'o'", false, StringTools.isHex("afo45"));
    }

    public void testIsPrintable() {
        assertEquals("Valid char 'a'", true, StringTools.isPrintable('a'));
        assertEquals("Valid char 'Z'", true, StringTools.isPrintable('Z'));
        assertEquals("Valid char '9'", true, StringTools.isPrintable('9'));
        assertEquals("Valid char '-'", true, StringTools.isPrintable('-'));
        assertEquals("Valid char '*'", true, StringTools.isPrintable('*'));
        assertEquals("Invalid char 0x12", false, StringTools.isPrintable((char)0x12));
    }

    public void testToHexCharDigit() {
        assertEquals("Convert hex value 2", '2', StringTools.toHexCharDigit(2));
        assertEquals("Convert hex value 15", 'f', StringTools.toHexCharDigit(15));
    }

    public void testGetHexCharDigit() {
        assertEquals("Convert hex char '2'", 2, StringTools.getHexCharDigit('2'));
        assertEquals("Convert hex char 'F'", 15, StringTools.getHexCharDigit('F'));
    }

    public void testToHex() {
        assertEquals("Convert char ' '", 20, StringTools.toHex(' '));
    }

    public void testToByteArray() {
        String test = "45 86 12";
        byte[] result = StringTools.toByteArray(test);

        assertEquals("Byte array result index 0", 45, result[0]);
        assertEquals("Byte array result index 1", 86, result[1]);
        assertEquals("Byte array result index 2", 12, result[2]);
    }

    public void testToString() {
        byte[] test = new byte[] { 0x61, 0x42, 0x5f};
        assertEquals("Byte array convert to string", "61425f", StringTools.toString(test));
    }

    public void testValueOf() {
        byte[] test = new byte[] { 0x61, 0x42, 0x5f};
        assertEquals("Byte array convert to string", "aB_", StringTools.valueOf(test));
    }

    public void testEscapeUnicode() {
        String input = "acb";
        assertEquals("Convert unicode character", "a\\u00b6", StringTools.escapeUnicode(input));
    }

    public void testGetStringWithoutEncodingInterpretation() {
        char[] chars = new char[] {'a', 'e', 'i'};
        String input = new String(chars);


    }

    public void testGetStringFromAnsiBuffer() {
        char[] chars = new char[] {'a', 'e', 'i'};
        String input = new String(chars);
    }

    public void testGetFullInputStreamData() {
    }

    public void testGetStringRepeat() {
    }

    public void testGetStringRepeatWhitespace() {
    }

    public void testCapitalizeEachWordInString() {
    }

    public void testCapitalize() {
    }

    public void testReadBoolean() {
    }


}
