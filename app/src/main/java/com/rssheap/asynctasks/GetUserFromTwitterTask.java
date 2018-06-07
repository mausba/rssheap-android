package com.rssheap.asynctasks;

import org.json.JSONObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.rssheap.controls.MyProgressDialog;
import com.rssheap.interfaces.IJSONTaskCompleted;
import com.rssheap.interfaces.IObjectTaskCompleted;
import com.rssheap.utilities.TwitterUtil;
import com.rssheap.utilities.Utilities;

public class GetUserFromTwitterTask extends AsyncTask<Void, String, Object> {

	private IObjectTaskCompleted mCallback;
    private Context mContext;
    private String mVerifier;
	private String action;
	private MyProgressDialog dialog;
	
	public GetUserFromTwitterTask(Context context, String action, String verifier, IObjectTaskCompleted callback)
	{
        this.mContext = context;
        this.mCallback = callback;
		this.action = action;
        this.mVerifier = verifier;
	}

	@Override
	protected Object doInBackground(Void... params) {
		if(action == null || action == "token") {

			return TwitterUtil.getInstance().getRequestToken();

		} else if(action == "user") {
			User twitterUser = null;
			Twitter twitter = TwitterUtil.getInstance().getTwitter();
			RequestToken requestToken = TwitterUtil.getInstance().getRequestToken();
			if(mVerifier != null) { //passed in verifier
				try {

					AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, mVerifier);
					SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
					SharedPreferences.Editor editor = sharedPreferences.edit();
					editor.putString("TWITTER_OAUTH_TOKEN", accessToken.getToken());
					editor.putString("TWITTER_OAUTH_TOKEN_SECRET", accessToken.getTokenSecret());
					editor.commit();
					twitterUser = twitter.showUser(accessToken.getUserId());
				} catch (TwitterException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			} else {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
				String accessTokenString = sharedPreferences.getString("TWITTER_OAUTH_TOKEN", "");
				String accessTokenSecret = sharedPreferences.getString("TWITTER_OAUTH_TOKEN_SECRET", "");
				AccessToken accessToken = new AccessToken(accessTokenString, accessTokenSecret);
				try {
					TwitterUtil.getInstance().setTwitterFactory(accessToken);
					twitterUser = TwitterUtil.getInstance()
							.getTwitter()
							.showUser(accessToken.getUserId());
				} catch (TwitterException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}

			if(twitterUser == null) return null;
			return Utilities.toJson(twitterUser);
		}
		return null;
	}

    @Override
    protected void onPostExecute(Object obj) {
        if(mCallback != null) mCallback.onTaskCompleted(obj);
    }
}
