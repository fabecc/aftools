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

package com.android.aft.AFCoreTools;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.PowerManager;
import android.text.Html;

public class ApplicationTools {

    public static boolean isApplicationIsRunning(Context ctx, String applicationPkg) {
        ActivityManager activityManager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++)
            if (procInfos.get(i).processName.equals(applicationPkg))
                return true;

        return false;
    }

    // Disable the lock screen
    @SuppressWarnings("deprecation")
    public static void disableKeyguard(Context ctx)
    {
        try {
            KeyguardManager keyguardManager = (KeyguardManager)ctx.getSystemService(Activity.KEYGUARD_SERVICE);
            KeyguardLock lock = keyguardManager.newKeyguardLock(Context.KEYGUARD_SERVICE);
            lock.disableKeyguard();
        } catch(Exception e) {
            DebugTools.e("Failed to disable lock screen");
        }
    }

    public static String getApplicationVersion(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            DebugTools.e("Cannot get application version", e);
        }

        return "";
    }

    public static int getApplicationVersionCode(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0 ).versionCode;
        }
        catch (NameNotFoundException e) {
            DebugTools.e("Cannot get application version code", e);
        }

        return 0;
    }

    // Return true if the package pkg has the permissions declared in
    // uses-permission tag
    public static boolean hasPermission(Context ctx, String pkg, String permission) {
        PackageInfo pkgInfo;
        try {
            pkgInfo = ctx.getPackageManager().getPackageInfo(pkg, PackageManager.GET_PERMISSIONS);
        } catch (NameNotFoundException e) {
            DebugTools.e("Cannot get package information for '" + pkg + "'", e);
            return false;
        }

        return hasPermission(pkgInfo, permission);
    }

    public static Intent getIntentGoToMarket(final Context ctx) {
        return getIntentToOpenGooglePlayOnApp(ctx.getApplicationContext().getPackageName());
    }

    public static Intent getIntentToOpenGooglePlayOnApp(String pkg_uri) {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:" + pkg_uri));
    }

    // Return true if pkg has permission declared in uses-permission tag
    public static boolean hasPermission(PackageInfo pkg, String permission) {
        if (pkg == null)
            return false;

        if (pkg.requestedPermissions == null)
            return false;

        for (String p : pkg.requestedPermissions)
            if (p.equals(permission))
                return true;

        return false;
    }

    // Test if an application is installed
    public static boolean isPackageInstalled(Context ctx, String uri) {
        PackageManager pm = ctx.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    /**
     * Checks if the application is in the background (i.e behind another
     * application's Activity).
     *
     * @param activity
     * @return true if another application is above this one.
     */
    public static boolean isApplicationGoingToBackground(Context ctx) {
        // Check screen state: if it is Off consider application is on wake
        PowerManager pmng = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        if (!pmng.isScreenOn())
            return true;

        // Check running task activity to check if an activity (other than this pkg) is in top
        final ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            final String topActivityPkg = tasks.get(0).topActivity.getPackageName();
            final String myPkg = ctx.getPackageName();

            return !topActivityPkg.equals(myPkg);
        }

        return false;
    }

    /**
     * Return true if there is only one activity for this task
     */
    public static boolean isLastActivityFinishing(Context ctx) {
        ActivityManager actM = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = actM.getRunningTasks(1);
        int iNumActivity = tasks.get(0).numActivities;

        return iNumActivity == 1;
    }

    public static void launchEmailWithIntent(String titleAction, Activity activity, String mailTo, String subject,
            String text) {
        final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mailTo));
        intent.putExtra(Intent.EXTRA_TEXT, text);
        if (subject != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        activity.startActivity(Intent.createChooser(intent, titleAction));
    }

    public static void launchHtmlEmailWithIntent(String titleAction, Activity activity, String mailTo, String subject,
            String text) {
        final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mailTo));
        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(text));
        if (subject != null) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        activity.startActivity(Intent.createChooser(intent, titleAction));
    }
}
