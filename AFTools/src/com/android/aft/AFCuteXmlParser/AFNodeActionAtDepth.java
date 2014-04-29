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

/**
 * Add node test to check:
 * _ the depth of the node
 */
public abstract class AFNodeActionAtDepth<CookieType> extends AFNodeAction<CookieType> {

	private int mDepth;

	/**
     * Ctr
     *
     * @param depth
     *         Needed depth of the node
     */
	public AFNodeActionAtDepth(String nodeName, int depthNode) {
		super(nodeName);

		mDepth = depthNode;
	}

	/**
     * Check if this action is for the current node.
     * Add a test to check:
     * _ the depth of current node
     *
     * @return true if this action is for current node
     */
	public boolean isCorrectNode(AFCuteXmlParserContext context) {
		return super.isCorrectNode(context)
		    && context.getDepth() == mDepth;
	}
}