package com.android.aft.AFDatabase;

import java.util.ArrayList;
import java.util.Collection;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.android.aft.AFCoreTools.DebugTools;

public abstract class AFDbTable<ObjectType extends AFDbObject<?>> {

    public static long ID_NOT_SET = -1;

    // Same ID than SQLite default one
    protected static final String TBL_FIELD_ID = "_id";

    // Link to db helper
    private AFDbHelper mNfDbHelper;

    // Name of the table
    protected String mTableName;

    // List of field
    protected ArrayList<AFDbField> mFields;

    // Computed array of field for select query
    // It is generate for the first query then buffered here to avoid to much array creation
    protected String[] mFieldsArray = null;

    // Special field ID for primary key
    public AFDbField mFieldId;

    public static String[] ID_PROJECTION = new String[] {
        TBL_FIELD_ID
    };

    /**
     * Construct a NFDbTable
     *
     * @param name
     *          Name of the name
     *
     * @param create_id
     *          True to create the first default field ID as primary key
     */
    public AFDbTable(String name, boolean create_id) {
        mTableName = name;
        mFields = new ArrayList<AFDbField>();

        if (create_id)
            mFieldId = addField(TBL_FIELD_ID, "INTEGER PRIMARY KEY AUTOINCREMENT");
    }

    /**
     * Table creation
     *
     * @param database
     */
    public void onCreate(SQLiteDatabase database) {

        if (mFields.size() == 0) {
            mNfDbHelper.dbg.e("Cannot create table '" + getTableName() + "': field list is empty");
            return ;
        }

        // Create table
        StringBuilder request = new StringBuilder();

        request.append("CREATE TABLE IF NOT EXISTS " + getTableName() + " (");
        boolean isFirst = true;
        for (AFDbField f: mFields) {
            if (isFirst)
                isFirst = false;
            else
                request.append(",");
            request.append(f.getName() + " " + f.getType());
        }
        request.append(");");

        database.execSQL(request.toString());

        // Create index
        for (AFDbField f: mFields)
            if (f.isDBIndex())
                createIndex(database, f);
    }

    // Create an index
    private void createIndex(SQLiteDatabase database, AFDbField field) {
        final StringBuffer query = new StringBuffer();

        query.append("create index idx_")
             .append(getTableName().toLowerCase())
             .append('_')
             .append(field.getName())
             .append(" on ")
             .append(getTableName())
             .append(" (")
             .append(field.getName())
             .append(");");

        database.execSQL(query.toString());
    }

    // Inherit to update table
    public abstract void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion);

    /**
     * Check if the table is empty
     * @return
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Return the number of element in the table
     * @return
     */
    public int size() {
        Cursor c = getWritableDatabase().query(getTableName(), getFields(), null, null, null, null, null);

        if (c == null)
            return 0;

        int size = c.getCount();

        c.close();

        return size;
    }

    /**
     * @return The table access Uri
     */
    public Uri getTableUri() {
        return Uri.parse("content://" + AFDbProvider.AUTHORITY + "/" + getTableName());
    }

    /**
     * Dump all the table data
     */
    public void dump() {
        mNfDbHelper.dump(mTableName);
    }

    //
    // Table action
    //

    // Inflate an object from cursor
    public abstract ObjectType inflate(Cursor c, int shift);

    // Inflate an object from cursor with shift data due to a join request and data do not start a column 0
    public ObjectType inflate(Cursor c, AFDbTable<?> ... tables) {
        int shift = 0;
        for (AFDbTable<?> table: tables)
            shift += table.getFieldsList().size();

        return inflate(c, shift);
    }

    //
    // Select
    //

    public ObjectType select(long id) {
        Cursor c = getWritableDatabase().query(getTableName(),
                                               getFields(),
                                               mFieldId.getName() + " = " + id,
                                               null,
                                               null,
                                               null,
                                               null);

        if (c == null || c.getCount() < 1)
            return null;
        c.moveToFirst();

        ObjectType res = inflate(c);

        c.close();

        return res;
    }

    public Cursor select(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c = getWritableDatabase().query(getTableName(),
                                               projection,
                                               selection,
                                               selectionArgs,
                                               null,
                                               null,
                                               sortOrder);

        return c;
    }

    public abstract String getUniqGetWhereCondition(ObjectType obj);

    public long getStoredObjId(ObjectType obj) {
        long id = obj.getId();
        if (id != ID_NOT_SET)
            return id;

        String uniq = getUniqGetWhereCondition(obj);
        if (uniq == null)
            return ID_NOT_SET;

        Cursor c = getWritableDatabase().query(getTableName(),
                                               new String[] { mFieldId.getName() },
                                               uniq,
                                               null,
                                               null,
                                               null,
                                               null);

        if (c == null || c.getCount() < 1)
            return ID_NOT_SET;
        c.moveToFirst();

        obj.setId(c.getInt(0));

        c.close();

        return obj.getId();
    }

    //
    // Insert
    //

    public abstract void insert(ObjectType object, boolean propagate);

    public long insert(ContentValues values) {
        return getWritableDatabase().insert(getTableName(), null, values);
    }

    public void insert(Collection<ObjectType> objects, boolean propagate) {
        for (ObjectType obj: objects)
            insert(obj, propagate);
    }

    public void insertOrUpdate(Collection<ObjectType> objects) {
        insertOrUpdate(objects, false);
    }

    public void insertOrUpdate(Collection<ObjectType> objects, boolean propagate) {
        for (ObjectType obj: objects)
            insertOrUpdate(obj, propagate);
    }

    public void insertOrUpdate(ObjectType object) {
        insertOrUpdate(object, false);
    }

    public void insertOrUpdate(ObjectType object, boolean propagate) {
        long id = getStoredObjId(object);
        if (id == ID_NOT_SET)
            insert(object, propagate);
        else
            update(object, propagate);
    }

    //
    // Update
    //

    public int update(ContentValues values, String selection, String[] selectionArgs) {
        return getWritableDatabase().update(getTableName(), values, selection, selectionArgs);
    }

    public void update(Collection<ObjectType> objects, boolean propagate) {
        for (ObjectType obj: objects)
            update(obj, propagate);
    }

    public void update(ObjectType object, boolean propagate) {
        if (object.getId() == ID_NOT_SET) {
            DebugTools.e("Cannot update issue with unset id");
            return;
        }

        ContentValues values = new ContentValues();

        addValuesForUpdate(object, values, propagate);

        if (values.size() == 0)
            return;

        getWritableDatabase().update(getTableName(), values, getUniqGetWhereCondition(object), null);

        object.resetUpdateField();
    }

    /**
     * Call all addValueForUpdate methods that you need to update your object
     * @param object
     * @param values
     */
    public abstract void addValuesForUpdate(ObjectType object, ContentValues values, boolean propagate);

    // Set of helper method to fill content values
    protected void addValueForUpdate(ContentValues values, AFDbObject<?> object, AFDbField field, int value) {
        if (object.hasUpdate(field))
            values.put(field.getName(), value);
    }

    protected void addValueForUpdate(ContentValues values, AFDbObject<?> object, AFDbField field, long value) {
        if (object.hasUpdate(field))
            values.put(field.getName(), value);
    }

    protected void addValueForUpdate(ContentValues values, AFDbObject<?> object, AFDbField field, String value) {
        if (object.hasUpdate(field))
            values.put(field.getName(), value);
    }

    //
    // Delete
    //

    public int delete(String where, String[] whereArgs) {
        return getWritableDatabase().delete(getTableName(), where, whereArgs);
    }

    // Delete an object
    public void delete(ObjectType object, boolean propagate) {
        if (propagate) {
            mNfDbHelper.dbg.w("Table " + mTableName +": Cannot propage delete in default implementation for object " + object + "."
                              + " You should overwrite the method NFDbTable::delete(ObjectType object, boolean propagate)");
        }

        getWritableDatabase().delete(mTableName, mFieldId.getName() + " = " + object.getId(), null);
    }

    public void delete(Collection<ObjectType> objects, boolean propagate) {
        for (ObjectType object: objects)
            delete(object, propagate);
    }

    public void delete(ObjectType object) {
        delete(object, false);
    }

    // Delete all elements of the table
    public void delete() {
        SQLiteDatabase database = getWritableDatabase();
        drop(database);
        onCreate(database);
    }

    //
    // Drop
    //

    public void drop() {
        drop(getWritableDatabase());
    }

    public void drop(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + mTableName);
    }

    //
    // Accessor
    //

    public String getTableName() {
        return mTableName;
    }

    public void setTableName(String name) {
        mTableName = name;
    }

    public void setDbHelper(AFDbHelper helper) {
        mNfDbHelper = helper;
    }

    public SQLiteDatabase getWritableDatabase() {
        return mNfDbHelper.getWritableDatabase();
    }

    public AFDbField addField(String name, String type) {
        return addField(name, type, false);
    }

    public AFDbField addField(String name, String type, boolean isDBIndex) {
        AFDbField field = new AFDbField(getTableName(), name, type);

        mFields.add(field);

        field.setIdx(mFields.size() - 1);

        return field;
    }

    public String[] getFields() {
        if (mFieldsArray != null)
            return mFieldsArray;

        final int size = mFields.size();
        mFieldsArray = new String[size];

        for (int i = 0; i < size; ++i)
            mFieldsArray[i] = mFields.get(i).getName();

        return mFieldsArray;
    }

    public ArrayList<AFDbField> getFieldsList() {
        return mFields;
    }

    public AFDbField getFieldByName(String name) {
        final int size = mFields.size();

        for (int i = 0; i < size; ++i)
            if (mFields.get(i).getName().equals(name))
                return mFields.get(i);

        return null;
    }

    //
    // Some convenient method for sql expresion construction
    //

    /**
     * Construct where clause from a list of clauses
     *
     * @param clauses
     *          List of where clauses
     */
    private static String constructWhereClause(boolean isAnd, String... clauses) {
        StringBuilder wheres = new StringBuilder();

        for (String clause: clauses) {
            if (TextUtils.isEmpty(clause))
                continue ;

            if (wheres.length() != 0)
                wheres.append(' ')
                      .append(isAnd ? "AND" : "OR")
                      .append(' ');

            wheres.append(clause);
        }

        return wheres.toString();
    }

    /**
     * Construct where clause separate by 'AND' from a list of clauses
     *
     * @param clauses
     *          List of where clauses
     */
    public static String constructWhereClause(String... clauses) {
        return constructWhereClause(true, clauses);
    }

    public static String constructWhereAndClause(String... clauses) {
        return constructWhereClause(true, clauses);
    }

    public static String constructWhereOrClause(String... clauses) {
        return constructWhereClause(false, clauses);
    }

    /**
     * Construct field list for sql query from list of table
     * It is usefull to prepare a join request
     *
     * @param tables
     *          List of table
     *
     * @return field list for all tables
     */
    public static String constructFieldList(AFDbTable<?>... tables) {
        StringBuilder fields = new StringBuilder();

        for (AFDbTable<?> table: tables) {
            if (table == null)
                continue ;

            for (AFDbField field: table.getFieldsList()) {
                if (fields.length() != 0)
                    fields.append(", ");

                fields.append(field.getFullname());
            }
        }

        return fields.toString();
    }

    /**
     * Construct a test field clauses
     *
     * @param field_left
     *          Left field of the test
     * @param cmp
     *          Comparison operator
     * @param field_right
     *          Right field of the test
     *
     * @return The complete test clause
     */
    public static String constructTestField(AFDbField field_left, String cmp, AFDbField field_right) {
        return field_left.getFullname() + " " + cmp + " " + field_right.getFullname();
    }

    /**
     * Construct a test field clauses
     * @param field_left
     * @param cmp
     * @param field_right
     * @return
     */


    public static String constructTestField(AFDbField field_left, String cmp, String field_value) {
        return field_left.getFullname() + " " + cmp + " '" + field_value + "'";
    }

    public static String constructTestField(AFDbField field_left, String cmp, long field_value) {
        return field_left.getFullname() + " " + cmp + " " + field_value;
    }

    public static String constructTestField(String field_name, String cmp, String field_value) {
        return field_name + " " + cmp + " '" + field_value + "'";
    }

    public static String constructTestField(String field_name, String cmp, long field_value) {
        return field_name + " " + cmp + " " + field_value;
    }

    public static String constructTestFieldNotNull(AFDbField field_left) {
        return field_left.getFullname() + " NOT NULL";
    }

}
