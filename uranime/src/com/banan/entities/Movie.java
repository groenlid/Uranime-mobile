package com.banan.entities;

import java.util.Date;

// An implementation of the Movie class. http://trakt.tv/api-docs/movie-summary
public class Movie extends Media{
	
	
	/*public static String[] projection = {"_id", DBHelper.MOVIE_RELEASED_COL, DBHelper.MOVIE_TRAILER_COL,DBHelper.MOVIE_RUNTIME_COL,DBHelper.MOVIE_TAGLINE_COL,DBHelper.MOVIE_CERTIFICATION_COL,DBHelper.MOVIE_IMAGE_BANNER_COL,
		DBHelper.MOVIE_IMAGE_FANART_COL,DBHelper.MOVIE_IMAGE_POSTER_COL,DBHelper.MOVIE_OVERVIEW_COL,DBHelper.MOVIE_TITLE_COL,DBHelper.MOVIE_TMDB_COL,
		DBHelper.MOVIE_IMDB_COL,DBHelper.MOVIE_URL_COL,DBHelper.MOVIE_YEAR_COL};*/
	/** 
	 * This is loaded as a longint.
	 * Should probably save it as int and show it as Data in the get method. 
	 * */
	public Date released;
	
	public String trailer;
	
	public int runtime;
	
	public String tagline;
	
	public String imdb_id;
	
	// themoviedb id
	public String tmdb_id;
	
	/* Insert images here */
	
}
