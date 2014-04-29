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

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class contains utility methods for handling date
 */
public class DateTools {

    private static StringBuffer sBuffer1 = new StringBuffer();
    private static FieldPosition sField = new FieldPosition(0);
    private static Date sOtherDate = new Date();
    public static final String EUROPE_TIMEZONE = "Europe/Paris";

    public static String convertTimestampToDateString(final String pattern, final long timestamp) {
        synchronized (sBuffer1) {
            sBuffer1.setLength(0);
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.FRANCE);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(EUROPE_TIMEZONE));
            sOtherDate.setTime(timestamp);
            simpleDateFormat.format(sOtherDate, sBuffer1, sField);
        }
        return sBuffer1.toString();
    }

    /**
     * Convert a string to a date associated to its pattern (cf. doc of
     * SimpleDateFormat).
     *
     * @param pattern
     * @param date
     * @return timestamp
     */
    public static final long convertToTimestamp(final String pattern, final String date) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.FRANCE);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(EUROPE_TIMEZONE));
        final ParsePosition pos = new ParsePosition(0);
        final Date convertedDate = simpleDateFormat.parse(date, pos);
        return convertedDate.getTime();
    }

    /**
     * Check if the given date is valid, including leap year and 30/31 days in a
     * month
     *
     * @param day From 1 to 31
     * @param month From 1 to 12
     * @param year Positive value
     * @return true if the date is valid, false otherwise
     */
    public static final Boolean isValidDate(int day, int month, int year) {
        Boolean isValid = true;
        int[] monthLength = {
                0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        };

        if (isLeapYear(year)) {
            monthLength[2] = 29; // 29 days in February in a leap year
        }
        if (month < 1 || month > 12) {
            isValid = false;
        } else if (day < 1 || day > monthLength[month]) {
            isValid = false;
        }
        return isValid;
    }

    /**
     * Check if the given year is a leap year
     *
     * @param year Positive value
     * @return true if the given year is a leap year, false otherwise
     */
    public static final Boolean isLeapYear(int year) {
        Boolean result = true;

        if ((year % 4) != 0) {
            result = false;
        } else if ((year % 400) == 0) {
            result = true;
        } else if ((year % 100) == 0) { // divisible by 100 (but not by 400,
                                        // since that case considered already)
            result = false;
        }

        return result;
    }

}
