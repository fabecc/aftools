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

import android.content.Intent;

import com.android.aft.AFCoreTools.DebugTools;

public class AFNotificatorCore {

    // Constant
    protected final static String EXTRA_PARAM_EVENT = "AF_NOTIFICATOR_EXTRA_EVENT";
    protected final static String EXTRA_PARAM_ARG_PREFIX = "AF_NOTIFICATOR_EXTRA_ARG_";

    // Logger
    protected DebugTools.Logger dbg;

    // Name of the notificator
    protected String mName;

    // Events list
    protected HashSet<AFNotificatorEvent> mEvents;

    public AFNotificatorCore(String name, HashSet<AFNotificatorEvent> events) {
        mName = name;
        mEvents = events;

        dbg = new DebugTools.Logger("AFNotificator[" + mName + "]");
        testEventArgumentCompatibility(events);
    }

    protected void testEventArgumentCompatibility(HashSet<AFNotificatorEvent> events) {
        for (AFNotificatorEvent event: events) {
            for (Class<?> t: event.args) {
                if (t == Integer.TYPE)
                    continue ;
                else if (t == Long.TYPE)
                    continue ;
                else if (t == Float.TYPE)
                    continue ;
                else if (t == Boolean.TYPE)
                    continue ;
                else if (t == Double.TYPE)
                    continue ;
                else if (t == String.class)
                    continue ;
                else {
                    dbg.e("For event '" + event.name + "' use argument type " + t + " is not yet implemented in AFNotificator");
                    throw new AFNotificatorException("Event argument type is not allowed");
                }
            }
        }
    }

    protected void putExtraEventArgumentInIntent(Intent intent, AFNotificatorEvent event, Object[] args, int i) {
        String key = EXTRA_PARAM_ARG_PREFIX + i;
        Class<?> t = event.args[i];
        if (t == Integer.TYPE)
            intent.putExtra(key, (Integer)args[i]);
        else if (t == Long.TYPE)
            intent.putExtra(key, (Long)args[i]);
        else if (t == Float.TYPE)
            intent.putExtra(key, (Float)args[i]);
        else if (t == Boolean.TYPE)
            intent.putExtra(key, (Boolean)args[i]);
        else if (t == Double.TYPE)
            intent.putExtra(key, (Double)args[i]);
        else if (t == String.class)
            intent.putExtra(key, (String)args[i]);
        else {
            dbg.e("For event '" + event.name + "' use argument type " + t + " is not yet implemented in AFNotificator");
            throw new AFNotificatorException("Event argument type is not allowed");
        }
    }

    protected Object readExtraEventArgumentFromIntent(Intent intent, AFNotificatorEvent event, int i) {
        String key = EXTRA_PARAM_ARG_PREFIX + i;
        Class<?> t = event.args[i];
        if (t == Integer.TYPE)
            return intent.getIntExtra(key, 0);
        else if (t == Long.TYPE)
            return intent.getLongExtra(key, 0);
        else if (t == Float.TYPE)
            return intent.getFloatExtra(key, 0);
        else if (t == Boolean.TYPE)
            return intent.getBooleanExtra(key, false);
        else if (t == Double.TYPE)
            return intent.getDoubleExtra(key, 0);
        else if (t == String.class)
            return intent.getStringExtra(key);
        else {
            dbg.e("For event '" + event.name + "' use argument type " + t + " is not yet implemented in AFNotificator");
            throw new AFNotificatorException("Event argument type is not allowed");
        }
    }

    protected String formatActionName(String notificatorName) {
        return AFNotificatorEmitter.class.getName() + "." + notificatorName.hashCode();
    }

    protected AFNotificatorEvent getEventByEnum(Enum<?> event) {
        if (event == null)
            return null;
        return getEventByName(event.name());
    }

    protected AFNotificatorEvent getEventByName(String name) {
        if (name == null)
            return null;
        for (AFNotificatorEvent e: mEvents)
            if (e.name.equalsIgnoreCase(name))
                return e;
        return null;
    }

}
