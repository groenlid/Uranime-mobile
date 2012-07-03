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
	public static final int DB_VERS = 2;
	public static final boolean Debug = true;
	
	public Context context;
	
	// Tables
	public static final String ANIME_TABLE = "anime";
	public static final String EPISODE_TABLE = "episode";
	public static final String USER_EPISODE_TABLE = "user_episode";
	public static final String ANIME_RATING_TABLE = "anime_ratings";
	
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
		
		/*
		String sql3 = "CREATE TABLE IF NOT EXISTS " + USER_EPISODE_TABLE + " (_id INTEGER PRIMARY KEY, " + USER_EPISODE_EPISODEID_COL + " NUMERIC," + USER_EPISODE_USERID_COL + " NUMERIC, "
				+ USER_EPISODE_TIMESTAMP_COL + " TEXT );";
		db.execSQL(sql3);
		*/
		
		String sql4 = "CREATE TABLE IF NOT EXISTS " + ANIME_RATING_TABLE 
				+ " (_id INTEGER PRIMARY KEY, " 
				+ ANIME_RATING_ANIMEID_COL + " NUMERIC," 
				+ ANIME_RATING_USERID_COL + " NUMERIC, "
				+ ANIME_RATING_RATE_COL + " NUMERIC );";
		db.execSQL(sql4);
		
		if (Debug) {
			Log.d(TAG, "onCreate Called.");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//db.execSQL(String.format("DROP TABLE IF EXISTS %s", ANIME_TABLE));
		
		db.beginTransaction();
		
		try
        {          
            
            if(oldVersion == 1)
            {	
            	
            	//Check if the tables are missing.
        		this.onCreate(db);
        		
        		// Put in a list the existing columns
        		List<String> columns = DBHelper.GetColumns(db, EPISODE_TABLE);
        		//Backup table
        		db.execSQL("ALTER table " + EPISODE_TABLE + " RENAME TO temp_" + EPISODE_TABLE + ";");
        		
        		// Create the table with the new schema
        		this.onCreate(db);
        		//get the intersection with the new columns, this time columns taken from the upgraded table 
        		columns.retainAll(DBHelper.GetColumns(db, EPISODE_TABLE));
        		
        		// Restore data
        		String cols = DBHelper.join(columns, ","); 
        		db.execSQL(String.format( "INSERT INTO %s (%s) SELECT %s from temp_%s",EPISODE_TABLE, cols, cols, EPISODE_TABLE));
        		
        		//remove backup table 
        		db.execSQL("DROP table 'temp_" + EPISODE_TABLE);
        		
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
