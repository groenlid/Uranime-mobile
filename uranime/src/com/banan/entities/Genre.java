package com.banan.entities;

import com.banan.providers.DBHelper;

// Is also called Tags on server.
public class Genre { 
	public static String[] projection = {
		"_id",DBHelper.GENRE_ID,DBHelper.GENRE_NAME, DBHelper.GENRE_DESC, DBHelper.GENRE_IS_GENRE};
	
	public int id;
	public String name;
	public String description;
	public String is_genre;
}
