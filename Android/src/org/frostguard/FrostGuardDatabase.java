package org.frostguard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FrostGuardDatabase extends SQLiteOpenHelper {
	private static final String DEBUG_TAG = "FrostGuardDatabase";
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "frostguard_data";
	public static final String TABLE_TEMPERATURS = "temperatures";
	public static final String ID = "_id";
	public static final String COL_DATE = "date";
	public static final String COL_TEMP = "temp";
	
	private static final String CREATE_TABLE_TEMPERATURS = "create table " + TABLE_TEMPERATURS
			+ " (" + ID + " integer primary key autoincrement, " + COL_DATE
			+ " integer not null, " + COL_TEMP + " integer  not null);";
			 
	private static final String DB_SCHEMA = CREATE_TABLE_TEMPERATURS;
	public FrostGuardDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
	public FrostGuardDatabase(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		 db.execSQL(DB_SCHEMA);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DEBUG_TAG, "Upgrading database. Existing contents will be lost. ["
	            + oldVersion + "]->[" + newVersion + "]");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMPERATURS);
	    onCreate(db);
	}

}
