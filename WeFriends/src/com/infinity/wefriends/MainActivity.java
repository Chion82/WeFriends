package com.infinity.wefriends;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.app.ActionBar.LayoutParams;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.infinity.utils.*;
import com.infinity.wefriends.apis.Messages;
import com.infinity.wefriends.apis.Users;

public class MainActivity extends ActionBarActivity {

	public static final int MAIN_LOADALLONLINEDATA = 100;
	public static final int MAIN_LOADONLINECONTACTLIST = 101;
	public static final int SHOWTOAST =  102;
	public static final int MAIN_HANDLENEWMESSAGES = 103;

	public int currentPage = NavBarButton.CHATS;
	
	protected NavBarButton navBarChats = null;
	protected NavBarButton navBarContacts = null;
	protected NavBarButton navBarDiscovery = null;
	protected NavBarButton navBarMe = null;
	
	protected MainAsyncTask asyncTask = null;
	
	public MainActivityHandler handler = new MainActivityHandler();
	
	protected ViewPager mainViewPager = null;
	protected View chatsView = null;
	protected View friendsView = null;
	protected View discoveryView = null;
	protected View meView = null;
	
	protected AnimatedExpandableListView contactListView = null;
	protected Users usersAPI = null;
	
	protected boolean isNotifierServiceBound = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initNavBar();
		
		LayoutInflater inflater = LayoutInflater.from(this);
		chatsView = inflater.inflate(R.layout.main_chats_layout, null);
		friendsView = inflater.inflate(R.layout.main_friends_layout, null);
		discoveryView = inflater.inflate(R.layout.main_discovery_layout, null);
		meView = inflater.inflate(R.layout.main_me_layout, null);
		
		List<View> viewList = new ArrayList<View>();
		viewList.add(chatsView);
		viewList.add(friendsView);
		viewList.add(discoveryView);
		viewList.add(meView);
		
		ViewListPagerAdapter adapter = new ViewListPagerAdapter(viewList);
		
		mainViewPager = (ViewPager)findViewById(R.id.main_view_pager);
		mainViewPager.setAdapter(adapter);
		mainViewPager.setOnPageChangeListener(mainViewPagerListener);
		
		asyncTask = new MainAsyncTask(this);
		usersAPI = new Users(this);

		
		contactListView = (AnimatedExpandableListView)friendsView.findViewById(R.id.main_contact_list_view);
		
		/*Load Cached Data*/
		loadCachedData();
		
		/*Load Online Data*/
		/*Execute async tasks*/
		/*Should be called after initialization*/
		asyncTask.initCheckUserInfo();
		
	}
	
	public void initNotifierService() {
		Intent intent = new Intent();
		intent.setClass(this, NotifierService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		isNotifierServiceBound = true;
	}
	
	ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	public void loadAllOnlineData() {
		Log.d("WeFriends","Loading All Data.");
		asyncTask.loadOnlineFriendList();
		initNotifierService();
		//TODO
		

	}
	
	public void loadCachedData() {
		List<ContentValues> contactList = usersAPI.getCachedFriendList();
		if (contactList != null)
		{
			loadContactViewList(contactList);
			
		}
		//TODO
	}
	
	public void loadContactViewList(List<ContentValues> contactsInfo) {
		ContactExpandableListAdapter contactListAdapter = new ContactExpandableListAdapter(this, contactsInfo);
		//TODO : Update contact list with non-handled messages
		contactListView.setAdapter(contactListAdapter);
		contactListView.setOnGroupClickListener(new OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				if (contactListView.isGroupExpanded(groupPosition)) {
					contactListView.collapseGroupWithAnimation(groupPosition);
				} else {
					contactListView.expandGroupWithAnimation(groupPosition);
				}
				return true;
			}
		});
		
	}
	
	
	@Override
	protected void onDestroy() {
		if (isNotifierServiceBound)
			unbindService(conn);
		super.onDestroy();
	}


	protected class MainActivityHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MAIN_LOADALLONLINEDATA:
				loadAllOnlineData();
				break;
			case MAIN_LOADONLINECONTACTLIST:
				loadContactViewList((ArrayList<ContentValues>)(msg.getData().getParcelableArrayList("contactlist").get(0)));
				break;
			case SHOWTOAST:
				Toast.makeText(MainActivity.this, msg.getData().getString("text"), Toast.LENGTH_SHORT).show();
				break;
			case MAIN_HANDLENEWMESSAGES:
				
				break;
			}
			super.handleMessage(msg);
		}
		
	}
	
	
	/////////////////////////////UI Initialization///////////////////////////////////////
	/////////////////////////Currently no active modification here/////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	private ViewPager.OnPageChangeListener mainViewPagerListener = new ViewPager.OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int pageIndex) {
			onPageChanged(pageIndex,false);
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
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
	
	public void onPageChanged(int page, boolean changePage) {
		currentPage = page;
		navBarChats.updateState();
		navBarContacts.updateState();
		navBarDiscovery.updateState();
		navBarMe.updateState();
		if (changePage) {
			switch(page) {
			case NavBarButton.CHATS:
				mainViewPager.setCurrentItem(0);
				break;
			case NavBarButton.CONTACTS:
				mainViewPager.setCurrentItem(1);
				break;
			case NavBarButton.DISCOVERY:
				mainViewPager.setCurrentItem(2);
				break;
			case NavBarButton.ME:
				mainViewPager.setCurrentItem(3);
				break;
			}
		}
	}
}
