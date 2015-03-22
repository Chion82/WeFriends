package com.infinity.wefriends;

import java.io.File;
import java.io.IOException;

import com.infinity.utils.HttpRequest;
import com.infinity.utils.Storage;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VoiceMessageView extends LinearLayout {
	
	protected Context m_context = null;
	protected String m_voiceFile = "";
	protected String localFilePath = "";
	protected boolean isVoiceReady = false;
	protected MediaPlayer player = null;
	protected boolean fromRemote = true;
	
	public static final int DOWNLOAD_COMPLETED = 100;

	public VoiceMessageView(Context context, String voiceFile, boolean isVoiceFromRemote) {
		super(context);
		m_context = context;
		m_voiceFile = voiceFile;
		fromRemote = isVoiceFromRemote;
		init();
	}
	
	public VoiceMessageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		m_context = context;
		init();
	}
	
	protected void init() {
		String[] urlArray = m_voiceFile.split("/");
		String fileName = urlArray[urlArray.length-1];
		localFilePath = Storage.getStorageDirectory() + "/WeFriends/cache/audio/" + fileName;
		if (fromRemote) {
			LayoutInflater.from(m_context).inflate(R.layout.chat_message_voice_view_remote, this);
		} else {
			LayoutInflater.from(m_context).inflate(R.layout.chat_message_voice_view_me, this);
		}
		new VoiceMessageThread().start();
		this.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playVoice();
			}
		});
	}
	
	class VoiceMessageThread extends Thread {
		@Override
		public void run() {
			if (!new File(localFilePath).exists()) {
				if (HttpRequest.downloadFile("http://" + m_context.getString(R.string.server_host) + ":" + m_context.getString(R.string.server_web_service_port) + "/" +m_voiceFile, localFilePath)!=HttpRequest.HTTP_OK) {
					return;
				}
			}
			try {
				player = new MediaPlayer();
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				player.setDataSource(localFilePath);
				player.prepare();
				int duration = player.getDuration()/1000;
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putInt("duration", duration);
				msg.setData(bundle);
				msg.what=DOWNLOAD_COMPLETED;
				VoiceMessageView.this.handler.sendMessage(msg);
				isVoiceReady = true;
				player.release();
				player = null;
			} catch (Exception e) {
				Log.e("WeFriends","Error parsing voice file:"+localFilePath);
			}
			super.run();
		}
	}
	
	protected boolean isPlaying = false;
	public void playVoice() {
		if (isVoiceReady && !isPlaying) {
			try {
				player = new MediaPlayer();
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				player.setDataSource(localFilePath);
				player.prepare();
				player.start();
				isPlaying = true;
				player.setOnCompletionListener(new OnCompletionListener() {			
					@Override
					public void onCompletion(MediaPlayer mp) {
						player.release();
						player = null;
						isPlaying = false;
					}
				});
			} catch (Exception e) {
				
			}
		}
	}
	
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWNLOAD_COMPLETED:
				if (fromRemote) {
					((TextView)findViewById(R.id.chat_message_voice_view_text_remote)).setText(msg.getData().getInt("duration")+"s");
				} else {
					((TextView)findViewById(R.id.chat_message_voice_view_text_me)).setText(msg.getData().getInt("duration")+"s");
				}
				break;
			}
			super.handleMessage(msg);
		}
		
	};

}
