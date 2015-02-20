package com.infinity.wefriends.apis;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.infinity.utils.*;
import com.infinity.wefriends.MainActivity;
import com.infinity.wefriends.R;
import com.infinity.wefriends.apis.DataBaseHelper;

public class Users {
	protected DataBaseHelper database = null;
	protected Context m_context = null;
	
	public static final int TOKEN_VALID = 200;
	public static final int TOKEN_INVALID = 403;
	public static final int CONNECTION_ERROR = -1;
	
	public Users(Context context) {
		m_context = context;
		database = new DataBaseHelper(context,"wefriendsdb");
	}
	
	public int authenticateCachedAccessToken() {
		SQLiteDatabase db = database.getReadableDatabase();
		Cursor cursor = db.query("usercache", new String[]{"accesstoken"}, "", new String[]{}, "", "", "", "1");
		if (!cursor.moveToNext())
			return TOKEN_INVALID;
		String accessToken = cursor.getString(cursor.getColumnIndex("accesstoken"));
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
	
}