package com.infinity.wefriends.apis;

import java.io.File;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.CancellationSignal;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	protected Context m_context = null;
	protected String lockFile = "";

	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		//waitForFreeDatabase("getReadableDatabase()");
		return super.getReadableDatabase();
	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		//waitForFreeDatabase("getWritableDatabase()");
		return super.getWritableDatabase();
	}

	@Override
	public synchronized void close() {
		freeDatabase();
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
		
	}
	
	public synchronized void safeClose(SQLiteDatabase db) {
		freeDatabase();
		db.close();
	}
	
	public synchronized Cursor safeRawQuery(SQLiteDatabase db, String sql, String[] selectionArgs) throws SQLException {
		waitForFreeDatabase("safeRawQuery()" + ";sql=" + sql);
		return db.rawQuery(sql, selectionArgs);
	}
	
	public synchronized void safeExecSQL(SQLiteDatabase db, String sql) throws SQLException {
		waitForFreeDatabase("safeExecSQL()" + ";sql=" + sql);
		db.execSQL(sql);
	}
	
	public synchronized long safeInsert(SQLiteDatabase db, String table, String nullColumnHack, ContentValues values) {
		waitForFreeDatabase("safeInsert()");
		return db.insert(table, nullColumnHack, values);
	}
	
	public synchronized int safeDelete(SQLiteDatabase db, String table, String whereClause, String[] whereArgs) {
		waitForFreeDatabase("safeDelete()");
		return db.delete(table, whereClause, whereArgs);
	}
	
	public synchronized int safeUpdate(SQLiteDatabase db, String table, ContentValues values, String whereClause, String[] whereArgs) {
		waitForFreeDatabase("safeUpdate()");
		return db.update(table, values, whereClause, whereArgs);
	}
	
/*	public synchronized Cursor safeQuery(SQLiteDatabase db, boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit, 
			 CancellationSignal cancellationSignal) {
		waitForFreeDatabase();
		return db.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit, cancellationSignal);
	}*/
	
	public synchronized Cursor safeQuery(SQLiteDatabase db, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
		waitForFreeDatabase("safeQuery()");
		return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
	}
	
	public synchronized Cursor safeQuery(SQLiteDatabase db, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		waitForFreeDatabase("safeQuery()");
		return db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
	}
	
	public synchronized Cursor safeQuery(SQLiteDatabase db ,boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		waitForFreeDatabase("safeQuery()");
		return db.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
	}
	
	protected void waitForFreeDatabase(String method) {
		while (new File(lockFile).exists()) {
			Log.d("WeFriendsDatabase","Database occupied. Waiting.");
			Log.d("WeFriendsDatabase","at DatabaseHelper." + method);
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
	
	public void freeDatabase() {
		File file = new File(lockFile);
		while (file.exists())
			file.delete();
	}
}
