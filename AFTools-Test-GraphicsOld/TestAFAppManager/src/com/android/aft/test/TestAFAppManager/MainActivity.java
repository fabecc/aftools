package com.android.aft.test.TestAFAppManager;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.aft.AFCoreTools.DebugTools;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DebugTools.init("TestConfigApp");

		((TextView)findViewById(R.id.config_host)).setText("Host: " + Settings.host);
		((TextView)findViewById(R.id.config_port)).setText("Port: " + Settings.port);
        ((TextView)findViewById(R.id.config_request)).setText("Request: " + Settings.request);
        ((TextView)findViewById(R.id.config_user_id)).setText("User Id: " + Settings.user_id);
        ((TextView)findViewById(R.id.config_mode)).setText("Mode: " + Settings.mode);
        ((TextView)findViewById(R.id.config_connect_at_start)).setText("ConnectAtStart: " + Settings.connect_at_start);
	}

}
