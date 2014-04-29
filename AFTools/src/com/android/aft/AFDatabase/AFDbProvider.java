package com.android.aft.AFDatabase;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public abstract class AFDbProvider<DbHelper extends AFDbHelper> extends ContentProvider {

    public static String AUTHORITY;

    public static final String START_TRANSACTION_ACTION = "startTransaction";
    public static final String COMMIT_TRANSACTION_ACTION = "commitTransaction";
    public static final String ROLLBACK_TRANSACTION_ACTION = "rollbackTransaction";

    public static Uri START_TRANSACTION_URI = Uri.parse("content://" + AUTHORITY + "/" + START_TRANSACTION_ACTION);
    public static Uri COMMIT_TRANSACTION_URI = Uri.parse("content://" + AUTHORITY + "/" + COMMIT_TRANSACTION_ACTION);
    public static Uri ROLLBACK_TRANSACTION_URI = Uri.parse("content://" + AUTHORITY + "/" + ROLLBACK_TRANSACTION_ACTION);

    protected static final int START_TRANSACTION = 0;
    protected static final int COMMIT_TRANSACTION = START_TRANSACTION + 1;
    protected static final int ROLLBACK_TRANSACTION = START_TRANSACTION + 2;

    protected UriMatcher mUriMatcher;
    private HashMap<Integer, AFDbTable<?>> mTables;
    protected DbHelper mDbHelper;

    /**
     * Gets Database helper from AppManager
     * @return
     */
    public abstract DbHelper getDatabaseHelper();

    /**
     * Set the provider authority
     *
     * @param authority
     */
    protected static void setProviderAuthority(String authority) {
        AUTHORITY = authority;
        START_TRANSACTION_URI = Uri.parse("content://" + AUTHORITY + "/" + START_TRANSACTION_ACTION);
        COMMIT_TRANSACTION_URI = Uri.parse("content://" + AUTHORITY + "/" + COMMIT_TRANSACTION_ACTION);
        ROLLBACK_TRANSACTION_URI = Uri.parse("content://" + AUTHORITY + "/" + ROLLBACK_TRANSACTION_ACTION);
    }

    @Override
    public boolean onCreate() {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mTables = new HashMap<Integer, AFDbTable<?>>();
        mDbHelper = getDatabaseHelper();

        // Create list of table for request redirection
        int table_code = 0x0100;
        for (AFDbTable<?> table: mDbHelper.getTables()) {
            addUriMatch(table, table.getTableName(), table_code);
            table_code += 0x0100;
        }

        return true;
    }

    @Override
    public String getType(Uri uri) {
        AFDbTable<?> matchTable = getTableFromUri(uri);

        if (matchTable != null)
            return "vnd.android.cursor.dir/" + getApplicationType() + "." + matchTable.getTableName();

        //TODO: Handle Raw queries methods
        /*else {

            return "vnd.android.cursor.item/"+getApplicationType()+"."+
        }*/

        return null;
    }

    /**
     * Add a new uri to match
     *
     * @param table
     *          The represent table
     * @param name
     *          Name of the table
     * @param code
     *          Associated table code
     */
    protected void addUriMatch(AFDbTable<?> table, String name, int code) {
        mUriMatcher.addURI(AUTHORITY, name, code);
        mTables.put(code, table);
    }

    /**
     * Link a table with an Uri using UriMatcher
     * @param uri
     * @return the linked table or null if no table matches
     */
    private AFDbTable<?> getTableFromUri(Uri uri) {
        return mTables.get(mUriMatcher.match(uri));
    }


    /**
     * Gets an application MIME type like
     * "vnd.applicationname"
     * @return
     */
    public abstract String getApplicationType();

    /**
     * Bulk insert database entry
     * @param uri
     *          Uri of the table
     * @param values
     *          All values to insert
     *
     * @return Number of insert entry
     */
    @Override
    public int bulkInsert(final Uri uri, final ContentValues[] values) {
        if (values == null
            || values.length == 0)
            return 0;

        // Get database
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();

        // Get table for insert
        AFDbTable<?> table = getTableFromUri(uri);
        if (table == null) {
            mDbHelper.dbg.e("Cannot get table for uri: " + uri);
            return 0;
        }

        // Prepare transaction
        final boolean dbAlreadyInTransaction = db.inTransaction();
        if (!dbAlreadyInTransaction)
            db.beginTransaction();

        // Do insertion
        int i = 0;
        for (; i < values.length; )
            table.insert(values[i++]);

        // Finalize transaction
        if (!dbAlreadyInTransaction) {
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return i;
    }

    /**
     * Insert a data in database
     *
     * @param uri
     *          Uri of the table
     * @param values
     *          Values of the data to insert
     *
     * @return Uri of the new inserted element
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (values == null)
            throw new SQLException("Failed to insert row into " + uri);

        AFDbTable<?> table = getTableFromUri(uri);
        if (table == null) {
            mDbHelper.dbg.e("Cannot get table for uri: " + uri);
            throw new SQLException("Cannot get table for uri: " + uri);
        }

        long entry_id = table.insert(values);
        if (entry_id == 0)
            throw new SQLException("Failed to insert row into " + uri);

        Uri entry_uri = ContentUris.withAppendedId(uri, entry_id);
        getContext().getContentResolver().notifyChange(entry_uri, null);

        return entry_uri;
    }

    /**
     * Resolve a query request
     *
     * @param uri
     *          Uri of the query
     * @param projection
     *          Projection of field
     * @param selection
     *          Where clauses
     * @param selectionArgs
     *          Where argument
     * @param sortOrder
     *          Order clause
     *
     * @return Cursor of result
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Get table for query
        AFDbTable<?> table = getTableFromUri(uri);

        // If query is not a table => check for default action
        if (table == null) {
            final SQLiteDatabase db = mDbHelper.getWritableDatabase();

            switch (mUriMatcher.match(uri)) {
                case START_TRANSACTION:
                    if (!db.inTransaction())
                        db.beginTransaction();
                    break;
                case COMMIT_TRANSACTION:
                    if (db.inTransaction()) {
                        db.setTransactionSuccessful();
                        db.endTransaction();
                    }
                    break;
                case ROLLBACK_TRANSACTION:
                    if (db.inTransaction())
                        db.endTransaction();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

            return null;
        }

        // Do query select on table
        Cursor c = table.select(projection, selection, selectionArgs, sortOrder);
        if (c != null && !isTemporary())
            c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    /**
     * Update an entry
     *
     * @param uri
     *          Uri of the query
     * @param values
     *          Values to update
     * @param selection
     *          Where clauses
     * @param selectionArgs
     *          Where argument
     *
     * @return The number of affected row
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values == null)
            return 0;

        AFDbTable<?> table = getTableFromUri(uri);
        if(table == null) {
            mDbHelper.dbg.e("Cannot get table for uri: " + uri);
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        int result = table.update(values, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        return result;
    }

    /**
     * Delete an entry
     * To remove all rows and get a count pass "1" as the whereClause.
     *
     * @param uri
     *          Uri of the query
     * @param values
     *          Values to update
     * @param selection
     *          Where clauses
     * @param selectionArgs
     *          Where argument
     *
     * @return The number of rows affected if a whereClause is passed in, 0 otherwise.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get the table
        AFDbTable<?> table = getTableFromUri(uri);
        if(table == null) {
            mDbHelper.dbg.e("Cannot get table for uri: " + uri);
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        int result = table.delete(selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        return result;
    }
}
