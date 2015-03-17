package com.infinity.wefriends;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NewMessageReceiver extends BroadcastReceiver {
	
	protected MainActivity mainActivity = null;
	
	public NewMessageReceiver(MainActivity parent) {
		mainActivity = parent;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		mainActivity.loadCachedData();
		//mainActivity.loadAllOnlineData();
		
	}

}
