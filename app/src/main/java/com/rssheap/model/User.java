package com.rssheap.model;

import java.util.ArrayList;

public class User {
	public int Id;
	public String Guid;
	public ArrayList<Feed> Feeds = new ArrayList<>();
	public boolean HideVisited;
	public ArrayList<Folder> Folders = new ArrayList<>();
	public ArrayList<Integer> TagIds = new ArrayList<>();
	public ArrayList<Tag> Tags = new ArrayList<>();
}
