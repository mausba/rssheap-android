package com.rssheap.adapters;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.rssheap.utilities.JsonRequest;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

public class TagsAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
	
	private Context mContext;
	private List<String> mData = new ArrayList<String>();
    
    public TagsAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    	mContext = context;
    }
    
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int index) {
        return mData.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                // This method is called in a worker thread

                final FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    try {
                    	JSONObject post = new JSONObject();
						try {
							post.put("tag", constraint.toString());
						} catch (JSONException e) {
							e.printStackTrace();
						}
						JSONObject result = new JsonRequest(mContext, "/api/tags").Post(post);
						if(result != null) {
							List<String> tags = new ArrayList<String>();
							
							try {
								JSONArray array;
								array = result.getJSONArray("tags");
							
								for(int i = 0; i < array.length(); i++) {
									tags.add(array.getString(i));
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
							
	                        filterResults.values = tags;
	                        filterResults.count = tags.size();
						}
                    }
                    catch(Exception e) 
                    {
                    	e.printStackTrace(); 
                	}

                }
                return filterResults;
            }

            @SuppressWarnings("unchecked")
			@Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                if(results != null && results.count > 0) {
                    mData = (List<String>)results.values;
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return myFilter;
    }
}
