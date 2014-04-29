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

package com.android.aft.AFNotificator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;


public class AFNotificatorReceiver extends AFNotificatorCore {

    // Target object
    private Object mTarget;

    // Connection event to method list
    private HashMap<String, Method> mMethods = new HashMap<String, Method>();

    // Local broadcast manager
    LocalBroadcastManager mLbm;

    // Event receiver from the LocalBroadcastReceiver
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String e = intent.getStringExtra(AFNotificatorEmitter.EXTRA_PARAM_EVENT);
            dbg.d("Receive local event: " + e);
            AFNotificatorEvent event = getEventByName(e);

            Method m = mMethods.get(event.name);
            if (m == null) {
                dbg.w("There is no connected method for event '" + event.name + "'");
                return ;
            }

            Object[] params = new Object[event.args.length];
            for (int i = 0; i < event.args.length; ++i)
                params[i] = readExtraEventArgumentFromIntent(intent, event, i);

            try {
                m.invoke(mTarget, params);
            } catch (Exception ex) {
                dbg.e("Failed to call target method for event '" + event.name + "'", ex);
            }
        }
    };

    public AFNotificatorReceiver(Context ctx, String name, Object target, HashSet<AFNotificatorEvent> events) {
        super(name, events);
        mTarget = target;

        mLbm = LocalBroadcastManager.getInstance(ctx);
        mLbm.registerReceiver(mMessageReceiver, new IntentFilter(formatActionName(mName)));
    }

    public void connect(Enum<?> e) {
        dbg.d("Connect to event " + e.name());
        AFNotificatorEvent event = getEventByEnum(e);
        if (event == null)
            throw new AFNotificatorException("Cannot find connect event parameter '" + e.name() + "' in the list of " + mName + " events");

        try {
            Method method = mTarget.getClass().getMethod(event.name, event.args);
            mMethods.put(event.name, method);
            return ;
        } catch (Exception ex) {
            dbg.e("Cannot find method '" + event.name + "' to connect on the target " + mTarget, ex);
            dbg.e("Potential method are:");
            for (Method m: mTarget.getClass().getMethods()) {
                if (m.getName().equals(event.name))
                    dbg.e("- " + m);
            }

            throw new AFNotificatorException("Cannot connect event '" + event.name + "' on target " + mTarget);
        }
    }

}
