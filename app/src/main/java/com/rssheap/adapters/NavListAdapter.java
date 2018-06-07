package com.rssheap.adapters;

import java.util.ArrayList;

import com.rssheap.R;
import com.rssheap.model.NavListItem;
import com.rssheap.utilities.ImageLoader;
import com.rssheap.utilities.TypefaceUtil.Fonts;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

public class NavListAdapter extends BaseExpandableListAdapter {
	
	private Context context;
	private ArrayList<NavListItem> navDrawerItems;
	public ImageLoader imageLoader;
	
	public NavListAdapter(Context context, ArrayList<NavListItem> navDrawerItems){
		this.context = context;
		this.navDrawerItems = navDrawerItems;
		imageLoader = new ImageLoader(context);
	}

	@Override
	public int getGroupCount() {
		return navDrawerItems.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return navDrawerItems.get(groupPosition).children.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return navDrawerItems.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return navDrawerItems.get(groupPosition).children.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		long id = navDrawerItems.get(groupPosition).IdLong;
		if(id == 0) {
			return groupPosition;
		}
		return id;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getGroupView(final int groupPosition, final boolean isExpanded,
			View convertView, final ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_nav_list_item, null);
        }
		
		NavListItem item = navDrawerItems.get(groupPosition);
         
        TextView txtIcon = (TextView) convertView.findViewById(R.id.icon2);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        TextView txtExpander = (TextView) convertView.findViewById(R.id.expander);

        if(item.Title == null)
        {
            txtTitle.setText("");
            txtIcon.setText("");
            return convertView;
        }
        
        txtIcon.setText(item.Icon);
        txtIcon.setTypeface(Fonts.getFontAwesome(convertView.getContext()));
        
        txtTitle.setText(item.Title);
        
        if(item.children.size() > 0) {
        	txtExpander.setVisibility(View.VISIBLE);
        	
        	if(!isExpanded) 
        		txtExpander.setText(R.string.icon_down);
        	else
        		txtExpander.setText(R.string.icon_up);
        	
            txtExpander.setTypeface(Fonts.getFontAwesome(convertView.getContext()));
            txtExpander.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					
					ExpandableListView listView = (ExpandableListView) parent;
					if(!isExpanded) {
						listView.expandGroup(groupPosition);
					}  else {
						listView.collapseGroup(groupPosition);
					}
				}
			});
        } else {
        	txtExpander.setVisibility(View.GONE);
        }
        
        return convertView;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_nav_list_item_child, null);
        }
		
		NavListItem item = navDrawerItems.get(groupPosition).children.get(childPosition);
         
        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.favicon);
        imageLoader.DisplayImage(item.IconUrl, imgIcon);
        
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        txtTitle.setText(item.Title);

        return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
