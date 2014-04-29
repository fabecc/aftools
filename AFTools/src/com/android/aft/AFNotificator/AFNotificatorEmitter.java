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

import java.util.HashSet;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class AFNotificatorEmitter extends AFNotificatorCore {

    // Broadcast action name
    private String mActionName;

    // Local broadcast manager
    LocalBroadcastManager mLbm;

    public AFNotificatorEmitter(Context ctx, String name, HashSet<AFNotificatorEvent> events) {
        super(name, events);

        mActionName = formatActionName(name);
        mLbm = LocalBroadcastManager.getInstance(ctx);
    }

    public void connect(Object receiver, Enum<?> event) {
        dbg.d("Connect to " + receiver + " for event " + event.name());
    }

    public void emit(Enum<?> e, Object... args) {
        dbg.d("Emit an event on " + e.name());
        AFNotificatorEvent event = getEventByEnum(e);
        if (event == null)
            throw new AFNotificatorException("Cannot find event '" + e.name() + "' in events list");
        if (event.args.length != args.length)
            throw new AFNotificatorException("Bad argument list for event '" + e.name() + "': expected size of " + event.args.length + " has " + args.length);

        Intent intent = new Intent(mActionName);
        intent.putExtra(EXTRA_PARAM_EVENT, event.name);
        for (int i = 0; i < args.length; ++i) {
            putExtraEventArgumentInIntent(intent, event, args, i);
        }
        mLbm.sendBroadcast(intent);
    }

}
