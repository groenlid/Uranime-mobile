package com.banan.entities;

import com.banan.providers.DBHelper;

public class AnimeGenre {
	public static String[] projection = {
		"_id",DBHelper.ANIME_GENRE_ID,DBHelper.ANIME_GENRE_ANIME_ID, DBHelper.ANIME_GENRE_GENRE_ID};
	public int id;
	public int anime_id;
	public int genre_id;
}
