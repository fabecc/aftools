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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Tools class Helper for Json action
 */
public class JsonTools {

    /**
     * Returns the value mapped by name in json object
     * <p>
     * Same as org.json.JSONObject.getString<br>
     * except that it return null (instead of throwing an Exception)<br>
     * when the item does not exist
     *
     * @param json Json object to be parsed for the 'name' key
     * @param name key
     * @return Value mapped by name, null if no mapping
     * @see org.json.JSONObject
     */
    public static String getStringOrNull(JSONObject json, String name) {
        try {
            return json.getString(name);
        } catch (JSONException e) {
            // The mapping does not exist
            return null;
        }
    }

    /**
     * Returns the value mapped by name in json object
     * <p>
     * Same as org.json.JSONObject.getString<br>
     * except that it return null (instead of throwing an Exception)<br>
     * when the item does not exist
     *
     * @param json Json object to be parsed for the 'name' key
     * @param name key
     * @return Value mapped by name, "" if no mapping
     * @see org.json.JSONObject
     */
    public static String getStringOrEmpty(JSONObject json, String name) {
        try {
            return json.getString(name);
        } catch (JSONException e) {
            // The mapping does not exist
            return "";
        }
    }

    public static int getInt(JSONObject json, String name) {
        try {
            return json.getInt(name);
        } catch (JSONException e) {
            // The mapping does not exist
            return -1;
        }
    }
}
