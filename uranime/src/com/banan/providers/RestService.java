package com.banan.providers;

import java.util.ArrayList;
import java.util.Calendar;

import com.banan.entities.AnimeRequest;
import com.banan.entities.Constants;
import com.banan.entities.Episode;
import com.banan.entities.EpisodeList;
import com.banan.entities.Anime;
import com.banan.entities.UserEpisode;
import com.banan.entities.UserEpisodeList;
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
			
			String department, type;
			switch(method_type){
			case RestService.GET:
				switch(object_type){
					case RestService.OBJECT_TYPE_ANIMELIST:
						GET_USER_ANIME();
					break;
					case RestService.OBJECT_TYPE_ANIME:
						FETCH_ANIME(getApplicationContext(), listparam, null);
					break;
					case RestService.OBJECT_TYPE_EPISODE:
						FETCH_ANIME_EPISODES(getApplicationContext(),Integer.parseInt(listparam.get(0)),null);
					break;
				}
				break;
			case RestService.PUT:
				switch(object_type){
				case RestService.OBJECT_TYPE_EPISODE:
					RestService.MARK_EPISODE_AS_WATCHED(getApplicationContext(), Integer.parseInt(listparam.get(0)),listparam.get(1),null);
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
					RestService.MARK_EPISODE_AS_UNWATCHED(getApplicationContext(), Integer.parseInt(listparam.get(0)), null);
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
			
			//Log.e("TAG","debug");
			
			Context c = getApplicationContext();
			String userid = "";
			
			
			
			if(Constants.getUserID(c).equals(Constants.NO_ID))
				return false;
			else
				userid = Constants.getUserID(c);
			
			String response = rest.ReadMethod(Constants.REST_USER_LIBRARY + userid + ".json");
			
			Gson gson = new Gson();

			AnimeRequest requests = gson.fromJson(response,AnimeRequest.class);
			
			if(requests == null)
				return false;
			
			String responseWatchlist = rest.ReadMethod(Constants.REST_USER_WATCHLIST + userid + ".json");
			
			AnimeRequest requestsWatchlist = gson.fromJson(responseWatchlist,AnimeRequest.class);
			
			if(requestsWatchlist == null)
				return false;
			
			int amount = requests.getAnimelist().size() + requestsWatchlist.getAnimelist().size();
			int current = 0;
			
			// Reset episodes seen before registering the new one
			ContentValues nullPointer = new ContentValues();
			nullPointer.putNull(DBHelper.EPISODE_SEEN_COL);
			int nullRows = getContentResolver().update(EpisodeProvider.CONTENT_URI , nullPointer, DBHelper.EPISODE_SEEN_COL + " IS NOT NULL", null);
			//Log.e("Episode seen purge", "Purged " + nullRows + " episodes");
			
			ArrayList<Integer> ids = new ArrayList<Integer>();
			
			for(Anime r : requests.animelist)
			{
				Cursor shows = getContentResolver().query(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID+"="+r.id, null, null);
				ContentValues aResult = new ContentValues();
				
				aResult.put(DBHelper.ANIME_DESC_COL, r.desc);
				aResult.put(DBHelper.ANIME_FANART_COL, r.fanart);
				aResult.put(DBHelper.ANIME_IMAGE_COL, r.image);
				aResult.put(DBHelper.ANIME_STATUS_COL, r.status); // NOTHING HERE?
				aResult.put(DBHelper.ANIME_TITLE_COL, r.title);
				aResult.put(DBHelper.ANIME_ID, r.id);
				aResult.put(DBHelper.ANIME_RUNTIME_COL, r.runtime);
				
				// If it does not exist.. we add it to the database
				if(shows.getCount() == 0)
					getContentResolver().insert(AnimeProvider.CONTENT_URI, aResult);
				else if(shows.getCount() == 1)
					getContentResolver().update(AnimeProvider.CONTENT_URI, aResult, DBHelper.ANIME_ID+"="+r.id , null);
				else
					Log.e(TAG, "The db includes duplicates of show with id :" + r.id + " and title: " + r.title);
				
				current++;
				if(ids.contains(r.id))
					continue;
								
				// Fetch the anime's episode
				RestService.FETCH_ANIME_EPISODES(c,r.id,rest);
				
				

				
				double percent = current / (amount*1.0) * 100;
				//Log.e("2",""+percent);
				publishProgress("Added '" + r.title + "' to the animelist", ""+ (int)percent);
				ids.add(r.id);
			}
			
			for(Anime r : requestsWatchlist.animelist)
			{

				Cursor shows = getContentResolver().query(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID+"="+r.id, null, null);
				ContentValues aResult = new ContentValues();
				
				aResult.put(DBHelper.ANIME_DESC_COL, r.desc);
				aResult.put(DBHelper.ANIME_FANART_COL, r.fanart);
				aResult.put(DBHelper.ANIME_IMAGE_COL, r.image);
				aResult.put(DBHelper.ANIME_STATUS_COL, r.status); // NOTHING HERE?
				aResult.put(DBHelper.ANIME_TITLE_COL, r.title);
				aResult.put(DBHelper.ANIME_ID, r.id);
				aResult.put(DBHelper.ANIME_RUNTIME_COL, r.runtime);
				aResult.put(DBHelper.ANIME_WATCHLIST, "true");
				
				// If it does not exist.. we add it to the database
				if(shows.getCount() == 0)
					getContentResolver().insert(AnimeProvider.CONTENT_URI, aResult);
				else if(shows.getCount() == 1)
					getContentResolver().update(AnimeProvider.CONTENT_URI, aResult, DBHelper.ANIME_ID+"="+r.id , null);
				else
					Log.e(TAG, "The db includes duplicates of show with id :" + r.id + " and title: " + r.title);
				
				
				current++;
				if(ids.contains(r.id))
					continue;
				
				// Fetch the anime's episode
				RestService.FETCH_ANIME_EPISODES(c,r.id,rest);

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
	
	public static void FETCH_USER_SEEN_EPISODES(Context c, int animeID, RestClient rest){
		String userID;
		if(Constants.getUserID(c).equals(Constants.NO_ID))
			return;
		else
			userID = Constants.getUserID(c);
		
		// Reset episodes seen before registering the new one
		ContentValues nullPointer = new ContentValues();
		nullPointer.putNull(DBHelper.EPISODE_SEEN_COL);
		int nullRows = c.getContentResolver().update(EpisodeProvider.CONTENT_URI , nullPointer, DBHelper.EPISODE_SEEN_COL + " IS NOT NULL AND "+DBHelper.EPISODE_ANIME_ID_COL +"="+animeID, null);
		//Log.e("Episode seen purge", "Purged " + nullRows + " episodes");
		
		if(rest == null)
			rest = RestClient.getInstance(c);
		// Fetch the episodes which the user has seen
		String userepString = rest.ReadMethod(Constants.REST_USER_EPISODES + animeID + "/" + userID + ".json" );
		
		Gson gson = new Gson();
		
		UserEpisodeList userepList = gson.fromJson(userepString,UserEpisodeList.class);
		
		if(userepList == null)
			return;
		
		for(UserEpisode e : userepList.data.episodes){
			ContentValues result = new ContentValues();
			
			result.put(DBHelper.EPISODE_SEEN_COL, e.timestamp);
			
			int rows = c.getContentResolver().update(EpisodeProvider.CONTENT_URI , result, DBHelper.EPISODE_ID_COL+"="+ e.episode_id, null);
			//Log.e("USER HAS SEEN EPISODE: ", ""+e.episode_id);
			
		}
		
	}
	
	public static void FETCH_ANIME(Context c, int animeID, RestClient rest){
		
		if(rest == null)
			rest = RestClient.getInstance(c);
		
		String animeString = rest.ReadMethod(Constants.REST_ANIME + animeID + ".json" );
		
		Gson gson = new Gson();
		
		AnimeRequest animelist = gson.fromJson(animeString,AnimeRequest.class);
		
		if(animelist == null)
			return;
		
		Anime anime = animelist.getAnime();
		
		Cursor shows = c.getContentResolver().query(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID+"="+animeID, null, null);
		
		ContentValues aResult = new ContentValues();
		
		aResult.put(DBHelper.ANIME_DESC_COL, anime.desc);
		aResult.put(DBHelper.ANIME_FANART_COL, anime.fanart);
		aResult.put(DBHelper.ANIME_IMAGE_COL, anime.image);
		aResult.put(DBHelper.ANIME_STATUS_COL, anime.status);
		aResult.put(DBHelper.ANIME_TITLE_COL, anime.title);
		aResult.put(DBHelper.ANIME_ID, anime.id);
		aResult.put(DBHelper.ANIME_RUNTIME_COL, anime.runtime);
		
		// If it does not exist.. we add it to the database
		if(shows.getCount() == 0)
			c.getContentResolver().insert(AnimeProvider.CONTENT_URI, aResult);
		else if(shows.getCount() == 1)
			c.getContentResolver().update(AnimeProvider.CONTENT_URI, aResult, DBHelper.ANIME_ID+"="+anime.id , null);
		else
			Log.e(TAG, "The db includes duplicates of show with id :" + anime.id + " and title: " + anime.title);
		
		RestService.FETCH_ANIME_EPISODES(c,animeID,rest);
	}
	
	public static void FETCH_ANIME_EPISODES(Context c, int animeID, RestClient rest){
		if(rest == null)
			rest = RestClient.getInstance(c);
		
		String epString = rest.ReadMethod(Constants.REST_ANIME_EPISODES + animeID + ".json" );
		
		Gson gson = new Gson();
		
		EpisodeList epList = gson.fromJson(epString,EpisodeList.class);
		if(epList == null)
			return;
		
		for(Episode e : epList.data.episodes)
		{
			//Log.e("EPISODE: ", e.name);
			Cursor episode = c.getContentResolver().query(EpisodeProvider.CONTENT_URI, Episode.projection, DBHelper.EPISODE_ID_COL+"="+e.id, null, null);
			
			ContentValues result = new ContentValues();
			
			result.put(DBHelper.EPISODE_AIRED_COL, e.aired);
			result.put(DBHelper.EPISODE_ANIME_ID_COL, e.anime_id);
			result.put(DBHelper.EPISODE_DESC_COL, e.description);
			result.put(DBHelper.EPISODE_TITLE_COL, e.name);
			result.put(DBHelper.EPISODE_ID_COL, e.id);
			result.put(DBHelper.EPISODE_NUMBER_COL, e.number);
			result.put(DBHelper.EPISODE_SPECIAL_COL, e.special);
			result.put(DBHelper.EPISODE_IMAGE_COL, e.image);
			
			if(episode.getCount() == 0)
				c.getContentResolver().insert(EpisodeProvider.CONTENT_URI, result);
			else
				c.getContentResolver().update(EpisodeProvider.CONTENT_URI, result, DBHelper.EPISODE_ID_COL +"="+e.id, null);
		}
		
		RestService.FETCH_USER_SEEN_EPISODES(c,animeID,rest);
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
		
		Cursor anime = c.getContentResolver().query(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID+"="+anime_id, null, null);
		ContentValues result = new ContentValues();
		
		if(watched)
			result.put(DBHelper.EPISODE_SEEN_COL, Constants.timeToString(null));
		else
			result.putNull(DBHelper.EPISODE_SEEN_COL);
		
		c.getContentResolver().update(EpisodeProvider.CONTENT_URI, result, DBHelper.EPISODE_ANIME_ID_COL +"="+anime_id, null);
		c.getContentResolver().notifyChange(EpisodeProvider.CONTENT_URI, null);
		
		if(Constants.getUserID(c).equals(Constants.NO_ID))
		{
			if(!anime.moveToFirst())
				return false;
			
			// Check if anime is currently airingSSSSSSSS
			String animeStatus = anime.getString(anime.getColumnIndexOrThrow(DBHelper.ANIME_STATUS_COL));
			if(!animeStatus.equals("currently") && animeStatus != null)
				return false;
			
			long lastScrapeSet = anime.getLong(anime.getColumnIndexOrThrow(DBHelper.ANIME_SCRAPE_REQUEST));
			long lastScrapeSetCriteria = Calendar.getInstance().getTimeInMillis() - (1000 * 60 * 60 * 24);
			
			if(anime.isNull(anime.getColumnIndexOrThrow(DBHelper.ANIME_SCRAPE_REQUEST)) || lastScrapeSet < lastScrapeSetCriteria)
			{
				// Send scrape request to server
				int animeid = anime.getInt(anime.getColumnIndexOrThrow(DBHelper.ANIME_ID));
				String scrapeStatus = rest.ReadMethod(Constants.REST_ANIME_SET_SCRAPE + "/" + animeid + ".json");
				
				// Update scrape flag in db
				ContentValues animeresult = new ContentValues();
				animeresult.put(DBHelper.ANIME_SCRAPE_REQUEST,Calendar.getInstance().getTimeInMillis());
				c.getContentResolver().update(AnimeProvider.CONTENT_URI, animeresult, DBHelper.ANIME_ID +"="+anime_id, null);
				c.getContentResolver().notifyChange(AnimeProvider.CONTENT_URI, null);
			}
			return false;
		}
		else
			userid = Constants.getUserID(c);
		
		String watchedBooleanString = (watched) ? "true" : "false";
		
		String anString = rest.ReadMethod(Constants.REST_WATCH_ANIME + userid + "/" + anime_id + "/" + watchedBooleanString + ".json");
		
		/*** TODO: Need to check results from this method **/
		
		return true;
	}
	public static boolean MARK_EPISODE_AS_UNWATCHED(Context c, int episode_id, RestClient rest){
		if(rest == null)
			rest = RestClient.getInstance(c);
		
		Cursor episode = c.getContentResolver().query(EpisodeProvider.CONTENT_URI, Episode.projection, DBHelper.EPISODE_ID_COL+"="+episode_id, null, null);
		ContentValues result = new ContentValues();
		result.putNull(DBHelper.EPISODE_SEEN_COL);
		c.getContentResolver().update(EpisodeProvider.CONTENT_URI, result, DBHelper.EPISODE_ID_COL +"="+episode_id, null);
		c.getContentResolver().notifyChange(EpisodeProvider.CONTENT_URI, null);
		
		if(!Constants.getUserID(c).equals(Constants.NO_ID))
		{	
			String userid = Constants.getUserID(c);
			String epString = rest.ReadMethod(Constants.REST_UNWATCH_EPISODE + userid + "/" + episode_id + ".json");
		}		
		return true;
	}
	
	public static boolean MARK_EPISODE_AS_WATCHED(Context c, int episode_id, String timestamp, RestClient rest){
		
		if(rest == null)
			rest = RestClient.getInstance(c);
		
		String userid = "";
		
		Cursor episode = c.getContentResolver().query(EpisodeProvider.CONTENT_URI, Episode.projection, DBHelper.EPISODE_ID_COL+"="+episode_id, null, null);
		ContentValues result = new ContentValues();
		result.put(DBHelper.EPISODE_SEEN_COL, timestamp);
		c.getContentResolver().update(EpisodeProvider.CONTENT_URI, result, DBHelper.EPISODE_ID_COL +"="+episode_id, null);
		c.getContentResolver().notifyChange(EpisodeProvider.CONTENT_URI, null);
		
		if(Constants.getUserID(c).equals(Constants.NO_ID))
		{
			if(!episode.moveToFirst())
				return false;
			int anime_id = episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL));
			// If the anime is not finished we try to send rest request to server for scraping
			Cursor anime = c.getContentResolver().query(
					AnimeProvider.CONTENT_URI, 
					Anime.projection, 
					DBHelper.ANIME_ID+"="+anime_id, 
					null, 
					null
				);
			if(!anime.moveToFirst())
				return false;
			
			// Check if anime is currently airing
			String animeStatus = anime.getString(anime.getColumnIndexOrThrow(DBHelper.ANIME_STATUS_COL));
			if(!animeStatus.equals("currently") && animeStatus != null)
				return false;
			
			long lastScrapeSet = anime.getLong(anime.getColumnIndexOrThrow(DBHelper.ANIME_SCRAPE_REQUEST));
			long lastScrapeSetCriteria = Calendar.getInstance().getTimeInMillis() - (1000 * 60 * 60 * 24);
			
			if(anime.isNull(anime.getColumnIndexOrThrow(DBHelper.ANIME_SCRAPE_REQUEST)) || lastScrapeSet < lastScrapeSetCriteria)
			{
				// Send scrape request to server
				int animeid = anime.getInt(anime.getColumnIndexOrThrow(DBHelper.ANIME_ID));
				String scrapeStatus = rest.ReadMethod(Constants.REST_ANIME_SET_SCRAPE + "/" + animeid + ".json");
				
				// Update scrape flag in db
				ContentValues animeresult = new ContentValues();
				animeresult.put(DBHelper.ANIME_SCRAPE_REQUEST,Calendar.getInstance().getTimeInMillis());
				c.getContentResolver().update(AnimeProvider.CONTENT_URI, animeresult, DBHelper.ANIME_ID +"="+anime_id, null);
				c.getContentResolver().notifyChange(AnimeProvider.CONTENT_URI, null);
			}
			return false;
		}
		else
			userid = Constants.getUserID(c);
		
		String epString = rest.ReadMethod(Constants.REST_WATCH_EPISODE + userid + "/" + episode_id + ".json");
		
		/*** TODO: Need to check results from this method **/
		return true;
	}
	
	public static boolean CHANGE_ANIME_WATCHLIST(Context c, int anime_id, String inWatchlist, RestClient rest){
		
		if(rest == null)
			rest = RestClient.getInstance(c);
		
		String userid = "";
		
		ContentValues result = new ContentValues();
		if(!inWatchlist.equals("true"))
			result.putNull(DBHelper.ANIME_WATCHLIST); // This should be boolean
		else
			result.put(DBHelper.ANIME_WATCHLIST,"true");
		c.getContentResolver().update(AnimeProvider.CONTENT_URI, result, DBHelper.ANIME_ID +"="+anime_id, null);
		c.getContentResolver().notifyChange(EpisodeProvider.CONTENT_URI, null);
		
		if(!Constants.getUserID(c).equals(Constants.NO_ID))
		{
			userid = Constants.getUserID(c);
			String changes = inWatchlist;//(inWatchlist) ? "true": "false";
			String epString = rest.ReadMethod(Constants.REST_ANIME_WATCHLIST + userid + "/" + anime_id + "/" + changes + ".json");
		}
		/*** TODO: Need to check results from this method **/
		return true;
	}
}