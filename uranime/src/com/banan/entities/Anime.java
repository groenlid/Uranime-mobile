package com.banan.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import android.util.Log;

import com.banan.providers.DBHelper;

// An implementation of the Anime class. http://trakt.tv/api-docs/show-summary
public class Anime{
	
	public static String[] projection = {
		"_id",DBHelper.ANIME_ID,DBHelper.ANIME_WATCHLIST, DBHelper.ANIME_BAYES_RATING, 
		DBHelper.ANIME_DESC_COL, DBHelper.ANIME_FANART_COL,DBHelper.ANIME_IMAGE_COL,
		DBHelper.ANIME_TITLE_COL,DBHelper.ANIME_STATUS_COL, DBHelper.ANIME_RUNTIME_COL,
		DBHelper.ANIME_SCRAPE_REQUEST};
	
	public static String[] projection_only_id = {"_id",DBHelper.ANIME_ID};
	
	public String title;
	
	public int id;
	
	// Enum?
	public String desc;
	
	public String image;
	
	public String fanart;
	
	public String status;
	
	public Double bayes;
	
	public int runtime;
	
	public String watchlist; // null if not in watchlist, timestamp otherwise
	
	public Map<String, Episode> episodes;
	
	public Map<String, Synonym> synonyms;
	
	public Map<String, Genre> tags;
	
	public Collection<Episode> getEpisodes()
	{
		if(episodes == null)
			return new ArrayList<Episode>();
		return episodes.values();
	}
	
	public Collection<Genre> getTags() {
		if(tags == null)
			return new ArrayList<Genre>();
		return tags.values();
	}
	
	public Collection<Synonym> getSynonyms()
	{
		if(synonyms == null)
			return new ArrayList<Synonym>();
		return synonyms.values();
	}
	
	public String getImage(int width){
		return Constants.IMAGE_RESIZE + image + "/" + width;
	}
	
	/**
	 * If the user gives 0 as height or width.. This will be calculated by the system.
	 * @param width
	 * @param height
	 * @param image
	 * @return
	 */
	public static String resizeImage(int width, int height, String image){
		//Log.e("asdasd","http://src.sencha.io/" + width + "/" + height + "/" + Constants.IMAGE_PATH + image);
		
		//return "http://src.sencha.io/" + width + "/" + height + "/" + Constants.IMAGE_PATH + image; 
		
		
		if(width == -1 || height == -1)
			return Constants.IMAGE_RESIZE + image;
		return Constants.IMAGE_RESIZE + image + "/" + width + "/" + height;
		
	}
	
	public String getFanart(int width){
		return "http://src.sencha.io/" + width + "/" + fanart;
	}
	
}
