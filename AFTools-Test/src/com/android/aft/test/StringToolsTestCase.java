package com.android.aft.test;

import android.test.AndroidTestCase;

import com.android.aft.AFCoreTools.StringTools;

public class StringToolsTestCase extends AndroidTestCase {

    public void testIsAlnum() {
        assertEquals(true, StringTools.isAlNum('a'));
        assertEquals(true, StringTools.isAlNum('5'));
        assertEquals(true, StringTools.isAlNum('H'));
        assertEquals(false, StringTools.isAlNum(';'));
    }

}
