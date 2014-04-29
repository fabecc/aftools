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

import java.util.Collection;

public abstract class AFArrayJsonAction<CookieType> extends AFJsonAction<CookieType> {

    // Ctr
    public AFArrayJsonAction(String name) {
        super(name);
    }

    /**
     * Implement this method to add your code
     *
     * @param cookie
     */
    public abstract void onArray(CookieType cookie);

    /**
     * Method used only by NFCuteJsonParser class to avoid argument type conversion problem
     * because it is too complicate to get type of cookie.
     * Maybe it exists a better solution
     *
     * @param cookie
     */
    @Override
    @SuppressWarnings("unchecked")
    public void doActionBridge(Object cookie) {
        onArray((CookieType)cookie);
    }

    /**
     * Get the number of entries in array
     *
     * @return number of entries
     */
    public int getNumberOfEntry() {
        return mContext.getCurrent().getChildren().size();
    }

    /**
     * Return all the value in this array
     *
     * @return values
     */
    public Collection<AFJsonValue> getValues() {
        return mContext.getCurrent().getChildren();
    }

    /**
     * Return one value of the json array at given index
     *
     * @param index of the value
     *
     * @return value
     */
    public AFJsonValue getValue(int index) {
    	return mContext.getCurrent().getJsonArrayEntry(index);
    }

    /**
     * Return true if the current value correspond to this JsonAction
     *
     * @param context
     *             Current context
     */
    @Override
    public boolean isCorrectNode(AFCuteJsonParserContext ctx) {
        final AFJsonValue current = ctx.getCurrent();
        return current.getType() == AFJsonValue.JsonValueType.JsonArray
            && mName.equals(current.getName());
    }

}
