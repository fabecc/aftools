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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;


public class ServiceTools {

    /**
     * Return true if this service is running
     *
     * @param ctx Application context
     * @return True if the service is running
     */
    public static boolean isServiceRunning(Context ctx, Class<? extends Service> service) {
        ActivityManager manager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo s: manager.getRunningServices(Integer.MAX_VALUE))
            if (s.service.getClassName().equals(service.getName()))
                return true;

        return false;
    }

    public static void killAllRunningService(Context ctx) {
        ActivityManager manager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo s: manager.getRunningServices(Integer.MAX_VALUE)) {
            try {
                Class<?> c = Class.forName(s.service.getClassName());
                Class<? extends Service> cc = c.asSubclass(Service.class);
                DebugTools.d("Try to kill service: " + cc.getSimpleName());
                ctx.stopService(new Intent(ctx, cc));
            } catch (ClassNotFoundException e) {
                DebugTools.e("Failed to read read service class: " + s.service.getClassName());
            }
        }
    }

}
