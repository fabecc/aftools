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
import java.io.IOException;
import java.io.InputStreamReader;

import com.android.aft.AFCuteXmlParser.AFCuteXmlParser;

/**
 * Read data from InputStream but buffered data in a StringBuilder
 */
public class AFBufferedInputStream {

    private static int CIRCULAR_BUFFER_SIZE = 1024;

    private InputStreamReader mIn;

    private char[] mCircularBuffer;
    private int mCircularBufferIdxBegin;
    private int mCircularBufferIdxEnd;

    public AFBufferedInputStream(InputStreamReader in) {
        mIn = in;

        mCircularBuffer = new char[CIRCULAR_BUFFER_SIZE];
        mCircularBufferIdxBegin = 0;
        mCircularBufferIdxEnd = 0;
    }

    public InputStreamReader getIn() {
        return mIn;
    }

    /**
     * Return true if there is no more data to read
     *
     * @return True if input stream is empty
     */
    public boolean isEmpty() {
        return mIn == null
               && mCircularBufferIdxBegin == mCircularBufferIdxEnd;
    }

    private int getCircularBufferContentSize() {
        if (mCircularBufferIdxBegin == mCircularBufferIdxEnd)
            return 0;

        if (mCircularBufferIdxBegin < mCircularBufferIdxEnd)
            return mCircularBufferIdxEnd - mCircularBufferIdxBegin;

        return CIRCULAR_BUFFER_SIZE - mCircularBufferIdxBegin + mCircularBufferIdxEnd;
    }

    private char getFirstCharOfCircularBuffer() {
        return mCircularBuffer[mCircularBufferIdxBegin];
    }

    private void increasePositionOfCircularBuffer(int length) {
        if (getCircularBufferContentSize() < length)
            fillCircularBuffer();

        if (getCircularBufferContentSize() < length) {
            mCircularBufferIdxBegin = mCircularBufferIdxEnd == 0 ?
                                      CIRCULAR_BUFFER_SIZE - 1 :
                                      mCircularBufferIdxEnd - 1;
            return ;
        }

        mCircularBufferIdxBegin = (mCircularBufferIdxBegin + length) % CIRCULAR_BUFFER_SIZE;
    }

    private boolean isCircularBufferStartWith(String str) {
        byte[] str_b = str.getBytes();

        int buf_idx = mCircularBufferIdxBegin;
        for (int str_idx = 0; str_idx < str_b.length; ++str_idx, ++buf_idx) {
            // Reset buffer index if at end of buffer
            if (buf_idx == CIRCULAR_BUFFER_SIZE)
                buf_idx = 0;

            if (mCircularBuffer[buf_idx] != str_b[str_idx])
                return false;
        }

        return true;
    }

    /**
     * Fill the circular buffer with data from input stream
     *
     * @return True if success
     */
    private boolean fillCircularBuffer() {
        if (mIn == null)
            return false;

        try {
            if (mCircularBufferIdxEnd >= mCircularBufferIdxBegin) {
                int length = CIRCULAR_BUFFER_SIZE - mCircularBufferIdxEnd;
                if (mCircularBufferIdxBegin == 0)
                    --length;

                int s = mIn.read(mCircularBuffer, mCircularBufferIdxEnd, length);
                if (s == -1) {
                    mIn.close();
                    mIn = null;
                    return false;
                }

                mCircularBufferIdxEnd = (mCircularBufferIdxEnd + s) % CIRCULAR_BUFFER_SIZE;
                if (mCircularBufferIdxEnd != 0)
                    return true;
            }

            if (mCircularBufferIdxBegin - mCircularBufferIdxEnd - 1 != 0) {
                int s = mIn.read(mCircularBuffer, mCircularBufferIdxEnd, mCircularBufferIdxBegin - mCircularBufferIdxEnd - 1);
                if (s == -1) {
                    mIn.close();
                    mIn = null;
                }

                mCircularBufferIdxEnd = (mCircularBufferIdxEnd + s) % CIRCULAR_BUFFER_SIZE;
            }

            if (mCircularBufferIdxBegin == mCircularBufferIdxEnd)
                return false;
        }
        catch (IOException e) {
            AFCuteXmlParser.dbg.e("Cannot read stream", e);
            return false;
        }

        return true;
    }

    /**
     * Return the first char
     *
     * @return The first character
     *
     * @throws EOFException
     */
    public char getHead() throws EOFException {
        if (getCircularBufferContentSize() == 0)
            fillCircularBuffer();

        if (getCircularBufferContentSize() == 0)
            throw new EOFException();

        return getFirstCharOfCircularBuffer();
    }

    /**
     * Eat the head character
     *
     * @return Return the head character
     *
     * @throws EOFException
     */
    public char eatHead() throws EOFException {
        char c = getHead();

        increasePositionOfCircularBuffer(1);

        return c;
    }

    /**
     * Eat a number a character
     *
     * @param length the eat
     */
    public void eat(int length) {
        if (getCircularBufferContentSize() < length)
            fillCircularBuffer();

        increasePositionOfCircularBuffer(length);
    }

    public boolean startWith(String str) {
        if (getCircularBufferContentSize() < str.length())
            fillCircularBuffer();

        if (getCircularBufferContentSize() < str.length())
            return false;

        return isCircularBufferStartWith(str);
    }

}
