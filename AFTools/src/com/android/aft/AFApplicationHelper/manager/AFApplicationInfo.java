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

package com.android.aft.AFApplicationHelper.manager;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.os.Environment;

import com.android.aft.AFApplicationHelper.manager.AFDeviceInfo.StorageSpace;

public class AFApplicationInfo {

    private Context mContext;

    private String mPackageName;
    private PackageInfo mPackageInfo;
    private PackageStats mPackageStats;

    public AFApplicationInfo(Context context) {
        mContext = context;

        mPackageName = context.getPackageName();
        try {
            mPackageInfo = context.getPackageManager().getPackageInfo(mPackageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        mPackageStats = new PackageStats(mPackageName);
    }

    public String getPackageName() {
        return mPackageName;
    }

    public PackageStats getPackageStats() {
        return mPackageStats;
    }

    public String getVersionCode() {
        return String.valueOf(mPackageInfo.versionCode);
    }

    public String getVersionName() {
        return String.valueOf(mPackageInfo.versionName);
    }

    public boolean isInstalledOnExternalStorage() throws Exception {
        ApplicationInfo applicationInfo = mPackageInfo.applicationInfo;

        return (applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE;
    }

    @TargetApi(9)
    public boolean isInstalledOnRemovableExternalStorage() throws Exception {
        return isInstalledOnExternalStorage() && Environment.isExternalStorageRemovable();
    }

    public String getMainStoragePath() {
        return getAllStorageSpaces().get(0).getPath();
    }

    public ArrayList<StorageSpace> getAllStorageSpaces() {
        ArrayList<StorageSpace> storageSpaces = AFDeviceInfo.getStorageMounts();

        for (StorageSpace storageSpace : storageSpaces) {
            storageSpace.setPath(storageSpace.getPath() + "/Android/data/" + mPackageName);
        }

        storageSpaces.add(new StorageSpace(mContext.getFilesDir().getAbsolutePath(), false, false));

        return storageSpaces;
    }

    public int getRessourceId(String group, String name) {
        return mContext.getResources().getIdentifier(name, group, getPackageName());
    }

}
