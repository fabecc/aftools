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

import android.content.Context;

public abstract class AFJsonAction<CookieType> {

    // Name of the value associated to this action
    protected String mName;

    // Json parser
    protected AFCuteJsonParser mParser = null;

    // Parsing context
    protected AFCuteJsonParserContext mContext = null;

    /**
     * Constructor
     *
     * @param name
     *          Name of this action (use null for root action)
     */
    public AFJsonAction(String name) {
        mName = name;
    }

    /**
     * Return true if the current tag correspond to this ActionNode
     *
     * @param context
     *             Current context
     */
    public abstract boolean isCorrectNode(AFCuteJsonParserContext context);

    /**
     * Method used only by NFCuteJsonParser class to avoid argument type conversion problem
     * because it is too complicate to get type of cookie.
     * Maybe it exists a better solution
     *
     * @param cookie
     */
    public abstract void doActionBridge(Object cookie);

    /**
    * Re launch parser to read children node
    */
    protected void read_values() {
        mParser.read_values(mContext);
    }

    /**
     * Re launch parser to read children node
     *
     * @param cookie
     *         Cookie to pass to the children action node
     */
    public void read_values(Object cookie)
    {
        // Save old cookie
        Object old_cookie = mContext.getCookie();

        // Set cookie
        mContext.setCookie(cookie);

        // Relaunch parsing
        mParser.read_values(mContext);

        // Reset old cookie
        mContext.setCookie(old_cookie);
    }


    //
    // Accessor
    //

    /**
     * @return name of the action
     */
    public String getValueName() {
        return mName;
    }

    /**
     * Set the parser
     */
    public void setParser(AFCuteJsonParser parser) {
        mParser = parser;
    }

    /**
     * Set the parsing context
     */
    public void setContext(AFCuteJsonParserContext context) {
        mContext = context;
    }

    /**
     * @return The parsing context
     */
    public AFCuteJsonParserContext getContext() {
        return mContext;
    }

    public Context getApplicationContext() {
        return mContext.getApplicationContext();
    }

    public void setResultData(Object data) {
        mContext.getResult().setData(data);
    }

}
