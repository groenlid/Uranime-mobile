package com.banan.entities;

import com.banan.providers.DBHelper;

public class Synonym {
	public int id;
	public int anime_id;
	public String title;
	public String lang;
	
	public static String[] projection = {"_id",DBHelper.SYNONYM_ID, DBHelper.SYNONYM_LANG, DBHelper.SYNONYM_TITLE, DBHelper.SYNONYM_ANIME_ID};
}
