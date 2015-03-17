package com.infinity.wefriends.apis;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	protected Context m_context = null;
	protected String lockFile = "";

	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		//waitForFreeDatabase();
		return super.getReadableDatabase();
	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		//waitForFreeDatabase();
		return super.getWritableDatabase();
	}

	@Override
	public synchronized void close() {
		//freeDatabase();
		super.close();
	}

	public DatabaseHelper(Context context, String dataBaseName) {
		this(context, dataBaseName, null, 1);
		m_context = context;
		lockFile = context.getCacheDir() + "/database.lock";
	}

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		m_context = context;
		lockFile = context.getCacheDir() + "/database.lock";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE usercache(_id INTEGER PRIMARY KEY AUTOINCREMENT, accesstoken TEXT, wefriendsid TEXT, nickname TEXT, avatar TEXT, whatsup TEXT, phone TEXT, email TEXT, intro TEXT, gender INTEGER, region TEXT, collegeid TEXT)");
		db.execSQL("CREATE TABLE friendscache(_id INTEGER PRIMARY KEY AUTOINCREMENT, wefriendsid TEXT, nickname TEXT, avatar TEXT, whatsup TEXT, intro TEXT, gender INTEGER, region TEXT, collegeid TEXT, friendgroup TEXT)");
		db.execSQL("CREATE TABLE messagecache(_id INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, receivers TEXT, messagetype TEXT, chatgroup TEXT, timestramp INTEGER, message TEXT, ishandled INTEGER, sendernickname TEXT, senderavatar TEXT, messageid TEXT, notificationid INTEGER)");
		db.execSQL("CREATE TABLE chats(_id INTEGER PRIMARY KEY AUTOINCREMENT, contact TEXT, chatgroup TEXT, contactnickname TEXT, contactavatar TEXT, chattype TEXT, addtime INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	protected void waitForFreeDatabase() {
		while (new File(lockFile).exists()) {
			Log.d("WeFriendsDatabase","Database occupied. Waiting.");
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			new File(lockFile).createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void freeDatabase() {
		new File(lockFile).delete();
	}
	
}
