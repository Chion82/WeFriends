package com.infinity.wefriends;

import java.util.List;

import jp.sharakova.android.emoji.EmojiTextView;

import com.infinity.utils.OnlineImageView;
import com.infinity.utils.Storage;
import com.infinity.wefriends.apis.Messages;
import com.infinity.wefriends.apis.Users;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatListAdapter extends BaseAdapter {
	
	protected List<ContentValues> chatList = null;
	protected Context m_context = null;
	protected LayoutInflater inflater = null;
	protected Messages messagesAPI = null;
	
	protected Users usersAPI = null;
	protected String userId = "";
	
	public ChatListAdapter(Context context, List<ContentValues> listValues) {
		chatList = listValues;
		m_context = context;
		inflater = LayoutInflater.from(context);
		messagesAPI = new Messages(context);
		usersAPI = new Users(context);
		ContentValues userInfo = usersAPI.getCachedUserInfo();
		if (userInfo!=null) {
			userId = userInfo.getAsString("wefriendsid");
		}
	}

	@Override
	public int getCount() {
		return chatList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(final int pos, View arg1, ViewGroup arg2) {
		final ContentValues chatInfo = chatList.get(pos);
		View itemView = inflater.inflate(R.layout.contact_list_item_view, null);
		TextView mainTitle = (TextView)itemView.findViewById(R.id.contact_list_item_view_main_title);
		EmojiTextView subtitle = (EmojiTextView)itemView.findViewById(R.id.contact_list_item_view_subtitle);
		OnlineImageView image = (OnlineImageView)itemView.findViewById(R.id.contact_list_item_view_avatar);
		TextView messageNotification = (TextView)itemView.findViewById(R.id.chat_item_notification);
		String chatType = chatInfo.getAsString("chattype");
		if (chatType.equals(Messages.MESSAGE_FRIEND_REQUEST)) {
			mainTitle.setText(m_context.getString(R.string.friend_request));
			subtitle.setText(chatInfo.getAsString("contactnickname"));
		} else {
			if (chatInfo.getAsString("chatgroup").equals("")) {
				mainTitle.setText(chatInfo.getAsString("contactnickname"));
				if (chatInfo.getAsString("chattype").equals(Messages.MESSAGE_TEXT))
					subtitle.setEmojiText(messagesAPI.getLastMessageFrom(chatInfo.getAsString("contact"), chatInfo.getAsString("chatgroup")).replaceAll("<.*?>", " "));
				else if (chatInfo.getAsString("chattype").equals(Messages.MESSAGE_VOICE))
					subtitle.setText(m_context.getString(R.string.message_audio));
			} else {
				mainTitle.setText(chatInfo.getAsString("chatgroup"));
				if (!chatInfo.getAsString("contact").equals("")) {
					if (chatInfo.getAsString("chattype").equals(Messages.MESSAGE_TEXT))
						subtitle.setText(chatInfo.getAsString("contactnickname") + " : " + messagesAPI.getLastMessageFrom(chatInfo.getAsString("contact"), chatInfo.getAsString("chatgroup")));
					else
						subtitle.setText(chatInfo.getAsString("contactnickname"));
				}
			}
		}
		subtitle.setText(subtitle.getText().toString().replaceAll("^static/upload.*?\\.amr$", m_context.getString(R.string.message_audio)));
		subtitle.setText(subtitle.getText().toString().replaceAll("^static/upload.*?\\.(jpg|png|gif|jpeg)$", m_context.getString(R.string.message_audio)));
		int newMsgCnt = messagesAPI.getNonHandledMessageCountWith(chatInfo.getAsString("contact"),chatInfo.getAsString("chatgroup"));
		if (newMsgCnt>0)
			messageNotification.setVisibility(View.VISIBLE);
		if (newMsgCnt>99)
			messageNotification.setText("99");
		else
			messageNotification.setText(newMsgCnt + "");
		image.asyncLoadOnlineImage("http://" + m_context.getString(R.string.server_host) + ":" + m_context.getString(R.string.server_web_service_port) + "/" + chatInfo.getAsString("contactavatar"),Storage.getStorageDirectory()+"/wefriends/cache");
		long lastMessageTime = messagesAPI.getLastMessageTimestramp(chatInfo.getAsString("contact"), chatInfo.getAsString("chatgroup"));
		if (lastMessageTime>0) {
			((TextView)itemView.findViewById(R.id.contact_list_item_view_time)).setText(Messages.timestrampToString(lastMessageTime));
		}
		
		itemView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("contactid", chatInfo.getAsString("contact"));
				intent.putExtra("contactnickname", chatInfo.getAsString("contactnickname"));
				intent.putExtra("contactavatar", chatInfo.getAsString("contactavatar"));
				intent.putExtra("chatgroup", chatInfo.getAsString("chatgroup"));
				intent.setClass(m_context, ChatActivity.class);
				m_context.startActivity(intent);
			}
		});
		
		MainActivity.chatsInfo.add(pos, chatInfo);
		itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.add(MainActivity.CONTEXT_MENU_GROUP_PIN_TO_TOP, pos, 1, R.string.pin_to_top);
				menu.add(MainActivity.CONTEXT_MENU_GROUP_DELETE_CHAT, pos, 2, R.string.delete_chat);
			}
		});
		
		return itemView;
	}

}
