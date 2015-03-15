package com.infinity.wefriends;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Message;

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
	
}
