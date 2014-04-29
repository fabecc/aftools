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

import java.util.Calendar;

public class TimeUtils {

    private static StringBuilder sStringBuilder;

    /**
     * Formats a duration (in seconds) in the format "hh:mm:ss"
     * 
     * @param duration The duration in seconds
     * @return The formatted duration
     */
    public static String formatDuration(final int duration) {
        if (duration < 0) {
            return "";
        } else {
            if (sStringBuilder == null) {
                sStringBuilder = new StringBuilder();
            } else {
                sStringBuilder.setLength(0);
            }

            final int hours = duration / 3600;
            final int minutes = (duration % 3600) / 60;
            final int seconds = (duration % 60);
            sStringBuilder.append(hours);
            sStringBuilder.append(":");
            sStringBuilder.append(minutes < 10 ? "0" : "");
            sStringBuilder.append(minutes);
            sStringBuilder.append(":");
            sStringBuilder.append(seconds < 10 ? "0" : "");
            sStringBuilder.append(seconds);
            return sStringBuilder.toString();
        }
    }

    public static final int[] calculateWatchTime(final Calendar cal, final long start, final long end) {
        final int[] startAndEndTime = new int[2];
        cal.setTimeInMillis(start);
        startAndEndTime[0] = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        final int dayStartNumber = cal.get(Calendar.DAY_OF_YEAR);
        final int nbDayOfStartYear = cal.get(Calendar.YEAR) % 4 == 0 ? 366 : 365;
        cal.setTimeInMillis(end);
        final int dayEndNumber = cal.get(Calendar.DAY_OF_YEAR);
        startAndEndTime[1] = (cal.get(Calendar.HOUR_OF_DAY) + (nbDayOfStartYear + dayEndNumber - dayStartNumber) * 24)
                * 60 + cal.get(Calendar.MINUTE);
        return startAndEndTime;
    }
}
