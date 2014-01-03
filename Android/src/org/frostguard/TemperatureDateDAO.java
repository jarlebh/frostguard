package org.frostguard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Comment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TemperatureDateDAO {
	// Database fields
	private SQLiteDatabase database;
	private FrostGuardDatabase dbHelper;
	private String[] allColumns = { FrostGuardDatabase.ID,
			FrostGuardDatabase.COL_DATE, FrostGuardDatabase.COL_TEMP};

	public TemperatureDateDAO(Context context) {
		dbHelper = new FrostGuardDatabase(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public TempDate createEntry(TempDate temp) {
		ContentValues values = new ContentValues();
		values.put(FrostGuardDatabase.COL_DATE, temp.getFromTime().getTime());
		values.put(FrostGuardDatabase.COL_TEMP, temp.getTemperature());
		long insertId = database.insert(FrostGuardDatabase.TABLE_TEMPERATURS, null,
				values);
		Cursor cursor = database.query(FrostGuardDatabase.TABLE_TEMPERATURS,
				allColumns, FrostGuardDatabase.ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		TempDate newComment = cursorToTempDate(cursor);
		Log.i("DAO","inserted at "+newComment.getFromTime());
		cursor.close();
		return newComment;
	}
	public int createEntries(List<TempDate> temps) {
		for (TempDate temp : temps) {
			createEntry(temp);
		}
		return temps.size();
	}

	public void deleteComment(TempDate comment) {
		long id = comment.getId();
		System.out.println("Comment deleted with id: " + id);
		database.delete(FrostGuardDatabase.TABLE_TEMPERATURS, FrostGuardDatabase.ID
				+ " = " + id, null);
	}
	public void deleteAll() {
		Log.i("TemperatureDateDAO","Deleting all TEMPS");
		database.delete(FrostGuardDatabase.TABLE_TEMPERATURS, null, null);
	}
	public List<TempDate> getAllTemps() {
		List<TempDate> comments = new ArrayList<TempDate>();

		Cursor cursor = database.query(FrostGuardDatabase.TABLE_TEMPERATURS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			TempDate comment = cursorToTempDate(cursor);
			comments.add(comment);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return comments;
	}

	private TempDate cursorToTempDate(Cursor cursor) {
		TempDate comment = new TempDate(cursor.getInt(2), new Date(cursor.getLong(1)));
		comment.setId(cursor.getLong(0));
		return comment;
	}

	public void resetEntries(List<TempDate> data) {
		deleteAll();
		createEntries(data);
	}
}
