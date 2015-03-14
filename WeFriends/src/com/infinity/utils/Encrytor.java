package com.infinity.utils;

import android.util.Log;

public class Encrytor {
    static public String getRealKey(String wefriendsId) {
    	String key = null;
    	key = Md5.md5(wefriendsId);
    	if (key!=null)
    		return key.substring(0,8);
    	else
    		return "00000000";
    }
    
    static public String autoDecrypt(String data, String wefriendsId) {
    	String key = getRealKey(wefriendsId);
    	try {
    		byte[] encryptedBuffer = Base64.decode(data);
			byte[] decryptedBuffer = DES.decrypt(encryptedBuffer,key);
			return new String(decryptedBuffer);
		} catch (Exception e) {
			Log.e("WeFriends","Decryption Error.");
			e.printStackTrace();
			return "";
		}
    }
    
    static public String autoEncrypt(String data, String wefriendsId) {
    	String key = getRealKey(wefriendsId);
    	try {
    		byte[] encryptedBuffer = DES.encrypt(data.getBytes(), key);
    		return Base64.encode(encryptedBuffer);
    	} catch (Exception e) {
			Log.e("WeFriends","Encryption Error.");
			e.printStackTrace();
			return "";    		
    	}
    }
}
