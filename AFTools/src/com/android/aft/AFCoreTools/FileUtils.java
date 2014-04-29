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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.android.aft.AFCoreTools.IoUtils.MemoryUnit;

/**
 * Helper class for file manipulation.
 */
public class FileUtils {

    /**
     * Write raw data to a file and save it on the external storage.
     *
     * @param rawData
     *            Data to save.
     * @param directoryName
     *            Name of the directory where the file will be written. This
     *            parameter is OPTIONAL, don't provide one if you want to save
     *            file on the root directory. Provided directory will be created
     *            if necessary.
     * @param fileName
     *            Name of the file.
     * @param ctx
     *            Context used to open the saved file (typically, an activity).
     *            This parameter is OPTIONAL, don't provide one if you don't
     *            want to open it.
     * @return The saved file.
     */
    public static File saveDataToExternalStorage(InputStream rawData,
                                                 String directoryName,
                                                 String fileName,
                                                 Context ctx) {

        File file = null;

        if (fileName != null && !fileName.equals("")) {
            File externalStorage = Environment.getExternalStorageDirectory();
            File dir = new File(externalStorage.getAbsolutePath() + "/" + directoryName);

            if (dir.exists() || dir.mkdirs()) {
                file = new File(dir, fileName);

                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    IoUtils.copy(rawData, fileOutputStream);

                    if (ctx != null) {
                        openFile(file, ctx);
                    }
                } catch (FileNotFoundException exception) {
                    DebugTools.e("Impossible to save data : couldn't open file output stream");
                } catch (IOException exception) {
                    DebugTools.e("Impossible to save data : couldn't write data to file");
                }
            }
        }

        return file;
    }

    public static boolean copyInputStreamInFile(InputStream input, File output_file) {
        OutputStream output = null;
        try {
            output = new FileOutputStream(output_file);
        } catch (FileNotFoundException e) {
            DebugTools.e("Cannot open output file " + output_file.getAbsolutePath(), e);
            return false;
        }

        try {
            IoUtils.copy(input, output);
        } catch (IOException e) {
            DebugTools.e("Failed to opy data", e);
            return false;
        }

        return true;
    }


    /**
     * Open a file in an appropriate activity, based on its extension.
     *
     * @param path
     *            The path of the file to open.
     * @param context
     *            Context used to open the file (typically, an activity).
     * @return true if the file has been opened, false otherwise.
     */
    public static boolean openFile(String path, Context context) {
        return openFile(new File(path), context);
    }

    /**
     * Open a file in an appropriate activity, based on its extension.
     *
     * @param file
     *            The file to open.
     * @param context
     *            Context used to open the file (typically, an activity).
     * @return true if the file has been opened, false otherwise.
     */
    public static boolean openFile(File file, Context context) {

        boolean openingSucceed = false;

        if (file != null && file.exists() && !file.isDirectory()
                && context != null) {

            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mimeType = mimeTypeMap
                    .getMimeTypeFromExtension(getFileExtension(file.toString())
                            .substring(1));

            Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);
            newIntent.setDataAndType(Uri.fromFile(file), mimeType);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                context.startActivity(newIntent);

                openingSucceed = true;
            } catch (android.content.ActivityNotFoundException e) {
                DebugTools.e("Impossible to open file : couldn't find an appropriate task to handle it");
            }
        }

        return openingSucceed;
    }

    /**
     * Delete a file.
     *
     * @param path
     *          The path to the file to delete.
     */
    public static boolean deleteFile(String path) {
        File file = new File(path);

        boolean deletionSucceed = true;

        if (file.exists()) {
            if (file.isDirectory()) {
                for (String subPath: file.list()) {
                    deletionSucceed &= deleteFile(new File(path, subPath).getAbsolutePath());
                }
            }

            deletionSucceed &= file.delete();
        }
        else {
            deletionSucceed = false;
        }

        return deletionSucceed;
    }

    /**
     * Load file content to String
     */
    public static String loadFileContent(String path) throws java.io.IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        StringBuffer sb = new StringBuffer(1000);

        char[] buf = new char[1024];
        for (int n = reader.read(buf); n != -1; n = reader.read(buf)) {
            String readData = String.valueOf(buf, 0, n);
            sb.append(readData);
        }
        reader.close();

        return sb.toString();
    }

    /**
     * Load asset content to String
     * @param path The path to the ressource in asset dire
     */
    public static String loadAssetContent(Context ctx, String path) throws java.io.IOException {
        InputStream is = ctx.getAssets().open(path);
        InputStreamReader reader = new InputStreamReader(is);

        StringBuffer sb = new StringBuffer(1000);

        char[] buf = new char[1024];

        while (true) {
            int n = reader.read(buf);
            if (n == -1)
                break ;
            String readData = String.valueOf(buf, 0, n);
            sb.append(readData);
        }
        reader.close();

        return sb.toString();
    }

    /**
     * Empty a directory.
     *
     * @param directory
     *            The directory to empty.
     * @param deleteRoot
     *            true to also delete the provided directory.
     * @return true if the directory has been emptied, false otherwise.
     */
    public static boolean emptyDirectory(File directory, boolean deleteRoot) {

        boolean emptySucceed = true;

        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (String subFile : directory.list()) {
                emptySucceed &= emptyDirectory(new File(directory, subFile), true);
            }

            if (deleteRoot) {
                emptySucceed &= directory.delete();
            }
        }
        else {
            emptySucceed = false;
        }

        return emptySucceed;
    }

    /**
     * Extract the extension part from a file name.
     *
     * @param fileName
     *            The file to extract the extension from.
     * @return
     */
    public static String getFileExtension(String fileName) {
        if (fileName.indexOf("?") > -1) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        if (fileName.lastIndexOf(".") == -1) {
            return null;
        }
        else {
            String extension = fileName.substring(fileName.lastIndexOf("."));

            if (extension.indexOf("%") > -1) {
                extension = extension.substring(0, extension.indexOf("%"));
            }
            if (extension.indexOf("/") > -1) {
                extension = extension.substring(0, extension.indexOf("/"));
            }

            return extension.toLowerCase();
        }
    }

    /**
     * Compute the size of a directory.
     *
     * @param directory
     *          The directory to compute the size of.
     * @return
     */
    public static long getDirectorySize(File directory) {
        return getDirectorySize(directory, MemoryUnit.BYTE);
    }

    /**
     * Compute the size of a directory in a specified unit.
     *
     * @param directory
     *          The directory to compute the size of.
     * @param memoryUnit
     *          The memory unit used to compute the size.
     * @return
     */
    public static long getDirectorySize(File directory, MemoryUnit memoryUnit) {
        if (directory.exists()) {
            long directorySize = 0;
            for (File file : directory.listFiles()) {
                if(file.isDirectory()) {
                    directorySize += getDirectorySize(file, memoryUnit);
                }
                else {
                    directorySize += file.length() / memoryUnit.getValue();
                }
            }

            return directorySize;
        }

        return 0;
    }

}
