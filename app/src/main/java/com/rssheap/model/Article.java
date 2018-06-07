package com.rssheap.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class Article implements Serializable {
	private static final long serialVersionUID = 7402407782556453745L;
	public int Id;
	public String Url;
	public String Name;
	public String Body;
	public String TimeAgo;
	public String TimeAgoLong;
	public boolean IsMyFavorite;
	public boolean IVoted;
	public boolean IDownVoted;
	public String ShortUrl;
	public String Tweet;
	public int Votes;
	public int Views;
	public int Favorites;
	public Date Published;
	public int FeedId;
	public String FeedName;
	public ArrayList<String> Tags;
}
