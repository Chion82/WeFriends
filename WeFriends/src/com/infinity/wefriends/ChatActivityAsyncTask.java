package com.infinity.wefriends;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.infinity.utils.HttpRequest;
import com.infinity.wefriends.apis.Messages;
import com.infinity.wefriends.apis.Users;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class ChatActivityAsyncTask {
	protected ChatActivity chatActivity = null;
	
	public ChatActivityAsyncTask(ChatActivity parent) {
		chatActivity = parent;
	}
	
	public void loadMessageHistory() {
		new Thread() {
			@Override
			public void run() {
				List<ContentValues> historyList = chatActivity.getHistory();
				ArrayList list = new ArrayList();
				list.add(historyList);
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("history", list);
				Message message = new Message();
				message.setData(bundle);
				message.what = ChatActivity.HISTORY_LOADING_FINISHED;
				chatActivity.handler.sendMessage(message);
				super.run();
			}
		}.start();
	}
	
	public void sendMessage(final ContentValues message, final View messageView) {
		Bundle bundle = new Bundle();
		ArrayList list = new ArrayList();
		list.add(messageView);
		bundle.putParcelableArrayList("messageview", list);
		final Message msg = new Message();
		msg.setData(bundle);
		new Thread() {
			@Override
			public void run() {
				if (chatActivity.messagesAPI.sendMessage(message)==Messages.SUCCESS) {
					msg.what = ChatActivity.MESSAGE_SENDING_FINISHED;
					chatActivity.handler.sendMessage(msg);
					ContentValues chatInfo = new ContentValues();
					chatInfo.put("contact", chatActivity.contactId);
					chatInfo.put("chatgroup", chatActivity.chatGroup);
					chatInfo.put("contactnickname", chatActivity.contactNickname);
					chatInfo.put("contactavatar", chatActivity.contactAvatar);
					chatInfo.put("chattype", message.getAsString("messagetype"));
					chatInfo.put("addtime", message.getAsLong("timestramp"));
					chatActivity.chatsAPI.addChat(chatInfo);
				} else {
					msg.what = ChatActivity.MESSAGE_SENDING_FAILED;
					chatActivity.handler.sendMessage(msg);
				}
				super.run();
			}
			
		}.start();
	}
	
	public void uploadVoice(final String recordFile) {
		new Thread() {
			@Override
			public void run() {
				HttpRequest.Response response = new HttpRequest.Response();
				String url = "http://"
						+ chatActivity.getString(R.string.server_host) + ":" 
						+ chatActivity.getString(R.string.server_web_service_port)
						+ "/files/upload";
				int status = HttpRequest.uploadFile(url, recordFile, "upfile", "application/octet-stream", response);
				if (status == HttpRequest.HTTP_OK) {
					//Log.d("WeFriends",response.getString());
					try {
						JSONObject jsonObj = new JSONObject(response.getString());
						if (jsonObj.getInt("status") == 200) {
							String messageText = jsonObj.getString("url");
							Message msg = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("messagetype", Messages.MESSAGE_VOICE);
							bundle.putString("messagetext", messageText);
							msg.what = ChatActivity.SEND_MESSAGE;
							msg.setData(bundle);
							chatActivity.handler.sendMessage(msg);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				super.run();
			}
		}.start();
	}
	
}
