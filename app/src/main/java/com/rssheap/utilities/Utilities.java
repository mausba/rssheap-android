package com.rssheap.utilities;

import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.rssheap.ArticleListActivity;
import com.rssheap.TagsActivity;
import com.rssheap.model.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.widget.Toast;


public class Utilities {
	
	public static void showErrorToast(Activity activity) {
		Toast.makeText(activity,
				"There was an error in processing your request, please try again", Toast.LENGTH_LONG)
				.show();
	}
	
	public static boolean isDeviceOnline(Activity activity) {
		boolean hasConnectedWifi = false;
	    boolean hasConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	            	hasConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	            	hasConnectedMobile = true;
	    }
	    if(hasConnectedWifi || hasConnectedMobile) {
	    	return true;
	    } else {
	    	Toast.makeText(activity,
					"No internet connection", Toast.LENGTH_LONG)
					.show();
		    return false;
	    }
    }
	
	public static String FormatCount(int count)
    {
        if (count >= 1000)
            return count / 1000 + "k";
        if (count >= 1000000)
            return count / 1000000 + "m";

        return Integer.toString(count);
    }
	
	public static String getUserGUID(Context context) {
		try {
			SharedPreferences prefs = context.getSharedPreferences("MyPref", 0);
			return prefs.getString("guid", "");
		} catch (Exception e) {
			return "";
		}
	}
	
	public static void resetUserGuid(Context context) {
		SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
		Editor editor = pref.edit();
		editor.clear();
		editor.commit();
	}

	public static String getVersionFromSharedPreferences(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("MyPref", 0);
		String version = prefs.getString("appversion", "");
		if(version == null) return "";
		return version;
	}

	public static void setVersionToSharedPreferences(Context context, String version) {
		SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
		Editor editor = pref.edit();
		editor.putString("appversion", version);
		editor.commit();
	}

    public static User setUserToSharedPreferences(Context context, JSONObject user) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        Editor editor = pref.edit();
        editor.putString("user", user.toString());
        editor.commit();
        return getUserFromSharedPreferences(context);
    }

    public static void setUserGUID(Context context, JSONObject idAndGuid) {
        try {
            setUserGUID(context, idAndGuid.getString("guid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setUserGUID(Context context, String guid) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
        Editor editor = pref.edit();
        editor.putString("guid", guid);
        editor.commit();
    }

	public static User getUserFromSharedPreferences(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("MyPref", 0);
		String feedsStr = prefs.getString("user", "");
		if(TextUtils.isEmpty(feedsStr)) return null;
		try {
			JSONObject json = new JSONObject(feedsStr);
			User user = new User();
			user.Feeds = Converter.ToFeeds(json, "feeds");
			user.Folders = Converter.ToFolders(json, "folders");
			user.TagIds = Converter.ToListOfIntegers(json, "tags");
			user.HideVisited = json.getBoolean("hidevisited");
			user.Tags = Converter.ToTags(json, "tagsobjects");
			return user;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void disableOrientationChange(Activity activity) {
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
	}
	
	public static void enableOrientationChange(Activity activity) {
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
	public static JSONObject toJson(Object obj) {
		try {
			return new JSONObject(new Gson().toJson(obj));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void replaceUserObject(Context context, JSONObject user) {
		SharedPreferences pref = context.getSharedPreferences("MyPref", 0);
		Editor editor = pref.edit();
		editor.putString("user", ((JSONObject) user).toString());
		editor.commit();	
	}
}
