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

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AFAppSettingsDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "NFSettings_data.db";

    public static final String TBL_SETTINGS_VALUE = "SettingsValue";
    private static final String TBL_SETTINGS_VALUE_FIELD_ID = "_id";
    private static final String TBL_SETTINGS_VALUE_FIELD_NAME = "name";
    private static final String TBL_SETTINGS_VALUE_FIELD_VALUE = "value";

    public AFAppSettingsDBHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void delete(Context ctx) {
        ctx.deleteDatabase(DATABASE_NAME);
    }

    @Override
    // Method called automatically during creation of the database if the
    // database does not exist
    public void onCreate(SQLiteDatabase database) {
        // Create UploadFile table
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TBL_SETTINGS_VALUE + " ("
                         + TBL_SETTINGS_VALUE_FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                         + TBL_SETTINGS_VALUE_FIELD_NAME + " TEXT NOT NULL, "
                         + TBL_SETTINGS_VALUE_FIELD_VALUE + " TEXT NOT NULL);");
    }

    @Override
    // Method called automatically if database version is increase
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Drop all table
        database.execSQL("DROP TABLE IF EXISTS " + TBL_SETTINGS_VALUE);
        onCreate(database);
    }

    public synchronized ArrayList<AFAppSettingsEntry> readSettings()  {
        ArrayList<AFAppSettingsEntry> entries = new ArrayList<AFAppSettingsEntry>();

        SQLiteDatabase database = getWritableDatabase();

        Cursor c_entries = database.query(TBL_SETTINGS_VALUE,
                                          new String[] {
                                              TBL_SETTINGS_VALUE_FIELD_ID,
                                              TBL_SETTINGS_VALUE_FIELD_NAME,
                                              TBL_SETTINGS_VALUE_FIELD_VALUE
                                          },
                                          null,
                                          null,
                                          null,
                                          null,
                                          null);
        if (c_entries == null) {
            database.close();
            return entries;
        }

        for (c_entries.moveToFirst(); !c_entries.isAfterLast(); c_entries.moveToNext()) {
            AFAppSettingsEntry e = new AFAppSettingsEntry();

            e.name = c_entries.getString(1);
            e.value = c_entries.getString(2);

            entries.add(e);
        }

        c_entries.close();
        database.close();
        return entries;
    }

    public synchronized void replaceSettings(ArrayList<AFAppSettingsEntry> entries) {
        deleteAllValue();
        saveSettings(entries);
    }

    private synchronized void saveSettings(ArrayList<AFAppSettingsEntry> entries) {
        SQLiteDatabase database = getWritableDatabase();

        for (AFAppSettingsEntry e: entries) {
            ContentValues values = new ContentValues();

            values.put(TBL_SETTINGS_VALUE_FIELD_NAME, e.name);
            values.put(TBL_SETTINGS_VALUE_FIELD_VALUE, e.value.toString());

            database.insert(TBL_SETTINGS_VALUE, null, values);
        }

        database.close();
    }

    private synchronized void deleteAllValue() {
        SQLiteDatabase database = getWritableDatabase();

        database.delete(TBL_SETTINGS_VALUE, null, null);

        database.close();
    }

}
