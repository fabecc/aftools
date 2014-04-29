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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.apache.http.conn.util.InetAddressUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.android.aft.AFCoreTools.AFNetworkMonitoring;
import com.android.aft.AFCoreTools.AFNetworkMonitoring.NetworkMode;
import com.android.aft.AFCoreTools.DebugTools;
import com.android.aft.AFCoreTools.FileTools;
import com.android.aft.AFCoreTools.IoTools.MemoryUnit;

public class AFDeviceInfo {

    public static class StorageSpace {
        private String mPath;
        private boolean mIsExternal;
        private boolean mIsRemovable;

        public StorageSpace(String path, boolean isExternal, boolean isRemovable) {
            mPath = path;
            mIsExternal = isExternal;
            mIsRemovable = isRemovable;
        }

        public String getPath() {
            return mPath;
        }

        public void setPath(String path) {
            mPath = path;
        }

        public boolean isExternal() {
            return mIsExternal;
        }

        public boolean isRemovable() {
            return mIsRemovable;
        }

        @Override
        public String toString() {
            return "Stockage "
                    + (mIsExternal ? "externe " + (mIsRemovable ? "amovible " : "inamovible ") : "interne ") + ": "
                    + getPath();
        }
    }

    public enum DeviceType {
        SMARTPHONE, TABLET
    }

    private Context mContext;

    private AFNetworkMonitoring mNetworkMonitoring;
    private float mDensity;
    private int mWidth;
    private int mHeight;
    private DeviceType mType;

    public AFDeviceInfo(Context context) {
        mContext = context;

        mNetworkMonitoring = new AFNetworkMonitoring(mContext);
        mDensity = context.getResources().getDisplayMetrics().density;
        mWidth = context.getResources().getDisplayMetrics().widthPixels;
        mHeight = context.getResources().getDisplayMetrics().heightPixels;
        mType = isATablet() ? DeviceType.TABLET : DeviceType.SMARTPHONE;
    }

    public float getDensity() {
        return mDensity;
    }

    public boolean isLDPI() {
        return mDensity == DisplayMetrics.DENSITY_LOW;
    }

    public boolean isMDPI() {
        return mDensity == DisplayMetrics.DENSITY_MEDIUM;
    }

    public boolean isHDPI() {
        return mDensity == DisplayMetrics.DENSITY_HIGH;
    }

    public boolean isXHDPI() {
        // BCU: replace by numeric value to be 2.2 compliant (DENSITY_XHIGH is
        // 2.3)
        return mDensity == 320; // DisplayMetrics.DENSITY_XHIGH;
    }

    /**
     * Check whether the device is a tablet or not.
     *
     * @return true if device is a tablet, false otherwise
     */
    @TargetApi(9)
    public boolean isATablet() {
        // Test sdk < 10 => No tablet
        if (android.os.Build.VERSION.SDK_INT < 10)
            return false;

        // Test telephony feadture: No phone => Not a smartphone so it is a
        // tablet
        if (!hasTelephonyFeature())
            return true;

        // Test screen layout in x-large
        int layout = mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if (layout == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            return true;

        // So, arrive here, it is not a tablet
        return false;
    }

    public int getScreenWidth() {
        return mWidth;
    }

    public int getScreenHeight() {
        return mHeight;
    }

    public int getCurrentWidth() {
        return mContext.getResources().getDisplayMetrics().widthPixels;
    }

    public int getCurrentHeight() {
        return mContext.getResources().getDisplayMetrics().heightPixels;
    }

    public DeviceType getType() {
        return mType;
    }

    public int getAPILevel() {
        return android.os.Build.VERSION.SDK_INT;
    }

    public String getManufacturer() {
        return android.os.Build.MANUFACTURER;
    }

    public String getModel() {
        return android.os.Build.MODEL;
    }

    public String getCarrier() {
        return android.os.Build.BRAND;
    }

    public String getIMEI() {
        try {
            return ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            DebugTools.e("Impossible to read IMEI : check the READ_PHONE_STATE permission");
            return null;
        }
    }

    private static final String FAKE_ANDROID_ID = "9774d56d682e549c";

    /**
     * Read Secure.ANDROID_ID property.
     * @return Secure.ANDROID_ID property or null if data is empty or not valid.
     */
    public String getDeviceId() {
        String androidID = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);

        if (TextUtils.isEmpty(androidID)) {
            DebugTools.e("Impossible to read device Id");
            return null;
        } else if(androidID.equals(FAKE_ANDROID_ID)) {
            DebugTools.e("Device Id is the Fake Android Id, it should not be used (not unique): return null");
            return null;
        }

        return androidID;
    }

    /**
     * Get the MAC address of the wifi interface
     * Request permission android.permission.ACCESS_WIFI_STATE.
     * @return the MAC address of the device
     */
    public String getWifiMacAddress() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();

        return macAddress;
    }

    /**
     * Get the Mac address of eth0 interface
     * @return the eth0 interface MAC address of the device
     */
    public String getEth0MacAddress() {
        try {
            return FileTools.loadFileContent("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
        } catch (IOException e) {
            DebugTools.e("Failed to read eth0 Mac address", e);
            return null;
        }
    }

    /**
     * Get IP address from first non-localhost interface
     * @param ipv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public String getIPAddress(boolean useIPv4) {
        try {
            for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(intf.getInetAddresses())) {
                    if (addr.isLoopbackAddress())
                        continue ;

                    String sAddr = addr.getHostAddress().toUpperCase();

                    boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                    if (isIPv4 && !useIPv4)
                        continue ;
                    else if (isIPv4 && useIPv4)
                        return sAddr;
                    else if (!isIPv4 && useIPv4)
                        continue ;
                    else if (!isIPv4 && !useIPv4)
                    {
                        int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                        return delim < 0 ? sAddr : sAddr.substring(0, delim);
                    }
                }
            }
        }
        catch (Exception e) {
            DebugTools.e("Failed to get Ip addresses", e);
        }
        return null;
    }

    public String getLineNumber() {
        try {
            return ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
        } catch (Exception e) {
            DebugTools.e("Impossible to read line number : check the READ_PHONE_STATE permission");
            return null;
        }
    }

    public boolean hasTelephonyFeature() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
    }

    public static long getTotalStorageSpace(String directoryPath, MemoryUnit memoryUnit) {
        try {
            StatFs stat = new StatFs(directoryPath);

            return (long) stat.getBlockCount() * (long) stat.getBlockSize() / memoryUnit.getValue();
        } catch (IllegalArgumentException exception) {
            DebugTools.e("Failed to read total storage space : chech " + directoryPath + " exists");

            return 0;
        }
    }

    public static long getTotalInternalStorageSpace(MemoryUnit memoryUnit) {
        return getTotalStorageSpace(Environment.getDataDirectory().getAbsolutePath(), memoryUnit);
    }

    public static long getTotalExternalStorageSpace(MemoryUnit memoryUnit) {
        return getTotalStorageSpace(Environment.getExternalStorageDirectory().getAbsolutePath(), memoryUnit);
    }

    public static long getAvailableStorageSpace(String directoryPath, MemoryUnit memoryUnit) {
        try {
            StatFs stat = new StatFs(directoryPath);

            return (long) stat.getAvailableBlocks() * (long) stat.getBlockSize() / memoryUnit.getValue();
        } catch (IllegalArgumentException exception) {
            DebugTools.e("Failed to read available storage space : chech " + directoryPath + " exists");

            return 0;
        }
    }

    public static long getAvailableInternalStorageSpace(MemoryUnit memoryUnit) {
        return getAvailableStorageSpace(Environment.getDataDirectory().getAbsolutePath(), memoryUnit);
    }

    public static long getAvailableExternalStorageSpace(MemoryUnit memoryUnit) {
        return getAvailableStorageSpace(Environment.getExternalStorageDirectory().getAbsolutePath(), memoryUnit);
    }

    /**
     * Compute the used storage space for a given directory. WARNING: as this
     * method relies on StatFs, the computed size is related to the partition
     * not to the single provided directory. See FileUtils.getDirectorySize() to
     * compute the size of a single directory.
     *
     * @param directoryPath The directory to compute the used storage space of.
     * @param memoryUnit The memory unit used to compute the size.
     * @return
     */
    public static long getUsedStorageSpace(String directoryPath, MemoryUnit memoryUnit) {
        return getTotalStorageSpace(directoryPath, memoryUnit) - getAvailableStorageSpace(directoryPath, memoryUnit);
    }

    public static long getUsedInternalStorageSpace(MemoryUnit memoryUnit) {
        return getUsedStorageSpace(Environment.getDataDirectory().getAbsolutePath(), memoryUnit);
    }

    public static long getUsedExternalStorageSpace(MemoryUnit memoryUnit) {
        return getUsedStorageSpace(Environment.getExternalStorageDirectory().getAbsolutePath(), memoryUnit);
    }

    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    @TargetApi(9)
    public static boolean isRemovableExternalStorageAvailable() throws Exception {
        return Environment.isExternalStorageRemovable()
                && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private static final String DEFAULT_MOUNT_PATH = "/mnt/sdcard";
    private static final String MOUNTS_FILE_PATH = "/proc/mounts";
    private static final String VOLUMES_FILE_PATH = "/system/etc/vold.fstab";
    private static final String MOUNT_LINE_PREFIX = "/dev/block/vold/";
    private static final String VOLUME_LINE_PREFIX = "dev_mount";

    @TargetApi(9)
    public static String getPrefferedStoragePath() {
        String prefferedStoragePath = null;

        ArrayList<StorageSpace> storageMounts = getStorageMounts();

        if (storageMounts.size() == 1) {
            prefferedStoragePath = storageMounts.get(0).getPath();
        } else {
            for (StorageSpace storageMount : storageMounts) {
                if (storageMount.isRemovable()) {
                    prefferedStoragePath = storageMount.getPath();
                    break;
                }
            }

            if (prefferedStoragePath == null) {
                prefferedStoragePath = storageMounts.get(0).getPath();
            }
        }

        return prefferedStoragePath;
    }

    @TargetApi(9)
    public static ArrayList<StorageSpace> getStorageMounts() {
        ArrayList<String> mounts = getMounts();
        mounts.retainAll(getVolumes());

        filterWritableMounts(mounts);

        return createStorageSpaces(mounts);
    }

    @TargetApi(9)
    public static ArrayList<StorageSpace> getStorageSpaces() {
        ArrayList<StorageSpace> storagesSpaces = getStorageMounts();

        storagesSpaces.add(0, new StorageSpace(Environment.getDataDirectory().getAbsolutePath(), false, false));

        return storagesSpaces;
    }

    private static ArrayList<String> getMounts() {
        ArrayList<String> mounts = new ArrayList<String>();

        mounts.add(DEFAULT_MOUNT_PATH);

        try {
            Scanner scanner = new Scanner(new File(MOUNTS_FILE_PATH));

            while (scanner.hasNext()) {
                String line = scanner.nextLine();

                if (line.startsWith(MOUNT_LINE_PREFIX)) {
                    String[] lineElements = line.split(" ");
                    String element = lineElements[1];

                    if (!element.equals(DEFAULT_MOUNT_PATH)) {
                        mounts.add(element);
                    }
                }
            }
        } catch (Exception e) {
            DebugTools.e("Error while reading " + MOUNTS_FILE_PATH);
            e.printStackTrace();
        }

        return mounts;
    }

    private static ArrayList<String> getVolumes() {
        ArrayList<String> volumes = new ArrayList<String>();

        volumes.add(DEFAULT_MOUNT_PATH);

        try {
            Scanner scanner = new Scanner(new File(VOLUMES_FILE_PATH));

            while (scanner.hasNext()) {
                String line = scanner.nextLine();

                if (line.startsWith(VOLUME_LINE_PREFIX)) {
                    String[] lineElements = line.split(" ");
                    String element = lineElements[2];

                    if (element.contains(":")) {
                        element = element.substring(0, element.indexOf(":"));
                    }

                    if (!element.equals(DEFAULT_MOUNT_PATH)) {
                        volumes.add(element);
                    }
                }
            }
        } catch (Exception e) {
            DebugTools.e("Error while reading " + VOLUMES_FILE_PATH);
            e.printStackTrace();
        }

        return volumes;
    }

    private static void filterWritableMounts(ArrayList<String> mounts) {
        for (int i = 0; i < mounts.size(); i++) {
            File mountRoot = new File(mounts.get(i));

            if (!mountRoot.exists() || !mountRoot.isDirectory() || !mountRoot.canWrite())
                mounts.remove(i--);
        }
    }

    @TargetApi(9)
    private static ArrayList<StorageSpace> createStorageSpaces(ArrayList<String> mounts) {
        ArrayList<StorageSpace> storageSpaces = new ArrayList<AFDeviceInfo.StorageSpace>();

        if (mounts.size() > 0) {
            if (Environment.isExternalStorageRemovable()) {
                storageSpaces.add(new StorageSpace(mounts.get(0), true, true));
            } else {
                storageSpaces.add(new StorageSpace(mounts.get(0), true, false));
            }

            for (int i = 1; i < mounts.size(); i++) {
                storageSpaces.add(new StorageSpace(mounts.get(i), true, true));
            }
        }

        return storageSpaces;
    }

    public AFNetworkMonitoring getNetworkMonitoring() {
        return mNetworkMonitoring;
    }

    public boolean isConnected() {
        return mNetworkMonitoring.mode != NetworkMode.NotConnected;
    }

    public boolean isConnectedOnWifi() {
        return mNetworkMonitoring.mode == NetworkMode.ConnectedToWifi;
    }

    public boolean isConnectedOn3G() {
        return mNetworkMonitoring.mode == NetworkMode.ConnectedTo3G;
    }

    public boolean isAirplaneModeOn() {
        return Settings.System.getInt(mContext.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }
}
