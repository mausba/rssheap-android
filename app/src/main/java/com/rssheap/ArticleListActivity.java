package com.rssheap;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.rssheap.adapters.*;
import com.rssheap.interfaces.IJSONTaskCompleted;
import com.rssheap.model.Article;
import com.rssheap.model.Feed;
import com.rssheap.model.Folder;
import com.rssheap.model.NavListItem;
import com.rssheap.model.Tag;
import com.rssheap.model.User;
import com.rssheap.utilities.AnalyticsTrackers;
import com.rssheap.utilities.AppRater;
import com.rssheap.utilities.Converter;
import com.rssheap.utilities.JsonRequest;
import com.rssheap.utilities.Utilities;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ListView;

public class ArticleListActivity extends BaseActivity {
	
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private User mUser;
	private Menu mMenu;
    private ArrayList<NavListItem> mNavListItems;
    private NavListItem mSelectedNavItem;
    private ArticlesListAdapter mArticlesListAdapter;
    private ListView mListView;
    private PullToRefreshListView mPullToRefreshView;
    private boolean mLoading;
    private View mFooterView;
    private int mCurrentPage = 0;
    private ActionBar mActionBar;
	
	@SuppressLint("InflateParams")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boolean logOutClicked = getIntent().getBooleanExtra("finish", false);
        if (logOutClicked) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
            return;
        }

        showLoadingDialog();
		
		setContentView(R.layout.activity_article_list);
        AppRater.app_launched(ArticleListActivity.this);

        mActionBar = getActionBar();
        mActionBar.setIcon(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        mNavListItems = getNavListItems();
        mSelectedNavItem = mNavListItems.get(0);
		
		mUser = Utilities.getUserFromSharedPreferences(ArticleListActivity.this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ExpandableListView mDrawerList = (ExpandableListView) findViewById(R.id.list_slidermenu);
        NavListAdapter adapter = new NavListAdapter(getApplicationContext(), mNavListItems);
        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnGroupClickListener(new OnGroupClickListener() {

            public boolean onGroupClick(ExpandableListView parent, View v, final int groupPosition, long id) {

                mSelectedNavItem = mNavListItems.get(groupPosition);
                String value = mSelectedNavItem.Title;
                if(value == null) return false;

                Tracker t = AnalyticsTrackers.getInstance().get();
                t.setScreenName(value);
                t.send(new HitBuilders.ScreenViewBuilder().build());

                switch ((int)id) {
                    case -1:
                        Utilities.resetUserGuid(getApplicationContext());
                        Intent intent = getIntent();
                        intent.putExtra("finish", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
                        finish();
                        startActivity(intent);
                        break;

                    case -2:
                        Intent tagsIntent = new Intent(ArticleListActivity.this, TagsActivity.class);
                        startActivity(tagsIntent);
                        finish();
                        break;

                    default:
                        mCurrentPage = 0;
                        loadArticlesTask(new IJSONTaskCompleted() {
                            @Override
                            public void onTaskCompleted(JSONObject result) {
                                mActionBar.setTitle(mSelectedNavItem.Title);
                                mDrawerList.setItemChecked(groupPosition, true);
                                mDrawerLayout.closeDrawers();
                            }
                        });
                }

                return true;
            }
        });

        mDrawerList.setOnChildClickListener(new OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {

                mSelectedNavItem = mNavListItems.get(groupPosition).children.get(childPosition);
                String value = mSelectedNavItem.Title;
                if(value == null) return false;

                Tracker t = AnalyticsTrackers.getInstance().get();
                t.setScreenName(value);
                t.send(new HitBuilders.ScreenViewBuilder().build());

                mCurrentPage = 0;
                loadArticlesTask(new IJSONTaskCompleted() {
                    @Override
                    public void onTaskCompleted(JSONObject result) {
                        mActionBar.setTitle(mSelectedNavItem.Title);

                        int index = mDrawerList.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
                        mDrawerList.setItemChecked(index, true);
                        mDrawerLayout.closeDrawers();
                    }
                });
                return true;
            }
        });
        mDrawerList.bringToFront();
        mDrawerLayout.requestLayout();

        // enabling action bar app icon and behaving it as toggle button
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mPullToRefreshView = (PullToRefreshListView) findViewById(R.id.list_articles);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mCurrentPage = 0;
                loadArticlesTask();
            }
        });

        mFooterView = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.articlelist_footer, null, false);

        mListView = mPullToRefreshView.getRefreshableView();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ArticleListActivity.this, ArticleActivity.class);
                i.putExtra("index", Integer.toString(position));
                i.putExtra("view", mSelectedNavItem.Id);
                startActivity(i);
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if(totalItemCount == 0) return;

                int l = visibleItemCount + firstVisibleItem;

                if (l >= totalItemCount && !mLoading) {
                    mCurrentPage = mCurrentPage + 1;
                    mListView.addFooterView(mFooterView);
                    loadArticlesTask();
                    mLoading = true;
                }
            }
        });

        mDrawerList.setItemChecked(0, true);
        mActionBar.setTitle(mSelectedNavItem.Title);

        mCurrentPage = 0;
        loadArticlesTask(new IJSONTaskCompleted() {
            @Override
            public void onTaskCompleted(JSONObject result) {
                mListView.setEmptyView(findViewById(android.R.id.empty));
            }
        });
	}

    private void loadArticlesTask() {
        loadArticlesTask(null);
    }

    private void loadArticlesTask(final IJSONTaskCompleted callback) {

        if(mCurrentPage == 0) showLoadingDialog();

        new AsyncTask<Boolean, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Boolean... params) {

                JSONObject post = new JSONObject();
                try {
                    post.put("page", mCurrentPage);
                    post.put("view", mSelectedNavItem.Id);  //week, month, votes etc
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return new JsonRequest(getApplicationContext(), "/api/GetArticles").Post(post);
            }

            protected void onPostExecute(JSONObject result) {

                ArrayList<Article> articles = Converter.ToArticles(result, "articles");

                if(mArticlesListAdapter == null || mCurrentPage == 0) {
                    mLoading = false;
                    mArticlesListAdapter = new ArticlesListAdapter(ArticleListActivity.this, articles);
                    mListView.setAdapter(mArticlesListAdapter);
                }
                else {
                    if(articles.size() == 0) {
                        mLoading = true; //prevent future requests
                    } else {
                        mLoading = false;
                        mArticlesListAdapter.append(articles);
                    }
                }
                mArticlesListAdapter.notifyDataSetChanged();

                mPullToRefreshView.onRefreshComplete();

                if(mCurrentPage > 0 && mFooterView != null) {
                    mListView.removeFooterView(mFooterView);
                }

                if(callback != null) callback.onTaskCompleted(result);

                if(mCurrentPage == 0) hideLoadingDialog();
            }
        }.execute();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.activity_articles, menu);
		if(mUser.HideVisited) {
			mMenu.findItem(R.id.menu_hide_visited).setVisible(false);
			mMenu.findItem(R.id.menu_show_visited).setVisible(true);
		} else {
			mMenu.findItem(R.id.menu_hide_visited).setVisible(true);
			mMenu.findItem(R.id.menu_show_visited).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
			case R.id.menu_settings:
				return true;
			case R.id.menu_hide_visited:
				showHideVisitedArticlesTask();
				mMenu.findItem(R.id.menu_hide_visited).setVisible(false);
				mMenu.findItem(R.id.menu_show_visited).setVisible(true);
				return true;
			case R.id.menu_show_visited:
				showHideVisitedArticlesTask();
				mMenu.findItem(R.id.menu_hide_visited).setVisible(true);
				mMenu.findItem(R.id.menu_show_visited).setVisible(false);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void showHideVisitedArticlesTask() {

        new AsyncTask<Void, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Void... params) {
                return new JsonRequest(getApplicationContext(), "/api/ToggleVisited").Post(null);
            }

            @Override
            protected void onPostExecute(JSONObject json) {
                Utilities.replaceUserObject(getApplicationContext(), json);
                mCurrentPage = 0;
                loadArticlesTask();
            }
        }.execute();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		//boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		//menu.findItem(R.id.menu_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public void onBackPressed() {
		FragmentManager fm = getFragmentManager();
	    if (fm != null && fm.getBackStackEntryCount() > 0){
	    	fm.popBackStack();
	    }
	    else{
	        super.onBackPressed();          
	    }
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	private ArrayList<NavListItem> getNavListItems() {
		if(mUser == null) {
			mUser = Utilities.getUserFromSharedPreferences(getApplicationContext());
		}
		
		ArrayList<NavListItem> items = new ArrayList<>();

		// adding nav drawer items to array
		items.add(new NavListItem("week", "Week", R.string.icon_week));
		items.add(new NavListItem("month", "Month", R.string.icon_month));
		items.add(new NavListItem("votes", "Votes", R.string.icon_votes));
		items.add(new NavListItem("untagged", "Untagged", R.string.icon_untagged));
		items.add(new NavListItem("favorites", "Favorites", R.string.icon_favorites));
		items.add(new NavListItem());
			
		if(mUser != null) {
			if(mUser.Tags.size() > 0) {
				//NavListItem myTags = new NavListItem("mytags", "My tags", R.string.icon_untagged);
				for(Tag tag : mUser.Tags) {
					items.add(new NavListItem("tag" + tag.Id, tag.Name, R.string.icon_tag));
				}
			}

            items.add(new NavListItem());
			
			if(mUser.Feeds.size() > 0) {
				NavListItem myFeeds = new NavListItem("myfeeds", "My Feeds", R.string.icon_feed);
				
				for(Feed feed : mUser.Feeds) {
					myFeeds.children.add(new NavListItem("feed" + String.valueOf(feed.Id), feed.Name, feed.Favicon));
				}
				items.add(myFeeds);
			}

            items.add(new NavListItem());

			if(mUser.Folders.size() > 0) {
				for(Folder folder : mUser.Folders) {
					NavListItem folderItem = new NavListItem("folder" + folder.Id, folder.Name, R.string.icon_folder);
					for(Feed feed : folder.Feeds) {
						folderItem.children.add(new NavListItem("feed" + String.valueOf(feed.Id), feed.Name, feed.Favicon));
					}
					items.add(folderItem);
				}
			}
		}

        items.add(new NavListItem());

		items.add(new NavListItem(-2, "Edit Tags", R.string.icon_tags));
		items.add(new NavListItem(-1, "Logout", R.string.icon_signout));
        items.add(new NavListItem());

		return items;
	}
}
