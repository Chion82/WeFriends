package com.example.hello;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.support.v4.widget.DrawerLayout;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends Activity {
	protected CustomedView customedView = null;
	
	protected ViewFlipper myViewFlipper = null;
	
	protected ListView leftListView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlelayout);
		
		TextView myTextView = (TextView)findViewById(R.id.myTextView);
		Button myButton = (Button)findViewById(R.id.myButton);
		myTextView.setText("This is my first Android program!");
		myButton.setText("This is my first button!");
		
		myButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v)
			{
				Intent intent = new Intent();
				intent.putExtra("value", "This is a string");
				intent.setClass(MainActivity.this, NewActivity.class);
				startActivity(intent);
				//finish();
			}
		});
		
		myViewFlipper = (ViewFlipper)findViewById(R.id.myViewFlipper);
		
		
		customedView = new CustomedView(this);
		customedView.Init("Hello World!",this);
		
		RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.mainlayout);
		mainLayout.addView(customedView);
		
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		param.addRule(RelativeLayout.ALIGN_RIGHT, R.id.myButton);
		param.addRule(RelativeLayout.CENTER_VERTICAL);
		
		customedView.setLayoutParams(param);
		
		
		((Button)findViewById(R.id.titlebutton)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "You Clicked the title button", Toast.LENGTH_SHORT).show();
				
				myViewFlipper.showNext();
			}
		});
		
		LinearLayout testLayout = (LinearLayout)findViewById(R.id.myLayout);
		for (int i=0;i<10;i++)
		{
			Button button = new Button(this);
			button.setText(i + "");
			testLayout.addView(button);
		}
		
		((Button)findViewById(R.id.statusButton)).setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				((DrawerLayout)findViewById(R.id.myDrawerLayout)).openDrawer(Gravity.LEFT);
			}
			
		});
		
		leftListView = (ListView)findViewById(R.id.left_list_view);
		
		ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
		
		HashMap datas[] = new HashMap[5];
		for (int i=0;i<5;i++)
		{
			datas[i] = new HashMap<String,Object>();
			datas[i].put("string", "Option#" + i);
			datas[i].put("image", R.drawable.icon1);
			list.add(datas[i]);
		}
		
		SimpleAdapter adapter = new SimpleAdapter(
				this,
				list,
				R.layout.listwidget,
				new String[]{"string","image"},
				new int[]{R.id.listText,R.id.listImg});
		leftListView.setAdapter(adapter);
		
		leftListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Toast.makeText(getApplicationContext(),"position=" + arg2 + " id=" + arg3,Toast.LENGTH_SHORT).show();
				
			}
			
		});
		
	}
	
	protected float prevX;
	protected boolean isPressed = false;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction()==MotionEvent.ACTION_DOWN)
		{
			prevX = event.getX();
			isPressed = true;
		}
		else if (event.getAction()==MotionEvent.ACTION_MOVE && isPressed)
		{
			if (event.getX() > (prevX + 100))
			{
				((Button)findViewById(R.id.statusButton)).setText("right");
			}
			else if (event.getX() < (prevX - 100))
			{
				((Button)findViewById(R.id.statusButton)).setText("left");
			}
		}
		else if (event.getAction()==MotionEvent.ACTION_UP)
		{
			isPressed = false;
			((Button)findViewById(R.id.statusButton)).setText("ready");
		}
				
		
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0, 0, 0, "Quit");
		menu.add(0,1,1,"TestGPS");
		menu.add(0,2,2,"TestProvider");
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId()==0)
			finish();
		else if (item.getItemId()==1)
		{
			LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,new myLocationListener());
		}
		else if (item.getItemId()==2) {
			getContentResolver().query(Uri.parse("content://com.test.myprovider/getData"), new String[]{}, "", new String[]{}, "");
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	protected class myLocationListener implements LocationListener
	{
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			((TextView)findViewById(R.id.textView1)).setText("la=" + location.getLatitude() + "lo=" + location.getLongitude());
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	}
	
	
	public void callback()
	{
		((TextView)findViewById(R.id.myTextView)).setText("Callback method executed.");
	}

}
