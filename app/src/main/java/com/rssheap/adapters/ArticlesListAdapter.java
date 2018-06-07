package com.rssheap.adapters;

import java.util.ArrayList;
import com.rssheap.R;
import com.rssheap.model.Article;
import com.rssheap.utilities.Utilities;
import com.rssheap.viewholders.ArticleListViewHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ArticlesListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<Article> articles;
	
	public ArticlesListAdapter(Context context, ArrayList<Article> articles) {
		this.context = context;
		this.articles = articles;
	}
	
	public void append(ArrayList<Article> articles) {
		if(this.articles != null) {
			this.articles.addAll(articles);
		}
	}
	
	@Override
	public int getCount() {
		return articles.size();
	}

	@Override
	public Object getItem(int position) {
		return articles.get(position);
	}

	@Override
	public long getItemId(int position) {
		return articles.get(position).Id;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ArticleListViewHolder viewHolder = null;
		
		if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_article_list_item, null);
            
            viewHolder = new ArticleListViewHolder();
            viewHolder.txtTimeAgo = (TextView) convertView.findViewById(R.id.time_ago);
            viewHolder.txtTitle = (TextView) convertView.findViewById(R.id.title);
            viewHolder.txtVotesCount = (TextView) convertView.findViewById(R.id.votes_count);
            //viewHolder.txtViewsCount = (TextView) convertView.findViewById(R.id.view_count);
            viewHolder.txtTags = (TextView) convertView.findViewById(R.id.tags);
            convertView.setTag(viewHolder);
        } else {
        	viewHolder = (ArticleListViewHolder) convertView.getTag();
        }
		
		Article article = articles.get(position);
		
		viewHolder.txtTimeAgo.setText(article.TimeAgo);
		viewHolder.txtTitle.setText(Html.fromHtml(article.Name));
		viewHolder.txtVotesCount.setText(Utilities.FormatCount(article.Votes));
		//viewHolder.txtViewsCount.setText(Utilities.FormatCount(article.getViews()));

        String tagsStr = "";
        for(String tag : article.Tags) {
        	tagsStr += tag + ", ";
        }
        if(tagsStr.length() > 0) {
        	tagsStr = tagsStr.substring(0, tagsStr.length() - 2);
        	viewHolder.txtTags.setText(tagsStr);
        } else {
        	viewHolder.txtTags.setVisibility(View.GONE);
        }
        
        return convertView;
	}
}
