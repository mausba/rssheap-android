package com.rssheap;

import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.rssheap.adapters.TagsListAdapter;
import com.rssheap.controls.MyProgressDialog;
import com.rssheap.model.Tag;
import com.rssheap.model.User;
import com.rssheap.utilities.AnalyticsTrackers;
import com.rssheap.utilities.Converter;
import com.rssheap.utilities.JsonRequest;
import com.rssheap.utilities.Utilities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

public class TagsActivity extends BaseActivity {

	private MyProgressDialog mDialog;
	private TagsListAdapter mTagsAdapter;
	private ListView mListView;
	private View mFooterView;
	private int mCurrentPage = 0;
	private boolean mLoading;
	private ArrayList<Integer> myTags;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_tags);
		
		getWindow().getDecorView().setBackgroundColor(Color.WHITE);

		ActionBar actionBar = getActionBar();

		if(actionBar != null) {
			actionBar.setHomeButtonEnabled(false);
			actionBar.setDisplayShowHomeEnabled(false);
		}

		User user = Utilities.getUserFromSharedPreferences(getApplicationContext());
		if(user == null) finish();
        assert user != null;

        myTags = user.TagIds;
		
		mFooterView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.articlelist_footer, null);
		
		mListView = (ListView) findViewById(R.id.lvTags);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {

                showLoadingDialog();
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        JSONObject post = new JSONObject();
                        try {
                            post.put("id", id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JSONObject user = new JsonRequest(getApplicationContext(), "/api/AddRemoveTag").Post(post);

                        if(myTags.contains((int)id)) {
                            myTags.remove(position);
                        } else {
                            myTags.add((int) id);
                        }
                        if(user != null) {
                            Utilities.replaceUserObject(getApplicationContext(), user);
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        hideLoadingDialog();
                    }
                }.execute();
			}
		});
		
        mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) { }
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
				if(totalItemCount == 0) return;
				
				int l = visibleItemCount + firstVisibleItem;
				
				if (l >= totalItemCount && !mLoading) {
					mCurrentPage = mCurrentPage + 1;
					loadTags(false, mCurrentPage);
					mLoading = true;
				}
			}
		});
		
		loadTags(true, 0);

		Tracker t = AnalyticsTrackers.getInstance().get();
        t.setScreenName("EditTags");
        t.send(new HitBuilders.ScreenViewBuilder().build());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tags, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_done) {
			Intent intent = new Intent(TagsActivity.this, ArticleListActivity.class);
			startActivity(intent);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void loadTags(final Boolean showLoading, final int page) {
		
		mListView.addFooterView(mFooterView);
		new AsyncTask<Boolean, Void, JSONObject>() {
			
			@Override
			protected JSONObject doInBackground(Boolean... params) {
				
				JSONObject post = new JSONObject();
				try {
					post.put("page", page);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return new JsonRequest(TagsActivity.this, "/api/GetTags").Post(post);
			}
			
			protected void onPreExecute() {
				
				if(showLoading) {
					mDialog = MyProgressDialog.show(TagsActivity.this);
				} 
			}
			
			protected void onPostExecute(JSONObject result) {
				
				if(result != null) {
					ArrayList<Tag> tags = Converter.ToTags(result, "tags");
					
					if(mTagsAdapter == null || page == 0) {
						mCurrentPage = 0;
						mLoading = false;
						mTagsAdapter = new TagsListAdapter(TagsActivity.this, tags, 
														mListView, myTags);
						mListView.setAdapter(mTagsAdapter);
					}
					else {
						if(tags.size() == 0) {
							mLoading = true; //prevent future requests
						} else {
							mLoading = false;
							mTagsAdapter.append(tags);
							mTagsAdapter.notifyDataSetChanged();
						}
					}
					if(mDialog != null && mDialog.isShowing()) mDialog.dismiss();
	
					if(mFooterView != null && mListView.getFooterViewsCount() > 0) {
						try {
							mListView.removeFooterView(mFooterView);
						} catch (Exception e) {
							Log.e("TagsActivity", e.getLocalizedMessage());
						}
					}
				}
			}
		}.execute();
	}
}
