package com.infinity.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class HttpRequest {
	
	public static final int HTTP_OK = 1;
	public static final int HTTP_FAILED = 0;
	
	public static final class Response {
		protected String str;
		public void setString(String string) {
			str = string;
		}
		public String getString() {
			return str;
		}
	}
	
	public static final int get(String url, HttpRequest.Response response) {
		HttpGet request = new HttpGet(url);
		
		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200)
				return HTTP_FAILED;
			response.setString(EntityUtils.toString(httpResponse.getEntity()));
			
		} catch (Exception e) {
			Log.e("WeFriends",e.getMessage());
			return HTTP_FAILED;
		}
		return HTTP_OK;
	}
	
	public static final int post(String url, List<NameValuePair> postFields, HttpRequest.Response response) {
		HttpPost request = new HttpPost(url);
		
		try {
			request.setEntity(new UrlEncodedFormEntity(postFields, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200)
				return HTTP_FAILED;
			response.setString(EntityUtils.toString(httpResponse.getEntity()));
			
		} catch (Exception e) {
			Log.e("WeFriends",e.getMessage());
			return HTTP_FAILED;
		}		
		
		return HTTP_OK;
	}
	
	public static final int downloadFile(String url,String filePath) {
		HttpGet request = new HttpGet(url);
		
		try {
			HttpResponse httpResponse = new DefaultHttpClient().execute(request);
			if (httpResponse.getStatusLine().getStatusCode() != 200)
				return HTTP_FAILED;
			byte[] buffer = EntityUtils.toByteArray(httpResponse.getEntity());
			
			File file = new File(filePath);
			if (!file.exists())
				file.createNewFile();
			
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(buffer);
			stream.flush();
			stream.close();
		
		} catch (Exception e) {
			Log.e("WeFriends",e.getMessage());
			return HTTP_FAILED;
		}
		return HTTP_OK;
	}

}
