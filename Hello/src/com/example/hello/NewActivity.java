package com.example.hello;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class NewActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new);
		
		Intent intent = getIntent();
		TextView newText = (TextView)findViewById(R.id.testText);
		newText.setText(intent.getStringExtra("value"));
		
	}

}
