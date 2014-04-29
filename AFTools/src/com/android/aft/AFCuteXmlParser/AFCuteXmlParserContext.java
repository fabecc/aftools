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

import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;

import com.android.aft.AFCuteXmlParser.LowParser.HomeMade.AFBufferedInputStream;
import com.android.aft.AFCuteXmlParser.LowParser.HomeMade.AFXmlToken;

/**
 * Parsing context
 */
public class AFCuteXmlParserContext {

    //
    // Common attribute
    //

    // Special cookie useful to store context data during parsing
    // It is only use by a parsing implementation
    private Object mCookie = null;

    // Stack of node from the root to the current position
    private Stack<AFXmlTag> mNodes = new Stack<AFXmlTag>();

	// Current depth of parsing
	private int mDepth = 0;

	// Internal using to mark if an ActionNode call make a call to read_node method
	private boolean mHasReadChildren;

	// Result parsing object to store code / error msg or data at parsing time and get it at the and of parsing
	private AFCuteXmlParserResult mResult;

	// Set by action node if the parser need to be stop
	private boolean mStopParser = false;

	// Config accept several node on root
	private boolean mAcceptSeveralNodeOnRoot = false;

	//
	// Special attribute for HomeMade parser
	//

    // Buffered input management
    private AFBufferedInputStream mStream;

    // Current token: used only in case of buffering if it is read but not used at parsing
    private AFXmlToken mCurrentToken;

    // Special attribute for lexing context that permit to make difference between id and text
    // ex: '< ID > TEXT </ ID >'
    private boolean mIsParsingInTag = false;

    //
    // Special attribute for Pullxml parser
    //

    // Instance of low level parser
    private XmlPullParser mLowParser;

    // True if the current token is read
    private boolean mHasReadCurrentXmlPullParserToken;

	public AFCuteXmlParserContext() {
	    mResult = new AFCuteXmlParserResult();
	}

	public void setStream(InputStreamReader stream) {
	    mStream = new AFBufferedInputStream(stream);
	}

    public AFBufferedInputStream getStream() {
        return mStream;
    }

    public void setCurrentToken(AFXmlToken token) {
        mCurrentToken = token;
    }

    public AFXmlToken getCurrentToken() {
        return mCurrentToken;
    }

	public void setCookie(Object cookie) {
		mCookie = cookie;
	}

	public Object getCookie() {
		return mCookie;
	}

	public void pushNode(AFXmlTag tag) {
	    mNodes.push(tag);
	}

	public void popNode() {
	    mNodes.pop();
	}

	public Stack<AFXmlTag> getNodes() {
	    return mNodes;
	}

	public AFXmlTag getParent() {
	    final int size = mNodes.size();
	    if (size < 2)
	        return null;

		return mNodes.elementAt(size - 2);
	}

	public String getParentNodeName(){
	    return getParent().getName();
	}

	public Hashtable<String, String> getParentNodeAttributes() {
	    return getParent().getAttributs();
	}

	public AFXmlTag getCurrent() {
		return mNodes.peek();
	}

    public String getCurrentNodeName(){
        return mNodes.peek().getName();
    }

    public Hashtable<String, String> getCurrentNodeAttributes() {
        return mNodes.peek().getAttributs();
    }

	public void setDepth(int depth) {
		mDepth = depth;
	}

	public int getDepth() {
		return mDepth;
	}

	public void increase_depth() {
		++mDepth;
	}

	public void decrease_depth() {
		--mDepth;
	}

	public void hasReadChildren(boolean hasReadChildren) {
		mHasReadChildren = hasReadChildren;
	}

	public boolean hasReadChildren() {
		return mHasReadChildren;
	}

	public boolean isParsingInTag() {
	    return mIsParsingInTag;
	}

	public void setIsParsingInTag(boolean isParsingInTag) {
	    mIsParsingInTag = isParsingInTag;
	}

	public void setResult(AFCuteXmlParserResult result) {
	    mResult = result;
	}

	public AFCuteXmlParserResult getResult() {
	    return mResult;
	}

    public void setStopParser() {
        mStopParser = true;
    }

    public boolean requestStopParser() {
        return mStopParser;
    }

    public void setXmlPullParser(XmlPullParser parser) {
        mLowParser = parser;
    }

    public XmlPullParser getXmlPullParser() {
        return mLowParser;
    }

    public boolean hasReadCurrentXmlPullParserToken() {
        return mHasReadCurrentXmlPullParserToken;
    }

    public void setHasReadCurrentXmlPullParserToken(boolean hasRead) {
        mHasReadCurrentXmlPullParserToken = hasRead;
    }

    public void setAcceptSeveralNodeOnRoot(boolean accept) {
        mAcceptSeveralNodeOnRoot = accept;
    }

    public boolean acceptSeveralNodeOnRoot() {
        return mAcceptSeveralNodeOnRoot;
    }

}