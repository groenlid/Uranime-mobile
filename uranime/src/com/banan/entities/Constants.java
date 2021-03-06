package com.banan.entities;

import com.banan.trakt.RestClient;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

public class Constants {

	public static final String PREFERENCE_NAME = "traktpreferences";

	public static final String APIKEY = "786bdbb42b9d59fd922b21659c488250";
	
	public static final String SERVER = "http://api.urani.me/";
	
	public static final String IMAGESERVER = "http://urani.me/";

	public static final String REST_USER_LIBRARY_WATCHLIST = SERVER + "users/";
	
	public static final String REST_WATCH_EPISODE = SERVER + "api/watchepisode/";
	
	public static final String REST_UNWATCH_EPISODE = SERVER + "api/unwatchepisode/";
	
	public static final String REST_WATCH_ANIME = SERVER + "api/watchanime/";
	
	public static final String REST_SEARCH = SERVER + "search?q=";

	public static final String REST_ANIME = SERVER 
			+ "anime/";
	
	public static final String REST_ANIME_EPISODES = SERVER
			+ "api/animeepisodes/";
	
	public static final String REST_TRENDING_ANIME = SERVER
			+ "api/trendingAnime/";//"api/trendingAnime/10.json";
	
	public static final String REST_USER_EPISODES = SERVER
			+ "api/userepisodes/";

	public static final String REST_LOGIN = SERVER
			+ "auth/check";

	public static final String REST_ANIME_WATCHLIST = SERVER
			+ "api/animeWatchlist/";
	
	public static final String IMAGE_RESIZE = IMAGESERVER + "api/imageresize/";
	
	public static final String REST_ANIME_SET_SCRAPE = SERVER
			+ "api/setanimescrape/";

	public static final String NO_ID = "";

	public static final String IMAGE_PATH = IMAGESERVER
			+ "attachments/photos/orginal/";
	
	public static final String EPISODE_IMAGE_PATH = IMAGESERVER 
			+ "attachments/episodes/"; // + animeid / episodeid.extension
	
	public static final int CALENDAR_WEEKS = 20;

	public static final String REST_GENRE = SERVER + "tags";
	
	private static final String REST_GENRE_ANIME = SERVER + "search?tag_id=";
	
	// USEFULL FUNCTIONS
	public int dpToPx(Context c, float dp){
	    Resources resources = c.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
	    return (int) px;
	}
	
	public static String getGenreAnimeURL(int genre_id) {
		return Constants.REST_GENRE_ANIME + genre_id;
	}
	
	public static String getLatestAnimeURL(int limit, int offset) {
		//Log.e("getLatest", "limit: " + limit + "; startingWith: " + offset);
		return Constants.REST_ANIME + "?limit=" + limit + "&offset=" + offset;
	}
	
	public static String getTopAnimeURL(int limit, int offset) {
		//Log.e("getTop", "limit: " + limit + "; startingWith: " + offset);
		// TODO: CHANGE THIS
		return Constants.getLatestAnimeURL(limit, offset);
		//return Constants.REST_TRENDING_ANIME + limit + "/" + offset + ".json";
	}
	
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
		String password = sp.getString("password", null);
		
		Long time = Calendar.getInstance().getTimeInMillis();
		String insideHash = PrivateConfig.SALT + password + Constants.getUsername(c);
		
		String query;
		try {
			query = AeSimpleSHA1.SHA1(
					AeSimpleSHA1.SHA1(
							PrivateConfig.SALT + 
							password) + 
					//(int)(time / 600) + 
					Constants.getUsername(c));
		} catch (NoSuchAlgorithmException e) {
			query = "";
		} catch (UnsupportedEncodingException e) {
			query = "";
		}
		
		return query;
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
	
	public static long getLastAdClick(Context c){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);

		return sp.getLong("adclick", 0);
	}
	
	public static void setLastAdClick(Context c, long millis){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		Editor e = sp.edit();
		e.putLong("adclick", millis);
		e.commit();
	}
	
	/**
	 * Convert from unix time to string with the format yyyy-mm-dd.
	 * If @now is null, current unix time is used.
	 * @param now
	 * @return
	 */
	public static String timeToString(Long now){
		
		java.util.Calendar nowCalendar = java.util.Calendar.getInstance(); 
		if(now != null)
			nowCalendar.setTimeInMillis(now);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(nowCalendar.getTime());
		
		
		/*return "" + nowCalendar.get(Calendar.YEAR) 
				+ "-" + java.util.Formatter.format(nowCalendar.get(Calendar.MONTH))
				+ "-" + nowCalendar.get(Calendar.DATE);*/
	}
	
	/**
	 * Convert the string with the format yyyy-mm-dd to long unix time.
	 * @param time
	 * @return
	 */
	public static long stringToTime(String time){
		if(time == null)
			return 0l;
		
		
		String format = "yyyy-MM-dd";
		
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		try {
			Date dateTime = sdf.parse(time);
			
			return dateTime.getTime();
		} catch (ParseException e) {
			Log.e("StringToTime", "Could not parse string " + time);
			return 0l;
		}
	}
	
	// Check if user is logged in. If he is, then return userid.. if not return
	// no id.
	// Meanwhile try to login if checkbox is set.
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



	public static String truncate(String input, int length) {
		return Constants.truncate(input, length, true);
	}
	
	public static String truncate(String input, int length, boolean appendDots) {
		if (input.length() <= length - 1)
			return input;
		String returnus = input.substring(0, length);
		if(appendDots)
			returnus += "..";
		return returnus;
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

			if(user == null || user.error != null){
				// Something went wrong... Wrong password?
				return false;
			}
			else if (user.id != 0) {
				String id = "" + user.id;
				Constants.setUserID(c, id);
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
