package uk.org.openseizuredetector.hrmonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by graham on 20/01/17.
 */

public class HrmDb {
    private static final String DATABASE_NAME = "HrmDb";
    private static final String HR_DATA_TABLE_NAME = "HrData";
    private static final String HR_DATA_COL_DATETIME = "DATE_MS";
    private static final String HR_DATA_COL_HR = "HR";
    private static final String HR_DATA_COL_NOTE = "NOTE";


    private static final String TAG = "HrmDb";
    private SQLiteDatabase mDb;
    private Context mContext;

    public HrmDb(Context context) {
        Log.v(TAG,"HrmDb()");
        mContext = context;
        mDb = new HrmDbOpenHelper(context).getWritableDatabase();
    }

    public void close() {
        Log.v(TAG,"close()");
        mDb.close();
    }

    public boolean insertReading(int hr, String note) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(HR_DATA_COL_HR, hr);
        values.put(HR_DATA_COL_NOTE, note);
        values.put(HR_DATA_COL_DATETIME, System.currentTimeMillis());
        long newRowId = mDb.insert(HR_DATA_TABLE_NAME, null, values);
        Log.v(TAG,"insertReading - Added row ID "+newRowId);
        return true;
    }

    public Cursor getReadings(long dateTimeMin, long dateTimeMax) {
        Log.v(TAG,"getReadings("+dateTimeMin+", "+dateTimeMax+")");
        // Select column list
        String[] projection = {
                "ID", HR_DATA_COL_DATETIME, HR_DATA_COL_HR, HR_DATA_COL_NOTE
        };
        // Filter results WHERE clause
        String selection = HR_DATA_COL_DATETIME + " >= ? and "+HR_DATA_COL_DATETIME+" <= ?";
        String[] selectionArgs = { ""+dateTimeMin, ""+dateTimeMax };
        String sortOrder = HR_DATA_COL_DATETIME + " ASC";
        Cursor cursor = mDb.query(
                HR_DATA_TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        return cursor;
    }

    /**
     * Return  the latest numReadings number of readings from the database
     * @param numReadings
     * @return a Cursor object contining the data.
     */
    public Cursor getLatestReadings(int numReadings) {
        Log.v(TAG,"getLatestReadings()");
        // Select column list
        String[] projection = {
                "ID", HR_DATA_COL_DATETIME, HR_DATA_COL_HR, HR_DATA_COL_NOTE
        };
        // Filter results WHERE clause
        //String selection = "ID = (SELECT MAX(ID) from "+HR_DATA_TABLE_NAME+")";
        String selection = null;
        String sortOrder = HR_DATA_COL_DATETIME+" DESC";
        Cursor cursor = mDb.query(
                HR_DATA_TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder,                                 // The sort order
                numReadings+""                            // Max number of records to return
        );
        Log.v(TAG,"returning "+cursor.getCount()+" rows");
        return cursor;
    }


    public Cursor getAllReadings() {
        return getReadings(0,System.currentTimeMillis());
    }

    private class HrmDbOpenHelper extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;
        private static final String HR_DATA_TABLE_CREATE =
                "CREATE TABLE " + HR_DATA_TABLE_NAME + " (" +
                        "ID INTEGER PRIMARY KEY, " +
                        HR_DATA_COL_DATETIME+" INT,"+
                        HR_DATA_COL_HR+" INTEGER,"+
                        HR_DATA_COL_NOTE+" TEXT"+
                        ");";
        private static final String HR_DATA_TABLE_DELETE = "DROP TABLE IF EXISTS "+HR_DATA_TABLE_NAME+";";

        HrmDbOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            Log.v(TAG,"HrmDbOpenHelper()");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.v(TAG,"onCreate()");
            db.execSQL(HR_DATA_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            Log.v(TAG,"onUpgrade - just creating a new database");
            db.execSQL(HR_DATA_TABLE_DELETE);
            this.onCreate(db);
        }
    }



}
