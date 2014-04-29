package com.android.aft.AFDatabase;

import java.util.HashSet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.aft.AFCoreTools.DebugTools;
import com.android.aft.AFCoreTools.StringTools;

// This class provide access to database
public class AFDbHelper extends SQLiteOpenHelper {

    // Debug logger
    public DebugTools.Logger dbg;

    // List of table to manage
    public HashSet<AFDbTable<?>> mTables;

    // Link to database
    private SQLiteDatabase mDatabase;

    /**
     * Constructor
     *
     * @param context
     *          Application context
     * @param name
     *          Name of the database
     * @param version
     *          Version of the database
     */
    public AFDbHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    /**
     * Constructor
     *
     * @param context
     *          Application context
     * @param name
     *          Name of the database
     * @param factory
     *          Cursor factory
     * @param version
     *          Version of the database
     */
    public AFDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        dbg = new DebugTools.Logger(AFDbHelper.class.getSimpleName());
        mTables = new HashSet<AFDbTable<?>>();
    }

    /**
     * Method called automatically during creation of the database if the database does not exist
     *
     * @param database
     *          The database
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        // Set the temporary database access
        mDatabase = database;

        for (AFDbTable<?> table: mTables)
            table.onCreate(database);

        // Reset database access
        mDatabase = null;
    }

    /**
     * Method called automatically if database version is increase
     *
     * @param database
     *          The database
     * @param oldVersion
     *          Old version number
     * @param newVersion
     *          New version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        for (AFDbTable<?> table: mTables)
            table.onUpgrade(database, oldVersion, newVersion);
    }

    /**
     * Reset the database
     */
    public void reset() {
        for (AFDbTable<?> table: mTables)
            table.delete();
    }

    /**
     * Close the database
     */
    @Override
    public void close() {
        super.close();
        closeDatabase();
    }

    /**
     * Close the open writable database
     */
    public void closeDatabase() {
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
    }

    /**
     * Return a writable database
     */
    @Override
    public SQLiteDatabase getWritableDatabase() {
        if (mDatabase == null)
            mDatabase = super.getWritableDatabase();
        return mDatabase;
    }

    //
    // Accessor
    //

    public void addTable(AFDbTable<?> table) {
        mTables.add(table);
        table.setDbHelper(this);
    }

    public HashSet<AFDbTable<?>> getTables() {
        return mTables;
    }

    //
    // Data dump
    //

    private static int[] calculateDumpSize(Cursor c) {
        int nbCol = c.getColumnCount();
        int tblMaxLength[] = new int[nbCol];

        // Init the column length table with size of column name
        for (int i = 0; i < nbCol; ++i)
            tblMaxLength[i] = c.getColumnName(i).length();

        // Increase the column length with data size
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
            for (int i = 0; i < nbCol; ++i) {
                int length = 0;
                try {
                    String value = c.getString(i);
                    if (value != null)
                        length = c.getString(i).length();
                } catch (SQLiteException e) { } // Exception can be raise if columns data is a blob
                tblMaxLength[i] = Math.max(tblMaxLength[i], Math.min(length, 30));
            }

        return tblMaxLength;
    }

    public void dump(String table) {
        dump(this, table, dbg);
    }

    public static void dump(SQLiteOpenHelper helper, String table, DebugTools.Logger dbg) {
        if (dbg == null)
            dbg = new DebugTools.Logger(DebugTools.getTag());

        Cursor c = null;
        try {
            c = helper.getWritableDatabase().query(table, null, null, null, null, null, null);
        } catch (Exception e) {
            dbg.e("Cannot get data of table '" + table + "'", e);
            return ;
        }

        dump(table, c, dbg);
        c.close();
    }


    public static void dump(Cursor c) {
    	dump(null, c);
    }

    public static void dump(String table, Cursor c) {
        dump(table, c, false);
    }

    public static void dumpRaw(String table, Cursor c) {
        dump(table, c, true);
    }

    public static void dump(String table, Cursor c, boolean displayOneRaw) {
        dump(table, c, new DebugTools.Logger(DebugTools.getTag()), displayOneRaw);
    }

    public static void dump(String table, Cursor c, DebugTools.Logger dbg) {
        dump(table, c, dbg, false);
    }

    public static void dump(String table, Cursor c, DebugTools.Logger dbg, boolean displayOneRaw) {
        int position = c.getPosition();

        dbg.d(".");
        dbg.d(".");
        if (table != null)
        	dbg.d("================ Table " + table + " (" + c.getCount() + ") ================");

        if (c.getCount() == 0) {
            dbg.d("===> Table is empty !");
            return ;
        }

        int nbCol = c.getColumnCount();
        int tblSize[] = calculateDumpSize(c);
        StringBuilder sep_line = new StringBuilder();
        sep_line.append('+');

        StringBuilder line = new StringBuilder();
        line.append('|');
        for (int i = 0; i < nbCol; ++i) {
            line.append(' ')
                .append(c.getColumnName(i))
                .append(StringTools.getStringRepeatWhitespace(tblSize[i] - c.getColumnName(i).length()))
                .append(" |");

            sep_line.append(StringTools.getStringRepeat('-', 2 + tblSize[i]))
                    .append('+');
        }

        dbg.d(sep_line.toString());
        dbg.d(line.toString());
        dbg.d(sep_line.toString());

        if (displayOneRaw) {
            c.moveToPosition(position);
            displayRaw(dbg, c, tblSize, nbCol);
        }
        else {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
                displayRaw(dbg, c, tblSize, nbCol);
        }

        dbg.d(sep_line.toString());
    }

    private static void displayRaw(DebugTools.Logger dbg, Cursor c, int tblSize[], int nbCol) {
        StringBuilder line = new StringBuilder();

        line.append('|');
        for (int i = 0; i < nbCol; ++i) {
            String value = "";
            try {
                value = c.getString(i);
            } catch (SQLiteException e) { } // Exception can be raise if columns data is a blob
            if (value == null) {
                line.append(' ')
                    .append(StringTools.getStringRepeatWhitespace(tblSize[i]))
                    .append(" |");

                continue ;
            }

            if (value.length() > 30)
                value = value.substring(0, 27) + "...";

            line.append(' ')
                .append(value)
                .append(StringTools.getStringRepeatWhitespace(tblSize[i] - value.length()))
                .append(" |");
        }
        dbg.d(line.toString());
    }

    public void dump() {
        Cursor c = getWritableDatabase().rawQuery("select name from sqlite_master where type = 'table'", null);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
            dump(c.getString(0));

        c.close();
    }

    public static void dump(SQLiteDatabase db) {
        Cursor cAllTables = db.rawQuery("select name from sqlite_master where type = 'table'", null);

        for (cAllTables.moveToFirst(); !cAllTables.isAfterLast(); cAllTables.moveToNext()) {
            Cursor c = db.rawQuery("select * from " + cAllTables.getString(0), null);
            dump(cAllTables.getString(0), c);
        }

        cAllTables.close();
    }


}
