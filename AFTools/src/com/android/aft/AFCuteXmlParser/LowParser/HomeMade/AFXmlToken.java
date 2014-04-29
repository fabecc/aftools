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

package com.android.aft.AFCuteXmlParser.LowParser.HomeMade;

public class AFXmlToken {

    /**
     * Possible types of token
     */
    public enum Type {
        UNKNOWN,
        EOF,
        TAG_BEGIN,            // <
        TAG_BEGIN_TERMINAL,   // </
        TAG_END,              // >
        TAG_END_TERMINAL,     // />
        EQUAL,                // =
        STRING,               // foo, "foo", <![CDATA[foo]]>
//        CD_BEGIN,             // <![CDATA[
//        CD_END,               // ]]>
//        COMMENT_BEGIN,        // <!--
//        COMMENT_END,          // -->
    };

    // Type of token
    private Type mType = Type.UNKNOWN;

    // Value associated to token
    private String mValue;

    /**
     * XmlToken constructor
     *
     * @param type of token
     */
    public AFXmlToken(Type type) {
        this(type, null);
    }

    /**
     * XmlToken constructor
     *
     * @param type of token
     * @param value
     */
    public AFXmlToken(Type type, String value) {
        mType = type;
        mValue = value;
    }

    /**
     * Get type ok token
     *
     * @return Type of token
     */
    public Type getType() {
        return mType;
    }

    /**
     * Set type of token
     *
     * @param type
     */
    public void setType(Type type) {
        mType = type;
    }

    /**
     * Get value of token
     *
     * @return Value of token
     */
    public String getValue() {
        return mValue;
    }

    /**
     * Set value of token
     *
     * @param value
     */
    public void setValue(String value) {
        mValue = value;
    }

}
