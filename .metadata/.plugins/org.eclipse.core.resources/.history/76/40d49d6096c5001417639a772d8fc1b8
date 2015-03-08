package com.infinity.wefriends;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.support.v4.view.ViewPager;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.infinity.utils.*;
import com.infinity.wefriends.apis.DataBaseHelper;
import com.infinity.wefriends.apis.Users;

public class MainActivity extends ActionBarActivity {

	public static final int MAIN_LOADALLONLINEDATA = 100;
	public static final int MAIN_LOADONLINECONTACTLIST = 101;

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
	
	protected ExpandableListView contactListView = null;
	
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
		
		//TODO
		
		contactListView = (ExpandableListView)friendsView.findViewById(R.id.main_contact_list_view);
		
		/*Execute async tasks*/
		/*Should be called after initialization*/
		asyncTask.initCheckUserInfo();
		asyncTask.loadOnlineFriendList();
		
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
	
	public void loadContactViewList(List<ContentValues> contactsInfo) {
		ContactExpandableListAdapter contactListAdapter = new ContactExpandableListAdapter(this, contactsInfo);
		contactListView.setAdapter(contactListAdapter);
		
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
			case MAIN_LOADONLINECONTACTLIST:
				loadContactViewList((ArrayList<ContentValues>)(msg.getData().getParcelableArrayList("contactlist").get(0)));
				break;
			}
			super.handleMessage(msg);
		}
		
	}
	
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
}
