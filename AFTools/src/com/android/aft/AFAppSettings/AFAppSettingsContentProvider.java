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
import java.util.ArrayList;
import java.util.Map.Entry;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.text.TextUtils;

import com.android.aft.AFAppSettings.exception.AFAppSettingsException;
import com.android.aft.AFAppSettings.exception.BadFieldNameAFAppSettingsException;
import com.android.aft.AFAppSettings.exception.BadFieldTypeAFAppSettingsException;
import com.android.aft.AFCoreTools.DebugTools;
import com.android.aft.AFCoreTools.ServiceTools;

public class AFAppSettingsContentProvider extends ContentProvider {

    /**
     * Enum for manage element type.
     * There is entry and action
     */
    public enum ElementType {
        Entry,
        Action,
    }

	// List of settings entries
    private ArrayList<AFAppSettingsEntry> mEntries = new ArrayList<AFAppSettingsEntry>();

    // List of settings actions
    private ArrayList<AFAppSettingsAction> mActions = new ArrayList<AFAppSettingsAction>();

	@Override
	public final boolean onCreate() {
	    loadStoreSettings();
		return true;
	}

    protected void reset(Context ctx) {
        AFAppSettingsDBHelper.delete(ctx);
        for (AFAppSettingsEntry entry: mEntries) {
            entry.value = entry.defaultValue;
            setSettingValueInField(entry);
        }
    }

    protected void killApplication() {
        // Kill all services
        onKillAllServices();
        ServiceTools.killAllRunningService(getContext());

        killAllActivity();
    }

    protected void onKillAllServices() {
        // Nothing to do
    }

    protected void killAllActivity() {
        // Nothing to do
    }

	/**
	 * This method is called when an settings entry is updated.
	 * Inherit this to receive notification.
	 *
	 * @param name	The name of the entry
	 */
	protected void onAttributeUpdated(String name) {
		// Nothing to do here
	}

	/**
	 * This method is called when an action is receive.
	 * Inherit this to receive the notification.
	 *
	 * @param id of the notification
	 */
	protected void onAction(int id) {
		// Nothing to do here
	}

	@Override
	public final String getType(Uri uri) {
		return null;
	}

	// Find attribute link in given class
	private final Field getAttributField(String name, Class<?> type) throws AFAppSettingsException {
        Field f;
        try {
            f = this.getClass().getField(name);
        } catch (NoSuchFieldException e) {
            DebugTools.e("NFSettings: Cannot find attribut name '" + name
                         + "' in class " + this.getClass().getSimpleName()
                         + " needed to associate this class to this wanted settings", e);

            throw new BadFieldNameAFAppSettingsException(this.getClass().getSimpleName(), name);
        }

        if (!f.getType().equals(type)) {
            DebugTools.e("NFSettings: Type mismatch for settings entry '" + name + "'");
            throw new BadFieldTypeAFAppSettingsException(name);
        }

        return f;
	}

    private final void addEntry(String name, Class<?> type, Object default_value, Object[] values) throws AFAppSettingsException {
        // Create entry object
        AFAppSettingsEntry e = new AFAppSettingsEntry(name, default_value);
        e.type = type;
        e.settings_obj = this;
        e.settings_field = getAttributField(name, type);
        e.values = values;

        mEntries.add(e);

        // Set default value
        setSettingValueInField(e);
    }

    protected final void addEntry(String name, String default_value) throws AFAppSettingsException {
        addEntry(name, String.class, default_value, null);
    }

    protected final void addEntry(String name, int default_value) throws AFAppSettingsException {
        addEntry(name, Integer.TYPE, default_value, null);
    }

    protected final void addEntry(String name, boolean default_value) throws AFAppSettingsException {
        addEntry(name, Boolean.TYPE, default_value, null);
    }

    protected final void addEntry(String name, String... values) throws AFAppSettingsException {
        addEntry(name, String.class, values[0], values);
    }

    protected final void addEntry(String name, Integer... values) throws AFAppSettingsException {
        addEntry(name, Integer.TYPE, values[0], values);
    }

    protected final void addEntry(String name, Enum<?>... values) throws AFAppSettingsException {
        addEntry(name, values[0].getClass(), values[0].toString(), values);
    }

    /**
     * Add an action
     *
     * @param id        Id of the action
     * @param name      Name of the action
     */
	public void addAction(int id, String name) {
		mActions.add(new AFAppSettingsAction(id, name));
	}

    /**
     * Load all settings value from internal database
     */
    protected void loadStoreSettings() {
        AFAppSettingsDBHelper db = new AFAppSettingsDBHelper(getContext());

        ArrayList<AFAppSettingsEntry> db_entries = db.readSettings();

        for (AFAppSettingsEntry db_entry: db_entries) {
            for (AFAppSettingsEntry entry: mEntries) {
                if (entry.name.equals(db_entry.name)) {
                    if (entry.type.equals(Integer.TYPE))
                        entry.value = Integer.valueOf((String)db_entry.value);
                    else if (entry.type.equals(Boolean.TYPE))
                        entry.value = Boolean.valueOf((String)db_entry.value);
                    else
                        entry.value = db_entry.value;

                    setSettingValueInField(entry);
                }
            }
        }

        db.replaceSettings(mEntries);
    }

	/**
	 * Use query method to return all configuration value
	 */
	@Override
	public final Cursor query(Uri uri,
						String[] projection,
						String selection,
						String[] selectionArgs,
						String sortOrder) {
		DebugTools.d("Query configuration value");

		MatrixCursor c = new MatrixCursor(new String[] { "element-type", "name", "value", "type", "values" });

		// Add all settings entry
		for (AFAppSettingsEntry e: mEntries) {
		    // Special case for enum
		    Class<?> type = e.type;
		    if (type.isEnum())
		        type = String.class;

		    // Add element in cursor
		    c.addRow(new Object[] {
		                ElementType.Entry.ordinal(),
		                e.name,
		                e.value,
		                type.getName(),
		                e.values == null ? "" : TextUtils.join(":", e.values)
		             });
		}

		// Add all action
        for (AFAppSettingsAction a: mActions) {
            // Add element in cursor
            c.addRow(new Object[] {
                        ElementType.Action.ordinal(),
                        a.name,
                        "",
                        "",
                        ""
                     });
        }

		return c;
	}

	/**
	 * Use update method to set new configuration
	 */
	@Override
	public final int update(Uri uri,
	                        ContentValues values,
	                        String selection,
	                        String[] selectionArgs) {
		DebugTools.d("Update settings value");

		// Read the content values
		for (Entry<String, Object> entry: values.valueSet()) {
			String key = entry.getKey();

			for (AFAppSettingsEntry e: mEntries)
			    if (key.equals(e.name)) {
			    	if (e.value.toString().equals(entry.getValue().toString())) {
						DebugTools.d("_ No change for " + key + ": '" + values.getAsString(key) + "'");
			    		continue ;
			    	}
					DebugTools.d("_ " + key + " - " + values.getAsString(key));

                    // Set new local value
			        if (e.type.equals(Integer.TYPE))
		                e.value = Integer.valueOf(entry.getValue().toString());
			        else if (e.type.equals(Boolean.TYPE))
                        e.value = Boolean.valueOf(entry.getValue().toString());
		            else if (e.type.equals(String.class))
	                    e.value = entry.getValue().toString();
		            else if (e.type.isEnum())
		                e.value = entry.getValue().toString();

			        // Set the final field
			        setSettingValueInField(e);

			        // Notify settings update
			        onAttributeUpdated(e.name);
			    }
		}

		// Update database
		AFAppSettingsDBHelper db = new AFAppSettingsDBHelper(getContext());

		db.replaceSettings(mEntries);

		db.close();

		return values.size();
	}

	@Override
	public final int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException("'Delete' is not supported for NFSettings");
	}

	@Override
	public final Uri insert(Uri uri, ContentValues values) {
        // Read the content values
        for (Entry<String, Object> entry: values.valueSet()) {
            String key = entry.getKey();

            if (key.equals("__kill")) {
                killApplication();
                return uri;
            }

            for (AFAppSettingsAction a: mActions)
                if (key.equals(a.name)) {
                    DebugTools.d("AFAppManager send action '" + key + "'");

                    // Notify settings update
                    onAction(a.id);

                    // There is only one action so return directly
                    return uri;
                }
        }

        return uri;
	}

    private final void setSettingValueInField(AFAppSettingsEntry entry) {
        try {
            if (entry.type.equals(Integer.TYPE))
                entry.settings_field.setInt(this, (Integer)entry.value);
            else if (entry.type.equals(Boolean.TYPE))
                entry.settings_field.setBoolean(this, (Boolean)entry.value);
            else if (entry.type.equals(String.class))
                entry.settings_field.set(this, entry.value);
            else if (entry.type.isEnum()) {
                Enum<?>[] values = (Enum<?>[])entry.values;
                for (Enum<?> e: values) {
                    if (e.name().equals(entry.value)) {
                        entry.settings_field.set(this, e);
                        break ;
                    }
                }
            }
        } catch (Exception e) {
            DebugTools.e("Cannot update field '" + entry.name + "' with value '" + entry.value + "'", e);
            throw new UnsupportedOperationException(e);
        }
    }


}
