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

package com.android.aft.AFCuteJsonParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AFJsonValue {

    // Enum for type of value
    public enum JsonValueType {
        None,
        JsonObject,
        JsonArray,
        JsonString,
        JsonNumber,
        JsonBoolean,
        JsonNull,
    };

    // Special name for the root value
    public static final String ROOT_VALUE_NAME = "<<root>>";

    // Type of current value
    private JsonValueType mType;

    // Name of the values
    private String mName;

    // List of possible value
    private JSONObject mJsonObject;
    private JSONArray mJsonArray;
    private String mJsonString;
    private Number mJsonNumber;
    private boolean mJsonBoolean;

    private ArrayList<AFJsonValue> mChildren;

    /**
     * Constructor
     */
    public AFJsonValue(Object json) {
        this("", json);
    }

    public AFJsonValue(String name, Object json) {
        mName = name;

        if (json instanceof JSONObject) {
            mType = JsonValueType.JsonObject;
            mJsonObject = (JSONObject) json;
            mChildren = new ArrayList<AFJsonValue>();
            try {
                Iterator<?> keys = mJsonObject.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    mChildren.add(new AFJsonValue(key, mJsonObject.get(key)));
                }
            } catch (JSONException e) {
            }
        } else if (json instanceof JSONArray) {
            mType = JsonValueType.JsonArray;
            mJsonArray = (JSONArray) json;
            mChildren = new ArrayList<AFJsonValue>();
            try {
                for (int i = 0; i < mJsonArray.length(); ++i)
                    mChildren.add(new AFJsonValue("" + i, mJsonArray.get(i)));
            } catch (JSONException e) {
            }
        } else if (json instanceof String) {
            mType = JsonValueType.JsonString;
            mJsonString = (String) json;
        } else if (json instanceof Number) {
            mType = JsonValueType.JsonNumber;
            mJsonNumber = (Number) json;
        } else if (json instanceof Boolean) {
            mType = JsonValueType.JsonBoolean;
            mJsonBoolean = ((Boolean) json).booleanValue();
        } else if (json.equals(JSONObject.NULL)) {
            mType = JsonValueType.JsonNull;
        } else
            mType = JsonValueType.None;
    }

    /**
     * @return Type of value
     */
    public JsonValueType getType() {
        return mType;
    }

    /**
     * @return Name of the value
     */
    public String getName() {
        return mName;
    }

    //
    // Value accessor
    //

    public Number getValueAsNumber() {
        return mJsonNumber;
    }

    public String getValueAsString() {
        return mJsonString;
    }

    public Boolean getValueAsBoolean() {
        return Boolean.valueOf(mJsonBoolean);
    }

    /**
     * @return true if the value as some children values
     */
    public boolean hasChildren() {
        if (mChildren == null)
            return false;

        return mChildren.size() != 0;
    }

    public Collection<AFJsonValue> getChildren() {
        if (mChildren == null)
            return null;

        return mChildren;
    }

    private AFJsonValue getJsonChild(String name) {
        if (mChildren == null)
            return null;

        if (mChildren.size() < 1)
            return null;

        for (AFJsonValue c: mChildren)
            if (c.getName().equals(name))
                return c;

        return null;
    }

    public AFJsonValue getJsonArrayEntry(int index) {
        return getJsonChild(Integer.toString(index));
    }

    public AFJsonValue getJsonObjectEntry(String name) {
        return getJsonChild(name);
    }

    // Convert to String
    @Override
    public String toString() {
        switch (mType) {
            case JsonBoolean:
                return mJsonBoolean ? "true" : "false";

            case JsonNumber:
                return mJsonNumber.toString();

            case JsonArray:
                return "array[" + mJsonArray.length() + "]";

            case JsonNull:
                return "null";

            case JsonString:
                return '"' + mJsonString + '"';

            case JsonObject:
                StringBuilder str = new StringBuilder();

                str.append("{");
                Iterator<?> keys = mJsonObject.keys();
                while (keys.hasNext()) {
                    if (str.length() > 1)
                        str.append(" ,");
                    str.append('"');
                    str.append(keys.next());
                    str.append('"');
                }
                str.append("}");

                return str.toString();
        }

        return "<undifined>";
    }

}
