package com.infinity.wefriends.apis;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.infinity.utils.*;
import com.infinity.wefriends.MainActivity;
import com.infinity.wefriends.R;
import com.infinity.wefriends.apis.DatabaseHelper;

public class Users {
	protected DatabaseHelper database = null;
	protected Context m_context = null;
	
	public static final int TOKEN_VALID = 200;
	public static final int TOKEN_INVALID = 403;
	public static final int CONNECTION_ERROR = -1;
	
	public static final int LOGIN_OK = 200;
	public static final int LOGIN_FAILED = 403;
	
	public Users(Context context) {
		m_context = context;
		database = new DatabaseHelper(context,"wefriendsdb");
	}
	
	public int authenticateCachedAccessToken() {
		SQLiteDatabase db = database.getReadableDatabase();
		Cursor cursor = db.query("usercache", new String[]{"accesstoken"}, "", new String[]{}, "", "", "", "1");
		if (!cursor.moveToNext()) {
			db.close();
			return TOKEN_INVALID;
		}
		String accessToken = cursor.getString(cursor.getColumnIndex("accesstoken"));
		db.close();
		return authenticateAccessToken(accessToken);
	}
	
	public int authenticateAccessToken(String accessToken) {
		HttpRequest.Response response = new HttpRequest.Response();
		String requestURL = "http://" + m_context.getString(R.string.server_host) + ":" + m_context.getString(R.string.server_web_service_port) + "/users/getuserinfobytoken?accesstoken=" + accessToken;
		if(HttpRequest.get(requestURL,response) == HttpRequest.HTTP_FAILED)
			return CONNECTION_ERROR;
		try {
			JSONObject jsonObj = new JSONObject(response.getString());
			if (jsonObj.getInt("status")==200) {
				return TOKEN_VALID;
			} else {
				return TOKEN_INVALID;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return CONNECTION_ERROR;
	}
	
	public int login(String phone, String password) {
		HttpRequest.Response response = new HttpRequest.Response();
		String requestURL = "http://" + m_context.getString(R.string.server_host) + ":" + m_context.getString(R.string.server_web_service_port) + "/users/login";
		List<NameValuePair> postFields = new ArrayList<NameValuePair>();
		postFields.add(new BasicNameValuePair("phone",phone));
		postFields.add(new BasicNameValuePair("password",password));
		if (HttpRequest.post(requestURL, postFields, response) == HttpRequest.HTTP_FAILED)
			return CONNECTION_ERROR;
		try {
			JSONObject jsonObj = new JSONObject(response.getString());
			Log.d("test",jsonObj.getInt("status")+"");
			if (jsonObj.getInt("status")==200) {
				JSONObject userInfo = jsonObj.getJSONObject("userinfo");
				SQLiteDatabase db = database.getWritableDatabase();
				db.execSQL("DELETE FROM usercache");
				ContentValues values = new ContentValues();
				values.put("accesstoken", jsonObj.getString("accesstoken"));			
				values.put("wefriendsid", URLDecoder.decode(userInfo.getString("wefriendsid"),"utf-8"));
				values.put("nickname", URLDecoder.decode(userInfo.getString("nickname"),"utf-8"));
				values.put("avatar", URLDecoder.decode(userInfo.getString("avatar"),"utf-8"));
				values.put("phone", URLDecoder.decode(userInfo.getString("phone"),"utf-8"));
				values.put("email", URLDecoder.decode(userInfo.getString("email"),"utf-8"));
				values.put("intro", URLDecoder.decode(userInfo.getString("intro"),"utf-8"));
				values.put("gender", userInfo.getInt("gender"));
				values.put("region", URLDecoder.decode(userInfo.getString("region"),"utf-8"));
				values.put("collegeid", URLDecoder.decode(userInfo.getString("collegeid"),"utf-8"));
				values.put("whatsup", URLDecoder.decode(userInfo.getString("whatsup"),"utf-8"));
				db.insert("usercache", "", values);
				db.close();
				return LOGIN_OK;
			} else {
				return LOGIN_FAILED;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return CONNECTION_ERROR;
	}
	
	public ContentValues getCachedUserInfo() {
		SQLiteDatabase db = database.getReadableDatabase();
		Cursor cursor = db.query("usercache", new String[]{"wefriendsid","nickname","avatar","phone","email","intro","gender","region","collegeid","whatsup"}, "", new String[]{}, "", "", "","1");
		if (!cursor.moveToNext()) {
			db.close();
			return null;
		}
		ContentValues values = new ContentValues();
		values.put("wefriendsid", cursor.getString(cursor.getColumnIndex("wefriendsid")));
		values.put("nickname", cursor.getString(cursor.getColumnIndex("nickname")));
		values.put("avatar", cursor.getString(cursor.getColumnIndex("avatar")));
		values.put("phone", cursor.getString(cursor.getColumnIndex("phone")));
		values.put("email", cursor.getString(cursor.getColumnIndex("email")));
		values.put("intro", cursor.getString(cursor.getColumnIndex("intro")));
		values.put("gender", cursor.getInt(cursor.getColumnIndex("gender")));
		values.put("region", cursor.getString(cursor.getColumnIndex("region")));
		values.put("collegeid", cursor.getString(cursor.getColumnIndex("collegeid")));
		values.put("whatsup", cursor.getString(cursor.getColumnIndex("whatsup")));
		return values;
	}
	
	public ContentValues getAndSaveUserInfo() {
		HttpRequest.Response response = new HttpRequest.Response();
		String accessToken = getCachedAccessToken();
		String requestURL = "http://" + m_context.getString(R.string.server_host) + ":" + m_context.getString(R.string.server_web_service_port) + "/users/getuserinfobytoken?accesstoken=" + accessToken;
		if (HttpRequest.get(requestURL, response)==HttpRequest.HTTP_FAILED) {
			return null;
		}
		try {
			JSONObject jsonObj = new JSONObject(response.getString());
			if (jsonObj.getInt("status")!=200) {
				broadcastReLoginAction();
				return null;
			}
			JSONObject userInfo = jsonObj.getJSONObject("userinfo");
			SQLiteDatabase db = database.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("wefriendsid", URLDecoder.decode(userInfo.getString("wefriendsid"),"utf-8"));
			values.put("nickname", URLDecoder.decode(userInfo.getString("nickname"),"utf-8"));
			values.put("avatar", URLDecoder.decode(userInfo.getString("avatar"),"utf-8"));
			values.put("phone", URLDecoder.decode(userInfo.getString("phone"),"utf-8"));
			values.put("email", URLDecoder.decode(userInfo.getString("email"),"utf-8"));
			values.put("intro", URLDecoder.decode(userInfo.getString("intro"),"utf-8"));
			values.put("gender", userInfo.getInt("gender"));
			values.put("region", URLDecoder.decode(userInfo.getString("region"),"utf-8"));
			values.put("collegeid", URLDecoder.decode(userInfo.getString("collegeid"),"utf-8"));
			values.put("whatsup", URLDecoder.decode(userInfo.getString("whatsup"),"utf-8"));
			db.update("usercache", values, "", new String[]{});
			db.close();
			return values;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public List<ContentValues> getCachedFriendList() {
		SQLiteDatabase db = database.getReadableDatabase();
		Cursor cursor = db.query("friendscache", new String[]{"wefriendsid", "nickname", "avatar", "whatsup", "intro", "gender", "region", "collegeid", "friendgroup"}, "", new String[]{}, "", "", "");
		if (cursor.getCount() == 0)
			return null;
		List<ContentValues> list = new ArrayList<ContentValues>();
		ContentValues friendInfo = null;
		while (cursor.moveToNext()) {
			friendInfo = new ContentValues();
			friendInfo.put("wefriendsid", cursor.getString(cursor.getColumnIndex("wefriendsid")));
			friendInfo.put("nickname", cursor.getString(cursor.getColumnIndex("nickname")));
			friendInfo.put("avatar", cursor.getString(cursor.getColumnIndex("avatar")));
			friendInfo.put("whatsup", cursor.getString(cursor.getColumnIndex("whatsup")));
			friendInfo.put("intro", cursor.getString(cursor.getColumnIndex("intro")));
			friendInfo.put("gender", cursor.getInt(cursor.getColumnIndex("gender")));
			friendInfo.put("region", cursor.getString(cursor.getColumnIndex("region")));
			friendInfo.put("collegeid", cursor.getString(cursor.getColumnIndex("collegeid")));
			friendInfo.put("friendgroup", cursor.getString(cursor.getColumnIndex("friendgroup")));
			list.add(friendInfo);
		}
		return list;
	}
	
	public List<ContentValues> getAndSaveFriendList() {
		String accessToken = getCachedAccessToken();
		HttpRequest.Response response = new HttpRequest.Response();
		String requestURL = "http://" + m_context.getString(R.string.server_host) + ":" + m_context.getString(R.string.server_web_service_port) + "/users/getfriendlist?accesstoken=" + accessToken;
		if (HttpRequest.get(requestURL, response)==HttpRequest.HTTP_FAILED) {
			return null;
		}
		try {
			JSONObject jsonObj = new JSONObject(response.getString());
			if (jsonObj.getInt("status")!=200) {
				broadcastReLoginAction();
				return null;
			}
			SQLiteDatabase db = database.getWritableDatabase();
			db.execSQL("DELETE FROM friendscache");
			JSONArray friendArray = jsonObj.getJSONArray("friendlist");
			JSONObject person = null;
			ContentValues info = null;
			List<ContentValues> friendList = new ArrayList<ContentValues>();
			int personCnt = friendArray.length();
			for (int i=0;i<personCnt;i++) {
				person = friendArray.getJSONObject(i);
				info = new ContentValues();
				info.put("wefriendsid", URLDecoder.decode(person.getString("wefriendsid"),"utf-8"));
				info.put("nickname", URLDecoder.decode(person.getString("nickname"),"utf-8"));
				info.put("avatar", URLDecoder.decode(person.getString("avatar"),"utf-8"));
				info.put("whatsup", URLDecoder.decode(person.getString("whatsup"),"utf-8"));
				info.put("intro", URLDecoder.decode(person.getString("intro"),"utf-8"));
				info.put("gender", person.getInt("gender"));
				info.put("region", URLDecoder.decode(person.getString("region"),"utf-8"));
				info.put("collegeid", URLDecoder.decode(person.getString("collegeid"),"utf-8"));
				info.put("friendgroup", URLDecoder.decode(person.getString("friendgroup"),"utf-8"));
				db.insert("friendscache", "", info);
				friendList.add(info);
			}
			db.close();
			return friendList;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String getCachedAccessToken() {
		SQLiteDatabase db = database.getReadableDatabase();
		Cursor cursor = db.query("usercache", new String[]{"accesstoken"}, "", new String[]{}, "", "", "", "1");
		if (!cursor.moveToNext()) {
			db.close();
			broadcastReLoginAction();
			return null;
		}
		String accessToken = cursor.getString(cursor.getColumnIndex("accesstoken"));
		db.close();
		return accessToken;
	}
	
	public void broadcastReLoginAction() {
		Intent intent = new Intent();
		intent.setAction("WEFRIENDS_RELOGIN");
		m_context.sendBroadcast(intent);
	}
	
}
