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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtils {

    public enum MemoryUnit {
        BYTE(1), KILOBYTE(1024), MEGABYTE(1048576), GIGABYTE(1073741824);

        private final long value;

        MemoryUnit(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }

    // Size of the buffer
    private static final int BUFFER_SIZE = 1024 * 2;

    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0;
        int n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
//                DebugTools.d("Copy bytes " + count);
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        }
        finally {
            try {
                out.close();
            }
            catch (IOException e) {
                DebugTools.e(e.getMessage(), e);
            }
            try {
                in.close();
            }
            catch (IOException e) {
                DebugTools.e(e.getMessage(), e);
            }
        }
        return count;
    }

    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        while ((len = inputStream.read(buffer)) > -1) {
            outputStream.write(buffer, 0, len);
        }

        outputStream.flush();

        return outputStream.toByteArray();
    }

    /**
     * Delete a directory recursively
     *
     * @param path
     */
    public static void delete(String path) {
        File file = new File(path);

        if (file.exists()) {
            // If path is a directory, remove files that are inside recursively
            if (file.isDirectory())
                for (String subPath : file.list())
                    delete(new File(path, subPath).getAbsolutePath());

            // Delete file
            file.delete();
        }
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // Temporary buffer to read all the input stream data
        byte[] buffer = new byte[1024];
        int len = 0;

        // Move all data in the ByteArrayOutputStream
        while ((len = inputStream.read(buffer)) != -1)
            byteBuffer.write(buffer, 0, len);

        // Return all the bytes
        return byteBuffer.toByteArray();
    }

}
