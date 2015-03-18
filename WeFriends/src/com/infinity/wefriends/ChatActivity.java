package com.infinity.wefriends;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import jp.sharakova.android.emoji.EmojiEditText;
import jp.sharakova.android.emoji.EmojiTextView;

import com.infinity.utils.Calculations;
import com.infinity.utils.OnlineImageView;
import com.infinity.utils.WrapViewGroup;
import com.infinity.wefriends.apis.Messages;
import com.infinity.wefriends.apis.Users;
import com.nineoldandroids.animation.ObjectAnimator;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class ChatActivity extends ActionBarActivity {
	
	static final public int SCROLL_TO_BOTTOM = 100;
	static final public int LOAD_HISTORY = 101;
	static final public int HISTORY_LOADING_FINISHED = 102;
	static final public int MESSAGE_SENDING_FINISHED = 103;
	static final public int MESSAGE_SENDING_FAILED = 104;
	
	public String contactId = "";
	protected String contactNickname = "";
	protected String contactAvatar = "";
	public String chatGroup = "";
	protected String userId = "";
	protected String userNickname = "";
	protected String userAvatar = "";
	
	protected Users usersAPI = null;
	public Messages messagesAPI = null;
	protected ContentValues userInfo = null;
	protected ChatActivityAsyncTask asyncTask = null;
	
	protected LinearLayout messageListLayout = null;
	protected long lastMessageTimestramp = System.currentTimeMillis()/1000;
	
	protected int currentPage = 1;
	
	protected boolean init = true;
	
	protected ChatMessageReceiver chatMessageReceiver = null;
	
	public ChatActivityHandler handler = new ChatActivityHandler();
	
	protected ScrollView scrollList = null;
	protected EmojiEditText messageEditText = null;
	
	List<String> LoadedMessages = new ArrayList<String>();
	
	protected boolean isLoadingHistory = false;
	
	protected NotificationManager notificationManager = null;
	
	protected boolean isEmotionListVisible = false;
	
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
			userNickname = userInfo.getAsString("nickname");
			userAvatar = userInfo.getAsString("avatar");
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

		chatMessageReceiver = new ChatMessageReceiver(this);
		registerReceiver(chatMessageReceiver, new IntentFilter(NotifierService.NEW_MESSAGE_ACTION));
		
		scrollList = (ScrollView)findViewById(R.id.chat_message_scroll_view);
		scrollList.setOnTouchListener(new ScrollViewListener());
		
		((Button)findViewById(R.id.chat_send_message_button)).setOnClickListener(new SendButtonListener());
		
		messageEditText = (EmojiEditText)findViewById(R.id.chat_message_edit_text);
		
		loadAllEmoji();
		
		((Button)findViewById(R.id.chat_emotion_button)).setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				if (isEmotionListVisible) {
					((ScrollView)findViewById(R.id.chat_emotion_list)).setVisibility(View.GONE);
					isEmotionListVisible = false;
				} else {
					((ScrollView)findViewById(R.id.chat_emotion_list)).setVisibility(View.VISIBLE);
					isEmotionListVisible = true;
				}
			}
		});
		
		/*UI Initialization Completed*/
		
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		
		handler.sendEmptyMessage(LOAD_HISTORY);
		messagesAPI.markMessagesAsRead(contactId, chatGroup);
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
		List<ContentValues> messageHistoryList = messagesAPI.getChatHistory(contactId,chatGroup,currentPage);
		if (messageHistoryList.size()>0)
			currentPage++;
		return messageHistoryList;		
	}
	
	public View addMessageToView(ContentValues message, boolean addToBottom) {
		View messageView = null;
		LinearLayout messageContainerView = null;
		if (isMessageLoaded(message))
			return null;
		if (message.getAsString("messagetype").equals(Messages.MESSAGE_FRIEND_REQUEST))
			return null;
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
			lastMessageTimestramp = message.getAsLong("timestramp");
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
		notificationManager.cancel(message.getAsInteger("notificationid"));
		LoadedMessages.add(message.getAsString("messageid"));
		return messageView;
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
			//TODO : Display Emoji in textView
			EmojiTextView textView = new EmojiTextView(this);
			textView.setEmojiText(message.getAsString("message"));
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
				if (isLoadingHistory)
					break;
				isLoadingHistory = true;
				animatedSetScrollViewMarginTop(0,Calculations.dip2px(ChatActivity.this, 25));
				((ProgressBar)findViewById(R.id.chat_progress_bar)).setVisibility(View.VISIBLE);
				asyncTask.loadMessageHistory();
				break;
			case HISTORY_LOADING_FINISHED:
				loadHistory((List<ContentValues>)(msg.getData().getParcelableArrayList("history").get(0)));
				((ProgressBar)findViewById(R.id.chat_progress_bar)).setVisibility(View.GONE);
				animatedSetScrollViewMarginTop(Calculations.dip2px(ChatActivity.this, 25),0);
				if (init) {
					ChatActivity.this.handler.sendEmptyMessage(SCROLL_TO_BOTTOM);
					init = false;
				}
				isLoadingHistory = false;
				break;
			case MESSAGE_SENDING_FAILED:
				Log.e("WeFriends","Message sending failed.");
			case MESSAGE_SENDING_FINISHED:
				View messageView = (View)msg.getData().getParcelableArrayList("messageview").get(0);
				messageView.findViewById(R.id.chat_message_container_progress_bar).setVisibility(View.GONE);
				break;
			}
			super.handleMessage(msg);
		}
		
	}
	
	public void animatedSetScrollViewMarginTop(int start, int end) {
		ObjectAnimator animation = ObjectAnimator.ofInt(this, "scrollViewMarginTop", start, end);
		animation.setDuration(300);
		animation.start();
	}
	
	public void setScrollViewMarginTop(int marginTop) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)scrollList.getLayoutParams();
		params.setMargins(0, marginTop, 0, Calculations.dip2px(this, 60));
		scrollList.setLayoutParams(params);
	}
	
	public void sendMessage(String messageType, String messageText) {
		String receivers = null;
		if (chatGroup.equals("")) {
			receivers = contactId;
		} else {
			//TODO : for chat-group, add every member id in receivers string.
		}
		ContentValues message = new ContentValues();
		message.put("sender", userId);
		message.put("sendernickname", userNickname);
		message.put("senderavatar", userAvatar);
		message.put("messagetype", messageType);
		message.put("chatgroup", chatGroup);
		message.put("timestramp", System.currentTimeMillis()/1000);
		message.put("message",messageText);
		message.put("receivers",receivers);
		message.put("ishandled", 1);
		message.put("notificationid", 0);
		message.put("messageid", messagesAPI.generateMessageId());
		View messageView = addMessageToView(message, true);
		messageView.findViewById(R.id.chat_message_container_progress_bar).setVisibility(View.VISIBLE);
		asyncTask.sendMessage(message, messageView);
	}
	
	class SendButtonListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			String messageText = messageEditText.getHtml();
			sendMessage("text",messageText);
			((ScrollView)findViewById(R.id.chat_emotion_list)).setVisibility(View.GONE);
			messageEditText.setText("");
		}
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
			if (event.getAction()==MotionEvent.ACTION_MOVE && scrollList.getScrollY()==0 && (event.getY()-initPos>20) && isPressed) {
				handler.sendEmptyMessage(LOAD_HISTORY);
				isPressed = false;
			}
			if (event.getAction()==MotionEvent.ACTION_UP) {
				isPressed = false;
			}
			return false;
		}
	}
	
	protected void loadAllEmoji() {
		WrapViewGroup emojiViewGroup = (WrapViewGroup)findViewById(R.id.chat_emoji_container);
		for (int i=1;i<=45;i++) {
			ImageView emojiBtn = new ImageView(this);
			emojiBtn.setImageResource(R.drawable.e01 + (i-1));
			emojiBtn.setOnClickListener(new EmojiOnClickListener(i));		
			emojiViewGroup.addView(emojiBtn);
		}
	}
	
	protected class EmojiOnClickListener implements View.OnClickListener {
		int index = 1;
		public EmojiOnClickListener(int emojiIndex) {
			index = emojiIndex;
		}
		@Override
		public void onClick(View v) {
			messageEditText.setEmojiText(messageEditText.getHtml() + "<img src=\"se" + ((index<10)?"0":"") + index + "\"/>");
		}
		
	}
	
}
 