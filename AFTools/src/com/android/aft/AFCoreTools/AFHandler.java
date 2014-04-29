package com.android.aft.AFCoreTools;

import android.content.Context;
import android.os.Handler;

public class AFHandler extends Handler {

    protected Context mContext;

    public void setContext(Context ctx) {
        mContext = ctx;
    }

}
