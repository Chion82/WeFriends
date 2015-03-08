package com.infinity.utils;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;

public class OnlineImageView extends ImageView {
	
	static public final int LOADBITMAP = 100;
	
	public ImageHandler handler = new ImageHandler();
	
	protected String currentUrl = new String();
	
	class ImageHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case LOADBITMAP:
				OnlineImageView.this.setImageBitmap((Bitmap)(msg.getData().getParcelable("bitmap")));
				break;
			}
			
			super.handleMessage(msg);
		}
		
	}
	
	public void asyncLoadOnlineImage(final String url, final String cacheDirectory) {
		if (currentUrl.equals(url))
			return;
		currentUrl = url;
		
		File directory = new File(cacheDirectory);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String[] urlArray = url.split("/");
		String fileName = urlArray[urlArray.length-1];
		final String localFilePath = cacheDirectory + "/" + fileName;
		if (new File(localFilePath).exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(localFilePath);
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putParcelable("bitmap",bitmap);
			msg.setData(bundle);
			msg.what = OnlineImageView.LOADBITMAP;
			this.handler.sendMessage(msg);
			return;
		}
		
		new Thread() {
			@Override
			public void run() {
				if(HttpRequest.downloadFile(url,localFilePath)==HttpRequest.HTTP_OK) {
					Bitmap bitmap = BitmapFactory.decodeFile(localFilePath);
					Message msg = new Message();
					Bundle bundle = new Bundle();
					bundle.putParcelable("bitmap",bitmap);
					msg.setData(bundle);
					msg.what = OnlineImageView.LOADBITMAP;
					OnlineImageView.this.handler.sendMessage(msg);
				}
				super.run();
			}
		}.start();
	}

	public OnlineImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public OnlineImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public OnlineImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


}
