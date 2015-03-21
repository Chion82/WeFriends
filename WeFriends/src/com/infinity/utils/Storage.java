package com.infinity.utils;

import android.os.Environment;

public class Storage {
	static public String getStorageDirectory() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			return Environment.getExternalStorageDirectory().toString();
		else
			return Environment.getDataDirectory().toString();
	}
}
