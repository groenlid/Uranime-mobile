package com.banan.providers;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.banan.entities.AnimeGenre;
import com.banan.entities.AnimeRequest;
import com.banan.entities.Constants;
import com.banan.entities.Episode;
import com.banan.entities.EpisodeList;
import com.banan.entities.Anime;
import com.banan.entities.Genre;
import com.banan.entities.Synonym;
import com.banan.entities.UserList;
import com.banan.trakt.RestClient;
import com.banan.anime.R;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class RestService extends Service
{
	public static final String TAG = RestService.class.getName();
	private DownloadTask asyncTasker;
	
	public static final String ACTIVITY = "activity";
	
	public static final int PUT = 1;
	public static final int GET = 2;
	public static final int PUSH = 3;
	public static final int DELETE = 4;
	
	public static final int OBJECT_TYPE_ANIMELIST = 5;
	public static final int OBJECT_TYPE_EPISODE = 6;
	public static final int OBJECT_TYPE_ANIME = 7;
	public static final int OBJECT_TYPE_WATCHLIST = 8;
	
	public static final int UPDATE_NOTIFICATION = 100;

	public static final String PARAMS = "input_params";
	public static final String ACTION = "action";
	public static final String OBJECT_TYPE = "object_type";
	
	public static Intent intent;
	
	public static Intent getInstance(){
		return intent;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if(intent == null)
			return 0;
		RestService.intent = intent;
		
		asyncTasker = new DownloadTask();
		asyncTasker.execute((Object)intent.getIntExtra(ACTION, GET),(Object)intent.getIntExtra(OBJECT_TYPE, OBJECT_TYPE_ANIMELIST), 
				(Object)intent.getStringArrayListExtra(PARAMS));
		return Service.START_FLAG_REDELIVERY;
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}
	
	/**
	 * Inputs:
	 * method_type, object_type
	 * @author groenlid
	 *
	 */
	private class DownloadTask extends AsyncTask<Object, String, Boolean> {
		
		private final String TAG = DownloadTask.class.getName();
		Handler mHandler;
		private NotificationManager notificationManager;
		private Notification notification;
		private RemoteViews contentView;
		
		@Override
		protected Boolean doInBackground(Object... params){
			//What kind of object are we downloading
			int method_type = (Integer)params[0];
			int object_type = (Integer)params[1];
			
			ArrayList<String> listparam = (ArrayList<String>)params[2];
			
			switch(method_type){
			case RestService.GET:
				switch(object_type){
					case RestService.OBJECT_TYPE_ANIMELIST:
						GET_USER_ANIME();
					break;
					case RestService.OBJECT_TYPE_ANIME:
						FETCH_ANIME(getApplicationContext(), listparam, null);
					break;
				}
				break;
			case RestService.PUT:
				switch(object_type){
				case RestService.OBJECT_TYPE_EPISODE:
					RestService.MARK_EPISODE_AS_WATCHED(getApplicationContext(), Integer.parseInt(listparam.get(0)), true, null);
					break;
				case RestService.OBJECT_TYPE_WATCHLIST:
					RestService.CHANGE_ANIME_WATCHLIST(getApplicationContext(), Integer.parseInt(listparam.get(0)), listparam.get(1), null);
					break;
				case RestService.OBJECT_TYPE_ANIME:
					RestService.MARK_ANIME_AS_WATCHED(getApplicationContext(), Integer.parseInt(listparam.get(0)), Boolean.parseBoolean(listparam.get(1)), null);
				}
				break;
			case RestService.DELETE:
				switch(object_type){
				case RestService.OBJECT_TYPE_EPISODE:
					RestService.MARK_EPISODE_AS_WATCHED(getApplicationContext(), Integer.parseInt(listparam.get(0)), false, null);
					break;
				}
				break;
			}
			return null;
		}
		
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			//getActivity().setSupportProgressBarIndeterminateVisibility(true);
			mHandler = new Handler();
			/*SherlockActivity activity = (SherlockActivity)intent.getSerializableExtra(ACTIVITY);
			
			if (activity != null)
				activity.setSupportProgress(0);*/

			
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			 
	        notification = new Notification(R.drawable.ic_action_arrow_right, "UrAnime -_-", System.currentTimeMillis());
	        //notification.flags |= Notification.FLAG_ONGOING_EVENT;
	        
	        notification.contentView = new RemoteViews(getPackageName(), R.layout.progress);
	        
	        notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.ic_action_refresh);
	        notification.contentView.setTextViewText(R.id.status_text, "Fetching information: 0%");
	        notification.contentView.setProgressBar(R.id.status_progress, 100, 0, false);
	        
	        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
	        	    new Intent(), 0);
	        
	        notification.setLatestEventInfo(getApplicationContext(),"Contacting server", "0%", contentIntent);
	       
	        notificationManager.notify(69, notification);
		}
		
		@Override
		protected void onPostExecute(Boolean result){
			Toast.makeText(getApplicationContext(), "Finished fetching/sending information", Toast.LENGTH_SHORT).show();
			
			notificationManager.cancel(69);
			
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			Toast.makeText(getApplicationContext(), values[0] , Toast.LENGTH_SHORT).show();
			
			PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
	        	    new Intent(), 0);
			
			
			notification.contentView.setTextViewText(R.id.status_text, "Fetching information: " + values[1] + "%");
			notification.contentView.setProgressBar(R.id.status_progress, 100, Integer.parseInt(values[1]), false);
			notification.setLatestEventInfo(getApplicationContext(),"Contacting server", values[1]+"%", contentIntent);
			notificationManager.notify(69, notification);
			
		}
		
		private boolean GET_USER_ANIME(){
			RestClient rest = RestClient.getInstance(getApplicationContext());
			
			Context c = getApplicationContext();
			String userid = "";
			
			if(Constants.getUserID(c).equals(Constants.NO_ID))
				return false;
			else
				userid = Constants.getUserID(c);
			
			String response = rest.ReadMethod(Constants.REST_USER_LIBRARY_WATCHLIST + userid + ".json");
			
			Gson gson = new Gson();

			UserList requests = gson.fromJson(response,UserList.class);
			
			if(requests == null)
				return false;
			
			int amount = requests.getTotalSize();
			int current = 0;
			
			// Reset episodes seen before registering the new one
			ContentValues nullPointer = new ContentValues();
			nullPointer.putNull(DBHelper.EPISODE_SEEN_COL);
			int nullRows = getContentResolver().update(EpisodeProvider.CONTENT_URI , nullPointer, DBHelper.EPISODE_SEEN_COL + " IS NOT NULL", null);
			
			// Reset watchlist statistics
			ContentValues nullPointerWatchlist = new ContentValues();
			nullPointerWatchlist.putNull(DBHelper.ANIME_WATCHLIST);
			int nullRowsWatchlist = getContentResolver().update(AnimeProvider.CONTENT_URI , nullPointerWatchlist, DBHelper.ANIME_WATCHLIST + " IS NOT NULL", null);
			
			//Log.e("Episode seen purge", "Purged " + nullRows + " episodes");
			
			ArrayList<Integer> ids = new ArrayList<Integer>();
			
			for(UserList.UserSeen r : requests.Library)
			{
				
				
				current++;
				if(ids.contains(r.id))
					continue;
				
				RestService.FETCH_ANIME(c, r.id,rest);
				
				double percent = current / (amount*1.0) * 100;
				//Log.e("2",""+percent);
				publishProgress("Added '" + r.title + "' to the animelist", ""+ (int)percent);
				ids.add(r.id);
			}
			
			for(UserList.UserWatchlist r : requests.Watchlist)
			{
				
		
				current++;
				if(ids.contains(r.id))
					continue;
				
				RestService.FETCH_ANIME(c, r.id,rest);
				
				// Fetch the anime's episode
				// TODO: Implement private function
				//RestService.FETCH_ANIME_EPISODES(c,r.id,rest);

				double percent = current / (amount*1.0) * 100;
				//Log.e("2",""+percent);
				publishProgress("Added '" + r.title + "' to the watchlist", ""+ (int)percent);
				ids.add(r.id);
			}
			
			
			
			return true;
		}
		
		private void FETCH_ANIME(Context c, ArrayList<String> animeIDs, RestClient rest)
		{
			for(int i = 0; i < animeIDs.size(); i++)
			{
				double percent = i / (animeIDs.size()*1.0) * 100;
				publishProgress("Refreshed anime id:'" + animeIDs.get(i) + "'", ""+ (int)percent);
				RestService.FETCH_ANIME(c, Integer.parseInt(animeIDs.get(i)),rest);
			}
		}
		
	}
	
	public static void FETCH_ANIME(Context c, int animeID, RestClient rest){
		
		if(rest == null)
			rest = RestClient.getInstance(c);
		
		// Check if user is logged in.
		String userID = Constants.getUserID(c);
		
		String animeString = rest.ReadMethod(Constants.REST_ANIME + animeID );
		
		Gson gson = new Gson();
		
		Anime anime = gson.fromJson(animeString,Anime.class);
		
		if(anime == null)
			return;
		
		RestService.parse_anime(c, anime);
		
		//RestService.FETCH_ANIME_EPISODES(c,animeID,rest);
	}
	
	private static void parse_anime(Context c, Anime anime)
	{
		
		int animeID = anime.id;
		String userID = Constants.getUserID(c);
		
		Cursor shows = c.getContentResolver().query(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID+"="+animeID, null, null);
		
		ContentValues aResult = new ContentValues();
		
		aResult.put(DBHelper.ANIME_DESC_COL, anime.desc);
		aResult.put(DBHelper.ANIME_FANART_COL, anime.fanart);
		aResult.put(DBHelper.ANIME_IMAGE_COL, anime.image);
		aResult.put(DBHelper.ANIME_STATUS_COL, anime.status);
		aResult.put(DBHelper.ANIME_TITLE_COL, anime.title);
		aResult.put(DBHelper.ANIME_ID, anime.id);
		aResult.put(DBHelper.ANIME_RUNTIME_COL, anime.runtime);
		
		if(!userID.equals(Constants.NO_ID))
		{
			if(anime.watchlist == null)
				aResult.putNull(DBHelper.ANIME_WATCHLIST);
			else
				aResult.put(DBHelper.ANIME_WATCHLIST, anime.watchlist);
		}
		// If it does not exist.. we add it to the database
		if(shows.getCount() == 0)
			c.getContentResolver().insert(AnimeProvider.CONTENT_URI, aResult);
		else if(shows.getCount() == 1)
			c.getContentResolver().update(AnimeProvider.CONTENT_URI, aResult, DBHelper.ANIME_ID+"="+anime.id , null);
		else
			Log.e(TAG, "The db includes duplicates of show with id :" + anime.id + " and title: " + anime.title);
		
		shows.close();
		
		// Update/Insert anime episodes
		
		for(Episode e : anime.getEpisodes())
		{
			//Log.e("EPISODE: ", e.name);
			// Using custom string select 
			Cursor episode = c.getContentResolver().query(EpisodeProvider.CONTENT_URI, new String[]{"_id",DBHelper.EPISODE_ID_COL}, DBHelper.EPISODE_ID_COL+"= ?",  new String[]{"" + e.id}, null);
			
			ContentValues result = new ContentValues();
			
			result.put(DBHelper.EPISODE_AIRED_COL, e.aired);
			result.put(DBHelper.EPISODE_ANIME_ID_COL, e.anime_id);
			result.put(DBHelper.EPISODE_DESC_COL, e.description);
			result.put(DBHelper.EPISODE_TITLE_COL, e.name);
			result.put(DBHelper.EPISODE_ID_COL, e.id);
			result.put(DBHelper.EPISODE_NUMBER_COL, e.number);
			result.put(DBHelper.EPISODE_SPECIAL_COL, e.special);
			result.put(DBHelper.EPISODE_IMAGE_COL, e.image);
			
			if(!userID.equals(Constants.NO_ID))
			{
				if(e.seen == null)
					result.putNull(DBHelper.EPISODE_SEEN_COL);
				else
					result.put(DBHelper.EPISODE_SEEN_COL, e.seen);
			}
			
			if(episode.getCount() == 0)
				c.getContentResolver().insert(EpisodeProvider.CONTENT_URI, result);
			else
				c.getContentResolver().update(EpisodeProvider.CONTENT_URI, result, DBHelper.EPISODE_ID_COL +"= ?", new String[]{"" + e.id} );
			episode.close();
		}
		
		// Update/Insert anime synonyms
		
		for(Synonym s : anime.getSynonyms())
		{
			Cursor synonym = c.getContentResolver().query(SynonymProvider.CONTENT_URI, Synonym.projection, DBHelper.SYNONYM_ID+"="+s.id, null, null);
			ContentValues synonymContent = new ContentValues();
			synonymContent.put(DBHelper.SYNONYM_ID, s.id);
			synonymContent.put(DBHelper.SYNONYM_ANIME_ID, s.anime_id);
			synonymContent.put(DBHelper.SYNONYM_TITLE, s.title);
			synonymContent.put(DBHelper.SYNONYM_LANG, s.lang);
			
			if(synonym.getCount() == 0)
				c.getContentResolver().insert(SynonymProvider.CONTENT_URI, synonymContent);
			else
				c.getContentResolver().update(SynonymProvider.CONTENT_URI, synonymContent, DBHelper.SYNONYM_ID +"="+s.id, null);
			if(DBHelper.Debug)
				Log.e(TAG, "Anime " + anime.title + " is also called " + s.title);
			
			synonym.close();
		}
		
		// Update/Insert anime genres
		for(Genre g : anime.getTags())
		{
			// Add the genres for the anime
			Cursor genre = c.getContentResolver().query(GenreProvider.CONTENT_URI, Genre.projection, DBHelper.GENRE_ID+"="+g.id, null, null);
			ContentValues genreContent = new ContentValues();
			genreContent.put(DBHelper.GENRE_ID, g.id);
			genreContent.put(DBHelper.GENRE_DESC, g.description);
			genreContent.put(DBHelper.GENRE_NAME, g.name);
			genreContent.put(DBHelper.GENRE_IS_GENRE, (g.is_genre != null) ? 1 : null);
			
			if(genre.getCount() == 0)
				c.getContentResolver().insert(GenreProvider.CONTENT_URI, genreContent);
			else
				c.getContentResolver().update(GenreProvider.CONTENT_URI, genreContent, DBHelper.GENRE_ID +"="+g.id, null);

			genre.close();
			
			// Connect the genres/tags to the anime if it is not already connected
			Cursor genreAnime = c.getContentResolver().query(
					AnimeGenreProvider.CONTENT_URI, 
					AnimeGenre.projection, 
					DBHelper.ANIME_GENRE_ANIME_ID + "= ? AND " +
					DBHelper.ANIME_GENRE_GENRE_ID + "= ?", 
					new String[]{""+anime.id,""+g.id}, 
					null);
			
			ContentValues genreAnimeContent = new ContentValues();
			genreAnimeContent.put(DBHelper.ANIME_GENRE_ANIME_ID, anime.id);
			genreAnimeContent.put(DBHelper.ANIME_GENRE_GENRE_ID, g.id);
			
			if(genreAnime.getCount() == 0)
				// We add the link between genre and anime to the database..
				c.getContentResolver().insert(AnimeGenreProvider.CONTENT_URI, genreAnimeContent); 
			
			genreAnime.close();
			
		}
	}
	
	/**
	 * 
	 * @param c
	 * @param anime_id
	 * @param watched: if the anime should be marked as watched or unseen. true = watched.
	 * @param rest
	 * @return
	 */
	public static boolean MARK_ANIME_AS_WATCHED(Context c, int anime_id, boolean watched, RestClient rest){
		if(rest == null)
			rest = RestClient.getInstance(c);
		
		String userid = "";
		
		//Cursor animeCursor = c.getContentResolver().query(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID+"="+anime_id, null, null);
		ContentValues result = new ContentValues();
		
		if(watched)
			result.put(DBHelper.EPISODE_SEEN_COL, Constants.timeToString(null));
		else
			result.putNull(DBHelper.EPISODE_SEEN_COL);
		
		c.getContentResolver().update(EpisodeProvider.CONTENT_URI, result, DBHelper.EPISODE_ANIME_ID_COL +"="+anime_id, null);
		c.getContentResolver().notifyChange(EpisodeProvider.CONTENT_URI, null);
		
		if(Constants.getUserID(c).equals(Constants.NO_ID))
			return false;
		else
			userid = Constants.getUserID(c);
		
		
		Cursor episodesCursor = c.getContentResolver().query(EpisodeProvider.CONTENT_URI, Episode.projection, DBHelper.EPISODE_ANIME_ID_COL+"="+anime_id, null, null);
		
		JSONArray episodes = new JSONArray();
		
		if(!episodesCursor.moveToFirst())
			return false;
		
		try {
			while(episodesCursor.moveToNext())
			{
				JSONObject episode = new JSONObject();
				episode.put("id", episodesCursor.getInt(episodesCursor.getColumnIndexOrThrow(DBHelper.EPISODE_ID_COL)));
				if(watched)
					episode.put("seen", Constants.timeToString(null));
				else
					episode.put("seen", JSONObject.NULL);
				episodes.put(episode);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		JSONObject putString = RestClient.createJSONObject();
		
		putString = RestClient.addColumn("episodes", episodes);
		
		String animeString = rest.PutMethod(Constants.REST_ANIME + anime_id, putString);
		
		//Gson gson = new Gson();
		
		//Anime anime = gson.fromJson(animeString,Anime.class);
		
		//if(anime == null)
		//	return false;
		
		// This is to slow..
		// RestService.parse_anime(c, anime);
		
		//String anString = rest.ReadMethod(Constants.REST_ANIME + "/" + anime_id);
		
		/*** TODO: Need to check results from this method **/
		
		return true;
	}
	public static boolean MARK_EPISODE_AS_WATCHED(Context c, int episode_id, boolean watched, RestClient rest){
		if(rest == null)
			rest = RestClient.getInstance(c);
		
		Cursor episode = c.getContentResolver().query(EpisodeProvider.CONTENT_URI, Episode.projection, DBHelper.EPISODE_ID_COL+"="+episode_id, null, null);
		ContentValues result = new ContentValues();
		if(watched)
			result.put(DBHelper.EPISODE_SEEN_COL, Constants.timeToString(null));
		else
			result.putNull(DBHelper.EPISODE_SEEN_COL);
		
		c.getContentResolver().update(EpisodeProvider.CONTENT_URI, result, DBHelper.EPISODE_ID_COL +"="+episode_id, null);
		c.getContentResolver().notifyChange(EpisodeProvider.CONTENT_URI, null);
		
		
		if(!Constants.getUserID(c).equals(Constants.NO_ID))
		{	
			if(!episode.moveToFirst())
				return false;
			
			int animeid = episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL));
			
			//String userid = Constants.getUserID(c);
			//String epString = rest.ReadMethod(Constants.REST_UNWATCH_EPISODE + userid + "/" + episode_id + ".json");
			
			JSONArray episodes = new JSONArray();
			try {
				
				JSONObject episodeObject = new JSONObject();
				episodeObject.put("id", episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_ID_COL)));
				if(watched)
					episodeObject.put("seen", Constants.timeToString(null));
				else
					episodeObject.put("seen", JSONObject.NULL);
				episodes.put(episodeObject);
				
			} catch (IllegalArgumentException e) {
				
				e.printStackTrace();
				return false;
			
			} catch (JSONException e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
				
			}
			
			JSONObject putString = RestClient.createJSONObject();
			
			putString = RestClient.addColumn("episodes", episodes);
			
			String animeString = rest.PutMethod(Constants.REST_ANIME + animeid, putString);
		}		
		return true;
	}
	
	public static boolean CHANGE_ANIME_WATCHLIST(Context c, int anime_id, String inWatchlist, RestClient rest){
		
		if(rest == null)
			rest = RestClient.getInstance(c);
		
		String userid = "";
		
		ContentValues result = new ContentValues();
		
		String now = Constants.timeToString(null);
		
		if(!inWatchlist.equals("true"))
			result.putNull(DBHelper.ANIME_WATCHLIST); // This should be boolean
		else
			result.put(DBHelper.ANIME_WATCHLIST,now);
		c.getContentResolver().update(AnimeProvider.CONTENT_URI, result, DBHelper.ANIME_ID +"="+anime_id, null);
		c.getContentResolver().notifyChange(EpisodeProvider.CONTENT_URI, null);
		
		if(!Constants.getUserID(c).equals(Constants.NO_ID))
		{
			userid = Constants.getUserID(c);
			String changes = inWatchlist;//(inWatchlist) ? "true": "false";
			JSONObject putString = RestClient.createJSONObject();
			
			if(inWatchlist.equals("true"))
				putString = RestClient.addColumn("watchlist", now);
			else
				putString = RestClient.addNullColumn("watchlist");
			
			String animeString = rest.PutMethod(Constants.REST_ANIME + anime_id, putString);
			
			Gson gson = new Gson();
			
			Anime anime = gson.fromJson(animeString,Anime.class);
			
			if(anime == null)
				return false;
			
			
			
			// This is to slow..
			// RestService.parse_anime(c, anime);
			
			//String epString = rest.ReadMethod(Constants.REST_ANIME_WATCHLIST + userid + "/" + anime_id + "/" + changes + ".json");
		}
		
		return true;
	}
}