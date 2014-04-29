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

package com.android.aft.AFCuteXmlParser;

import java.util.Stack;

/**
 * Add node test to check:
 * _ the name of parent node
 * _ the relative depth between current node and the parent node
 */
public abstract class AFNodeActionUnderNodeAtRelativeDepth<CookieType> extends AFNodeAction<CookieType> {

    private String mParentNodeName;
    private int mRelativeDepth;

    /**
     * Ctr
     *
     * @param depth
     *         Depth between parent node and current node
     * @param parentNodeName
     *         Name of the parent node
     */
    public AFNodeActionUnderNodeAtRelativeDepth(String nodeName, String parentNodeName, int depth) {
        super(nodeName);

        mParentNodeName = parentNodeName;
        mRelativeDepth = depth;
    }

    @Override
    public boolean isCorrectNode(AFCuteXmlParserContext ctx) {
        if (!super.isCorrectNode(ctx))
            return false;

        Stack<AFXmlTag> tags = ctx.getNodes();
        if (tags.size() < mRelativeDepth + 1)
            return false;

        AFXmlTag parent = tags.elementAt(tags.size() - mRelativeDepth - 1);
        return parent.getName().equals(mParentNodeName);
    }

}
