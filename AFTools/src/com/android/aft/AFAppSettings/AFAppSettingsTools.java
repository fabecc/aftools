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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.android.aft.AFCoreTools.DebugTools;

public class AFAppSettingsTools {

	/**
	 * Return the list of installed application that can be configure with NFSettings
	 *
	 * @param ctx	Application context
	 *
	 * @return List of application
	 */
	public static ArrayList<AFAppSettingsApplication> getConfigurableAppByNFSetting(Context ctx) {
		ArrayList<AFAppSettingsApplication> apps = new ArrayList<AFAppSettingsApplication>();

        final PackageManager manager = ctx.getPackageManager();
        for (PackageInfo info : manager.getInstalledPackages(PackageManager.GET_PERMISSIONS))
        {
            // Read meta data
            ApplicationInfo metaDataInfo = null;
            try {
                metaDataInfo = manager.getApplicationInfo(info.packageName, PackageManager.GET_META_DATA);
            } catch (NameNotFoundException e) {
                // This can never happen because info.packageName is a
                // package return by system
            }

            // Test if there is meta data defined, else, it is not a configurable application
            if (metaDataInfo.metaData == null)
                continue;

            // Get authorities
            String authorities = metaDataInfo.metaData.getString("AFSettings-ContentProvider");
            if (TextUtils.isEmpty(authorities))
            	continue;

            AFAppSettingsApplication app = new AFAppSettingsApplication();
    		app.mPackage = info.packageName;
    		app.mName = manager.getApplicationLabel(info.applicationInfo).toString();
    		app.mAuthorities = authorities;

    		apps.add(app);
        }

        return apps;
	}


	/**
	 * Load all configuration entry given by an application content provider
	 *
	 * @param ctx			Application context
	 * @param authorities	Authorities of the content provider
	 *
	 * @return List of settings
	 */
	public static ArrayList<AFAppSettingsElement> loadConfigurationEntries(Context ctx, String authorities) {
		DebugTools.d("Load project configuration");

		ArrayList<AFAppSettingsElement> entries = new ArrayList<AFAppSettingsElement>();

		Uri uri = Uri.parse("content://" + authorities);
		Cursor c = ctx.getContentResolver().query(uri, null, null, null, null);
		if (c == null || c.getCount() == 0) {
			DebugTools.e("There is no configuration entry");
			return entries;
		}

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			if (c.getInt(0) == AFAppSettingsContentProvider.ElementType.Entry.ordinal()) {
	            AFAppSettingsEntry entry = new AFAppSettingsEntry();
    			entry.name = c.getString(1);
    			entry.value = c.getString(2);
    			String type = c.getString(3);
    			if (type.equals("int"))
    			    entry.type = Integer.TYPE;
    			else if (type.equals("boolean"))
                    entry.type = Boolean.TYPE;
    			else {
    			    try {
    			        entry.type = Class.forName(type);
                    } catch (ClassNotFoundException e) {
                        DebugTools.e("Cannot read entry type", e);
                        continue ;
                    }
    			}
    			if (TextUtils.isEmpty(c.getString(4)))
    			    entry.values = null;
    			else
    			    entry.values = TextUtils.split(c.getString(4), ":");

    			entries.add(entry);
			}
			else { // Action
			    AFAppSettingsAction action = new AFAppSettingsAction();
			    action.name = c.getString(1);

                entries.add(action);
			}
		}

		return entries;
	}

	/**
	 * Save all configuration entry for the givent application content provider authorities.
	 *
	 * @param ctx          Application context
	 * @param authorities  Content provider authorities
	 * @param entries      Application settings
	 */
	public static void saveConfigurationEntries(Context ctx,
												String authorities,
												ArrayList<AFAppSettingsElement> entries) {
		ContentValues v = new ContentValues();

		for (AFAppSettingsElement element: entries) {
		    if (element instanceof AFAppSettingsEntry) {
		        AFAppSettingsEntry e = (AFAppSettingsEntry)element;
		        v.put(e.name, e.value.toString());
		    }
		}

		Uri uri = Uri.parse("content://" + authorities);
		ctx.getContentResolver().update(uri, v, null, null);
	}

	public static void onAction(Context ctx,
	                            String authorities,
	                            AFAppSettingsAction action) {
        ContentValues v = new ContentValues();
        v.put(action.name, 42);

        Uri uri = Uri.parse("content://" + authorities);
        ctx.getContentResolver().insert(uri, v);
	}

}
