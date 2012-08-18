package com.taptag.beta.db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.taptag.beta.tap.Tap;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TapTagDatabase extends SQLiteOpenHelper {
	static final String DB_NAME = "TAPTAGDB";
	static final String TAPS = "TAPS";
	static final String ID = "ID";
	static final String USER_ID = "USER_ID";
	static final String VENDOR_ID = "VENDOR_ID";
	static final String COMPANY_ID ="COMPANY_ID";
	static final String TAPPED_TIME = "TAPPED_TIME";
	
	static final SimpleDateFormat tappedDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	
	public TapTagDatabase(Context context) {
		super(context, DB_NAME, null, 1);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String createTaps = "CREATE TABLE " + TAPS + " (" +
		ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," +
		USER_ID + "INTEGER NOT NULL, " +
		VENDOR_ID + "INTEGER NOT NULL, " +
		COMPANY_ID + "INTEGER NOT NULL, " +
		TAPPED_TIME + "TEXT NOT NULL" +
		");";
		db.execSQL(createTaps);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub	
	}
	
	public void insertTap(Tap tap) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put(USER_ID, tap.getUserID());
		cv.put(COMPANY_ID, tap.getCompanyID());
		cv.put(VENDOR_ID, tap.getVendorID());
		//Convert Date to ISO String before putting to DB
		cv.put(TAPPED_TIME, dateToISO(tap.getTappedTime()));
		db.insert(TAPS, null, cv);
		db.close();
	}
	
	public static String dateToISO(Date date) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		tappedDateFormat.setTimeZone(tz);
		return tappedDateFormat.format(date);
	}
	
}
