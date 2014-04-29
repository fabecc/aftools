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

public abstract class AFNodeAction<CookieType> {

    /**
     * The action block to execute
     *
     * @param cookie
     *          Cookie object used for this Action
     */
    public abstract void onNode(CookieType cookie);

	private AFCuteXmlParser mParser = null;
	private AFCuteXmlParserContext mContext = null;
	private String mNodeName;

	/**
	 * Create an ActionNode to correspond to a specific tag name
	 *
	 * @param nodeName
	 *         The name of the tag to apply this Action
	 */
	public AFNodeAction(String nodeName){
	    mNodeName = nodeName;
	}

	/**
	 * Re launch parser to read children node
	 */
	public void read_children() {
		mParser.read_children(mContext, true);
	}

	/**
	 * Re launch parser to read children node
	 *
	 * @param cookie
	 *         Cookie to pass to the children action node
	 */
	public void read_children(Object cookie)
	{
		// Save old cookie
		Object old_cookie = mContext.getCookie();

		// Set cookie
		mContext.setCookie(cookie);

		// Relaunch parsing
		mParser.read_children(mContext, true);

		// Reset old cookie
		mContext.setCookie(old_cookie);
	}

	/**
     * Re launch parser to read children node without treat potential action
     */
    public void consume_children() {
        mParser.read_children(mContext, false);
    }

	/**
	 * Read the text content of this tag
	 *
	 * @return text content
	 */
	public String read_content() {
		return mParser.read_content(mContext);
	}

	/**
	 * Stop the parser.
	 * This method should be called after a detected error in given data
	 */
	public void stop() {
	    mContext.setStopParser();
	}

    /**
     * Method used only by CuteXmlParser class to avoid argument type conversion problem
     * because it is too complicate to get type of cookie.
     * Maybe it exists a better solution
     *
     * Here, the call to onNode can generate a ClassCastException but it is not possible
     * to check if the final type of the cookie correspond to the wanted type (CookieType).
     *
     * @param cookie
     */
    @SuppressWarnings("unchecked")
    public void onNodeBridge(Object cookie) {
        onNode((CookieType)cookie);
    }

	/**
	 * Set the parsing context
	 */
	public void setContext(AFCuteXmlParserContext context) {
		mContext = context;
	}

	/**
	 * @return The parsing context
	 */
	public AFCuteXmlParserContext getContext() {
		return mContext;
	}

	/**
	 * Set the parser
	 */
	public void setParser(AFCuteXmlParser parser) {
		mParser = parser;
	}

	/**
	 * @return The parser
	 */
	public AFCuteXmlParser getParser() {
		return mParser;
	}

	/**
	 * @return The tag name to apply this action
	 */
	public String getNodeName(){
		return mNodeName;
	}

	/**
	 * @return True if the attribute is in node
	 */
	public boolean hasAttribute(String name) {
	    if (getContext().getCurrentNodeAttributes() == null)
	        return false;

	    return getContext().getCurrentNodeAttributes().containsKey(name);
	}

	/**
     * Get a node String attribute value
     *
     * @param name
     *         Name of the attribute
     * @return
     *         The attribute value (null if not exist)
     */
    public String getStringAttribute(String name) {
        return getStringAttribute(name, null);
    }

   /**
     * Get a node String attribute value
     *
     * @param name
     *         Name of the attribute
     * @param def
     *         Value to return if there in no string attribute
     * @return
     *         The attribute value
     */
    public String getStringAttribute(String name, String def) {
        if (!hasAttribute(name))
            return def;

        return getContext().getCurrentNodeAttributes().get(name);
    }

	/**
	 * Get a node int attribute value
	 *
	 * @param name
	 *         Name of the attribute
	 * @return
	 *         The attribute value (-1 if not exist)
	 */
	public int getIntAttribute(String name) {
	    return getIntAttribute(name, -1);
	}

   /**
     * Get a node int attribute value
     *
     * @param name
     *         Name of the attribute
     * @param def
     *         Value to return if there in no int attribute
     * @return
     *         The attribute value (-1 if not exist)
     */
	public int getIntAttribute(String name, int def) {
	    if (!hasAttribute(name))
	        return def;

	    String value = getContext().getCurrentNodeAttributes().get(name);

	    try {
	        return Integer.valueOf(value);
	    } catch (Throwable e) {
	        return def;
	    }
	}

    /**
     * Get a node boolean attribute value
     *
     * @param name
     *         Name of the attribute
     * @return
     *         The attribute value (false if not exist)
     */
    public boolean getBooleanAttribute(String name) {
        return getBooleanAttribute(name, false);
    }

   /**
     * Get a node boolean attribute value
     *
     * @param name
     *         Name of the attribute
     * @param def
     *         Value to return if there in no boolena attribute
     * @return
     *         The attribute value
     */
    public boolean getBooleanAttribute(String name, boolean def) {
        if (!hasAttribute(name))
            return def;

        String value = getContext().getCurrentNodeAttributes().get(name);
        if (value.equals("true")
            || value.equals("yes")
            || value.equals("1"))
            return true;

        return false;
    }

    /**
     * Convenient method to set the result error code
     *
     * @param errCode
     */
    public void setErrorCode(int errCode) {
        getContext().getResult().setErrorCode(errCode);
    }

    /**
     * Convenient method to set the result error message
     *
     * @param errMsg
     */
    public void setErrorMessage(String errMsg) {
        getContext().getResult().setErrorMsg(errMsg);
    }

    /**
     * Set the result data of this parsing
     *
     * @param data
     */
    public void setResultData(Object data) {
        getContext().getResult().setData(data);
    }

	/**
	 * Return true if the current tag correspond to this ActionNode
	 *
	 * @param context
	 *             Current context
	 */
	public boolean isCorrectNode(AFCuteXmlParserContext context) {
		return mNodeName.equals(context.getCurrent().getName());
	}

}
