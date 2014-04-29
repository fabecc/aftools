/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package com.android.aft.AFCoreTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Formatter;
import java.util.Locale;

public class StringTools {

    /**
     * Test if {@code str} contains only alphanumeric characters
     *
     * @param str String to test
     *
     * @return test result
     */
    public static boolean isAlNum(String str) {
        return str.matches("[[a-z][A-Z][0-9]]*");
    }

    /**
     * Test if {@code c} is an alphanumeric character
     *
     * @param c The char to test
     *
     * @return test result
     */
    public static boolean isAlNum(char c) {
        return c >= 'a' && c <= 'z'
            || c >= 'A' && c <= 'Z'
            || c >= '0' && c <= '9';
    }

    /**
     * Test if {@code str} contains only hexadecimal characters
     *
     * @param str The string to test
     *
     * @return test result
     */
    public static boolean isHex(String str) {
        return str.matches("[[a-f][A-F][0-9]]*");
    }

    /**
     * Test if {@code c} is printable
     *
     * @param c The char to test
     *
     * @return test result
     */
    public static boolean isPrintable(char c) {
        // From ascii table, all char from ' ' to '~' as printable
        return c >= 0x20 && c <= 0x7e;
    }

    /**
     * Convert the int {@code digit} parameter to its hexadecimal character
     * Example: toHexCharDigit(15) -> 'f'
     *
     * @param digit number to convert (need to be into the range [0;15])
     *
     * @return The character corresponding to digit value
     */
    public static char toHexCharDigit(int digit) throws AFException {
        if (digit >= 0 && digit <= 9)
            return (char) ('0' + digit);
        else if (digit >= 10 && digit <= 15)
            return (char) ('a' + (digit - 10));

//        DebugTools.e("Invalid conversion from " + digit + " to simple hex character");
        throw new AFException("StringTools", "toHexCharDigit", "Invalid conversion from " + digit + " to simple hex character");
    }

    /**
     * Convert the char {@code digit} parameter in hexadecimal format in the corresponding int value
     * Example: getHexCharDigit('f') -> 15
     *
     * @param digit character to convert (need to be into the range [a-fA-F0-9])
     *
     * @return The int corresponding to digit value
     */
    public static int getHexCharDigit(char digit) throws AFException {
        if (digit >= '0' && digit <= '9')
            return digit - '0';
        else if (digit >= 'a' && digit <= 'z')
            return 10 + digit - 'a';
        else if (digit >= 'A' && digit <= 'Z')
            return 10 + digit - 'A';

//        DebugTools.e("Invalid conversion from hex char '" + digit + "' to its value");
        throw new AFException("StringTools", "getHexCharDigit", "Invalid conversion from hex char '" + digit + "' to its value");
    }

    /**
     * Convert the char {@code c} parameter in a string with the corresponding hexedecimal value
     * Example: toHex(' ') => "20"
     *
     * @param c character to convert
     *
     * @return The string result with convert value
     */
    public static String toHex(char c) {
        StringBuffer result = new StringBuffer();

        byte b = (byte)c;
        byte up = (byte)((b >> 4) & 0xF);
        byte down = (byte)(b & 0xF);

        result.append(toHexCharDigit(up));
        result.append(toHexCharDigit(down));

        return result.toString();
    }

    /**
     * Convert {@code data} with a serial of hexadecimal value in string to a byte array
     * with the corresponding element value.
     *
     * The separator can be any number of characters without a-zA-Z0-9
     * Example: toByteArray("61 42 5f") => {0x61, 0x42, 0x5f }
     *
     * @param data The string with all elements value
     *
     * @return The byte array result
     */
    public static byte[] toByteArray(String data) {
        // Construct a temporary byte array because the number of element is not know
        byte[] buf = new byte[data.length() / 2];
        int len = 0;

        // Extract byte for data (use all digit in a-zA-Z0-9 as entry)
        byte[] data_b = data.getBytes();
        boolean hasUp = false;
        for (int i = 0; i < data_b.length; ++i) {
            char c = (char)data_b[i];
            if (c >= 'a' && c <= 'z'
                || c >= 'A' && c <= 'Z'
                || c >= '0' && c <= '9') {

                int value = getHexCharDigit(c);

                if (!hasUp) {
                    hasUp = true;
                    buf[len] = (byte)(value << 4);
                }
                else {
                    hasUp = false;
                    buf[len++] |= (byte)value;
                }
            }
        }

        // Construct the final byte array with good number of elements
        byte[] res = new byte[len];
        for (int i = 0; i < len; ++i)
            res[i] = buf[i];

        return res;
    }

    /**
     * Convert {@code data} byte array to a string with each byte convert in hexadecimal value
     * Example: toString({ 0x61, 0x42, 0x5f }) => "61425f"
     *
     * @param data The byte array with data to convert
     *
     * @return The string result
     */
    public static String toString(byte[] data) {
        StringBuffer str = new StringBuffer();

        for (byte b: data)
            str.append(toHex((char)b));

        return str.toString();
    }

    /**
     * Convert {@code data} byte array to a string with each byte cast as char
     * Example: toString({ 0x61, 0x42, 0x5f }) => "aB_"
     *
     * @param data The byte array with data to convert
     *
     * @return The string result
     */
    public static String valueOf(byte[] data) {
        StringBuffer str = new StringBuffer();

        for (byte b: data)
            str.append((char)b);

        return str.toString();
    }

    /**
     * Convert {@code str} in java string format (utf8) to a string with unicode encoding char
     *
     * @param str The string to convert
     *
     * @return A string in unicode format
     */
    public static String escapeUnicode(String str) {
    	StringBuilder b = new StringBuilder(str.length());
    	Formatter f = new Formatter(b);
    	for (char c : str.toCharArray()) {
    		if (c < 128)
		      b.append(c);
    		else
		      f.format("\\u%04x", (int)c);
    	}
    	f.close();
    	return b.toString();
    }

    /**
     * Convert {@code buf} content in String without take care of any encoding
     *
     * @param buf StringBuilder with some exotic encoding data

     * @return String result
     */
    public static String getStringWithoutEncodingInterpretation(StringBuilder buf) {
        char[] chars = new char[buf.length()];

        for (int j = 0; j < buf.length(); ++j)
            chars[j] = buf.charAt(j);

        return new String(chars);
    }

    /**
     * Convert {@code buf} content with ANSI format content in a normal java UTF8 String
     *
     * @param buf StringBuilder with ANSI char
     *
     * @return String result
     */
    public static String getStringFromAnsiBuffer(StringBuilder buf) {
        byte[] chars = new byte[buf.length() * 2];
        int chars_idx = 0;

        for (int j = 0; j < buf.length(); ++j) {
            char c = buf.charAt(j);

            // Special case for 0x19
            if (c == 0x19)
                chars[chars_idx++] = '\'';
            // For 0 <= c < 0x80 => Nothing
            else if (c < 0x80)
                chars[chars_idx++] = (byte)c;
            // For 0x80 <= c < 0xBF => Add 0xC2 before
            else if (c < 0xBF)
            {
                chars[chars_idx++] = (byte)0xC2;
                chars[chars_idx++] = (byte)c;
            }
            // For 0x80 <= c => Add 0xC3 before add substract 0x40 at c
            else
            {
                chars[chars_idx++] = (byte)0xC3;
                chars[chars_idx++] = (byte)(c - 0x40);
            }
        }

        return new String(chars, 0, chars_idx);
    }

    /**
     * Read all data from {@code input} an create a string with all reading element
     *
     * @param input input data
     *
     * @return String with all the data give by the input stream
     */
    public static String getFullInputStreamData(InputStream input) {
        InputStreamReader data_reader = new InputStreamReader(input);

        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[1024];
        do {
            int l = 0;
            try {
                l = data_reader.read(buffer);
            } catch (IOException e) {
                break ;
            }
            if (l <= 0)
                break ;
            builder.append(buffer, 0, l);
        } while (true);

        return builder.toString();
    }

    /**
     * Get a string with the {@code c} char repeat {@code n} times
     *
     * @param c The character to write in the string
     * @param n The number of times to add the char in the string
     *
     * @return result string
     */
    public static String getStringRepeat(char c, int n) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < n; ++i)
            sb.append(c);

        return sb.toString();
    }

    /**
     * Get a string with the space char (' ') repeat {@code length} times
     *
     * @param length The number of times to add the char in the string
     *
     * @return result string
     */
    public static String getStringRepeatWhitespace(int length) {
        return getStringRepeat(' ', length);
    }

    /**
     * Convert each word of the string {@code str}: for each word, put first letter
     * in upper case and other letters in lower case.
     *
     * @param str the string to convert
     *
     * @return the case modified String or null if {@code str} is null
     */
    public static String capitalizeEachWordInString(String str) {
        if (str == null)
            return null;

        char[] chars = str.toLowerCase(Locale.getDefault()).toCharArray();

        boolean firstLetterFound = false;

        for (int i = 0; i < chars.length; i++) {
            if (!firstLetterFound && Character.isLetter(chars[i])) {
                // This is the first letter of a word, capitalize the letter
                chars[i] = Character.toUpperCase(chars[i]);
                firstLetterFound = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i] == '.' || chars[i] == '\'' || chars[i] == '"') {
                // Note: other chars (word separator) can be added tested here
                // A word separator has been found
                firstLetterFound = false;
            }
        }

        return String.valueOf(chars);
    }

    /**
     * Capitalize the {@code text} string
     *
     * @param text The input string
     *
     * @return the capitalized result string
     */
    public static String capitalize(String text) {
        if (text.length() > 1)
            return text.substring(0, 1).toUpperCase(Locale.getDefault())
                 + text.substring(1).toLowerCase(Locale.getDefault());
        else
            return text.toUpperCase(Locale.getDefault());
    }

    /**
     * Convert {@code str} as a boolean value
     * Convert "true" and "1" to true
     * Convert "false" and "0" to false
     *
     * Throw an AFException if {@code str} cannot be parse
     *
     * @param str The string to convert
     *
     * @return the boolean result value
     */
    public static boolean readBoolean(String str) throws AFException {
        if (android.text.TextUtils.isEmpty(str))
            return false;
        else if (str.equals("0") || str.equals("false"))
            return false;
        else if (str.equals("1") || str.equals("true"))
            return true;
        else
            throw new AFException("StringTools", "readBoolean", "Cannot parse str: " + str);
    }

}
