package com.banan.entities;

import com.banan.trakt.RestClient;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Constants {

	public static final String PREFERENCE_NAME = "traktpreferences";

	public static final String APIKEY = "786bdbb42b9d59fd922b21659c488250";
	public static final String SERVER = "http://urani.me/";

	public static final String USERNAME = "groenlid"; // GET PREFERENCES STRING

	public static final String REST_USER_LIBRARY = SERVER + "api/animelist/";
	
	public static final String REST_USER_WATCHLIST = SERVER + "api/watchlist/";
	
	public static final String REST_WATCH_EPISODE = SERVER + "api/watchepisode/";
	
	public static final String REST_UNWATCH_EPISODE = SERVER + "api/unwatchepisode/";
	
	public static final String REST_WATCH_ANIME = SERVER + "api/watchanime/";
	
	public static final String REST_SEARCH = SERVER + "api/search/";

	public static final String REST_ANIME = SERVER 
			+ "api/anime/";
	
	public static final String REST_ANIME_EPISODES = SERVER
			+ "api/animeepisodes/";

	public static final String REST_LATEST_ANIME = SERVER
			+ "api/latestAnime/10/0.json";
	
	public static final String REST_TRENDING_ANIME = SERVER
			+ "api/trendingAnime/10.json";
	
	public static final String REST_USER_EPISODES = SERVER
			+ "api/userepisodes/";

	public static final String REST_LOGIN = SERVER
			+ "api/checkCredentials/.json";

	public static final String REST_ANIME_WATCHLIST = SERVER
			+ "api/animeWatchlist/";
	
	public static final String IMAGE_RESIZE = SERVER + "api/imageresize/";
	
	public static final String REST_ANIME_SET_SCRAPE = SERVER
			+ "api/setanimescrape/";

	public static final String NO_ID = "";

	public static final String IMAGE_PATH = SERVER
			+ "attachments/photos/orginal/";
	public static final String EPISODE_IMAGE_PATH = SERVER 
			+ "attachments/episodes/"; // + animeid / episodeid.extension
	
	// USEFULL FUNCTIONS

	public static String getUsername(Context c) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		// SharedPreferences sp = c.getSharedPreferences(PREFERENCE_NAME,
		// c.MODE_PRIVATE);
		String traktuser = sp.getString("username", null);
		return traktuser;
	}

	public static String getPassword(Context c) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		// SharedPreferences sp = c.getSharedPreferences(PREFERENCE_NAME,
		// c.MODE_PRIVATE);
		String traktuser = sp.getString("password", null);
		return traktuser;
	}

	/*
	 * public static void setUsername(Context c, String username){
	 * SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
	 * //SharedPreferences sp = c.getSharedPreferences(PREFERENCE_NAME,
	 * c.MODE_PRIVATE); Editor e = sp.edit(); e.putString("username", username);
	 * e.apply(); return; }
	 * 
	 * public static void setPassword(Context c,String password){
	 * SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
	 * //SharedPreferences sp = c.getSharedPreferences(PREFERENCE_NAME,
	 * c.MODE_PRIVATE); Editor e = sp.edit(); e.putString("password", password);
	 * e.apply(); return; }
	 */
	
	public static long getLastUpdate(Context c, int type){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);

		return sp.getLong("update"+type, 0);
	}
	public static void setLastUpdate(Context c, int type, long millis){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = sp.edit();
		e.putLong("update"+type, millis);
		e.commit();
	}

	public static String getUserID(Context c) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);

		String userid = sp.getString("user_id", Constants.NO_ID);

		Boolean useAccount = sp.getBoolean("useaccount", false);

		if (!useAccount)
		{
			Constants.setUserID(c, Constants.NO_ID);
			return Constants.NO_ID;
		}
		// Check what userid is
		if (userid.equals(Constants.NO_ID) && useAccount) {
			// Try to login.
			Constants co = new Constants();
			co.new LoginTask(c).execute(getUsername(c),getPassword(c));
		}

		//Log.e("USERID", userid);
		return userid;
	}

	public static void setUserID(Context c, String id) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = sp.edit();
		e.putString("user_id", id);
		e.commit();
	}

	// Check if user is logged in. If he is, then return userid.. if not return
	// no id.
	// Meanwhile try to login if checkbox is set.

	public static String truncate(String input, int length) {
		if (input.length() <= length - 1)
			return input;
		return input.substring(0, length) + "..";
	}

	/**
	 * Helper class for login
	 */

	private class LoginTask extends AsyncTask<Object, String, Boolean> {

		private final String TAG = LoginTask.class.getName();

		Context c;

		public LoginTask(Context c) {
			super();
			this.c = c;
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			String email = (String) params[0];
			String pwd = (String) params[1];

			// Constants.setBackupUsername(c,email);
			// Constants.setBackupPassword(c,pwd);

			RestClient client = RestClient.getInstance(c);

			String response = client.ReadMethod(Constants.REST_LOGIN);

			Gson gson = new Gson();

			User user = gson.fromJson(response, User.class);

			//Log.e(TAG, user.data.id);
			
			if (user.data.id != null) {
				Constants.setUserID(c, user.data.id);
				return true;
			}
			return false;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Toast.makeText(c, "Trying to login.", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(c, "Great success. You are now logged inn.", Toast.LENGTH_SHORT).show();
				// Intent i = new Intent(c,DashboardActivity.class);
				// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// getApplicationContext().startActivity(i);
			} else
				Toast.makeText(c, "Wrong username or password",
						Toast.LENGTH_SHORT).show();

		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			// Toast.makeText(getApplicationContext(), values[0] ,
			// Toast.LENGTH_SHORT).show();
		}
	}
}
