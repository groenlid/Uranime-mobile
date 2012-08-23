package com.banan.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class UserList {
	
	public ArrayList<UserSeen> Library;
	public ArrayList<UserWatchlist> Watchlist;
	
	
	public int getTotalSize()
	{
		return this.Library.size() + this.Watchlist.size();
	}
	
	public class UserSeen {
		public int id; // Anime id
		public int amount_seen;
		public String latest_seen;
		public String title;
		public String fanart;
		public String image;
	}
	
	public class UserWatchlist {
		public int id; // Anime id
		public String title;
		public String fanart;
		public String image;
		public String watchlist_since;
	}
}
