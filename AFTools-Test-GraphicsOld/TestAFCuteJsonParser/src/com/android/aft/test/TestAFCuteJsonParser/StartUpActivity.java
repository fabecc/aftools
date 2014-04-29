package com.android.aft.test.TestAFCuteJsonParser;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.android.aft.AFCoreTools.DebugTools;
import com.android.aft.AFCuteJsonParser.AFArrayJsonAction;
import com.android.aft.AFCuteJsonParser.AFCuteJsonParser;
import com.android.aft.AFCuteJsonParser.AFCuteJsonParserResult;
import com.android.aft.AFCuteJsonParser.AFJsonValue;
import com.android.aft.AFCuteJsonParser.AFJsonValue.JsonValueType;
import com.android.aft.AFCuteJsonParser.AFObjectJsonAction;
import com.android.aft.AFCuteJsonParser.AFRootObjectJsonAction;
import com.android.aft.test.TestAFCuteJsonParser.R;


public class StartUpActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Init debug
        DebugTools.init("TestNFCuteJsonParser");

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                launchParsing();
            }
        });
    }

    private void launchParsing() {
        AFCuteJsonParser parser = getParser();

        AFCuteJsonParserResult result = parser.parse(getResources().openRawResource(R.raw.test1));
        DebugTools.d("Json parsing " + (result.status() ? "Success" : "Failed") + " - " + result.getData());
    }


    private AFCuteJsonParser getParser() {
        // Create the parser
        AFCuteJsonParser parser = new AFCuteJsonParser();

        // Add an action on root object
        parser.addAction(new AFRootObjectJsonAction<Object>() {
            @Override
            public void onObject(Object cookie) {
                DebugTools.d("On root object with:");

                AFJsonValue value;

                // Try to get an no existing value
                value = getValueOf("foo");
                DebugTools.d(" - 'foo': " + (value == null ? "(null)" : value));

                // Try to get an int value
                value = getValueOf("root_value");
                DebugTools.d(" - 'root_value': " + (value == null ? "(null)" : value));

                // Try to get an object value
                value = getValueOf("menu");
                DebugTools.d(" - 'menu': " + (value == null ? "(null)" : value));

                // Read all object values
                read_values();
            }
        });

        // Add an action on root object
        parser.addAction(new AFObjectJsonAction<Object>("menu") {
            @Override
            public void onObject(Object cookie) {
                DebugTools.d("On menu object with:");

                getContext().getResult().setData(new Integer(getIntValueOf("id")));

                AFJsonValue value_ici = getValueOf("ici");
                if (value_ici.getType() == JsonValueType.JsonNull)
                	;
                else
                	;
            }
        });

        // Add an action on the array 'menuitem'
        parser.addAction(new AFArrayJsonAction<Object>("menuitem") {
            @Override
            public void onArray(Object cookie) {
                DebugTools.d("On 'menuitem' array with " + getNumberOfEntry() + " elements");

                // Iterate on each value in array
                int i = 0;
                for (AFJsonValue value: getValues()) {
                    DebugTools.d(" - [" + i + "]: " + (value == null ? "(null)" : value));

                    if (value.getType() == AFJsonValue.JsonValueType.JsonNull)
                        ;

                    ++i;
                }

                // Try to get an no existing value
                AFJsonValue value = getValue(10);
                DebugTools.d(" - [10]: " + (value == null ? "(null)" : value));

                // Read all array values
                read_values();
            }
        });

        return parser;
    }

}
