package com.android.aft.AFDatabase;

import java.util.Date;

import android.database.Cursor;

public class AFDbField {

    // Data
    private String mTableName;
    private String mName;
    private String mType;
    private boolean mIsDBIndex;
    private int mIdx = 0;

    public AFDbField(String tableName, String name, String type) {
        this(tableName, name, type, false);
    }

    public AFDbField(String tableName, String name, String type, boolean isDBIndex) {
        mTableName = tableName;
        mName = name;
        mType = type;
        mIsDBIndex = isDBIndex;
    }

    public String getTableName() {
        return mTableName;
    }

    public void setTableName(String tableName) {
        mTableName = tableName;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getFullname() {
        return mTableName + "." + mName;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public boolean isDBIndex() {
        return mIsDBIndex;
    }

    public int getIdx() {
        return mIdx;
    }

    public void setIdx(int idx) {
        mIdx = idx;
    }

    public int getInt(Cursor c, int shift) {
        if (c.isNull(mIdx))
            return 0;

        return c.getInt(mIdx + shift);
    }

    public long getLong(Cursor c, int shift) {
        if (c.isNull(mIdx))
            return 0;

        return c.getLong(mIdx + shift);
    }

    public String getString(Cursor c, int shift) {
        if (c.isNull(mIdx))
            return null;

        return c.getString(mIdx + shift);
    }

    public Date getDate(Cursor c, int shift) {
        if (c.isNull(mIdx))
            return null;

        return new Date(getLong(c, shift));
    }

    public byte[] getBlob(Cursor c, int shift) {
        if (c.isNull(mIdx))
            return null;

        return c.getBlob(mIdx + shift);
    }

}
