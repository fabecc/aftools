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

package com.android.aft.AFApplicationHelper.activity;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.aft.AFApplicationHelper.manager.AFApplicationManager;
import com.android.aft.AFCoreTools.ApplicationTools;

public abstract class AFActivity extends Activity implements Observer {

    protected ArrayList<Observable> mLstObservables = new ArrayList<Observable>();

    private static boolean mActivityIsInBackground = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        addObservers();

        if (mActivityIsInBackground) {
            mActivityIsInBackground = false;
            AFApplicationManager mng = getApplicationManager();
            if (mng != null)
                mng.onForeground(getApplicationContext());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!mActivityIsInBackground && ApplicationTools.isApplicationGoingToBackground(getApplicationContext())) {
            mActivityIsInBackground = true;
            AFApplicationManager mng = getApplicationManager();
            if (mng != null)
                mng.onBackground(getApplicationContext());
        }

        removeObservers();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void addObservers() {
        for (final Observable obs : mLstObservables) {
            obs.addObserver(this);
        }
    }

    protected void removeObservers() {
        for (final Observable obs : mLstObservables) {
            obs.deleteObserver(this);
        }
    }

    @Override
    protected void onDestroy() {
        cleanUiElements();
        cleanDataComponents();
        super.onDestroy();
    }

    /**
     * Gets the root view as found in the XML file assigned with setContentView
     *
     * @return
     */
    protected View getActivityRootView() {
        return ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
    }

    /**
     * use this method to remove views created dynamically.
     */
    protected abstract void cleanUiElements();

    /**
     * use this method to clean adapters.
     */
    protected abstract void cleanDataComponents();

    /**
     * Return the application manager
     */
    protected abstract AFApplicationManager getApplicationManager();

    /**
     * Convenient method to display a toast. Default duration =
     * Toast.LENGTH_SHORT
     *
     * @param sText Text to display
     */
    protected void ShowToast(String sText) {
        Toast.makeText(getApplicationContext(), sText, Toast.LENGTH_SHORT).show();
    }
}
