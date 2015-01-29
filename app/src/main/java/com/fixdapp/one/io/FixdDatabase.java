package com.fixdapp.one.io;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class FixdDatabase extends SQLiteAssetHelper{
	
	private static final String DATABASE_NAME = "dtc.db";
	private static final int DATABASE_VERSION = 1;

	public interface Tables { // Might add more tables
		String DTC = "DTC";
	}
	
	public interface DtcCategories {
		String TECHNICAL_DESCRIPTION = "TECHNICAL_DESCRIPTION";
		String THREAT_LEVEL = "THREAT_LEVEL";
		String CONSEQUENCES = "CONSEQUENCES";
		String EXTREME_CASE = "EXTREME_CASE";
		String RATIONALE = "RATIONALE";
	}
	
	public FixdDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public SQLiteDatabase getDatabase() throws SQLiteException{
		return getReadableDatabase();
	}
	
	public Cursor getExplainedCode(String PID) {
		Cursor c = null;
		SQLiteDatabase dB = getReadableDatabase();
		
		c = dB.rawQuery("SELECT * from DTC where _id = ?", new String[] {PID});
		return c;

	}
}
