package com.infinity.wefriends;

import java.util.ArrayList;
import java.util.List;

import com.infinity.wefriends.apis.Users;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainAsyncTask {
	Users users = null;
	Context m_context = null;
	
	public MainAsyncTask(Context context) {
		m_context = context;
		users = new Users(context);
	}
	
	public void initCheckUserInfo() {
		new Thread() {
			@Override
			public void run() {
				if (users.authenticateCachedAccessToken()==Users.TOKEN_INVALID) {
					Intent intent = new Intent();
					intent.setClass(m_context,LoginActivity.class);
					m_context.startActivity(intent);
					((MainActivity)m_context).finish();
				} else {
					((MainActivity)m_context).handler.sendEmptyMessage(MainActivity.MAIN_LOADALLONLINEDATA);
				}
				super.run();
			}
		}.start();
	}
	
	public void loadOnlineFriendList() {
		new Thread() {
			@Override
			public void run() {
				List<ContentValues> values = null;
				if ((values = users.getAndSaveFriendList()) != null) {
					Message msg = new Message();
					Bundle bundle = new Bundle();
					ArrayList list = new ArrayList();
					list.add(values);
					bundle.putParcelableArrayList("contactlist", list);
					msg.setData(bundle);
					msg.what = MainActivity.MAIN_LOADONLINECONTACTLIST;
					((MainActivity)m_context).handler.sendMessage(msg);
				}
				super.run();
			}
		}.start();
	}

}
