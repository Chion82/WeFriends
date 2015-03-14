package com.infinity.wefriends;

import java.util.List;

import com.infinity.wefriends.apis.Messages;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

public class ChatMessageReceiver extends BroadcastReceiver {
	
	ChatActivity chatActivity = null;
	
	public ChatMessageReceiver(ChatActivity parent) {
		chatActivity = parent;
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		List<ContentValues> messageList = null;
		if (chatActivity.chatGroup.equals(""))
			messageList = chatActivity.messagesAPI.getCachedNonHandledMessages("","",chatActivity.chatGroup);
		else
			messageList = chatActivity.messagesAPI.getCachedNonHandledMessages("", chatActivity.contactId, "");
		int count = messageList.size();
		for (int i=count-1;i>=0;i--) {
			chatActivity.addMessageToView(messageList.get(i), true);
		}
		chatActivity.messagesAPI.markMessagesAsRead(chatActivity.contactId, chatActivity.chatGroup);
	}

}
