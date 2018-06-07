package com.rssheap.asynctasks;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.*;
import com.google.android.gms.auth.*;
import com.rssheap.interfaces.IJSONTaskCompleted;

import android.accounts.Account;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class GetUserFromGoogleTask extends AsyncTask<Void,Void,JSONObject> {
	private static final String TAG = "GetUserFromGoogleTask";
	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
	
    protected String mEmail;
    protected String mType;
    protected IJSONTaskCompleted mCallback;
    protected Activity mActivity;
    
    public GetUserFromGoogleTask(Activity activity, String email, String type, IJSONTaskCompleted callback) {
		this.mEmail = email;
        this.mType = type;
        mCallback = callback;
        mActivity = activity;
	}

    @Override
    protected JSONObject doInBackground(Void... params) {
        try {
            return fetchJSONFromGoogle();
        } catch (IOException ex) {
            onError("Following Error occured, please try again. " + ex.getMessage(), ex);
        } catch (JSONException e) {
            onError("Bad response: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if(mCallback != null) mCallback.onTaskCompleted(jsonObject);
    }

    private JSONObject fetchJSONFromGoogle() throws IOException, JSONException {
		JSONObject profile = null;
		String token = fetchToken();
        if (token == null) {
          // error has already been handled in fetchToken()
          return profile;
        }
        
        URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int sc = con.getResponseCode();
        if (sc == 200) {
            InputStream is = con.getInputStream();
            profile = getProfile(readResponse(is));
            is.close();

            return profile;
        } else if (sc == 401) {
            try {
                GoogleAuthUtil.clearToken(mActivity, token);
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            onError("Server auth error, please try again.", null);
            Log.i(TAG, "Server auth error: " + readResponse(con.getErrorStream()));
            
            return profile;
        } else {
          onError("Server returned the following error code: " + sc, null);
          return profile;
        }
	}
	
	protected String fetchToken() throws IOException {
	      try {
              Account account = new Account(mEmail, mType);

	          String token = GoogleAuthUtil.getToken(mActivity, account, SCOPE);
	          return token;
	      } catch (UserRecoverableAuthException userRecoverableException) {
	          // Unable to authenticate, but the user can fix this, just show the authorization dialog
	    	  mActivity.startActivityForResult(userRecoverableException.getIntent(), 2000);
	      } catch (GoogleAuthException fatalException) {
	          onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
	      }
	      return null;
	  }
	
	private static String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }

    /**
     * Parses the response and returns the first name of the user.
     * @throws JSONException if the response is not JSON or if first name does not exist in response
     */
    private JSONObject getProfile(String jsonResponse) throws JSONException {
      JSONObject profile = new JSONObject(jsonResponse);
      return profile;
    }
	
	protected void onError(String msg, Exception e) {
        if (e != null) {
          Log.e(TAG, "Exception: ", e);
        }
    }
}
