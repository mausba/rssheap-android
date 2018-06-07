package com.rssheap.fragments;

import org.json.JSONException;
import org.json.JSONObject;
import com.rssheap.R;
import com.rssheap.model.Article;
import com.rssheap.utilities.JsonRequest;
import com.rssheap.utilities.Sharer;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ArticleFragment extends Fragment {
	
	private Article mArticle;
	private WebView mWebView;
	private TextView txtVoteUp;
	private TextView txtVoteDown;
	private TextView txtVotesCount;
	private Menu mMenu;
	private boolean mOpenDirectly;
	private ProgressBar mProgressBar;

    public static interface OnMenuClickedListener {
        public abstract void onMenuItemSelected(String data);
    }

    private OnMenuClickedListener mOnMenuClickedListener;

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mOnMenuClickedListener = (OnMenuClickedListener)context;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException("activity must implement OnCompleteListener");
        }
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mArticle = (Article) getArguments().getSerializable("article");

		setHasOptionsMenu(true);
		
		View v = inflater.inflate(R.layout.fragment_article, container, false);
		
		if(savedInstanceState != null) {
			mArticle = (Article) savedInstanceState.getSerializable("article");
		}

		if(mArticle == null) return v;
        setupView(v);

        return v;
    }

    private void setupView(View v) {
        // create new ProgressBar and style it
        mProgressBar = new ProgressBar(this.getActivity(), null, android.R.attr.progressBarStyleHorizontal);
        mProgressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 24));
        mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_horizontal_holo_no_background_light));
        mProgressBar.setMax(100);

        // retrieve the top view of our application
        final FrameLayout decorView = (FrameLayout) getActivity().getWindow().getDecorView();
        decorView.addView(mProgressBar);

        // Here we try to position the ProgressBar to the correct position by looking
        // at the position where content area starts. But during creating time, sizes
        // of the components are not set yet, so we have to wait until the components
        // has been laid out
        // Also note that doing progressBar.setY(136) will not work, because of different
        // screen densities and different sizes of actionBar
        ViewTreeObserver observer = mProgressBar.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                View contentView = decorView.findViewById(android.R.id.content);
                mProgressBar.setY(contentView.getY() - 10);

                ViewTreeObserver observer = mProgressBar.getViewTreeObserver();
                if (Build.VERSION.SDK_INT < 16) {
					//noinspection deprecation
					observer.removeGlobalOnLayoutListener(this);
                } else {
                    observer.removeOnGlobalLayoutListener(this);
                }
            }
        });

        mWebView = (WebView) v.findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.setInitialScale(1);
        if (Build.VERSION.SDK_INT >= 19) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                mWebView.loadData("Article could not be loaded", "text/html", "UTF-8");
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress >= 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }
        });

        mWebView.loadUrl(mArticle.Url);
    }

    @Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("article", mArticle);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		mMenu = menu;
		if(mMenu != null) {
			if(mArticle.IsMyFavorite) {
				mMenu.findItem(R.id.action_add_to_favorites).setVisible(false);
				mMenu.findItem(R.id.action_remove_from_favorites).setVisible(true);
			} else {
				mMenu.findItem(R.id.action_add_to_favorites).setVisible(true);
				mMenu.findItem(R.id.action_remove_from_favorites).setVisible(false);
			}
			if(mArticle.IVoted) {
				mMenu.findItem(R.id.action_vote_up).setVisible(false);
				mMenu.findItem(R.id.action_vote_down).setVisible(true);
			}
			if(mArticle.IDownVoted) {
				mMenu.findItem(R.id.action_vote_up).setVisible(true);
				mMenu.findItem(R.id.action_vote_down).setVisible(false);
			}
		}
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
            case R.id.action_next: mOnMenuClickedListener.onMenuItemSelected("next"); return true;
            case R.id.action_prev: mOnMenuClickedListener.onMenuItemSelected("prev"); return true;
			case R.id.action_add_to_favorites:  addRemoveFromFavoritesAsync(false); return true;
			case R.id.action_remove_from_favorites:  addRemoveFromFavoritesAsync(true); return true;
			case R.id.action_share_on_twitter: Sharer.shareOnTwitter(getActivity(), mArticle); return true;
			case R.id.action_share_on_facebook: Sharer.shareOnFacebook(getActivity(), mArticle); return true;		
			case R.id.action_share_on_google: Sharer.shareOnGoogle(getActivity(), mArticle);  return true;
			case R.id.action_share_on_linkedin: Sharer.shareOnLinkedIn(getActivity(), mArticle); return true;
			case R.id.action_share_on_other: Sharer.shareOnAndroid(getActivity(), mArticle); return true;
			case R.id.action_vote_up: voteOnArticleAsync(false); return true;
			case R.id.action_vote_down: voteOnArticleAsync(true); return true;
			case R.id.action_flag: flagArticle(); return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void flagArticle() {
		new AsyncTask<Void, Void, Void>() {
			
			@Override
			protected Void doInBackground(Void... params) {
				JSONObject post = new JSONObject();
				try {
					post.put("id", mArticle.Id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				new JsonRequest(getActivity(), "/api/Flag").Post(post);
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				mMenu.findItem(R.id.action_flag).setVisible(false);
				Toast.makeText(getActivity(), "Flagged", Toast.LENGTH_SHORT).show();
			}
		}.execute();
	}
	private void addRemoveFromFavoritesAsync(final boolean remove) {
		new AsyncTask<Void, Void, Void>() {
			
			@Override
			protected Void doInBackground(Void... params) {
				JSONObject post = new JSONObject();
				try {
					post.put("id", mArticle.Id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				new JsonRequest(getActivity(), remove ? "/api/RemoveFromFavorites" : "/api/AddToFavorites").Post(post);
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				if(!remove) {
					mMenu.findItem(R.id.action_add_to_favorites).setVisible(false);
					mMenu.findItem(R.id.action_remove_from_favorites).setVisible(true);
					Toast.makeText(getActivity(), "Added to favorites", Toast.LENGTH_SHORT).show();
				} else {
					mMenu.findItem(R.id.action_add_to_favorites).setVisible(true);
					mMenu.findItem(R.id.action_remove_from_favorites).setVisible(false);
					Toast.makeText(getActivity(), "Removed from favorites", Toast.LENGTH_SHORT).show();
				}
			}
		}.execute();
	}
	
	private void voteOnArticleAsync(final boolean downVoted) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				JSONObject json = new JSONObject();
				try {
					json.put("id", mArticle.Id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				new JsonRequest(getActivity(), downVoted ? "/api/VoteDown" : "/api/VoteUp").Post(json);
				return null;
			}
			
			protected void onPostExecute(Void result) {
				if(!downVoted) {
					if(mArticle.IVoted) return;
					mArticle.IVoted = true;
					mArticle.IDownVoted = false;
					
					mMenu.findItem(R.id.action_vote_down).setVisible(true);
					mMenu.findItem(R.id.action_vote_up).setVisible(false);
				} else {
					if(mArticle.IDownVoted) return;
					mArticle.IDownVoted = true;
					mArticle.IVoted = false;
					
					mMenu.findItem(R.id.action_vote_down).setVisible(false);
					mMenu.findItem(R.id.action_vote_up).setVisible(true);
				}
			};
		}.execute();
	}
}
