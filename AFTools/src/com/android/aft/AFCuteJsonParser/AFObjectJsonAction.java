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

public abstract class AFObjectJsonAction<CookieType> extends AFJsonAction<CookieType> {

    // Ctr
    public AFObjectJsonAction(String name) {
        super(name);
    }

    /**
     * Implement this method to add your code
     *
     * @param cookie
     */
    public abstract void onObject(CookieType cookie);

    /**
     * Method used only by NFCuteJsonParser class to avoid argument type
     * conversion problem because it is too complicate to get type of cookie.
     * Maybe it exists a better solution
     *
     * @param cookie
     */
    @Override
    @SuppressWarnings("unchecked")
    public void doActionBridge(Object cookie) {
        onObject((CookieType) cookie);
    }

    /**
     * Return true if the current value correspond to this JsonAction
     *
     * @param context Current context
     */
    @Override
    public boolean isCorrectNode(AFCuteJsonParserContext ctx) {
        final AFJsonValue current = ctx.getCurrent();
        return current.getType() == AFJsonValue.JsonValueType.JsonObject && current.getName().equals(mName);
    }

    /**
     * Return the current json object
     *
     * @return the current object
     */
    public AFJsonValue getJsonObject() {
        return mContext.getCurrent();
    }

    /**
     * Return one value of the json object
     *
     * @param name of the value
     * @return value
     */
    public AFJsonValue getValueOf(String name) {
        for (AFJsonValue value : mContext.getCurrent().getChildren())
            if (value.getName().equals(name))
                return value;

        return null;
    }

    /**
     * Return if the value is present
     *
     * @param name of the value
     * @return true if the value is present in the object
     */
    public boolean hasValueOf(String name) {
        for (AFJsonValue value : mContext.getCurrent().getChildren())
            if (value.getName().equals(name))
                return true;
        return false;
    }

    /**
     * Return one value of the json object as an int
     *
     * @param name of the value
     * @return value
     */
    public int getIntValueOf(String name) {
        AFJsonValue value = getValueOf(name);
        if (value == null) {
            AFCuteJsonParser.dbg.e("Cannot find property named '" + name + "' in Json Object named '" + mName + "'");
            return -1;
        }
        if (value.getType() == AFJsonValue.JsonValueType.JsonNumber)
            return value.getValueAsNumber().intValue();

        return -1;
    }

    /**
     * Return one value of the json object as an long
     *
     * @param name of the value
     * @return value
     */
    public long getLongValueOf(String name) {
        AFJsonValue value = getValueOf(name);
        if (value == null) {
            AFCuteJsonParser.dbg.e("Cannot find property named '" + name + "' in Json Object named '" + mName + "'");
            return -1;
        }
        if (value.getType() == AFJsonValue.JsonValueType.JsonNumber)
            return value.getValueAsNumber().longValue();

        return -1;
    }

    /**
     * Return one value of the json object as a String
     *
     * @param name of the value
     * @return value
     */
    public String getStringValueOf(String name) {
        AFJsonValue value = getValueOf(name);
        if (value == null) {
            AFCuteJsonParser.dbg.e("Cannot find property named '" + name + "' in Json Object named '" + mName + "'");
            return null;
        }

        if (value.getType() == AFJsonValue.JsonValueType.JsonString)
            return value.getValueAsString();
        return null;
    }

    /**
     * Return one value of the json object as a boolean
     *
     * @param name of the value
     * @return value
     */
    public Boolean getBooleanValueOf(String name) {
        AFJsonValue value = getValueOf(name);
        if (value == null) {
            AFCuteJsonParser.dbg.e("Cannot find property named '" + name + "' in Json Object named '" + mName + "'");
            return null;
        }

        if (value.getType() == AFJsonValue.JsonValueType.JsonBoolean)
            return value.getValueAsBoolean();
        return null;
    }

}
