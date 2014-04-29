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

package com.android.aft.AFAppManager.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.android.aft.AFAppManager.R;
import com.android.aft.AFAppSettings.AFAppSettingsAction;
import com.android.aft.AFAppSettings.AFAppSettingsApplication;
import com.android.aft.AFAppSettings.AFAppSettingsElement;
import com.android.aft.AFAppSettings.AFAppSettingsEntry;
import com.android.aft.AFAppSettings.AFAppSettingsTools;
import com.android.aft.AFCoreTools.DebugTools;

public class ConfigurationActivity extends SherlockActivity {

    // Parameter constant
    private static final String PARAM_APP_PACKAGE = "PARAM_APP_PKG";
    private static final String PARAM_APP_NAME = "PARAM_APP_NAME";
    private static final String PARAM_AUTHORITIES = "PARAM_AUTHORITIES";

    // Configurable application info
    private String mAppPackage;
    private String mAppName;
    private String mAppAuthorities;

    private boolean mIsDirty = false;

    // List of configuration entries
    private ArrayList<AFAppSettingsElement> mSettingElements;

    // Graphical widget
    private LinearLayout mConfigurationWidget;

    private interface DialogDismissedListener {
        public void onDismiss();
    }

    public static void display(ApplicationListActivity from, AFAppSettingsApplication app) {
        Intent i = new Intent(from, ConfigurationActivity.class);

        i.putExtra(PARAM_APP_PACKAGE, app.mPackage);
        i.putExtra(PARAM_APP_NAME, app.mName);
        i.putExtra(PARAM_AUTHORITIES, app.mAuthorities);

        from.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        mConfigurationWidget = (LinearLayout) findViewById(R.id.lstEntry);

        // Read parameter
        Intent i = getIntent();
        mAppPackage = i.getStringExtra(PARAM_APP_PACKAGE);
        mAppName = i.getStringExtra(PARAM_APP_NAME);
        mAppAuthorities = i.getStringExtra(PARAM_AUTHORITIES);

        setTitle(mAppName);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load configuration
        readApplicationSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_config, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_reload:
                checkDirtyState(new DialogDismissedListener() {

                    @Override
                    public void onDismiss() {
                        readApplicationSettings();
                    }
                });
                return true;

            case R.id.button_save:
                saveConfiguration();
                return true;

            case R.id.button_launch:
                checkDirtyState(new DialogDismissedListener() {

                    @Override
                    public void onDismiss() {
                        runApplication();
                    }
                });
                return true;

            case R.id.button_uninstall:
                uninstallApplication();
                return true;

            case R.id.button_settings:
                // android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS = "android.settings.APPLICATION_DETAILS_SETTINGS";
                Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + mAppPackage));
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        checkDirtyState(new DialogDismissedListener() {

            @Override
            public void onDismiss() {
                // Default behavior
                finish();
            }
        });
    }

    private void checkDirtyState(final DialogDismissedListener oDialogDismissedListener) {
        if (mIsDirty) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.saveConfirm).setPositiveButton(R.string.saveConfirmYes, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Save changes
                    saveConfiguration();
                    oDialogDismissedListener.onDismiss();
                }
            }).setNegativeButton(R.string.saveConfirmNo, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Go on
                    oDialogDismissedListener.onDismiss();
                }
            }).setCancelable(true).show();
        } else {
            // No change to save call listener now
            oDialogDismissedListener.onDismiss();
        }
    }

    @TargetApi(8)
    @SuppressWarnings("deprecation")
    private void runApplication() {
        //
        // First kill the process
        //
        DebugTools.d("Try to kill running application");

        // Find the pid
        ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        int pid = -1;
        for (ActivityManager.RunningAppProcessInfo i : activityManager.getRunningAppProcesses())
            if (i.processName.equals(mAppPackage)) {
                pid = i.pid;
                break;
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO)
            activityManager.killBackgroundProcesses(mAppPackage);
        activityManager.restartPackage(mAppPackage);

        if (pid != -1) {
            android.os.Process.sendSignal(pid, android.os.Process.SIGNAL_KILL);
            android.os.Process.killProcess(pid);
        }

        //
        // Start application
        //
        DebugTools.d("Launch application");
        Intent intent = getPackageManager().getLaunchIntentForPackage(mAppPackage);
        startActivity(intent);
    }

    private void uninstallApplication() {
        startActivity(new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", mAppPackage, null)));
        finish();
    }

    private void readApplicationSettings() {
        mSettingElements = AFAppSettingsTools.loadConfigurationEntries(getApplicationContext(), mAppAuthorities);
        createConfigurationWidget();

        setClean();
    }

    @SuppressWarnings("unused")
    private void createConfigurationWidget() {
        // Clean previous widget
        mConfigurationWidget.removeAllViews();

        // Create all configuration entry
        for (final AFAppSettingsElement element : mSettingElements) {
            RelativeLayout w = null;

            // Create settings entry widget
            if (element instanceof AFAppSettingsEntry) {
                AFAppSettingsEntry entry = (AFAppSettingsEntry)element;

                // Set entry widget for multiple values
                if (entry.values != null)
                    w = createSelectorEntryWidget(entry);
                // Special case for boolean: generate a list of value with true / false
                else if (entry.type.equals(Boolean.TYPE)) {
                    DebugTools.d("Boolean current value: " + entry.value);
                    entry.values = new String[] { "true", "false" };
                    w = createSelectorEntryWidget(entry);
                }
                // Else, use default widget
                else
                    w = createSimpleEntryWidget(entry);
            }
            // Create action button widget
            else if (element instanceof AFAppSettingsAction) {
                w = createActionButtonWidget((AFAppSettingsAction)element);
            }

            if (w == null)
                continue;

            mConfigurationWidget.addView(w);
        }
    }

    private RelativeLayout createSelectorEntryWidget(final AFAppSettingsEntry entry) {
        RelativeLayout w = (RelativeLayout)getLayoutInflater().inflate(R.layout.setting_entry_multiple_values, null);
        ((TextView)w.findViewById(R.id.textEntryLabel)).setText(entry.name);

        Spinner valuesSelector = (Spinner)w.findViewById(R.id.editEntryValues);
        List<String> list = new ArrayList<String>();

        int i = 0;
        int selectedItemPos = 0;
        for (Object v : entry.values) {
            if (entry.value.equals(v.toString()))
                selectedItemPos = i;
            list.add(v.toString());
            i++;
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        valuesSelector.setAdapter(dataAdapter);
        valuesSelector.setSelection(selectedItemPos);

        valuesSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
            private boolean ignoreFirst = true;

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (ignoreFirst) {
                    ignoreFirst = false;
                } else {
                    setDirty();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        return w;
    }

    private RelativeLayout createSimpleEntryWidget(final AFAppSettingsEntry entry) {
        RelativeLayout w = (RelativeLayout) getLayoutInflater().inflate(R.layout.setting_entry_basic, null);

        ((TextView)w.findViewById(R.id.textEntryLabel)).setText(entry.name);
        ((EditText)w.findViewById(R.id.editEntryValue)).setText(entry.value.toString());

        if (entry.type.equals(Integer.TYPE))
            ((EditText)w.findViewById(R.id.editEntryValue)).setInputType(InputType.TYPE_CLASS_NUMBER);

        ((EditText)w.findViewById(R.id.editEntryValue)).addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setDirty();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return w;
    }

    private RelativeLayout createActionButtonWidget(final AFAppSettingsAction action) {
        RelativeLayout w = (RelativeLayout)getLayoutInflater().inflate(R.layout.setting_action, null);

        Button b = (Button)w.findViewById(R.id.btAction);
        b.setText(action.name);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AFAppSettingsTools.onAction(getApplicationContext(), mAppAuthorities, action);
                readApplicationSettings();
            }
        });

        return w;
    }

    protected void setDirty() {
        mIsDirty = true;

        // TODO Enable the save icon?
    }

    protected void setClean() {
        mIsDirty = false;

        // TODO Disable the save icon?
    }

    private void saveConfiguration() {
        DebugTools.d("Save project configuration");

        for (int i = 0; i < mSettingElements.size(); ++i) {
            AFAppSettingsElement element = mSettingElements.get(i);
            if (element instanceof AFAppSettingsEntry) {
                AFAppSettingsEntry entry = (AFAppSettingsEntry) element;

                RelativeLayout w = (RelativeLayout) mConfigurationWidget.getChildAt(i);

                String value = null;
                if (entry.values != null) // Multiple values selector
                    value = ((Spinner) w.findViewById(R.id.editEntryValues)).getSelectedItem().toString();
                else
                    // Simple value
                    value = ((EditText) w.findViewById(R.id.editEntryValue)).getText().toString();

                if (value == null)
                    continue;

                if (entry.type.equals(Integer.TYPE)) {
                    try {
                        Integer.valueOf(value);
                    } catch (Exception e) {
                        DebugTools.e("Cannot parse as integer value for '" + entry.name + "' setting", e);
                        Toast.makeText(getApplicationContext(),
                                "Cannot parse int value for '" + entry.name + "' setting entry", Toast.LENGTH_LONG)
                                .show();
                        continue;
                    }
                }

                entry.value = value;
            }
        }

        AFAppSettingsTools.saveConfigurationEntries(getApplicationContext(), mAppAuthorities, mSettingElements);

        // Not dirty anymore
        setClean();
    }

}
