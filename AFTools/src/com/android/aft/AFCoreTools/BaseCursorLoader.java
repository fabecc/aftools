package com.android.aft.AFCoreTools;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;

public abstract class BaseCursorLoader extends CursorLoader {

    public BaseCursorLoader(Context context) {
        super(context, null, null, null, null, null);
    }

    @Override
    public abstract Cursor loadInBackground();

}
