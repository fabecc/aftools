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

package com.android.aft.AFCuteXmlParser.LowParser.HomeMade;

import java.io.EOFException;

import com.android.aft.AFCoreTools.StringTools;
import com.android.aft.AFCuteXmlParser.AFCuteXmlParserContext;

/**
 * XmlTokenizer This is a lexer for XML format.
 */
public class AFXmlTokenizer {

    // List of preallocated token to optimize
    final static AFXmlToken mUnknownToken = new AFXmlToken(AFXmlToken.Type.UNKNOWN);
    final static AFXmlToken mEofToken = new AFXmlToken(AFXmlToken.Type.EOF);
    final static AFXmlToken mTagBeginToken = new AFXmlToken(AFXmlToken.Type.TAG_BEGIN);
    final static AFXmlToken mTagBeginTerminalToken = new AFXmlToken(AFXmlToken.Type.TAG_BEGIN_TERMINAL);
    final static AFXmlToken mTagEndToken = new AFXmlToken(AFXmlToken.Type.TAG_END);
    final static AFXmlToken mTagEndTerminalToken = new AFXmlToken(AFXmlToken.Type.TAG_END_TERMINAL);
    final static AFXmlToken mEgualToken = new AFXmlToken(AFXmlToken.Type.EQUAL);

    final static String mDelemitorEndOfExtra = "?>";
    final static String mDelemitorStartOfComment = "!--";
    final static String mDelemitorEndOfComment = "-->";
    final static String mDelemitorStartOfCDATA = "![CDATA[";
    final static String mDelemitorEndOfCDATA = "]]>";

    /**
     * Return the next token of xml stream
     *
     * @param ctx
     * @return
     */
    public static AFXmlToken getNextToken(AFCuteXmlParserContext ctx) {

        // Look if there is a previous not used token
        AFXmlToken t = ctx.getCurrentToken();
        if (t != null) {
            ctx.setCurrentToken(null);
            return t;
        }

        AFBufferedInputStream stream = ctx.getStream();

        if (stream.isEmpty())
            return mEofToken;

        try {
            // Eat the head to token
            char c1;
            do {
                c1 = stream.eatHead();
            } while (isSpace(c1));
            char c2 = 0;
            try {
                c2 = stream.getHead();
            } catch (EOFException e) {
            }

            if (c1 == '=')
                return mEgualToken;
            else if (c1 == '>') {
                ctx.setIsParsingInTag(false);
                return mTagEndToken;
            } else if (c1 == '/'
                       && c2 == '>') {
                stream.eatHead();
                ctx.setIsParsingInTag(false);
                return mTagEndTerminalToken;
            } else if (c1 == '<') {
                if (c2 == 0) {
                    ctx.setIsParsingInTag(true);
                    return mTagBeginToken;
                } else if (c2 == '?') {
                    eatUntil(stream, mDelemitorEndOfExtra);
                    return getNextToken(ctx);
                } else if (c2 == '!') {
                    if (stream.startWith(mDelemitorStartOfComment)) {
                        eatUntil(stream, mDelemitorEndOfComment);
                        return getNextToken(ctx);
                    } else if (stream.startWith(mDelemitorStartOfCDATA)) {
                        stream.eat(8);
                        c2 = stream.getHead();

                        StringBuilder buf = new StringBuilder();
                        while (!stream.startWith(mDelemitorEndOfCDATA)) {
                            buf.append(c2);

                            stream.eatHead();
                            c2 = stream.getHead();
                        }
                        stream.eat(3);

                        return new AFXmlToken(AFXmlToken.Type.STRING, StringTools.getStringWithoutEncodingInterpretation(buf));
                    }
                } else if (c2 == '/') {
                    stream.eatHead();
                    ctx.setIsParsingInTag(true);
                    return mTagBeginTerminalToken;
                } else {
                    ctx.setIsParsingInTag(true);
                    return mTagBeginToken;
                }
            } else if (ctx.isParsingInTag()) {
                if (c1 == '\'' || c1 == '"') {
                    StringBuilder buf = new StringBuilder();
                    while (c2 != c1) {
                        // Manage escaped characters
                        if (c2 == '\\') {
                            stream.eatHead();
                            c2 = stream.getHead();

                            // Replace "\n" string by '\n' char
                            if (c2 == 'n')
                                c2 = '\n';
                            else if (c2 == 't')
                                c2 = '\t';
                        }

                        buf.append(c2);

                        stream.eatHead();
                        c2 = stream.getHead();
                    }
                    stream.eatHead();

                    return new AFXmlToken(AFXmlToken.Type.STRING, StringTools.getStringWithoutEncodingInterpretation(buf));
                } else if (isInId(c1)) {
                    StringBuilder buf = new StringBuilder();
                    buf.append(c1);
                    while (isInId(c2)) {
                        buf.append(c2);

                        stream.eatHead();
                        c2 = stream.getHead();
                    }

                    return new AFXmlToken(AFXmlToken.Type.STRING, StringTools.getStringWithoutEncodingInterpretation(buf));
                }
            } else {
                StringBuilder buf = new StringBuilder();
                buf.append(c1);
                while (c2 != '<') {
                    buf.append(c2);

                    stream.eatHead();
                    c2 = stream.getHead();
                }


                return new AFXmlToken(AFXmlToken.Type.STRING, StringTools.getStringWithoutEncodingInterpretation(buf));
            }

            return mUnknownToken;
        } catch (EOFException e) {
            return mEofToken;
        }
    }

    /**
     * Eat stream until char sequence not inside a string
     *
     * @param str String to reach
     */
    private static void eatUntil(AFBufferedInputStream stream, String str) {
        while (!stream.isEmpty()) {
            if (stream.startWith(str)) {
                stream.eat(str.length());
                break;
            }

            try {
                stream.eatHead();
            } catch (EOFException e) {
                return;
            }
        }
    }

    private static boolean isSpace(char c) {
        return c == ' ' || c == '\r' || c == '\n' || c == '\t';
    }

    private static boolean isInId(char c) {
        return StringTools.isAlNum(c) || c == ':' || c == '_' || c == '-';
    }

}
