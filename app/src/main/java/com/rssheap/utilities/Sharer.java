package com.rssheap.utilities;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.google.android.gms.plus.PlusShare;
import com.rssheap.model.Article;

public class Sharer {

	public static void shareOnFacebook(Activity activity, Article article) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		// intent.putExtra(Intent.EXTRA_SUBJECT, "Foo bar"); // NB: has no effect!
		intent.putExtra(Intent.EXTRA_TEXT, "http://rssheap.com/a/" + article.ShortUrl);

		boolean facebookAppFound = false;
		List<ResolveInfo> matches = activity.getPackageManager().queryIntentActivities(intent, 0);
		for (ResolveInfo info : matches) {
		    if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
		    	final ActivityInfo activityInfo = info.activityInfo;
                final ComponentName name = new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
                intent.setComponent(name);
		        facebookAppFound = true;
		        break;
		    }
		}

		if (!facebookAppFound) {
		    String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + "http://rssheap.com/a/" + article.ShortUrl;
		    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
		}

		activity.startActivity(intent);
	}
	
	public static void shareOnTwitter(Activity activity, Article mArticle) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/*");
		intent.putExtra(Intent.EXTRA_TEXT, mArticle.Tweet);

		boolean twitterAppFound = false;
		List<ResolveInfo> matches = activity.getPackageManager().queryIntentActivities(intent, 0);
		
		for (ResolveInfo info : matches) {
		    if (info.activityInfo.packageName.toLowerCase().startsWith("com.twitter.android")) {
		    	final ActivityInfo activityInfo = info.activityInfo;
                final ComponentName name = new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
                intent.setComponent(name);
		        twitterAppFound = true;
		        break;
		    }
		}

		if (!twitterAppFound) {
		    String tweetUrl = "https://twitter.com/intent/tweet?text=" + mArticle.Tweet;
		    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));
		}

		activity.startActivity(intent);
	}
	
	public static void shareOnGoogle(Activity activity, Article article) {
		Intent shareIntent = new PlusShare.Builder(activity)
	        .setType("text/plain")
	        .setText(article.Name)
	        .setContentUrl(Uri.parse("http://rssheap.com/a/" + article.ShortUrl))
	        .getIntent();

		activity.startActivityForResult(shareIntent, 0);
	}

	public static void shareOnLinkedIn(Activity activity, Article article) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/*");
		intent.putExtra(Intent.EXTRA_TEXT, "http://rssheap.com/a/" + article.ShortUrl);

		boolean linkedinAppFound = false;
		List<ResolveInfo> matches = activity.getPackageManager().queryIntentActivities(intent, 0);
		
		for (ResolveInfo info : matches) {
		    if (info.activityInfo.packageName.toLowerCase().startsWith("com.linkedin.android")) {
		    	final ActivityInfo activityInfo = info.activityInfo;
                final ComponentName name = new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
                intent.setComponent(name);
                linkedinAppFound = true;
		        break;
		    }
		}

		if (!linkedinAppFound) {
		    String tweetUrl = "http://www.linkedin.com/shareArticle?mini=true&amp;url=" + "http://rssheap.com/a/" + article.ShortUrl + "&amp;title=" + article.Name + " via http://www.rssheap.com";
		    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl));
		}

		activity.startActivity(intent);
	}

	public static void shareOnAndroid(Activity activity, Article article) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, article.Name);
		i.putExtra(Intent.EXTRA_TEXT, "http://rssheap.com/a/" + article.ShortUrl);
		activity.startActivity(Intent.createChooser(i, "Share URL"));
	}
}
