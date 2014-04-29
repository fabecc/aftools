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
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

public class DebugTools {

    public static final int LOG_LEVEL_DEBUG = 4;
    public static final int LOG_LEVEL_INFO = 3;
    public static final int LOG_LEVEL_WARNING = 2;
    public static final int LOG_LEVEL_ERROR = 1;
    public static final int LOG_LEVEL_NONE = 0;

    // Debug tag
    private static String mTag = "NFDebugTools";

    // Current log level
    private static int mLogLevel = LOG_LEVEL_DEBUG;

    /**
     * Init DebugTools tool
     * @param tag Tag used in Log.x methods
     */
    public static void init(String tag) {
        mTag = tag;
        Log.d(mTag, "DebugTools is init with tag: " + tag);
    }

    /**
     * Set log level.
     * @param level Then new log level. Value can be LOG_LEVEL_DEBUG or LOG_LEVEL_INFO or LOG_LEVEL_WARNING or LOG_LEVEL_ERROR or LOG_LEVEL_NONE
     */
    public static void setLogLevel(int level) {
        mLogLevel = level;
    }

    /**
     * Get the log level
     * @return LOG_LEVEL_DEBUG or LOG_LEVEL_INFO or LOG_LEVEL_WARNING or LOG_LEVEL_ERROR or LOG_LEVEL_NONE
     */
    public static int getLogLevel() {
        return mLogLevel;
    }

    /**
     * Get the tag
     * @see init()
     * @return The current tag
     */
    public static String getTag() {
        return mTag;
    }

    /**
     * Log a debug message, depending on log level
     * @param msg The message to print out
     */
    public static void d(String msg) {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        Log.d(mTag, msg);
    }

    /**
     * Log a debug message and show a Toast, depending on log level
     * @param msg The message to print out in log and in Toast
     */
    public static void dt(Context ctx, String msg) {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
        Log.d(mTag, msg);
    }

    /**
     * Log a verbose message, depending on log level
     * @param msg The message to print out
     */
    public static void v(String msg) {
        if (mLogLevel < LOG_LEVEL_INFO)
            return ;

        Log.v(mTag, msg);
    }

    /**
     * Log a info message, depending on log level
     * @param msg The message to print out
     */
    public static void i(String msg) {
        if (mLogLevel < LOG_LEVEL_INFO)
            return ;

        Log.i(mTag, msg);
    }

    /**
     * Log a error message, depending on log level
     * @param msg The message to print out
     */
    public static void e(String msg) {
        if (mLogLevel < LOG_LEVEL_ERROR)
            return ;

        Log.e(mTag, msg);
    }

    /**
     * Log a error message with a throwable, depending on log level
     * @param msg The message to print out
     * @param t The throwable to print out
     */
    public static void e(String msg, Throwable t) {
        if (mLogLevel < LOG_LEVEL_ERROR)
            return ;

        Log.e(mTag, msg, t);
    }

    /**
     * Log a debug message and show a Toast, depending on log level
     * @param msg The message to print out in log and in Toast
     */
    public static void et(Context ctx, String msg) {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
        Log.e(mTag, msg);
    }

    /**
     * Log a debug message and show a Toast, depending on log level
     * @param msg The message to print out in log and in Toast
     */
    public static void et(Context ctx, String msg, Throwable t) {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
        Log.e(mTag, msg, t);
    }

    /**
     * Log a warning message, depending on log level
     * @param msg The message to print out
     */
    public static void w(String msg) {
        if (mLogLevel < LOG_LEVEL_WARNING)
            return ;

        Log.w(mTag, msg);
    }

    /**
     * Log a warning message with a throwable, depending on log level
     * @param msg The message to print out
     * @param t The throwable to print out
     */
    public static void w(String msg, Throwable t) {
        if (mLogLevel < LOG_LEVEL_WARNING)
            return ;

        Log.w(mTag, msg, t);
    }

    public static void npe(String msg) {
        e(msg);
        throw new NullPointerException(msg);
    }

    // Special d with TextView for dump object in View
    private static void d(String txt, TextView output) {
        if (output == null)
            d(txt);
        else
            output.append(txt + "\n");
    }

    /**
     * Dump recursively all object attribute
     *
     * @param obj       Object to dump
     */
    public static void dump(Object obj) {
        dump(obj, null);
    }

    /**
     * Dump recursively all object attribute
     *
     * @param obj       Object to dump
     * @param output    Output (if null, use Log)
     */
    public static void dump(Object obj, TextView output) {
        if (obj == null) {
            d("Dump object is 'null'", output);
            return;
        }

        try {
            d("Dump object " + obj.getClass().getSimpleName(), output);
            dump(new HashSet<Object>(), obj, "", output);
        } catch (Exception e) {
            d("Failed to dump object", output);
            e("Failed to dump object", e);
        }
    }

    private static void dump(Set<Object> objs, Object obj, String space, TextView output) throws IllegalArgumentException, IllegalAccessException {
        // Test if obj is valid
        if (obj == null) {
            d(space + "_ ?: null", output);
            return ;
        }

        // Get object class
        Class<?> c = obj.getClass();

        // Test if obj was not already dump
        if (objs.contains(obj)) {
            d(space + "_ " + c.getSimpleName() + ": /!\\ Cycle detected (this object was already displayed)", output);
            return ;
        }

        objs.add(obj);

        // Read attributes
        for (Field f: getAllDeclaredFields(c))
        {
            // Do not show final static field
            if (Modifier.isStatic(f.getModifiers())
                && Modifier.isFinal(f.getModifiers()))
                continue ;

            // Set visibility
            f.setAccessible(true);

            // Ignore special generate field for enum
            if (f.getName().startsWith("$"))
                continue ;

            Class<?> type = f.getType();

            // First display primitive value
            if (type.isPrimitive()) {
                if (type == Integer.TYPE)
                    d(space + "  _ " + f.getName() + ": " + f.getInt(obj), output);
                else if (type == Long.TYPE)
                    d(space + "  _ " + f.getName() + ": " + f.getLong(obj), output);
                else if (type == Float.TYPE)
                    d(space + "  _ " + f.getName() + ": " + f.getFloat(obj), output);
                else if (type == Double.TYPE)
                    d(space + "  _ " + f.getName() + ": " + f.getDouble(obj), output);
                else if (type == Boolean.TYPE)
                    d(space + "  _ " + f.getName() + ": " + f.getBoolean(obj), output);
                else if (type == Character.TYPE)
                    d(space + "  _ " + f.getName() + ": '" + f.getChar(obj) + "'", output);
                else if (type == Byte.TYPE)
                    d(space + "  _ " + f.getName() + ": " + f.getByte(obj), output);
                else
                    d(space + "  _ " + f.getName() + "  /!\\ Unprintable", output);
            }
            else {
                Object attr_value = f.get(obj);
                // Null value
                if (attr_value == null)
                    d(space + "  _ " + f.getName() + ": null", output);
                // Case of auto display class
                else if (type == String.class
                    || type == Integer.class
                    || type == Double.class
                    || type == Float.class
                    || type == Long.class
                    || type == Date.class)
                    d(space + "  _ " + f.getName() + ": " + attr_value, output);
                // Collection
                else if (attr_value instanceof Collection) {
                    Collection<?> values = (Collection<?>)attr_value;
                    if (values.size() == 0)
                        d(space + "  _ " + f.getName() + ": <empty>", output);
                    else {
                        int i = 0;
                        for (Iterator<?> iterator = values.iterator(); iterator.hasNext(); ++i) {
                            Object value = iterator.next();
                            String dump = getSimpleObjectDump(value);
                            if (dump == null) {
                                d(space + "  _ " + f.getName() + "[" + i + "]: " + value.getClass().getSimpleName(), output);
                                dump(objs, value, space + "    ", output);
                            }
                            else {
                                d(space + "  _ " + f.getName() + "[" + i + "]: " + dump, output);
                            }
                        }
                    }
                }
                // Map
                else if (attr_value instanceof Map) {
                    Map<?, ?> values = (Map<?, ?>)attr_value;
                    if (values.size() == 0)
                        d(space + "  _ " + f.getName() + ": <empty>", output);
                    else {
                        int i = 0;
                        for (Object key: values.keySet()) {
                            ++i;
                            Object value = values.get(key);

                            String dump_key = getSimpleObjectDump(key);
                            String dump_value = getSimpleObjectDump(value);

                            if (dump_key == null || dump_value == null) {
                                d(space + "  _ " + f.getName() + "[" + i + "]:", output);
                                // print Key
                                if (dump_key == null) {
                                    d(space + "    _ key: " + key.getClass().getSimpleName(), output);
                                    dump(objs, key, space + "    ", output);
                                }
                                else
                                    d(space + "    _ key: " + dump_key, output);

                                // print value
                                if (dump_value == null) {
                                    d(space + "    _ value: " + value.getClass().getSimpleName(), output);
                                    dump(objs, value, space + "    ", output);
                                }
                                else
                                    d(space + "    _ value: " + dump_value, output);
                            }
                            else {
                                d(space + "  _ " + f.getName() + "[" + dump_key + "]: " + dump_value, output);
                            }
                        }
                    }
                }
                // Array
                else if (attr_value instanceof Object[]) {
                    Object[] values = (Object[])attr_value;
                    if (values.length == 0)
                        d(space + "  _ " + f.getName() + ": <empty>", output);
                    else {
                        for (int i = 0; i < values.length; ++i) {
                            Object value = values[i];
                            String dump = getSimpleObjectDump(value);
                            if (dump == null) {
                                d(space + "  _ " + f.getName() + "[" + i + "]: " + value.getClass().getSimpleName(), output);
                                dump(objs, value, space + "    ", output);
                            }
                            else {
                                d(space + "  _ " + f.getName() + "[" + i + "]: " + dump, output);
                            }
                        }
                    }
                }
                // SparseArray
                else if (attr_value instanceof SparseArray) {
                    SparseArray<?> values = (SparseArray<?>)attr_value;
                    if (values.size() == 0)
                        d(space + "  _ " + f.getName() + ": <empty>", output);
                    else
                        for (int i = 0; i < values.size(); ++i) {
                            int key = values.keyAt(i);
                            Object value = values.get(key);
                            String dump = getSimpleObjectDump(value);
                            if (dump == null) {
                                d(space + "  _ " + f.getName() + "[" + key + "]: " + value.getClass().getSimpleName(), output);
                                dump(objs, value, space + "    ", output);
                            }
                            else {
                                d(space + "  _ " + f.getName() + "[" + key + "]: " + dump, output);
                            }
                        }
                }
                // Enum value
                else if (type.isEnum())
                    d(space + "  _ " + f.getName() + ": " + f.get(obj), output);
                // Other object
                else {
                    String dump = getSimpleObjectDump(attr_value);
                    if (dump == null) {
                        d(space + "  _ " + f.getName() + ":", output);
                        dump(objs, attr_value, space + "  ", output);
                    }
                    else {
                        d(space + "  _ " + f.getName() + ": " + dump, output);
                    }
                }
            }
        }
    }

    private static String getSimpleObjectDump(Object obj) {
        if (obj == null)
            return "null";

        Class<?> type = obj.getClass();

        if (type == String.class
            || type == Integer.class
            || type == Double.class
            || type == Float.class
            || type == Long.class
            || type == Date.class) {
            return obj.toString();
        }

        return null;
    }

    public static ArrayList<Field> getAllDeclaredFields(Class<?> type) {
        ArrayList<Field> fields = new ArrayList<Field>();
        for (Field field: type.getDeclaredFields())
            fields.add(field);

        if (type.getSuperclass() != null)
            fields.addAll(getAllDeclaredFields(type.getSuperclass()));

        return fields;
    }

    // Print buffer to debug
    public static void hexdump(String msg, String buffer) {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        hexdump(mTag, msg, buffer.getBytes(), buffer.length());
    }

    public static void hexdump(String msg, byte[] buf) {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        hexdump(mTag, msg, buf, buf.length);
    }

    public static void hexdump(String msg, byte[] buf, int len) {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        hexdump(mTag, msg, buf, len);
    }

    private static void hexdump(String tag, String msg, byte[] buf, int len)
    {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        Log.d(tag, ">>>>>>>>>>>>>>>>  Dump buffer: " + msg);
        for (int offset = 0; offset < len; offset += 16)
        {
            StringBuffer str = new StringBuffer();

            int i;
            // Print hexchar
            for (i = 0; i < 16 && offset + i < len; ++i)
            {
                if (i == 8)
                    str.append(" ");
                str.append(StringTools.toHex((char)buf[offset + i]));
                str.append(" ");
            }
            for (; i < 16; ++i)
            {
                str.append("   ");
                if (i == 8)
                    str.append(" ");
            }
            str.append(" |");

            // Print ascii value
            for (i = 0; i < 16 && offset + i < len; ++i)
            {
                if (StringTools.isPrintable((char)buf[offset + i]))
                    str.append((char)buf[offset + i]);
                else
                    str.append(".");
            }
            for (; i < 16; ++i)
                str.append(" ");
            str.append("|");

            Log.d(tag, str.toString());
        }

        Log.d(tag, "<<<<<<<<<<<<<<<<  Dump buffer: " + msg);
    }

    public static void hexdump(String msg, int[] buf, int len)
    {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        byte[] buf_byte = new byte[len * 4];
        for (int i = 0; i < len; ++i)
            for (int j = 0; j < 4; ++j)
                buf_byte[i * 4 + j] = (byte)(buf[i] >> 4 * (4 - j - 1) & 0xff);

        hexdump(mTag, msg, buf_byte, len * 4);
    }


    // Print buffer to debug
    public static void stringdump(String msg, String buffer) {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        if (buffer == null)
            buffer = "";

        stringdump(mTag, msg, buffer);
    }

    public static void stringdump(String msg, byte[] buffer) {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        if (buffer == null)
            buffer = new byte[0];

        stringdump(mTag, msg, buffer, buffer.length);
    }

    private static void stringdump(String tag, String msg, String buffer) {
        if (buffer == null)
            buffer = "";

        stringdump(tag, msg, buffer.getBytes(), buffer.length());
    }

    private static void stringdump(String tag, String msg, byte[] buffer, int length)
    {
        if (mLogLevel < LOG_LEVEL_DEBUG)
            return ;

        Log.d(tag, ">>>>>>>>>>>>>>>>  Dump buffer: " + msg);

        byte[] buf = buffer;
        final int len = length;
        for (int offset = 0; offset < len; offset += 100)
        {
            StringBuffer str = new StringBuffer();

            int i;
            str.append(" |");

            // Print ascii value
            for (i = 0; i < 100 && offset + i < len; ++i)
            {
                if (StringTools.isPrintable((char)buf[offset + i]))
                    str.append((char)buf[offset + i]);
                else
                    str.append(".");
            }
            for (; i < 100; ++i)
                str.append(" ");
            str.append("|");

            Log.d(tag, str.toString());
        }

        Log.d(tag, "<<<<<<<<<<<<<<<<  Dump buffer: " + msg);
    }

    /**
     * Method to log the execution time of a task
     *
     * @param task
     *          Task name
     * @param action
     *          Action block to execute
     */
    public static void timelog(String task, Runnable action) {
        TimeLogger data = timelog(task);

        // Run code
        action.run();

        // Ending date
        data.finish();
    }

    /**
     * Construct a timelog action.
     * Use this if you can't use the function with action parameter
     *
     * @param task
     *          Task name
     *
     * @return TimelogData to pass to timelogCompute method
     */
    public static TimeLogger timelog(String task) {
        return new TimeLogger(task);
    }

    /**
     * Timelog object stored data
     */
    public static class TimeLogger {
        private String task;
        private long start;
        private int step = 0;

        /**
         * Construct a new TimelogLogger
         *
         * @param task Name of the task
         */
        public TimeLogger(String task) {
            // Starting date
            this.task = task;
            start =  new Date().getTime();

            d("----> Start task '" + task + "' ...");
        }

        /**
         * Compute timelog action to display time at a step of the process
         */
        public void step(String desc) {
            ++step;

            // Ending date
            long end = new Date().getTime();
            long diff = end - start;
            if (android.text.TextUtils.isEmpty(desc))
                d("----- Step " + step + " of task '" + task + "' reach in " + diff + " ms.");
            else
                d("----- Step " + step + " '" + desc + "' of task '" + task + "' reach in " + diff + " ms.");
        }

        public void step() {
            step("");
        }

        /**
         * Compute timelog action to display time
         */
        public void finish() {
            finish(null);
        }

        public void finish(String message) {
            // Ending date
            long end = new Date().getTime();
            long diff = end - start;

            d("<---- End of task '" + task + "' in " + diff + " ms"
              + (TextUtils.isEmpty(message) ? "" : ": " + message));
        }
    }

    // Logger class to add a category tag in prefix of each log
    @SuppressWarnings("serial")
    public static class Logger implements Serializable {

        // Black list of category to disable some tag
        private static HashSet<String> mCategoryBlackList = new HashSet<String>();

        private final String mCategory;

        // Disable display log of a category
        public static void disable(String category) {
            mCategoryBlackList.add(category);
        }

        // Enable display log of a category
        public static void enable(String category) {
            mCategoryBlackList.remove(category);
        }
        public static void enable(Class<?> category) {
            mCategoryBlackList.remove(category.getName());
        }

        public Logger(String category) {
            mCategory = category;
        }

        public void disable() {
            mCategoryBlackList.add(mCategory);
        }

        public void d(String msg) {
            if (mLogLevel < LOG_LEVEL_DEBUG)
                return ;

            if (mCategoryBlackList.contains(mCategory))
                return ;
            Log.d(mCategory, msg);
        }

        public void v(String msg) {
            if (mLogLevel < LOG_LEVEL_INFO)
                return ;

            if (mCategoryBlackList.contains(mCategory))
                return ;
            Log.v(mCategory, msg);
        }

        public void i(String msg) {
            if (mLogLevel < LOG_LEVEL_INFO)
                return ;

            if (mCategoryBlackList.contains(mCategory))
                return ;
            Log.i(mCategory, msg);
        }

        public void e(String msg) {
            if (mLogLevel < LOG_LEVEL_ERROR)
                return ;

            if (mCategoryBlackList.contains(mCategory))
                return ;
            Log.e(mCategory, msg);
        }

        public void e(String msg, Throwable t) {
            if (mLogLevel < LOG_LEVEL_ERROR)
                return ;

            if (mCategoryBlackList.contains(mCategory))
                return ;
            Log.e(mCategory, msg, t);
        }

        public void w(String msg) {
            if (mLogLevel < LOG_LEVEL_WARNING)
                return ;

            if (mCategoryBlackList.contains(mCategory))
                return ;
            Log.w(mCategory, msg);
        }

        public void w(String msg, Throwable t) {
            if (mLogLevel < LOG_LEVEL_WARNING)
                return ;

            if (mCategoryBlackList.contains(mCategory))
                return ;
            Log.w(mCategory, msg, t);
        }

        public void hexdump(String msg, String buffer) {
            if (mLogLevel < LOG_LEVEL_DEBUG)
                return ;

            DebugTools.hexdump(mCategory, msg, buffer.getBytes(), buffer.length());
        }

        public void hexdump(String msg, byte[] buf) {
            if (mLogLevel < LOG_LEVEL_DEBUG)
                return ;

            DebugTools.hexdump(mCategory, msg, buf, buf.length);
        }

        public void hexdump(String msg, byte[] buf, int len) {
            if (mLogLevel < LOG_LEVEL_DEBUG)
                return ;

            DebugTools.hexdump(mCategory, msg, buf, len);
        }

        public void stringdump(String msg, String buffer) {
            DebugTools.stringdump(mCategory, msg, buffer);
        }

    }

}
