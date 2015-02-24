package com.infinity.wefriends;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import com.infinity.wefriends.apis.Users;

public class LoginAsyncTask {
	Users users = null;
	Context m_context = null;
	
	public LoginAsyncTask(Context context) {
		m_context = context;
		users = new Users(context);
	}
	
	public void login(String phone, String password) {
		LoginThread loginThread = new LoginThread(phone,password);
		loginThread.start();
	}
	
	protected class LoginThread extends Thread {
		protected String m_phone,m_password;
		public LoginThread(String phone, String password) {
			m_phone = phone;
			m_password = password;
		}
		@Override
		public void run() {
			int result = users.login(m_phone, m_password);
			switch(result) {
			case Users.LOGIN_OK:
				Intent intent = new Intent();
				intent.setClass(m_context, MainActivity.class);
				m_context.startActivity(intent);
				((Activity)m_context).finish();
				break;
			case Users.LOGIN_FAILED:
				((LoginActivity)m_context).handler.sendEmptyMessage(LoginActivity.LOGIN_FAILED);
				break;
			case Users.CONNECTION_ERROR:
				((LoginActivity)m_context).handler.sendEmptyMessage(LoginActivity.CONNECTION_ERROR);
				break;
			}
			super.run();
		}
		
	}
}
