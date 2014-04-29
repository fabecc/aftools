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

package com.android.aft.AFApplicationHelper.manager;

import android.app.Application;
import android.content.Context;

public abstract class AFApplicationManager extends Application {

    protected static Context mContext = null;

    public AFApplicationInfo mApplicationInfo;
    public AFDeviceInfo mDeviceInfo;

    public AFApplicationManager() {
        mContext = null;
    }

    public static void init(Context ctx) {
       mContext = ctx;

       AFApplicationManager app = (AFApplicationManager)mContext.getApplicationContext();

       app.mApplicationInfo = new AFApplicationInfo(ctx);
       app.mDeviceInfo = new AFDeviceInfo(ctx);

       app.initData(ctx);
    }

    public abstract void initData(Context ctx);

    public static void reset(Context ctx) {
        AFApplicationManager app = (AFApplicationManager)ctx.getApplicationContext();

        // Reset inherit class data
        app.resetData(ctx);

        // Reset local data
        app.mApplicationInfo = null;
        app.mDeviceInfo = null;
    }

    public abstract void resetData(Context ctx);

    /**
     * Call when the application pass in background
     */
    public abstract void onBackground(Context context);

    /**
     * Call when the application pass in foreground
     */
    public abstract void onForeground(Context context);

    public Context getContext() {
        return mContext;
    }

}
