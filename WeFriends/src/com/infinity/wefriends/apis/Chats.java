package com.infinity.wefriends.apis;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Chats {
	
	protected DatabaseHelper database = null;
	protected Context m_context = null;
	
	public Chats(Context context) {
		m_context = context;
		database = DatabaseHelper.getInstance(context,"wefriendsdb");
	}
	
	public List<ContentValues> getChatList() {
		SQLiteDatabase db = database.getReadableDatabase();
		Cursor cursor = database.safeRawQuery(db, "SELECT * from chats ORDER BY addtime DESC", new String[]{});
		List<ContentValues> list = new ArrayList<ContentValues>();
		while (cursor.moveToNext()) {
			ContentValues chatInfo = new ContentValues();
			chatInfo.put("contact", cursor.getString(cursor.getColumnIndex("contact")));
			chatInfo.put("chatgroup", cursor.getString(cursor.getColumnIndex("chatgroup")));
			chatInfo.put("contactnickname", cursor.getString(cursor.getColumnIndex("contactnickname")));
			chatInfo.put("contactavatar", cursor.getString(cursor.getColumnIndex("contactavatar")));
			chatInfo.put("chattype", cursor.getString(cursor.getColumnIndex("chattype")));
			list.add(chatInfo);
		}
		database.safeClose(db);
		return list;
	}
	
	public boolean isChatFound(String contact, String chatGroup) {
		SQLiteDatabase db = database.getReadableDatabase();
		Cursor cursor = null;
		boolean result = false;
		try {
			if (!chatGroup.equals(""))
				cursor = database.safeRawQuery(db, "SELECT * FROM chats WHERE chatgroup='"+chatGroup+"'", new String[]{});
			else
				cursor = database.safeRawQuery(db, "SELECT * FROM chats WHERE chatgroup='' AND contact='"+contact+"'", new String[]{});
			
			if (cursor.getCount()>0)
				result = true;
		} catch (SQLException e) {
			Log.e("WeFriends","SQL Exception at Chats.isChatFound");
			Log.e("WeFriends",e.getMessage());
		}
		database.safeClose(db);
		return result;
	}
	
	public void addChat(ContentValues chatInfo) {
		SQLiteDatabase db = database.getWritableDatabase();
		String contact = chatInfo.getAsString("contact");
		String chatGroup = chatInfo.getAsString("chatgroup");
		try {
			if (!chatGroup.equals(""))
				database.safeExecSQL(db, "DELETE FROM chats WHERE chatgroup='" + chatGroup + "'");
			else
				database.safeExecSQL(db, "DELETE FROM chats WHERE contact='"+ contact + "' AND chatgroup=''");
		} catch (SQLException e) {
			Log.e("WeFriends","SQL Exception at apis.Chats.addChat");
			Log.e("WeFriends",e.getMessage());
		}
		database.safeInsert(db, "chats", "",chatInfo);
		database.safeClose(db);
	}
	
	public void deleteChat(ContentValues chatInfo) {
		SQLiteDatabase db = database.getWritableDatabase();
		String contact = chatInfo.getAsString("contact");
		String chatGroup = chatInfo.getAsString("chatgroup");
		try {
			if (!chatGroup.equals(""))
				database.safeExecSQL(db, "DELETE FROM chats WHERE chatgroup='" + chatGroup + "'");
			else
				database.safeExecSQL(db, "DELETE FROM chats WHERE contact='"+ contact + "' AND chatgroup=''");
		} catch (SQLException e) {
			Log.e("WeFriends","SQL Exception at apis.Chats.deleteChat");
			Log.e("WeFriends",e.getMessage());
		}
		database.safeClose(db);	
	}
	
	public void importFromNewMessages(List<ContentValues> messages) {
		int messageCount = messages.size();
		for (int i=messageCount-1;i>=0;i--) {
			ContentValues messageInfo = messages.get(i);
			ContentValues chatInfo = new ContentValues();
			chatInfo.put("contact", messageInfo.getAsString("sender"));
			chatInfo.put("chatgroup", messageInfo.getAsString("chatgroup"));
			chatInfo.put("contactnickname", messageInfo.getAsString("sendernickname"));
			chatInfo.put("contactavatar", messageInfo.getAsString("senderavatar"));
			chatInfo.put("chattype", messageInfo.getAsString("messagetype"));
			chatInfo.put("addtime", messageInfo.getAsLong("timestramp"));
			addChat(chatInfo);
			//Log.d("test","chatgroup=" + messageInfo.getAsString("chatgroup") + ";contactnickmane=" + messageInfo.getAsString("sendernickname"));
		}
	}

}
