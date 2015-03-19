package com.infinity.wefriends.apis;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.infinity.utils.Encryptor;
import com.infinity.utils.HttpRequest;
import com.infinity.wefriends.NotifierService;
import com.infinity.wefriends.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class Messages {
	protected Users users = null;
	protected Context m_context;
	protected DatabaseHelper database = null;
	protected String userId = "";
	
	static final public String MESSAGE_TEXT = "text";
	static final public String MESSAGE_IMAGE = "image";
	static final public String MESSAGE_FILE  = "file";
	static final public String MESSAGE_FRIEND_REQUEST = "friendrequest";
	
	static final public int SUCCESS = 200;
	static final public int FAILED = 201;
	
	public Messages(Context context) {
		m_context = context;
		users = new Users(context);
		database = DatabaseHelper.getInstance(context,"wefriendsdb");
		ContentValues userInfo = users.getCachedUserInfo();
		if (userInfo!=null)
			userId = userInfo.getAsString("wefriendsid");
	}
	
	static public String timestrampToString(long timestramp) {
		if (new SimpleDateFormat("yyyy/MM/dd").format(timestramp*1000).equals(new SimpleDateFormat("yyyy/MM/dd").format(System.currentTimeMillis())))
			return new SimpleDateFormat("HH:mm").format(timestramp*1000);
		else
			return new SimpleDateFormat("yyyy/MM/dd HH:mm").format(timestramp*1000);
	}
	
	public int sendMessage(ContentValues message) {
		HttpRequest.Response response = new HttpRequest.Response();
		String requestURL = "http://" + m_context.getString(R.string.server_host) + ":" + m_context.getString(R.string.server_web_service_port) + "/messages/sendmessage";
		List<NameValuePair> postFields = new ArrayList<NameValuePair>();
		postFields.add(new BasicNameValuePair("accesstoken", users.getCachedAccessToken()));
		postFields.add(new BasicNameValuePair("messagetype", message.getAsString("messagetype")));
		postFields.add(new BasicNameValuePair("chatgroup", message.getAsString("chatgroup")));
		postFields.add(new BasicNameValuePair("receivers", message.getAsString("receivers")));
		postFields.add(new BasicNameValuePair("message",Encryptor.autoEncrypt(message.getAsString("message"), message.getAsString("sender"))));
		if (HttpRequest.post(requestURL, postFields, response)==HttpRequest.HTTP_FAILED)
			return FAILED;
		try {
			JSONObject jsonObj = new JSONObject(response.getString());
			if (jsonObj.getInt("status")!=200) {
				users.broadcastReLoginAction();
				return FAILED;
			} else {
				SQLiteDatabase db = database.getWritableDatabase();
				database.safeInsert(db, "messagecache","",message);
				database.safeClose(db);
				return SUCCESS;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return FAILED;
	}
	
	public List<ContentValues> updateContactListWithNewMessages(List<ContentValues> srcList) {
		List<ContentValues> newList = new ArrayList<ContentValues>();
		int contactCount = srcList.size();
		for (int i=0;i<contactCount;i++) {
			ContentValues contact = srcList.get(i);
			int nonHandledMessageCount = getNonHandledMessageCountWith(contact.getAsString("wefriendsid"),"");
			contact.put("newmessagecount", nonHandledMessageCount);
			newList.add(contact);
		}
		return newList;
	}
	
	public int getNonHandledMessageCountWith(String sender, String chatGroup) {
		SQLiteDatabase db = database.getReadableDatabase();
		try {
			Cursor cursor = null;
			if (chatGroup.equals(""))
				cursor = database.safeRawQuery(db, "SELECT * FROM messagecache WHERE ( (sender='"+sender+"') OR (sender='" + userId + "' AND receivers='" + sender + "' ) ) AND chatgroup='' AND ishandled=0 ORDER BY timestramp DESC", new String[]{});
			else
				cursor = database.safeRawQuery(db ,"SELECT * FROM messagecache WHERE chatgroup='" + chatGroup + "' AND ishandled=0 ORDER BY timestramp DESC", new String[]{});
			int count = cursor.getCount();
			database.safeClose(db);
			return count;
		} catch (SQLException e) {
			Log.e("WeFriends","SQLException at Messages.getNonHandledMessageCountWith");
			Log.e("WeFriends",e.getMessage());
		}
		database.safeClose(db);
		return 0;
	}
	
	public List<ContentValues> getAllCachedNonHandledMessage() {	
		List<ContentValues> resultList = new ArrayList<ContentValues>();
		SQLiteDatabase db = database.getReadableDatabase();
		try {
			Cursor cursor = database.safeRawQuery(db, "SELECT * FROM messagecache WHERE ishandled=0 ORDER BY timestramp DESC", new String[]{});
			while (cursor.moveToNext()) {
				ContentValues value = new ContentValues();
				value.put("sender", cursor.getString(cursor.getColumnIndex("sender")));
				value.put("messagetype", cursor.getString(cursor.getColumnIndex("messagetype")));
				value.put("chatgroup", cursor.getString(cursor.getColumnIndex("chatgroup")));
				value.put("timestramp", cursor.getLong(cursor.getColumnIndex("timestramp")));
				value.put("message", cursor.getString(cursor.getColumnIndex("message")));
				value.put("ishandled", cursor.getInt(cursor.getColumnIndex("ishandled")));
				value.put("sendernickname", cursor.getString(cursor.getColumnIndex("sendernickname")));
				value.put("senderavatar", cursor.getString(cursor.getColumnIndex("senderavatar")));
				value.put("notificationid", cursor.getInt(cursor.getColumnIndex("notificationid")));
				value.put("messageid", cursor.getString(cursor.getColumnIndex("messageid")));
				value.put("receivers", cursor.getString(cursor.getColumnIndex("receivers")));
				resultList.add(value);
			}
			
		} catch (SQLException e) {
			Log.e("WeFriends","SQLException at Messages.getNonHandledMessageCountWith");
			Log.e("WeFriends",e.getMessage());
		}
		database.safeClose(db);
		return resultList;
	}
	
	public String getLastMessageFrom(String sender, String chatGroup) {
		SQLiteDatabase db = database.getReadableDatabase();
		try {
			Cursor cursor = null;
			if (chatGroup.equals(""))
				cursor = database.safeRawQuery(db, "SELECT * FROM messagecache WHERE ( (sender='"+sender+"') OR (sender='" + userId + "' AND receivers='" + sender + "' ) ) AND chatgroup='' ORDER BY timestramp DESC LIMIT 1", new String[]{});
			else
				cursor = database.safeRawQuery(db, "SELECT * FROM messagecache WHERE chatgroup='" + chatGroup + "' ORDER BY timestramp DESC LIMIT 1", new String[]{});
			
			if (!cursor.moveToNext()) {
				database.safeClose(db);
				return "";
			}
			String message = cursor.getString(cursor.getColumnIndex("message"));
			database.safeClose(db);
			return message;
		} catch (SQLException e) {
			Log.e("WeFriends","SQLException at Messages.getLastMessageFrom");
			Log.e("WeFriends",e.getMessage());
		}
		database.safeClose(db);
		return "";
	}
	
	public long getLastMessageTimestramp(String sender, String chatGroup) {
		SQLiteDatabase db = database.getReadableDatabase();
		try {
			Cursor cursor = null;
			if (chatGroup.equals(""))
				cursor = database.safeRawQuery(db, "SELECT * FROM messagecache WHERE ( (sender='"+sender+"') OR (sender='" + userId + "' AND receivers='" + sender + "' ) ) AND chatgroup='' ORDER BY timestramp DESC LIMIT 1", new String[]{});
			else
				cursor = database.safeRawQuery(db ,"SELECT * FROM messagecache WHERE chatgroup='" + chatGroup + "' ORDER BY timestramp DESC LIMIT 1", new String[]{});
			
			if (!cursor.moveToNext()) {
				database.safeClose(db);
				return -1;
			}
			long timestramp = cursor.getLong(cursor.getColumnIndex("timestramp"));
			database.safeClose(db);
			return timestramp;
		} catch (SQLException e) {
			Log.e("WeFriends","SQLException at Messages.getLastMessageFrom");
			Log.e("WeFriends",e.getMessage());
		}
		database.safeClose(db);
		return -1;
	}
	
	public List<ContentValues> getAndSaveNewMessages() {
		String accessToken = users.getCachedAccessToken();
		HttpRequest.Response response = new HttpRequest.Response();
		String requestURL = "http://" + m_context.getString(R.string.server_host) + ":" + m_context.getString(R.string.server_web_service_port) + "/messages/getnewmessages?accesstoken=" + accessToken;
		if (HttpRequest.get(requestURL, response) == HttpRequest.HTTP_FAILED)
			return null;
		try {
			JSONObject jsonObj = new JSONObject(response.getString());
			if(jsonObj.getInt("status")!=200) {
				users.broadcastReLoginAction();
				return null;
			}
			if (jsonObj.getInt("count")==0) {
				return null;
			}
			SQLiteDatabase db = database.getWritableDatabase();
			List<ContentValues> messageList = new ArrayList<ContentValues>();
			JSONArray jsonArray = jsonObj.getJSONArray("messages");
			int messageCount = jsonArray.length();
			for (int i=0;i<messageCount;i++) {
				JSONObject messageObj = jsonArray.getJSONObject(i);
				ContentValues message = new ContentValues();
				String sender = URLDecoder.decode(messageObj.getString("sender"),"utf-8");
				message.put("sender", sender);
				message.put("sendernickname", URLDecoder.decode(messageObj.getString("sendernickname"),"utf-8"));
				message.put("senderavatar", URLDecoder.decode(messageObj.getString("senderavatar"),"utf-8"));
				message.put("receivers", userId);
				message.put("messagetype",messageObj.getString("messagetype"));
				message.put("chatgroup", URLDecoder.decode(messageObj.getString("chatgroup"),"utf-8"));
				message.put("timestramp", messageObj.getLong("timestramp"));
				message.put("message", Encryptor.autoDecrypt(URLDecoder.decode(messageObj.getString("message"),"utf-8"),sender));
				message.put("ishandled", 0);
				message.put("notificationid", 0);
				message.put("messageid", generateMessageId());
				messageList.add(message);
				database.safeInsert(db, "messagecache", "", message);
			}
			database.safeClose(db);
			return messageList;
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<ContentValues> getCachedNonHandledMessages(String messageType, String sender, String chatGroup) {
		List<ContentValues> src = getCachedMessages(messageType, sender, chatGroup,0);
		List<ContentValues> newList = new ArrayList<ContentValues>();
		int msgCnt = src.size();
		for (int i=0;i<msgCnt;i++) {
			if (src.get(i).getAsInteger("ishandled").equals(0)) {
				newList.add(src.get(i));
			}
		}
		return newList;
	}
	
	public List<ContentValues> getCachedMessages(String messageType, String sender, String chatGroup, int page) {
		Cursor cursor = null;
		String selectionStr = "";
		boolean firstSelection = true;
		selectionStr = " WHERE";
		if (!messageType.equals("")) {
			selectionStr += (" messagetype='" + messageType + "'");
			firstSelection = false;
		}
		if (!sender.equals("")) {
			if (!firstSelection)
				selectionStr += " AND";
			selectionStr += (" sender='" + sender + "'");
			firstSelection = false;
		}

		if (!firstSelection)
			selectionStr += " AND";
		selectionStr += (" chatgroup='" + chatGroup + "'");
		firstSelection = false;
		
		selectionStr += " ORDER BY timestramp DESC";
		if (page!=0)
			selectionStr += (" LIMIT " + page*15);
		String sqlStr = "SELECT * FROM messagecache" + selectionStr;
		//Log.d("WeFriends","sql=" + sqlStr);
		SQLiteDatabase db = database.getReadableDatabase();
		try {
			cursor = database.safeRawQuery(db, sqlStr, new String[]{});
		} catch (Exception e) {
			database.safeClose(db);
			Log.e("WeFriends","SQL Exception at Messages.getCachedMessages");
			Log.e("WeFriends",e.getMessage());
			Log.e("WeFriends","SQL="+sqlStr);
			return new ArrayList<ContentValues>();
		}
		
		List<ContentValues> resultList = new ArrayList<ContentValues>();
		
		while (cursor.moveToNext()) {
			ContentValues value = new ContentValues();
			value.put("sender", cursor.getString(cursor.getColumnIndex("sender")));
			value.put("messagetype", cursor.getString(cursor.getColumnIndex("messagetype")));
			value.put("chatgroup", cursor.getString(cursor.getColumnIndex("chatgroup")));
			value.put("timestramp", cursor.getLong(cursor.getColumnIndex("timestramp")));
			value.put("message", cursor.getString(cursor.getColumnIndex("message")));
			value.put("ishandled", cursor.getInt(cursor.getColumnIndex("ishandled")));
			value.put("sendernickname", cursor.getString(cursor.getColumnIndex("sendernickname")));
			value.put("senderavatar", cursor.getString(cursor.getColumnIndex("senderavatar")));
			value.put("notificationid", cursor.getInt(cursor.getColumnIndex("notificationid")));
			value.put("messageid", cursor.getString(cursor.getColumnIndex("messageid")));
			value.put("receivers", cursor.getString(cursor.getColumnIndex("receivers")));
			resultList.add(value);
		}
		database.safeClose(db);
		
		if (page==0) {
			return resultList;
		}
		
		int skipCount = (page-1) * 15;
		for (int i=0;i<skipCount;i++) {
			if (resultList.size() > 0)
				resultList.remove(0);
		}
		
		return resultList;
	}
	
	public void bindNotification(String messageId, int notificationId) {
		SQLiteDatabase db = database.getWritableDatabase();
		database.safeExecSQL(db, "UPDATE messagecache SET notificationid=" + notificationId + " WHERE messageid='" + messageId + "'");
		database.safeClose(db);
	}

    public String generateMessageId() {
        String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    	int length = 32;
	    StringBuffer sb = new StringBuffer();
	    Random random = new Random();
	    for (int i = 0; i < length; i++)
	    {
	    	sb.append(allChar.charAt(random.nextInt(allChar.length())));
	    }
	    return sb.toString();
    }
    
    public void markMessagesAsRead(String contact, String chatGroup) {
    	SQLiteDatabase db = database.getWritableDatabase();
    	try {
	    	if (chatGroup.equals("")) {
	    		database.safeExecSQL(db, "UPDATE messagecache SET ishandled=1 WHERE chatgroup='' AND sender='" + contact + "'");
	    	} else {
	    		database.safeExecSQL(db, "UPDATE messagecache SET ishandled=1 WHERE chatgroup='" + chatGroup + "'");
	    	}
    	} catch (SQLException e) {
			Log.e("WeFriends","SQLException at Messages.markMessagesAsRead");
			Log.e("WeFriends",e.getMessage());
    	}
    	database.safeClose(db);
    }
    
    public List<ContentValues> getChatHistory(String contact, String chatGroup, int page) {
    	SQLiteDatabase db = database.getReadableDatabase();
    	List<ContentValues> historyList = new ArrayList<ContentValues>();
    	String sql = null;
    	Cursor cursor = null;
    	if (chatGroup.equals("")) {
    		sql = "SELECT * FROM messagecache WHERE ( (sender='" + userId + "' AND receivers='" + contact + "') OR sender='" + contact + "') AND chatgroup=''";
    	} else {
    		sql = "SELECT * FROM messagecache WHERE chatgroup='" + chatGroup + "'";
    	}
    	sql += " ORDER BY timestramp DESC";
    	if (page>0) {
    		sql += (" LIMIT " + page*15);
    	}
    	try {
    		cursor = database.safeRawQuery(db, sql, new String[]{});
    		while (cursor.moveToNext()) {
    			ContentValues value = new ContentValues();
    			value.put("sender", cursor.getString(cursor.getColumnIndex("sender")));
    			value.put("messagetype", cursor.getString(cursor.getColumnIndex("messagetype")));
    			value.put("chatgroup", cursor.getString(cursor.getColumnIndex("chatgroup")));
    			value.put("timestramp", cursor.getLong(cursor.getColumnIndex("timestramp")));
    			value.put("message", cursor.getString(cursor.getColumnIndex("message")));
    			value.put("ishandled", cursor.getInt(cursor.getColumnIndex("ishandled")));
    			value.put("sendernickname", cursor.getString(cursor.getColumnIndex("sendernickname")));
    			value.put("senderavatar", cursor.getString(cursor.getColumnIndex("senderavatar")));
    			value.put("notificationid", cursor.getInt(cursor.getColumnIndex("notificationid")));
    			value.put("messageid", cursor.getString(cursor.getColumnIndex("messageid")));
    			value.put("receivers", cursor.getString(cursor.getColumnIndex("receivers")));
    			historyList.add(value);
    		}
    	} catch (SQLException e) {
    		Log.e("WeFriends","SQLException at Messages.getChatHistory");
    		Log.e("WeFriends",e.getMessage());
    	}
    	database.safeClose(db);
    	if (page>0) {
    		int skipCount = (page-1) * 15;
    		for (int i=0;i<skipCount;i++) {
    			if (historyList.size() > 0)
    				historyList.remove(0);
    		}
    	}
    	return historyList;
    }
    
}
