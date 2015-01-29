package com.fixdapp.one.io;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class DatabaseUtility {
	
	private static final String PATH = "data/data/com.fixd.app/databases/";
	private static final String NAME = "dtc.db";
	
	public static boolean isThereDB() {
		SQLiteDatabase db = null;
		
		try {
			db = SQLiteDatabase.openDatabase(PATH + NAME, null, SQLiteDatabase.OPEN_READONLY);
		} catch(SQLiteException e) {
			e.printStackTrace();
		}
		
		if(db != null) {
			db.close();
		}
		
		return (db == null ? false : true);
	}
}
