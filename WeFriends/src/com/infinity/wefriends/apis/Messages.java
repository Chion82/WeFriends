package com.infinity.wefriends.apis;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.infinity.utils.HttpRequest;
import com.infinity.wefriends.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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
				message.put("sender", messageObj.getString("sender"));
				message.put("messagetype", messageObj.getString("messagetype"));
				message.put("chatgroup", messageObj.getString("chatgroup"));
				message.put("timestramp", messageObj.getLong("timestramp"));
				message.put("message", messageObj.getString("message"));
				message.put("ishandled", 0);
				messageList.add(message);
				db.insert("messagecache", "", message);
			}
			db.close();
			return messageList;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<ContentValues> getCachedMessages(String messageType, String sender, String chatGroup, int page) {
		return null;
	}

}
