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

import java.util.Hashtable;

public class AFXmlTag {

    public final static int TAG_TYPE_START = 1;
    public final static int TAG_TYPE_END = 2;
    public final static int TAG_TYPE_START_END = 3;

    private String name_;
    private Hashtable<String, String> attributs_ = null;
    private int type_ = TAG_TYPE_START;

    public AFXmlTag(String name) {
        setName(name);
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public int getType() {
        return type_;
    }

    public void setType(int type) {
        type_ = type;
    }

    public void setAttribut(String key, String value) {
        if (attributs_ == null)
            attributs_ = new Hashtable<String, String>();

        attributs_.put(key, value);
    }

    public String getAttribut(String key) {
        if (attributs_ == null)
            return "";

        return attributs_.get(key);
    }

    public boolean hasAttribute(String key) {
        if (attributs_ == null)
            return false;

        return attributs_.containsKey(key);
    }

    public Hashtable<String, String> getAttributs() {
        if (attributs_ == null)
            attributs_ = new Hashtable<String, String>();

        return attributs_;
    }

    public boolean isFinalOf(AFXmlTag tag) {
        return type_ == AFXmlTag.TAG_TYPE_END && name_.equals(tag.getName());
    }

}
