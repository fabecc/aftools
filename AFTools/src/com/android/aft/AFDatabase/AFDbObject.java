package com.android.aft.AFDatabase;

import java.util.HashSet;
import java.util.Observable;

public class AFDbObject<TableType> extends Observable {

    // Link to table
    protected TableType mTable;

    protected HashSet<AFDbField> mUpdateFields;

    // Uniq id
    protected long mId;

    // Cstr
    public AFDbObject(TableType table) {
        this(table, -1);
    }

    public AFDbObject(TableType table, int id) {
        mTable = table;
        mId = id;
    }

    // Accessor
    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    //
    // Update field tag management
    //
    public synchronized void setUpdateField(AFDbField field) {
        if (mUpdateFields == null)
            mUpdateFields = new HashSet<AFDbField>();

        mUpdateFields.add(field);
    }

    public synchronized void resetUpdateField() {
        if (mUpdateFields != null)
            mUpdateFields.clear();

        mUpdateFields = null;
    }

    public synchronized boolean hasUpdate(AFDbField field) {
        if (mUpdateFields == null)
            return false;

        return mUpdateFields.contains(field);
    }

}
