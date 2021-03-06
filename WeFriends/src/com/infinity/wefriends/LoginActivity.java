package com.infinity.wefriends;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	public static final int LOGIN_FAILED = 403;
	public static final int CONNECTION_ERROR = -1;
	
	public LoginActivityHandler handler = new LoginActivityHandler();
	
	protected LoginAsyncTask tasks = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		tasks = new LoginAsyncTask(this);
		((Button)findViewById(R.id.loginwindow_login_button)).setOnClickListener(new LoginButtonListener());
	}
	
	protected class LoginButtonListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			String phone = ((EditText)findViewById(R.id.loginwindow_username_edit)).getText().toString();
			String password = ((EditText)findViewById(R.id.loginwindow_password_edit)).getText().toString();
			tasks.login(phone, password);
			((Button)findViewById(R.id.loginwindow_login_button)).setClickable(false);
			((Button)findViewById(R.id.loginwindow_login_button)).setText(R.string.loggingin);
		}
		
	}
	
	public void onLoginFailed() {
		Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
		((Button)findViewById(R.id.loginwindow_login_button)).setClickable(true);
		((Button)findViewById(R.id.loginwindow_login_button)).setText(R.string.login);
	}
	
	public void onConnectionError() {
		Toast.makeText(this, R.string.connection_error, Toast.LENGTH_SHORT).show();
		((Button)findViewById(R.id.loginwindow_login_button)).setClickable(true);
		((Button)findViewById(R.id.loginwindow_login_button)).setText(R.string.login);
	}
	
	protected class LoginActivityHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case LOGIN_FAILED:
				onLoginFailed();
				break;
			case CONNECTION_ERROR:
				onConnectionError();
				break;
			}
			super.handleMessage(msg);
		}
		
	}

}
