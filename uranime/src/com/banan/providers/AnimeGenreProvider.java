package com.banan.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class AnimeGenreProvider extends ContentProvider{
		
		private static final String TAG = AnimeGenreProvider.class.getName();
		
		private static final UriMatcher sURIMatcher = new UriMatcher(
		        UriMatcher.NO_MATCH);
		
		public static final int ANIME_GENRE = 100;
		public static final int ANIME_GENRE_ID = 110;
		private static final String AUTHORITY = "com.banan.providers.AnimeGenreProvider";
		private static final String MOVIE_BASE_PATH = "episodes";
		
		private DBHelper db;

		static {
		    sURIMatcher.addURI(AUTHORITY, MOVIE_BASE_PATH, ANIME_GENRE);
		    sURIMatcher.addURI(AUTHORITY, MOVIE_BASE_PATH + "/#", ANIME_GENRE_ID);
		}
		
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
		        + "/" + MOVIE_BASE_PATH);
		
		@Override
	    public boolean onCreate() {
			db = new DBHelper(getContext());
	        return true;
	    }
		
		@Override
		public int delete(Uri uri, String selection, String[] selectionArgs)
		{
	        int uriType = sURIMatcher.match(uri);
	        SQLiteDatabase sqlDB = db.getWritableDatabase();
	        int rowsAffected = 0;
	        switch (uriType) {
	        case ANIME_GENRE:
	            rowsAffected = sqlDB.delete(DBHelper.ANIME_GENRE_TABLE,
	                    selection, selectionArgs);
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
	        }
	        getContext().getContentResolver().notifyChange(uri, null);
	        return rowsAffected;
		}

		@Override
		public String getType(Uri uri)
		{
			return null;
		}

		@Override
		public Uri insert(Uri uri, ContentValues values)
		{
	        int uriType = sURIMatcher.match(uri);
	        if (uriType != ANIME_GENRE) {
	            throw new IllegalArgumentException("Invalid URI for insert");
	        }
	        SQLiteDatabase sqlDB = db.getWritableDatabase();
	        
	        try {
	            long newID = sqlDB.insertOrThrow(DBHelper.ANIME_GENRE_TABLE,
	                    null, values);
	            if (newID > 0) {
	                Uri newUri = ContentUris.withAppendedId(uri, newID);
	                getContext().getContentResolver().notifyChange(uri, null);
	                return newUri;
	            } else {
	                Log.e(TAG, "Failed to insert row into " + uri);
	            }
	        } catch (SQLiteConstraintException e) {
	            Log.i(TAG, "Ignoring constraint failure.");
	        }
	        return null;
		}

		@Override
		public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
				String sortOrder)
		{
			SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
			queryBuilder.setTables(DBHelper.ANIME_GENRE_TABLE);
		
			int uriType = sURIMatcher.match(uri);
			switch (uriType) {
				case ANIME_GENRE_ID:
					queryBuilder.appendWhere("_id" + "="
							+ uri.getLastPathSegment());
				break;
				case ANIME_GENRE:
					// no filter
				break;
			default:
				throw new IllegalArgumentException("Unknown URI");
			}
		
			Cursor cursor = queryBuilder.query(db.getReadableDatabase(),
					projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			
			Context c = getContext();
			/*SharedPreferences sp = c.getSharedPreferences(Constants.PREFERENCE_FILENAME,Context.MODE_PRIVATE);
			String session_token = sp.getString(Constants.SESSION_TOKEN, "NONE");
			String default_department  = sp.getString(Constants.DEFAULT_DEPARTMENT, "NONE");*/
			
			return cursor;
		}

		@Override
		public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs)
		{
			SQLiteDatabase sqlDB = db.getWritableDatabase();
			
			int rowsAffected = sqlDB.update(DBHelper.ANIME_GENRE_TABLE, values, whereClause, whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			
			return rowsAffected;
		}
}
