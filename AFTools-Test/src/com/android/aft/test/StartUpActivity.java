package com.android.aft.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


public class StartUpActivity extends Activity {

    private final static String TAG = "AFToolsTest";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG, "Start Test App");
    }

}
