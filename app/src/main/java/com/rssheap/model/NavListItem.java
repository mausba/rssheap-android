package com.rssheap.model;

import java.io.Serializable;
import java.util.ArrayList;

public class NavListItem implements Serializable {
	private static final long serialVersionUID = -64439878917001403L;
	public String Id;
    public long IdLong;
    public String Title;
    public int Icon;
    public String IconUrl;
    
    public ArrayList<NavListItem> children;
     
    public NavListItem()
    {
        this.IdLong = -1;
        this.Id = "-1";
        children = new ArrayList<>();
    }
    
    public NavListItem(long id, String title, int icon) {
    	this.IdLong = id;
        this.Title = title;
        this.Icon = icon;
        this.children = new ArrayList<NavListItem>();
    }
    
    public NavListItem(String id, String title, int icon) {
    	this.Id = id;
        this.Title = title;
        this.Icon = icon;
        this.children = new ArrayList<NavListItem>();
    }
    
    public NavListItem(String id, String title, String iconUrl) {
    	this.Id = id;
        this.Title = title;
        this.children = new ArrayList<NavListItem>();
        this.IconUrl = iconUrl;
    }
}
