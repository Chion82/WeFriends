package com.infinity.wefriends;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;

public class ReloginReceiver extends BroadcastReceiver {
	
	MainActivity mainActivity = null;
	
	protected boolean isBroadcastReceived = false;
	
	public ReloginReceiver(MainActivity main) {
		mainActivity = main;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!isBroadcastReceived) {
			mainActivity.finish();
			Intent newIntent= new Intent();
			newIntent.setClass(mainActivity, LoginActivity.class);
			context.startActivity(newIntent);
		}
		isBroadcastReceived = true;
	}
	
	public void restore() {
		isBroadcastReceived = false;
	}
	

}
