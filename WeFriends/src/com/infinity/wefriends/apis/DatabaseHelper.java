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
	protected String m_databaseName = "";
	static protected DatabaseHelper m_instance = null;
	static protected SQLiteDatabase readableDatabase = null;
	static protected SQLiteDatabase writableDatabase = null;

	public DatabaseHelper(Context context, String databaseName) {
		this(context, databaseName, null, 1);
		m_context = context;
		m_databaseName = databaseName;
	}

	@Override
	public SQLiteDatabase getReadableDatabase() {
		if (DatabaseHelper.readableDatabase != null)
			if (DatabaseHelper.readableDatabase.isOpen())
				return DatabaseHelper.readableDatabase;
		readableDatabase = super.getReadableDatabase();
		return readableDatabase;
	}

	@Override
	public SQLiteDatabase getWritableDatabase() {
		if (DatabaseHelper.writableDatabase != null)
			if (DatabaseHelper.writableDatabase.isOpen())
				return DatabaseHelper.writableDatabase;
		writableDatabase = super.getWritableDatabase();
		return writableDatabase;
	}

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		m_context = context;
	}
	
	static public DatabaseHelper getInstance(Context context, String databaseName) {
		if (DatabaseHelper.m_instance==null)
			DatabaseHelper.m_instance = new DatabaseHelper(context, databaseName);
		return DatabaseHelper.m_instance;
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
		db.close();
	}
	
	public synchronized Cursor safeRawQuery(SQLiteDatabase db, String sql, String[] selectionArgs) throws SQLException {
		Cursor cursor = null;
		try {
			cursor = db.rawQuery(sql, selectionArgs);
			cursor.getCount();
			return cursor;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			Log.e("WeFriendsDatabase","safeRawQuery() Failed. Retrying.");
			/*try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}*/
			db = getReadableDatabase();
			return safeRawQuery(db, sql, selectionArgs);
		}
	}
	
	public synchronized void safeExecSQL(SQLiteDatabase db, String sql) throws SQLException {
		try {
			db.execSQL(sql);
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			Log.e("WeFriendsDatabase","safeExecSQL() Failed. Retrying.");
			/*try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}*/
			db = getWritableDatabase();
			safeExecSQL(db, sql);
		}
	}
	
	public synchronized long safeInsert(SQLiteDatabase db, String table, String nullColumnHack, ContentValues values) {
		try {
			long result = db.insert(table, nullColumnHack, values);
			return result;
		} catch (Exception e) {
			Log.e("WeFriendsDatabase","safeInsert() Failed. Retrying.");
			/*try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}*/
			db = getWritableDatabase();
			return safeInsert(db, table, nullColumnHack, values);
		}
	}
	
	public synchronized int safeDelete(SQLiteDatabase db, String table, String whereClause, String[] whereArgs) {
		try {
			int result = db.delete(table, whereClause, whereArgs);
			return result;
		} catch (Exception e) {
			Log.e("WeFriendsDatabase","safeDelete() Failed. Retrying.");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			db = getWritableDatabase();
			return safeDelete(db, table, whereClause, whereArgs);
		}
	}
	
	public synchronized int safeUpdate(SQLiteDatabase db, String table, ContentValues values, String whereClause, String[] whereArgs) {
		try {
			int result = db.update(table, values, whereClause, whereArgs);
			return result;
		} catch (Exception e) {
			Log.e("WeFriendsDatabase","safeUpdate() Failed. Retrying.");
			/*try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}*/
			db = getWritableDatabase();
			return safeUpdate(db, table, values, whereClause, whereArgs);
		}
	}
	
	public synchronized Cursor safeQuery(SQLiteDatabase db, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
		Cursor cursor = null;
		try {
			cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
			cursor.getCount();
			return cursor;
		} catch (Exception e) {
			Log.e("WeFriendsDatabase","safeQuery() Failed. Retrying.");
			/*try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}*/
			db = getReadableDatabase();
			return safeQuery(db, table, columns, selection, selectionArgs, groupBy, having, orderBy);
		}
	}
	
	public synchronized Cursor safeQuery(SQLiteDatabase db, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		Cursor cursor = null;
		try {
			cursor = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			cursor.getCount();
			return cursor;
		} catch (Exception e) {
			Log.e("WeFriendsDatabase","safeQuery() Failed. Retrying.");
			/*try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}*/
			db = getReadableDatabase();
			return safeQuery(db, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		}
	}
	
	public synchronized Cursor safeQuery(SQLiteDatabase db ,boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		Cursor cursor = null;
		try {
			cursor = db.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
			cursor.getCount();
			return cursor;
		} catch (Exception e) {
			Log.e("WeFriendsDatabase","safeQuery() Failed. Retrying.");
			/*try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}*/
			db = getReadableDatabase();
			return safeQuery(db ,distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
		}
	}
	
}
