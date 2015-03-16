package com.infinity.wefriends;

import java.util.ArrayList;
import java.util.List;

import com.infinity.wefriends.apis.Messages;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Message;
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
				} else {
					msg.what = ChatActivity.MESSAGE_SENDING_FAILED;
					chatActivity.handler.sendMessage(msg);
				}
				super.run();
			}
			
		}.start();
	}
	
}
