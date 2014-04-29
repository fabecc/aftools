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

public abstract class AFRootArrayJsonAction<CookieType> extends AFArrayJsonAction<CookieType> {

    public AFRootArrayJsonAction() {
        super(AFJsonValue.ROOT_VALUE_NAME);
    }

    /**
     * Return true if the current value correspond to this JsonAction
     *
     * @param context
     *             Current context
     */
    @Override
    public boolean isCorrectNode(AFCuteJsonParserContext ctx) {
        if (!super.isCorrectNode(ctx))
            return false;

        return ctx.getValues().size() == 1;
    }

}
