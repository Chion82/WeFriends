package com.infinity.wefriends.apis;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	

	public DatabaseHelper(Context context, String dataBaseName) {
		this(context, dataBaseName, null, 1);
	}

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE usercache(_id INTEGER PRIMARY KEY AUTOINCREMENT, accesstoken TEXT, wefriendsid TEXT, nickname TEXT, avatar TEXT, whatsup TEXT, phone TEXT, email TEXT, intro TEXT, gender INTEGER, region TEXT, collegeid TEXT)");
		db.execSQL("CREATE TABLE friendscache(_id INTEGER PRIMARY KEY AUTOINCREMENT, wefriendsid TEXT, nickname TEXT, avatar TEXT, whatsup TEXT, intro TEXT, gender INTEGER, region TEXT, collegeid TEXT, friendgroup TEXT)");
		db.execSQL("CREATE TABLE messagecache(_id INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, messagetype TEXT, chatgroup TEXT, timestramp INTEGER, message TEXT, ishandled INTEGER, sendernickname TEXT, senderavatar TEXT, messageid TEXT, notificationid INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
