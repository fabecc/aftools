package com.android.aft.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.android.aft.AFCoreTools.StringTools;


public class StartUpActivity extends Activity {

    private final static String TAG = "AFToolsTest";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG, "Start Test App");

        test('c', true);
        test(';', false);
    }

    public void test(char c, boolean isAlNum) {
        boolean res = StringTools.isAlNum(c);
        if (res ^ isAlNum)
            Log.d(TAG, "Test failed: char '" + c + "' extected to " + (isAlNum ? "" : "not ") + "be a alpha numeric char");
        else
            Log.d(TAG, "Test ok for '" + c + "'");
    }

}
