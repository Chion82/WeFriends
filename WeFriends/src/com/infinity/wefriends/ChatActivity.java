package com.infinity.wefriends;

import java.util.ArrayList;
import java.util.List;

import com.infinity.utils.OnlineImageView;
import com.infinity.wefriends.apis.Messages;
import com.infinity.wefriends.apis.Users;

import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class ChatActivity extends ActionBarActivity {
	
	static final public int SCROLL_TO_BOTTOM = 100;
	static final public int LOAD_HISTORY = 101;
	static final public int HISTORY_LOADING_FINISHED = 102;
	
	public String contactId = "";
	protected String contactNickname = "";
	protected String contactAvatar = "";
	public String chatGroup = "";
	protected String userId = "";
	
	protected Users usersAPI = null;
	public Messages messagesAPI = null;
	protected ContentValues userInfo = null;
	protected ChatActivityAsyncTask asyncTask = null;
	
	protected LinearLayout messageListLayout = null;
	protected long lastMessageTimestramp = System.currentTimeMillis()/1000;
	
	protected int currentPage = 0;
	
	protected boolean init = true;
	
	protected ChatMessageReceiver chatMessageReceiver = null;
	
	public ChatActivityHandler handler = new ChatActivityHandler();
	
	ScrollView scrollList = null;
	
	List<String> LoadedMessages = new ArrayList<String>();
	
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
		asyncTask = new ChatActivityAsyncTask(this);
		
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
		
		handler.sendEmptyMessage(LOAD_HISTORY);
		messagesAPI.markMessagesAsRead(contactId, chatGroup);
		
		chatMessageReceiver = new ChatMessageReceiver(this);
		registerReceiver(chatMessageReceiver, new IntentFilter(NotifierService.NEW_MESSAGE_ACTION));
		
		scrollList = (ScrollView)findViewById(R.id.chat_message_scroll_view);
		scrollList.setOnTouchListener(new ScrollViewListener());	
		
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return super.onSupportNavigateUp();
	}
	
	public void loadHistory(List<ContentValues> history) {
		int count = history.size();
		for (int i=0;i<count;i++) {
			addMessageToView(history.get(i),false);
		}
	}
	
	public List<ContentValues> getHistory() {
		List<ContentValues> messageHistoryList = null;
		if (!chatGroup.equals(""))
			messageHistoryList = messagesAPI.getCachedMessages("", "", chatGroup, currentPage+1);
		else
			messageHistoryList = messagesAPI.getCachedMessages("", contactId, "", currentPage+1);
		if (messageHistoryList.size()>0)
			currentPage++;
		return messageHistoryList;		
	}
	
	public void addMessageToView(ContentValues message, boolean addToBottom) {
		View messageView = null;
		LinearLayout messageContainerView = null;
		if (isMessageLoaded(message))
			return;
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
			ChatActivity.this.handler.sendEmptyMessage(SCROLL_TO_BOTTOM);
		} else {
			messageListLayout.addView(messageView,0);
			if (timeView!=null)
				messageListLayout.addView(timeView,0);
		}
		lastMessageTimestramp = message.getAsLong("timestramp");
		LoadedMessages.add(message.getAsString("messageid"));
	}
	
	protected boolean isMessageLoaded(ContentValues message) {
		int count = LoadedMessages.size();
		for (int i=0;i<count;i++)
			if (LoadedMessages.get(i).equals(message.getAsString("messageid")))
				return true;
		return false;
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(chatMessageReceiver);
		super.onDestroy();
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
	
	class ChatActivityHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case SCROLL_TO_BOTTOM:
			    handler.post(new Runnable() {  
			        @Override  
			        public void run() {
			            scrollList.fullScroll(ScrollView.FOCUS_DOWN); 
			        }  
			    });
			    break;
			case LOAD_HISTORY:
				asyncTask.loadMessageHistory();
				((ProgressBar)findViewById(R.id.chat_progress_bar)).setVisibility(View.VISIBLE);
				setScrollViewMarginTop(25);
				break;
			case HISTORY_LOADING_FINISHED:
				loadHistory((List<ContentValues>)(msg.getData().getParcelableArrayList("history").get(0)));
				((ProgressBar)findViewById(R.id.chat_progress_bar)).setVisibility(View.GONE);
				setScrollViewMarginTop(0);
				if (init) {
					ChatActivity.this.handler.sendEmptyMessage(SCROLL_TO_BOTTOM);
					init = false;
				}
				break;
			}
			super.handleMessage(msg);
		}
		
	}
	
	public void setScrollViewMarginTop(int marginTop) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)scrollList.getLayoutParams();
		params.setMargins(0, marginTop, 0, 50);
		scrollList.setLayoutParams(params);
	}
	
	class ScrollViewListener implements View.OnTouchListener {
		float initPos = 0;
		boolean isPressed = false;
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction()==MotionEvent.ACTION_DOWN) {
				initPos = event.getY();
				isPressed = true;
			}
			if (event.getAction()==MotionEvent.ACTION_UP && scrollList.getScrollY()==0 && (event.getY()-initPos>20) && isPressed) {
				handler.sendEmptyMessage(LOAD_HISTORY);
				isPressed = false;
			}
			return false;
		}
		
	}

}
 