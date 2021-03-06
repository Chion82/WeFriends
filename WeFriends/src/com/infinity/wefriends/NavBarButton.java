package com.infinity.wefriends;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NavBarButton extends LinearLayout {
	
	protected Context m_context = null;
	protected MainActivity m_parent = null;
	protected int m_label = 0;
	
	protected ImageView m_image = null;
	protected TextView m_text = null;
	
	public static final int CHATS = 0, CONTACTS = 1, DISCOVERY = 2, ME = 3;

	public NavBarButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		m_context = context;
		createView();
	}
	
	public NavBarButton(Context context, int label, MainActivity parent) {
		super(context);
		m_context = context;
		m_parent = parent;
		m_label = label;
		createView();
		load();
		updateState();
		this.setOnClickListener(new navBarButtonOnClickListener());
	}
	
	protected void createView() {
		LayoutInflater.from(m_context).inflate(R.layout.nav_bar_button, this);
		m_image = (ImageView)findViewById(R.id.nav_bar_button_image);
		m_text = (TextView)findViewById(R.id.nav_bar_button_text);
	}
	
	protected void load() {
		int resource = R.string.nav_bar_chats;
		switch(m_label) {
		case CHATS:
			resource = R.string.nav_bar_chats;
			break;
		case CONTACTS:
			resource = R.string.nav_bar_contacts;
			break;
		case DISCOVERY:
			resource = R.string.nav_bar_discovery;
			break;
		case ME:
			resource = R.string.nav_bar_me;
			break;
		}
		m_text.setText(resource);
	}
	
	public void updateState() {
		int resource = R.drawable.chats_normal;
		if (m_parent.currentPage == m_label) {
			switch(m_label) {
			case CHATS:
				resource = R.drawable.chats_selected;
				break;
			case CONTACTS:
				resource = R.drawable.contacts_selected;
				break;
			case DISCOVERY:
				resource = R.drawable.discovery_selected;
				break;
			case ME:
				resource = R.drawable.me_selected;
				break;
			}
			m_text.setTextColor(Color.parseColor("#6FA8DC"));
		} else {
			switch(m_label) {
			case CHATS:
				resource = R.drawable.chats_normal;
				break;
			case CONTACTS:
				resource = R.drawable.contacts_normal;
				break;
			case DISCOVERY:
				resource = R.drawable.discovery_normal;
				break;
			case ME:
				resource = R.drawable.me_normal;
				break;
			}
			m_text.setTextColor(Color.parseColor("#999999"));
		}
		m_image.setImageResource(resource);
	}
	
	protected class navBarButtonOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			m_parent.onPageChanged(m_label,true);
		}
	}

}
