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

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.android.aft.AFAppManager.R;
import com.android.aft.AFAppManager.model.ApplicationBuildInfo;
import com.android.aft.AFAppManager.model.DeliveryInfo;
import com.android.aft.AFAppSettings.AFAppSettingsApplication;
import com.android.aft.AFAppSettings.AFAppSettingsTools;
import com.android.aft.AFCoreTools.DebugTools;
import com.android.aft.build.BuildInfo;

public class ApplicationListActivity extends SherlockActivity {

	// Data
	private ArrayList<AFAppSettingsApplication> mApplications;

	// Graphical compoment
	private ListView mList;
	private ApplicationListAdater mAdapter;

	// Build information of the current NijiConfig version in Jenkins
	private ApplicationBuildInfo mAFAppConfigInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_application_list);

		// Init debug module
		DebugTools.init("AFAppManager");

		// Get configurable application
		mApplications = AFAppSettingsTools.getConfigurableAppByNFSetting(getApplicationContext());

		// Set list
		mList = (ListView)findViewById(R.id.listApplication);
		mList.setEmptyView(findViewById(R.id.emptyList));
		mAdapter = new ApplicationListAdater();
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				ConfigurationActivity.display(ApplicationListActivity.this, mApplications.get(position));
			}
		});
	}

	@Override
	protected void onResume() {
	    super.onResume();

        // This previous code was when the application run under Niji and its build system
//	    // Get current NijiConfig info
//	    GetApplicationBuildInfoLoader loader = new GetApplicationBuildInfoLoader(AMConfig.NIJI_CONFIG_PROJECT_DIR);
//	    loader.loadAsync(getApplicationContext(), new GetApplicationBuildInfoLoader.IDMDataListener<ApplicationBuildInfo>() {
//            @Override
//            public void onDataAvailable(ApplicationBuildInfo data) {
//                setNijiConfigBuildInfo(data);
//            }
//
//            @Override
//            public void onError(ApplicationBuildInfo data, int errCode, String errMsg) {
//                DebugTools.e("Failed to get NijiConfig info");
//            }
//        });
	}

    private void setNijiConfigBuildInfo(ApplicationBuildInfo info) {
        DebugTools.d("Get NijiConfig info done");
        DebugTools.dump(info);

        mAFAppConfigInfo = info;

        if (hasNewNijiConfigVersionAvailable()) {
            DebugTools.i("There a available version of NijiConfig (rev:" + info.revision + ")");
            supportInvalidateOptionsMenu();
        }
    }

    private boolean hasNewNijiConfigVersionAvailable() {
        if (mAFAppConfigInfo == null)
            return false;

        if (BuildInfo.rev.equals(mAFAppConfigInfo.revision))
            return false;

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(hasNewNijiConfigVersionAvailable() ?
                                         R.menu.menu_list_application_with_update :
                                         R.menu.menu_list_application,
                                         menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_reload:
                refreshApplicationList();
                return true;
            case R.id.button_update:
                updateNijiConfig();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshApplicationList() {
        mApplications = AFAppSettingsTools.getConfigurableAppByNFSetting(getApplicationContext());

        mAdapter.notifyDataSetChanged();
    }

    private void updateNijiConfig() {
        if (mAFAppConfigInfo == null)
            return ;

        DeliveryInfo nijiConfigDelivery = null;

        // Find the wanted version of NijiConfig
        ArrayList<DeliveryInfo> deliveries = mAFAppConfigInfo.mDeliveries;
        if (deliveries != null) {
            for (DeliveryInfo d: deliveries)
                if (d.mode.equals("Release")) {
                    nijiConfigDelivery = d;
                    break ;
                }
        }

        if (nijiConfigDelivery == null) {
            DebugTools.dt(this, "There is no release version of NijiConfig");
            return ;
        }

        startActivity(new Intent("android.intent.action.VIEW", Uri.parse(nijiConfigDelivery.getApkUrl())));
    }

	/**
	 * Adapter class for application list
	 */
	private class ApplicationListAdater extends BaseAdapter {

		@Override
		public int getCount() {
			if (mApplications == null)
				return 0;
			return mApplications.size();
		}

		@Override
		public Object getItem(int position) {
			return mApplications.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = getLayoutInflater().inflate(R.layout.application_item, null);

			AFAppSettingsApplication app = (AFAppSettingsApplication)getItem(position);

			// Get extra application info
			Drawable icon = null;
			String versionName = null;
			int versionCode = -1;
			try {
			    icon = getPackageManager().getApplicationIcon(app.mPackage);
			    versionName = getPackageManager().getPackageInfo(app.mPackage, 0 ).versionName;
			    versionCode = getPackageManager().getPackageInfo(app.mPackage, 0 ).versionCode;
			} catch (NameNotFoundException e) {
			    // This exception may never append
			}

            if (icon != null)
                ((ImageView)convertView.findViewById(R.id.imgAppEntry)).setImageDrawable(icon);
            else
                ((ImageView)convertView.findViewById(R.id.imgAppEntry)).setImageResource(R.drawable.no_icon);
            ((TextView)convertView.findViewById(R.id.textAppEntry)).setText(app.mName);
            ((TextView)convertView.findViewById(R.id.textAppExtraEntry)).setText("Version " + versionName
                                                                                 + " (code: " + versionCode + ")");

			return convertView;
		}

	}

}
