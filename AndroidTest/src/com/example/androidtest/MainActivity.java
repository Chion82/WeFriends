package com.example.androidtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.androidtest.MyService.MyBinder;

import android.support.v7.app.ActionBarActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {
	
	@Override
	protected void onDestroy() {
		
		unregisterReceiver(receiver);

		super.onDestroy();
	}

	protected Button testButton = null;
	protected Button testButton2 = null;
	protected MyReceiver receiver = null;
	
	protected MyBinder binder = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		testButton = (Button)findViewById(R.id.button1);
		testButton2 = (Button)findViewById(R.id.button2);
		
		testButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getContentResolver().query(Uri.parse("content://com.test.myprovider/getData"), new String[]{}, "", new String[]{}, "");
		
			}
		});
		
		testButton2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction("CUSTOM_ACTION");
				intent.putExtra("content", "Howly ho");
				sendBroadcast(intent);
		
			}
		});
		
		
		String jsonDoc = "{\"name\":\"Hello World!\"}";
		try {
			JSONObject obj = new JSONObject(jsonDoc);
			Log.d("test",obj.getString("name"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("CUSTOM_ACTION");
		registerReceiver(receiver, filter);
	
		((Button)findViewById(R.id.button3)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Socket socket = null;
				byte[] buf = new byte[1024];
				int byteCnt;
				
				try {
					socket = new Socket("192.168.1.100",9700);
					
					OutputStream output = socket.getOutputStream();
					InputStream input = socket.getInputStream();
					
					for (int i=0;i<10;i++) {
						
						output.write("Hello World".getBytes());
						byteCnt = 0;
						
						byteCnt = input.read(buf);		
						
						if (byteCnt<=0)
							break;
						
						Log.d("test","data received:");
						Log.d("test",new String(buf,0,byteCnt));
					}
					
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {

				}
				
			}
		});
		
		((Button)findViewById(R.id.button_bind_service)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, MyService.class);
				intent.putExtra("data", "Data From MainActivity");
				bindService(intent,conn,BIND_AUTO_CREATE);
			}
		});
		
		((Button)findViewById(R.id.button_transact_service)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Parcel parcel = Parcel.obtain();
				Parcel reply = Parcel.obtain();
				
				parcel.writeString("Hello My First Service");
				try {
					binder.transact(0, parcel, reply, 0);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Log.d("test","Reply From Service:" + reply.readString());
			}
		});
		
	}

	protected ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MainActivity.this.binder = (MyBinder)service;
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}