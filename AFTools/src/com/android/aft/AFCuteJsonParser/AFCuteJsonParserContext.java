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

import android.content.Context;

/**
 * Parsing context
 */
public class AFCuteJsonParserContext {

    // Android application context
    private Context mContext;

    // Special cookie useful to store context data during parsing
    // It is only use by a parsing implementation
    private Object mCookie = null;

	// Internal using to mark if an ActionNode call make a call to read_node method
	private boolean mHasReadChildren;

	// Stack of values from the root to the current position
    private Stack<AFJsonValue> mValues = new Stack<AFJsonValue>();

	// Result parsing object to store code / error msg or data at parsing time and get it at the and of parsing
	private AFCuteJsonParserResult mResult;

	public AFCuteJsonParserContext() {
	    mResult = new AFCuteJsonParserResult();
	}

	public void setCookie(Object cookie) {
		mCookie = cookie;
	}

	public Object getCookie() {
		return mCookie;
	}

	public void hasReadChildren(boolean hasReadChildren) {
		mHasReadChildren = hasReadChildren;
	}

	public boolean hasReadChildren() {
		return mHasReadChildren;
	}

    public void pushValue(AFJsonValue value) {
        mValues.push(value);
    }

    public void popValue() {
        mValues.pop();
    }

    public AFJsonValue getCurrent() {
        return mValues.peek();
    }

    public void setCurrent(AFJsonValue value) {
        pushValue(value);
    }

    public Stack<AFJsonValue> getValues() {
        return mValues;
    }

	public void setResult(AFCuteJsonParserResult result) {
	    mResult = result;
	}

	public AFCuteJsonParserResult getResult() {
	    return mResult;
	}

    public void setApplicationContext(Context ctx) {
        mContext = ctx;
    }

    public Context getApplicationContext() {
        return mContext;
    }

}