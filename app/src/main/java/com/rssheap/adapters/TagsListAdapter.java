package com.rssheap.adapters;

import java.util.ArrayList;
import com.rssheap.R;
import com.rssheap.model.Tag;
import com.rssheap.viewholders.TagListViewHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TagsListAdapter extends BaseAdapter {

	private Context context;
	public ArrayList<Tag> tags;
	private ListView mListView;
	private ArrayList<Integer> myTags;
	
	public TagsListAdapter(Context context, ArrayList<Tag> tags, ListView listView, ArrayList<Integer> myTags) {
		this.context = context;
		this.tags = tags;
		this.mListView = listView;
		this.myTags = myTags;
	}
	
	public void append(ArrayList<Tag> tags) {
		if(this.tags != null) {
			this.tags.addAll(tags);
		}
	}
	
	@Override
	public int getCount() {
		return tags.size();
	}

	@Override
	public Object getItem(int position) {
		return tags.get(position);
	}

	@Override
	public long getItemId(int position) {
		return tags.get(position).Id;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		TagListViewHolder viewHolder = null;
		
		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_tag_list_item, null);
            
            viewHolder = new TagListViewHolder();
            viewHolder.txtTitle = (TextView) convertView.findViewById(R.id.title);
            viewHolder.txtDescription = (TextView) convertView.findViewById(R.id.description);
            viewHolder.txtSubscribersCount = (TextView) convertView.findViewById(R.id.subscribers_count);
            convertView.setTag(viewHolder);
        } else {
        	viewHolder = (TagListViewHolder) convertView.getTag();
        }
		
		Tag tag = tags.get(position);
		
		viewHolder.txtTitle.setText(Html.fromHtml(tag.Name));
		viewHolder.txtDescription.setText(Html.fromHtml(tag.Description));
		viewHolder.txtSubscribersCount.setText(Integer.toString(tag.SubscribersCount) + " subscribers \n" + Integer.toString(tag.ArticlesCount) + " articles");
        
		if(myTags.contains(tag.Id)) {
			mListView.setItemChecked(position, true);
		}
		
		return convertView;
	}
}
