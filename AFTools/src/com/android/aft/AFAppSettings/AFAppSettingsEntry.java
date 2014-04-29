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

package com.android.aft.AFAppSettings;

import java.lang.reflect.Field;

/**
 * Class to represent an setting Entry
 */
public class AFAppSettingsEntry extends AFAppSettingsElement {

    // Type of the entry
    public Class<?> type;

    // Value of the configuration entry
    public Object value;

    // Default value
    public Object defaultValue;

    // List of possible values to use (needed for value selector)
    public Object[] values;

    // Link to attribute class owner
    public Object settings_obj;

    // Link to attribute
    public Field settings_field;

    public AFAppSettingsEntry() {
    }

    public AFAppSettingsEntry(String name, Object value) {
        this.name = name;
        this.value = value;
        defaultValue = value;
    }

}
