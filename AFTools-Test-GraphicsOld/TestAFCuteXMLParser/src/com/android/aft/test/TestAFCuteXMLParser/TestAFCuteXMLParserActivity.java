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

package com.android.aft.test.TestAFCuteXMLParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.aft.AFCoreTools.DebugTools;
import com.android.aft.AFCoreTools.DebugTools.TimeLogger;
import com.android.aft.AFCuteXmlParser.AFCuteXmlParser;
import com.android.aft.AFCuteXmlParser.AFCuteXmlParserResult;
import com.android.aft.AFCuteXmlParser.LowParser.PullXmlParser.AFPullXmlLowXmlParser;


public class TestAFCuteXMLParserActivity extends Activity {

    // private ArrayList<Category> mDatas = new ArrayList<Category>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        DebugTools.init("TestNFCuteXmlParser");
        DebugTools.v("Application started");
        AFCuteXmlParser.hasDebug = true;


//        parse("Test pds", R.raw.test_pds);
//        parse("Test unit", R.raw.test_unit);
//        parse("Test simple", R.raw.test_simple);
        parse("Test catagory small", R.raw.test_category_small);
//        parse("Test category", R.raw.test_category);
//        parse("Test catalogue small", R.raw.cataloguecent);
//        parse("Test catalogue", R.raw.catalogue);
//        parse("Test category", R.raw.test_encoding);
//        parse("Test xml with double name", R.raw.test_category_double_name);
    }

    private void parse(String name, int id) {
        InputStream data = getResources().openRawResource(id);

        InputStreamReader data_r = null;
        try {
            data_r = new InputStreamReader(data);
        } catch (Exception e) {
            DebugTools.e("Encoding error", e);
            return;
        }

        // Set the current low level parser
        AFCuteXmlParser.parser = new AFPullXmlLowXmlParser();

        // Create the parser
        ProductParser parser = new ProductParser();

        // Starting
        DebugTools.TimeLogger tm = new TimeLogger("Parsing '" + name + "'");
        AFCuteXmlParserResult result = parser.parse(data_r);
        tm.finish("Status: " + (result.status() ? "Ok" : "Ko"));

        Object parse_data = result.getData();
        if (parse_data instanceof String)
            ((TextView) findViewById(R.id.txtData)).setText((String)result.getData());
        else if (parse_data instanceof ArrayList<?>) {
            ArrayList<?> array = (ArrayList<?>)parse_data;
            if (array.size() == 0)
                DebugTools.e("No categories");

            if (array.get(0) instanceof Category) {
                @SuppressWarnings("unchecked")
                ArrayList<Category> categories = (ArrayList<Category>)parse_data;
                for (Category c : categories)
                    DebugTools.dump(c);
            }
        }
    }

}
