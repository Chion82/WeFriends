package com.infinity.wefriends.apis;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.infinity.utils.HttpRequest;
import com.infinity.wefriends.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Messages {
	protected Users users = null;
	protected Context m_context;
	protected DatabaseHelper database = null;
	
	public Messages(Context context) {
		m_context = context;
		users = new Users(context);
		database = new DatabaseHelper(context,"wefriendsdb");
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
				message.put("sender", URLDecoder.decode(messageObj.getString("sender"),"utf-8"));
				message.put("sendernickname", URLDecoder.decode(messageObj.getString("sendernickname"),"utf-8"));
				message.put("senderavatar", URLDecoder.decode(messageObj.getString("senderavatar"),"utf-8"));
				message.put("messagetype",messageObj.getString("messagetype"));
				message.put("chatgroup", URLDecoder.decode(messageObj.getString("chatgroup"),"utf-8"));
				message.put("timestramp", messageObj.getLong("timestramp"));
				message.put("message", URLDecoder.decode(messageObj.getString("message"),"utf-8"));
				message.put("ishandled", 0);
				message.put("notificationid", 0);
				message.put("messageid", generateMessageId());
				messageList.add(message);
				db.insert("messagecache", "", message);
			}
			db.close();
			return messageList;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<ContentValues> getCachedMessages(String messageType, String sender, String chatGroup, int page) {
		SQLiteDatabase db = database.getReadableDatabase();
		Cursor cursor = null;
		String selectionStr = "";
		boolean firstSelection = true;
		if (messageType!="" || sender!="" || chatGroup!="")
			selectionStr = " WHERE";
		if (messageType!="")
		{
			selectionStr += (" messagetype='" + messageType + "'");
			firstSelection = false;
		}
		if (sender!="")
		{
			if (!firstSelection)
				selectionStr += " AND";
			selectionStr += (" sender='" + sender + "'");
			firstSelection = false;
		}
		if (chatGroup!="")
		{
			if (!firstSelection)
				selectionStr += " AND";
			selectionStr += (" chatgroup='" + chatGroup + "'");
			firstSelection = false;
		}
		selectionStr += " ORDER BY timestramp DESC";
		if (page!=0)
			selectionStr += (" LIMIT " + page*15);
		String sqlStr = "SELECT * FROM messagecache" + selectionStr;
		Log.d("WeFriends","sql=" + sqlStr);
		cursor = db.rawQuery(sqlStr, new String[]{});
		
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
			resultList.add(value);
		}
		db.close();
		
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
		db.execSQL("UPDATE messagecache SET notificationid=" + notificationId + " WHERE messageid='" + messageId + "'");
		db.close();
	}

    public String generateMessageId()
    {
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
}
