package com.banan.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String TAG = DBHelper.class.getSimpleName();
	public static final String DB_NAME = "anime.db";
	public static final int DB_VERS = 6;
	public static final boolean Debug = false;
	
	public Context context;
	
	// Tables
	public static final String ANIME_TABLE = "anime";
	public static final String EPISODE_TABLE = "episode";
	public static final String USER_EPISODE_TABLE = "user_episode";
	public static final String ANIME_RATING_TABLE = "anime_ratings";
	public static final String SYNONYM_TABLE = "anime_synonyms";
	public static final String ANIME_GENRE_TABLE = "anime_genre";
	public static final String GENRE_TABLE = "genre";
	
	// ANIME COLUMNS
	public static final String ANIME_TITLE_COL = "title";
	public static final String ANIME_DESC_COL = "desc";
	public static final String ANIME_IMAGE_COL = "image";
	public static final String ANIME_FANART_COL = "fanart";
	public static final String ANIME_STATUS_COL = "status";
	public static final String ANIME_ID = "anime_id";
	public static final String ANIME_RUNTIME_COL = "anime_runtime";
	public static final String ANIME_WATCHLIST = "anime_watchlist";
	public static final String ANIME_BAYES_RATING = "anime_bayes";
	public static final String ANIME_SCRAPE_REQUEST = "anime_scrape";
	
	//EPISODE COLUMNS
	public static final String EPISODE_TITLE_COL = "name";
	public static final String EPISODE_NUMBER_COL = "number";
	public static final String EPISODE_AIRED_COL = "aired";
	public static final String EPISODE_DESC_COL = "desc";
	public static final String EPISODE_ANIME_ID_COL = "anime_id";
	public static final String EPISODE_SEEN_COL = "seen";
	public static final String EPISODE_ID_COL = "id";
	public static final String EPISODE_SPECIAL_COL = "episode_special";
	public static final String EPISODE_IMAGE_COL = "episode_image";
	
	// ANIME_SYNONYMS COLUMNS
	public static final String SYNONYM_ID = "synonym_id";
	public static final String SYNONYM_TITLE = "synonym_title";
	public static final String SYNONYM_ANIME_ID = "synonym_anime_id";
	public static final String SYNONYM_LANG = "synonym_lang";
	
	// ANIME_GENRE COLUMNS 
	public static final String ANIME_GENRE_ID = "anime_genre_id";
	public static final String ANIME_GENRE_ANIME_ID = "anime_id";
	public static final String ANIME_GENRE_GENRE_ID = "genre_id";
	
	// GENRE COLUMNS
	public static final String GENRE_ID = "genre_id";
	public static final String GENRE_NAME = "genre_name";
	public static final String GENRE_DESC = "genre_desc";
	public static final String GENRE_IS_GENRE = "is_genre";
	
	//USER_EPISODE COLUMNS; THIS IS NOT USED. USE EPISOCE_SEEN INSTEAD.
	//public static final String USER_EPISODE_USERID_COL = "user_id";
	//public static final String USER_EPISODE_EPISODEID_COL = "episode_id";
	//public static final String USER_EPISODE_TIMESTAMP_COL = "timestamp";
	
	// ANIME_RATINGS COLUMNS
	public static final String ANIME_RATING_ANIMEID_COL = "anime_id";
	public static final String ANIME_RATING_USERID_COL = "user_id";
	public static final String ANIME_RATING_RATE_COL = "rate";
	
	
	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERS);
		this.context = context;
	}

	public Cursor query(SQLiteDatabase db, String query) {
		Cursor cursor = db.rawQuery(query, null);
		if (Debug) {
			Log.d(TAG, "Executing Query: "+ query);
		}
		return cursor;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		// Anime SQL
		String sql1 = "CREATE TABLE IF NOT EXISTS " + ANIME_TABLE 
				+ " (_id INTEGER PRIMARY KEY, "
				+ ANIME_ID + " NUMERIC," 
				+ ANIME_TITLE_COL + " TEXT," 
				+ ANIME_DESC_COL + " TEXT, "
				+ ANIME_IMAGE_COL + " TEXT," 
				+ ANIME_FANART_COL + " TEXT," 
				+ ANIME_STATUS_COL + " TEXT," 
				+ ANIME_RUNTIME_COL + " NUMERIC," 
				+ ANIME_WATCHLIST + " TEXT," 
				+ ANIME_BAYES_RATING + " NUMERIC," 
				+ ANIME_SCRAPE_REQUEST +" NUMERIC );";
		db.execSQL(sql1);
		
		String sql2 = "CREATE TABLE IF NOT EXISTS " + EPISODE_TABLE 
				+ " (_id INTEGER PRIMARY KEY, " 
				+ EPISODE_TITLE_COL + " TEXT,"
				+ EPISODE_ID_COL + " NUMERIC, " 
				+ EPISODE_NUMBER_COL + " NUMERIC, "
				+ EPISODE_AIRED_COL + " TEXT," 
				+ EPISODE_DESC_COL + " TEXT," 
				+ EPISODE_SEEN_COL + " TEXT," 
				+ EPISODE_SPECIAL_COL + " NUMERIC, "
				+ EPISODE_IMAGE_COL + " TEXT, "
				+ EPISODE_ANIME_ID_COL + " NUMERIC );";
		db.execSQL(sql2);
		
		
		String sql3 = "CREATE TABLE IF NOT EXISTS " + SYNONYM_TABLE 
				+ " (_id INTEGER PRIMARY KEY, " 
				+ SYNONYM_ID + " NUMERIC," 
				+ SYNONYM_ANIME_ID + " NUMERIC, "
				+ SYNONYM_LANG + " TEXT, "
				+ SYNONYM_TITLE + " TEXT );";
		db.execSQL(sql3);
		
		
		String sql4 = "CREATE TABLE IF NOT EXISTS " + ANIME_RATING_TABLE 
				+ " (_id INTEGER PRIMARY KEY, " 
				+ ANIME_RATING_ANIMEID_COL + " NUMERIC," 
				+ ANIME_RATING_USERID_COL + " NUMERIC, "
				+ ANIME_RATING_RATE_COL + " NUMERIC );";
		db.execSQL(sql4);
		
		String sql5 = "CREATE TABLE IF NOT EXISTS " + GENRE_TABLE 
				+ " (_id INTEGER PRIMARY KEY, " 
				+ GENRE_ID + " NUMERIC," 
				+ GENRE_NAME + " TEXT, "
				+ GENRE_IS_GENRE + " NUMERIC, "
				+ GENRE_DESC + " TEXT );";
		db.execSQL(sql5);
		
		String sql6 = "CREATE TABLE IF NOT EXISTS " + ANIME_GENRE_TABLE 
				+ " (_id INTEGER PRIMARY KEY, " 
				+ ANIME_GENRE_ID + " NUMERIC," 
				+ ANIME_GENRE_ANIME_ID + " NUMERIC, "
				+ ANIME_GENRE_GENRE_ID + " NUMERIC );";
		db.execSQL(sql6);
		
		if (Debug) {
			Log.d(TAG, "onCreate Called.");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL(String.format("DROP TABLE IF EXISTS %s", ANIME_TABLE));
		
		db.beginTransaction();
		
		//Check if the tables are missing.
		this.onCreate(db);
		
		try
        {          
            
            if(oldVersion == 1)
            {	
            	alterTable(db, EPISODE_TABLE); // Adding the episode image and special field
            }
            
            //db.execSQL(DATABASE_UPGRADE);
            if (Debug) 
    			Log.d(TAG, "Upgrade: Successfull.");
            
            db.setTransactionSuccessful();
        } 
		finally 
	    {
			db.endTransaction();
	    }
		
		if (Debug) {
			Log.d(TAG, "Upgrade: From database version "+ oldVersion + " to version " + newVersion);
		}
		

	}
	
	private boolean alterTable(SQLiteDatabase db, String table)
	{
		
		// Put in a list the existing columns
		List<String> columns = DBHelper.GetColumns(db, table);
		//Backup table
		db.execSQL("ALTER table " + table + " RENAME TO temp_" + table + ";");
		
		// Create the table with the new schema
		this.onCreate(db);
		//get the intersection with the new columns, this time columns taken from the upgraded table 
		columns.retainAll(DBHelper.GetColumns(db, table));
		
		// Restore data
		String cols = DBHelper.join(columns, ","); 
		db.execSQL(String.format( "INSERT INTO %s (%s) SELECT %s from temp_%s",table, cols, cols, table));
		
		//remove backup table 
		db.execSQL("DROP table temp_" + table);
		
		return true;
	}
	
	public static List<String> GetColumns(SQLiteDatabase db, String tableName) {
	    List<String> ar = null;
	    Cursor c = null;
	    try {
	        c = db.rawQuery("select * from " + tableName + " limit 1", null);
	        if (c != null) {
	            ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
	        }
	    } catch (Exception e) {
	        Log.v(tableName, e.getMessage(), e);
	        e.printStackTrace();
	    } finally {
	        if (c != null)
	            c.close();
	    }
	    return ar;
	}

	public static String join(List<String> list, String delim) {
	    StringBuilder buf = new StringBuilder();
	    int num = list.size();
	    for (int i = 0; i < num; i++) {
	        if (i != 0)
	            buf.append(delim);
	        buf.append((String) list.get(i));
	    }
	    return buf.toString();
	}
}
