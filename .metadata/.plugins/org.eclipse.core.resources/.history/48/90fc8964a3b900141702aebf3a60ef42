package com.infinity.wefriends;

import com.infinity.wefriends.apis.Users;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;

public class MainAsyncTask {
	Users users = null;
	Context m_context = null;
	
	public MainAsyncTask(Context context) {
		m_context = context;
		users = new Users(context);
	}
	
	public void initCheckUserInfo() {
		Thread thread = new InitUserCheckThread();
		thread.start();
	}
	
	protected class InitUserCheckThread extends Thread {
		@Override
		public void run() {
			if (users.authenticateCachedAccessToken()==Users.TOKEN_INVALID) {
				Intent intent = new Intent();
				intent.setClass(m_context,LoginActivity.class);
				m_context.startActivity(intent);
				((MainActivity)m_context).finish();
			} else {
				((MainActivity)m_context).handler.sendEmptyMessage(MainActivity.MAIN_LOADALLDATA);
			}
			super.run();
		}
	}

}
