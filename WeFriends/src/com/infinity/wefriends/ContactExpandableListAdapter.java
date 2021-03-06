package com.infinity.wefriends;

import java.util.ArrayList;
import java.util.List;

import com.infinity.utils.AnimatedExpandableListView.AnimatedExpandableListAdapter;
import com.infinity.utils.OnlineImageView;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactExpandableListAdapter extends AnimatedExpandableListAdapter {
	
	protected List<ContentValues> contactList = null;
	protected List<String> groupList = null;
	protected List<ArrayList<ContentValues>> groupMemberList = null;
	protected Context m_context = null;
	protected LayoutInflater inflater = null;
	
	public ContactExpandableListAdapter(Context context, List<ContentValues> values) {
		m_context = context;
		contactList = values;
		groupList = getGroupList();
		groupMemberList = parseContactList();
		inflater = LayoutInflater.from(context);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return groupMemberList.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}


	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return groupList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View groupToggleView = inflater.inflate(R.layout.contact_list_group_toggle, null);
		((TextView)groupToggleView.findViewById(R.id.contact_list_group_toggle_title)).setText(groupList.get(groupPosition));
		if (isExpanded) {
			((ImageView)groupToggleView.findViewById(R.id.contact_list_group_toggle_icon)).setImageResource(R.drawable.ic_action_down);
		} else {
			((ImageView)groupToggleView.findViewById(R.id.contact_list_group_toggle_icon)).setImageResource(R.drawable.ic_action_play);
		}
		return groupToggleView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}
	
	protected List<ArrayList<ContentValues>> parseContactList() {
		int groupCount = groupList.size();
		int contactCount = contactList.size();
		List<ArrayList<ContentValues>> resultList = new  ArrayList<ArrayList<ContentValues>>();
		for (int groupIndex = 0; groupIndex<groupCount; groupIndex++) {
			String groupName = groupList.get(groupIndex);
			ArrayList<ContentValues> memberList = new ArrayList<ContentValues>();
			for (int contactIndex=0;contactIndex<contactCount;contactIndex++) {
				if (contactList.get(contactIndex).getAsString("friendgroup").equals(groupName)) {
					memberList.add(contactList.get(contactIndex));
				}
			}
			resultList.add(memberList);
		}
		return resultList;
	}
	
	protected List<String> getGroupList() {
		List<String> groupList = new ArrayList<String>();
		int contactCount = contactList.size();
		for (int i=0;i<contactCount;i++) {
			String groupName = contactList.get(i).getAsString("friendgroup");
			if (!groupList.contains(groupName)) {
				groupList.add(groupName);
			}
		}
		return groupList;
	}

	@Override
	public View getRealChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ContentValues contactInfo = (ContentValues)getChild(groupPosition,childPosition);
		View contactInfoView = inflater.inflate(R.layout.contact_list_item_view, null);
		((OnlineImageView)contactInfoView.findViewById(R.id.contact_list_item_view_avatar)).asyncLoadOnlineImage("http://" + m_context.getString(R.string.server_host) + ":" + m_context.getString(R.string.server_web_service_port) + "/" + contactInfo.getAsString("avatar"),Environment.getExternalStorageDirectory()+"/wefriends/cache");
		((TextView)contactInfoView.findViewById(R.id.contact_list_item_view_main_title)).setText(contactInfo.getAsString("nickname"));
		((TextView)contactInfoView.findViewById(R.id.contact_list_item_view_subtitle)).setText(contactInfo.getAsString("whatsup"));
		return contactInfoView;
	}

	@Override
	public int getRealChildrenCount(int groupPosition) {
		return groupMemberList.get(groupPosition).size();
	}

}
