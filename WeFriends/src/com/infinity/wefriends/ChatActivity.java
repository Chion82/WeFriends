package com.infinity.wefriends;

import java.util.List;

import com.infinity.utils.OnlineImageView;
import com.infinity.wefriends.apis.Messages;
import com.infinity.wefriends.apis.Users;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatActivity extends ActionBarActivity {
	
	protected String contactId = "";
	protected String contactNickname = "";
	protected String contactAvatar = "";
	protected String chatGroup = "";
	protected String userId = "";
	
	protected Users usersAPI = null;
	protected Messages messagesAPI = null;
	protected ContentValues userInfo = null;
	
	protected LinearLayout messageListLayout = null;
	protected long lastMessageTimestramp = System.currentTimeMillis()/1000;
	
	protected int currentPage = 0;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chat_activity_actionbar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		contactId = intent.getStringExtra("contactid");
		contactNickname = intent.getStringExtra("contactnickname");
		contactAvatar = intent.getStringExtra("contactavatar");
		chatGroup = intent.getStringExtra("chatgroup");
		
		usersAPI = new Users(this);
		messagesAPI = new Messages(this);
		
		userInfo = usersAPI.getCachedUserInfo();
		if (userInfo!=null) {
			userId = userInfo.getAsString("wefriendsid");
		}
		
		if (contactId==null) {
			contactId="";
		}
		if (chatGroup==null) {
			chatGroup="";
		}
		if (contactNickname==null) {
			contactNickname="";
		}
		
		if (!contactNickname.equals(""))
			setTitle(contactNickname);
		if (!chatGroup.equals(""))
			setTitle(chatGroup);
		
		messageListLayout = (LinearLayout)findViewById(R.id.chat_message_list);
		
		loadHistory();
		messagesAPI.markMessagesAsRead(contactId, chatGroup);
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return super.onSupportNavigateUp();
	}
	
	protected void loadHistory() {
		List<ContentValues> messageHistoryList = null;
		if (!chatGroup.equals(""))
			messageHistoryList = messagesAPI.getCachedMessages("", "", chatGroup, currentPage+1);
		else
			messageHistoryList = messagesAPI.getCachedMessages("", contactId, "", currentPage+1);
		if (messageHistoryList.size()>0)
			currentPage++;
		int count = messageHistoryList.size();
		for (int i=0;i<count;i++) {
			addMessageToView(messageHistoryList.get(i),false);
		}
	}
	
	protected void addMessageToView(ContentValues message, boolean addToBottom) {
		View messageView = null;
		LinearLayout messageContainerView = null;
		if (message.getAsString("messagetype").equals(Messages.MESSAGE_FRIEND_REQUEST))
			return;
		if (!message.getAsString("sender").equals(userId)) {
			messageView = LayoutInflater.from(this).inflate(R.layout.chat_message_container_remote, null);
			((OnlineImageView)messageView.findViewById(R.id.chat_message_avatar_remote)).asyncLoadOnlineImage("http://" + getString(R.string.server_host) + ":" + getString(R.string.server_web_service_port) + "/" + message.getAsString("senderavatar"),Environment.getExternalStorageDirectory()+"/wefriends/cache");
			if (!message.getAsString("chatgroup").equals("")) {
				TextView senderView = (TextView)messageView.findViewById(R.id.chat_message_contact_nickname);
				senderView.setVisibility(View.VISIBLE);
				senderView.setText(message.getAsString("sendernickname"));
			}
			messageContainerView = (LinearLayout)messageView.findViewById(R.id.chat_message_container_view_remote);
		} else {
			messageView = LayoutInflater.from(this).inflate(R.layout.chat_message_container_me, null);
			((OnlineImageView)messageView.findViewById(R.id.chat_message_avatar_me)).asyncLoadOnlineImage("http://" + getString(R.string.server_host) + ":" + getString(R.string.server_web_service_port) + "/" + message.getAsString("senderavatar"),Environment.getExternalStorageDirectory()+"/wefriends/cache");
			messageContainerView = (LinearLayout)messageView.findViewById(R.id.chat_message_container_view_me);
		}
		messageContainerView.addView(getMessageView(message));
		View timeView = null;
		if (Math.abs(message.getAsLong("timestramp")-lastMessageTimestramp) > 180) {
			timeView = LayoutInflater.from(this).inflate(R.layout.chat_message_time, null);
			((TextView)timeView.findViewById(R.id.chat_message_time_text)).setText(Messages.timestrampToString(message.getAsLong("timestramp")));
		}
		if (addToBottom) {
			if (timeView!=null)
				messageListLayout.addView(timeView);
			messageListLayout.addView(messageView);
		} else {
			messageListLayout.addView(messageView,0);
			if (timeView!=null)
				messageListLayout.addView(timeView,0);
		}
		lastMessageTimestramp = message.getAsLong("timestramp");
	}
	
	protected View getMessageView(ContentValues message) {
		String messageType = message.getAsString("messagetype");
		if (messageType.equals(Messages.MESSAGE_TEXT)) {
			TextView textView = new TextView(this);
			textView.setText(message.getAsString("message"));
			textView.setTextColor(Color.BLACK);
			return textView;
		} else if (messageType.equals(Messages.MESSAGE_IMAGE)) {
			OnlineImageView imageView = new OnlineImageView(this);
			imageView.asyncLoadOnlineImage("http://" + getString(R.string.server_host) + ":" + getString(R.string.server_web_service_port) + "/" + message.getAsString("message"),Environment.getExternalStorageDirectory()+"/wefriends/cache");
			return imageView;
		}
		//TODO : add view for other message types
		return new TextView(this);
	}

}
 