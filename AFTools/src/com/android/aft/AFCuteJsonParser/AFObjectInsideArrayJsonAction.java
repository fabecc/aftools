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

import java.util.Stack;

public abstract class AFObjectInsideArrayJsonAction<CookieType> extends AFObjectJsonAction<CookieType> {

    private String mArrayName;

    /**
     *
     * @param array_name
     */
    public AFObjectInsideArrayJsonAction(String array_name) {
        super("");
        mArrayName = array_name;
    }

    /**
     * Return true if the current value correspond to this JsonAction
     *
     * @param context
     *             Current context
     */
    @Override
    public boolean isCorrectNode(AFCuteJsonParserContext ctx) {
        Stack<AFJsonValue> values = ctx.getValues();
        final int size = values.size();
        if (size < 2)
            return false;

        if (values.peek().getType() != AFJsonValue.JsonValueType.JsonObject)
            return false;

        final AFJsonValue parent = values.elementAt(size - 2);
        if (parent.getType() != AFJsonValue.JsonValueType.JsonArray)
            return false ;

        if (!parent.getName().equals(mArrayName))
            return false ;

        return true;
    }

}
