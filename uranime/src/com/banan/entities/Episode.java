package com.banan.entities;

import com.banan.providers.DBHelper;

public class Episode {
	public int number;
	public int id;
	public String name;
	public int anime_id;
	public String description;
	public String aired;
	public String image;
	public boolean special;
	
	// Need to add ratings here
	
	public static String[] projection = {"_id",DBHelper.EPISODE_ID_COL, DBHelper.EPISODE_SPECIAL_COL, DBHelper.EPISODE_IMAGE_COL, DBHelper.EPISODE_AIRED_COL, DBHelper.EPISODE_ANIME_ID_COL, DBHelper.EPISODE_DESC_COL,DBHelper.EPISODE_NUMBER_COL,DBHelper.EPISODE_SEEN_COL,DBHelper.EPISODE_TITLE_COL};
	public static String[] projection_distinct = {"_id", DBHelper.EPISODE_ANIME_ID_COL};
}
