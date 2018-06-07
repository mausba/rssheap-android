package com.rssheap;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.rssheap.adapters.ArticlePageAdapter;
import com.rssheap.fragments.ArticleFragment;
import com.rssheap.model.Article;
import com.rssheap.utilities.AnalyticsTrackers;
import com.rssheap.utilities.Converter;
import com.rssheap.utilities.JsonRequest;
import com.rssheap.utilities.Utilities;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class ArticleActivity extends BaseActivity implements ArticleFragment.OnMenuClickedListener {

	private ViewPager mViewPager;
	private ArticlePageAdapter mAdapter;
	private String mArticleIndex;
	private String mArticlesView;
	private ActionBar mActionBar;
	
	private boolean firstLoad = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(!isDeviceOnline()) {
			finish();
			return;
		}
		
		setContentView(R.layout.activity_article);
		getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        mActionBar = getActionBar();

        assert mActionBar != null;

        mActionBar.setTitle("");
        mActionBar.setDisplayHomeAsUpEnabled(false);  //this will set the title in ArticleFragment
        mActionBar.setIcon(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        if(savedInstanceState == null) {
			Intent intent = getIntent();
			if(intent != null) {
				mArticleIndex = getIntent().getStringExtra("index");
				mArticlesView = getIntent().getStringExtra("view");
			}
			else {
				mArticleIndex = "0";
				mArticlesView = "week";
			}
		}  else {
			finish();
			return; //quick fix, doesn't want to hide dialog if state is recovered
		}
		
		loadArticlesAsync(mArticleIndex);
        
		mViewPager = (ViewPager)findViewById(R.id.viewpager);
        mAdapter = new ArticlePageAdapter(getSupportFragmentManager(), new ArrayList<Article>());
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(final int arg0) {
                final List<Article> articles = ((ArticlePageAdapter) mViewPager.getAdapter()).getArticles();
                if(articles.size() - 1 == arg0) {
                    loadArticlesAsync(Integer.toString(arg0 + Integer.parseInt(mArticleIndex) + 1) );
                }
                mActionBar.setTitle(articles.get(arg0).Name);
                mActionBar.setDisplayHomeAsUpEnabled(true);
                markArticleAsRead(articles.get(arg0));

                Tracker t = AnalyticsTrackers.getInstance().get();
                t.setScreenName("article/" + articles.get(arg0).Name);
                t.send(new HitBuilders.ScreenViewBuilder().build());
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) { }

            @Override
            public void onPageScrollStateChanged(int arg0) { }
        });
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("index", mArticleIndex);
		outState.putString("view", mArticlesView);

		super.onSaveInstanceState(outState);
	}
	
	private void markArticleAsRead(final Article article) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				JSONObject post = new JSONObject();
				try {
					post.put("id", article.Id);
				} catch (JSONException e) { Log.e("ArticleActivity", e.getLocalizedMessage()); }
				new JsonRequest(ArticleActivity.this, "/api/IncreaseArticleViewCount").Post(post);
				return null;
			}
		}.execute();
	}
	
	public void loadArticlesAsync(final String articleIndex) {

        showLoadingDialog();
		new AsyncTask<Void, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Void... params) {
                JSONObject post = new JSONObject();
                try {
                    post.put("index", articleIndex.equals("") ? mArticleIndex : articleIndex);
                    post.put("view", mArticlesView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return new JsonRequest(ArticleActivity.this, "/api/GetArticle").Post(post);
            }

            @Override
            protected void onPostExecute(JSONObject json) {

                if(json == null) {
                    Utilities.showErrorToast(ArticleActivity.this);
                    finish();
                    return;
                }

                ArrayList<Article> articles = Converter.ToArticles(json, "articles");

                if(firstLoad && articles != null && articles.size() > 0) {
                    mActionBar.setTitle(articles.get(0).Name);
                    mActionBar.setDisplayHomeAsUpEnabled(true);

                    //first load, increse views count
                    if(articles.size() > 0) {
                        markArticleAsRead(articles.get(0));

                        Tracker t = AnalyticsTrackers.getInstance().get();
                        t.setScreenName("article/" + articles.get(0).Name);
                        t.send(new HitBuilders.ScreenViewBuilder().build());
                    }
                }
                firstLoad = false;

                mAdapter.addRange(articles);
                mAdapter.notifyDataSetChanged();
                hideLoadingDialog();
            }

        }.execute();
	}
	
	@Override
	public void onBackPressed() {
		if (mViewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
        	mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.article, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id) {
			case android.R.id.home:
				Intent upIntent = new Intent(this, ArticleListActivity.class);
				if(NavUtils.shouldUpRecreateTask(this, upIntent)) {
					NavUtils.navigateUpTo(this, upIntent);
					finish();
				} else {
					finish();
				}
				return true;
			}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onMenuItemSelected(String data) {
		if(data.equals("next")) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
		} else if(data.equals("prev")) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
		}
	}
}
