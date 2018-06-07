package com.rssheap.utilities;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.rssheap.model.*;

public class Converter {
	
	public static ArrayList<Tag> ToTags(JSONObject json, String jsonFieldName) {
		ArrayList<Tag> result = new ArrayList<Tag>();
		JSONArray array;
		try {
			array = json.getJSONArray(jsonFieldName);
		
			if(array == null || array.length() == 0) return result;
			
			for(int i = 0; i < array.length(); i++) {
				JSONObject tagJson = array.getJSONObject(i);
				if(tagJson != null) {
					result.add(ToTag(tagJson));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static Tag ToTag(JSONObject json) {
		if(json == null) return null;
		try {
			Tag tag = new Tag();
			tag.Id = json.getInt("Id");
			tag.Name = json.getString("Name");
			tag.Description = json.getString("Description");
			tag.SubscribersCount = json.getInt("SubscribersCount");
			tag.ArticlesCount = json.getInt("ArticlesCount");
			return tag;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<Folder> ToFolders(JSONObject json, String jsonFieldName) {
		ArrayList<Folder> result = new ArrayList<Folder>();
		JSONArray array;
		try {
			array = json.getJSONArray(jsonFieldName);
		
			if(array == null || array.length() == 0) return result;
			
			for(int i = 0; i < array.length(); i++) {
				JSONObject folderJson = array.getJSONObject(i);
				if(folderJson != null) {
					result.add(ToFolder(folderJson));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static Folder ToFolder(JSONObject json) {
		if(json == null) return null;
		try {
			Folder folder = new Folder();
			folder.Id = json.getInt("Id");
			folder.Name = json.getString("Name");

			folder.Feeds = ToFeeds(json, "Feeds");
			return folder;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<Feed> ToFeeds(JSONObject json, String jsonFieldName) {
		ArrayList<Feed> result = new ArrayList<Feed>();
		JSONArray array;
		try {
			array = json.getJSONArray(jsonFieldName);
		
			if(array == null || array.length() == 0) return result;
			
			for(int i = 0; i < array.length(); i++) {
				JSONObject feedJson = array.getJSONObject(i);
				if(feedJson != null) {
					result.add(ToFeed(feedJson));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static Feed ToFeed(JSONObject json) {
		if(json == null) return null;
		try {
			Feed feed = new Feed();
            feed.Id = json.getInt("Id");
			feed.Name = json.getString("Name");
            feed.Favicon = json.getString("Favicon");
            return feed;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<Article> ToArticles(JSONObject json, String jsonFieldName){
		ArrayList<Article> result = new ArrayList<Article>();
		JSONArray array;
		try {
			array = json.getJSONArray(jsonFieldName);
		
			if(array == null || array.length() == 0) return result;
			
			for(int i = 0; i < array.length(); i++) {
				JSONObject articleJson = array.getJSONObject(i);
				if(articleJson != null) {
					result.add(ToArticle(articleJson));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static Article ToArticle(JSONObject json) {
		if(json == null) return null;
		
		Article article = null;
		try {
			article = new Article();
            article.Id = json.getInt("Id");
            article.Url = json.getString("Url");
            article.FeedId = json.getInt("FeedId");
            article.FeedName = json.getJSONObject("Feed").getString("Name");
            article.Name = json.getString("Name");
            article.Favorites = json.getInt("FavoriteCount");
            article.Votes = json.getInt("LikesCount");
            article.Views = json.getInt("ViewsCount");
            article.TimeAgo = json.getString("TimeAgo");
            article.Body = json.getString("Body");
			article.TimeAgoLong = json.getString("TimeAgoLong");
			article.IsMyFavorite = json.getBoolean("IsMyFavorite");
			article.IVoted = json.getInt("MyVotes") > 0;
			article.IDownVoted = json.getInt("MyVotes") < 0;
			article.ShortUrl = json.getString("ShortUrl");
			article.Tweet = json.getString("Tweet");

			ArrayList<String> tags = new ArrayList<String>();
			JSONArray tagsJsonArray = json.getJSONArray("Tags");
			for(int i=0; i < tagsJsonArray.length(); i++) {
				JSONObject tagJson = tagsJsonArray.getJSONObject(i);
				tags.add(tagJson.getString("Name"));
			}
			article.Tags = tags;
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		
		return article;
	}

	public static ArrayList<Integer> ToListOfIntegers(JSONObject json, String alias) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		JSONArray array;
		try {
			array = json.getJSONArray(alias);
		
			if(array == null || array.length() == 0) return result;
			
			for(int i = 0; i < array.length(); i++) {
				result.add(array.getInt(i));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
}
