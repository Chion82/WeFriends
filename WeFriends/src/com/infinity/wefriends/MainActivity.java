package com.infinity.wefriends;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar.LayoutParams;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.infinity.utils.*;
import com.infinity.wefriends.apis.DataBaseHelper;
import com.infinity.wefriends.apis.Users;

public class MainActivity extends ActionBarActivity {
	
	public static final int MAIN_LOADALLONLINEDATA = 100;

	public int currentPage = NavBarButton.CHATS;
	
	protected NavBarButton navBarChats = null;
	protected NavBarButton navBarContacts = null;
	protected NavBarButton navBarDiscovery = null;
	protected NavBarButton navBarMe = null;
	
	protected MainAsyncTask asyncTask = null;
	
	public MainActivityHandler handler = new MainActivityHandler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initNavBar();
		
		asyncTask = new MainAsyncTask(this);
		asyncTask.initCheckUserInfo();

	}
	
	public void loadAllOnlineData() {
		Log.d("WeFriends","Loading All Data.");
		Users users = new Users(this);
		users.getAndSaveUserInfo();
		users.getAndSaveFriendList();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		return super.onOptionsItemSelected(item);
	}
	
	public void onPageChanged(int page) {
		currentPage = page;
		navBarChats.updateState();
		navBarContacts.updateState();
		navBarDiscovery.updateState();
		navBarMe.updateState();
	}
	
	protected void initNavBar() {
		navBarChats = new NavBarButton(getApplicationContext(),NavBarButton.CHATS,this);
		navBarContacts = new NavBarButton(getApplicationContext(),NavBarButton.CONTACTS,this);
		navBarDiscovery = new NavBarButton(getApplicationContext(),NavBarButton.DISCOVERY,this);
		navBarMe = new NavBarButton(getApplicationContext(),NavBarButton.ME,this);
		
		LinearLayout navGroupLayout = (LinearLayout)findViewById(R.id.nav_bar_group);
		navGroupLayout.addView(navBarChats);
		navGroupLayout.addView(navBarContacts);
		navGroupLayout.addView(navBarDiscovery);
		navGroupLayout.addView(navBarMe);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT,1);
		
		navBarChats.setLayoutParams(params);
		navBarContacts.setLayoutParams(params);
		navBarDiscovery.setLayoutParams(params);
		navBarMe.setLayoutParams(params);
		
		navBarChats.setGravity(Gravity.CENTER_HORIZONTAL);
		navBarContacts.setGravity(Gravity.CENTER_HORIZONTAL);
		navBarDiscovery.setGravity(Gravity.CENTER_HORIZONTAL);
		navBarMe.setGravity(Gravity.CENTER_HORIZONTAL);
	}
	
	protected class MainActivityHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MAIN_LOADALLONLINEDATA:
				loadAllOnlineData();
				break;
			}
			super.handleMessage(msg);
		}
		
	}
}
