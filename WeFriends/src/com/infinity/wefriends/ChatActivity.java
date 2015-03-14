package com.infinity.wefriends;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;

public class ChatActivity extends ActionBarActivity {
	
	protected String contactId = "";
	protected String contactNickname = "";
	protected String contactAvatar = "";
	protected String chatGroup = "";

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
		
		if (contactNickname!=null)
			if (!contactNickname.equals(""))
				setTitle(contactNickname);
		if (chatGroup!=null)
			if (!chatGroup.equals(""))
				setTitle(chatGroup);
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return super.onSupportNavigateUp();
	}

}
 