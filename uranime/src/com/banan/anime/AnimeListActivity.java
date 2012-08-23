package com.banan.anime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.banan.entities.Anime;
import com.banan.entities.Constants;
import com.banan.fragments.SearchFragment;
import com.banan.providers.AnimeProvider;
import com.banan.providers.DBHelper;
import com.banan.providers.RestService;
import com.banan.anime.R;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.ads.mediation.admob.AdMobAdapterExtras;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AnimeListActivity extends SherlockActivity implements ActionBar.OnNavigationListener, AdListener
{
	//Global variables
	 private static final int ACTION_BAR_SEARCH=0;
	 private static final int ACTION_BAR_REFRESH=1;
	private ArrayAdapter<CharSequence> list;
	private ImageAdapter imgAdapter; 
	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_PROGRESS);
		
		setContentView(R.layout.gridview);
		
		// Set the actionbar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle("");
		actionBar.setHomeButtonEnabled(true);
		
		// So the user can get logged in without doing an action.
		Constants.getUserID(this);
		
		list = ArrayAdapter.createFromResource(this, R.array.list_type, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(list, this);
		
		//Actionbar End
		
		/*String animeIDSQL = DBHelper.ANIME_ID +" IN ("
	    + "SELECT "+DBHelper.EPISODE_ANIME_ID_COL 
	    + " FROM " + DBHelper.EPISODE_TABLE
	    + " WHERE " + DBHelper.EPISODE_SEEN_COL + " IS NOT NULL "
	    + " GROUP BY " + DBHelper.EPISODE_ANIME_ID_COL
	    + ")";*/
		
		/*final Cursor shows = managedQuery(
		AnimeProvider.CONTENT_URI, Anime.projection, animeIDSQL, null, DBHelper.ANIME_TITLE_COL + " ASC");*/
		
		GridView gview = (GridView) findViewById(R.id.gridview);
		imgAdapter = new ImageAdapter(this,null);
		gview.setAdapter(imgAdapter);

		gview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				if(imgAdapter == null || imgAdapter.getCursor() == null)
					return;
				Cursor innerCursor = imgAdapter.getCursor();
				innerCursor.moveToPosition(pos);
				String anime_id = "" + innerCursor.getInt(innerCursor.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL));
				
				Intent i = new Intent(getApplicationContext(),AnimeActivity.class);
				i.putExtra("anime_id", anime_id);
				startActivity(i);
			}
		
		});
		//long minTimeSinceAdClick = Calendar.getInstance().getTimeInMillis() - (5000);	
		
		/*
		long minTimeSinceAdClick = Calendar.getInstance().getTimeInMillis() - (1000 * 60 * 60);	
		
		AdView adView = (AdView)findViewById(R.id.ad);
		
		if(Constants.getLastAdClick(this) < minTimeSinceAdClick)
		{
			// ADS
			adView.setVisibility(View.VISIBLE);
			//AdView adView = new AdView(this, AdSize.SMART_BANNER, "a14f9c8699042f4");
			//AdView adView = (AdView) findViewById(R.id.ad);
			
			//LinearLayout layout = (LinearLayout)findViewById(R.id.ad_layout);
			//layout.addView(adView);
			AdRequest request = new AdRequest();
			request.addTestDevice(AdRequest.TEST_EMULATOR);
			
			
			// set colors
			AdMobAdapterExtras extras = new AdMobAdapterExtras()
				.addExtra("color_bg", "000000")
				.addExtra("color_bg_top", "000000")
				.addExtra("color_text", "eeeeee")
				.addExtra("color_url", "eeeeee")
				.addExtra("color_link", "eeeeee")
				.addExtra("color_border", "555555");
			
			request.setNetworkExtras(extras);
			adView.setAdListener(this);
		    // Initiate a generic request to load it with an ad
		    adView.loadAd(request);
			adView.invalidate();
		}else
			adView.setVisibility(View.GONE);
			
			*/
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_preferences:
			startActivity(new Intent(this, TvdbPreferenceActivity.class));
			break;
		case R.id.menu_search:
			startActivity(new Intent(this, SearchActivity.class));
			break;
		case R.id.menu_full_update:
			Intent i = new Intent(this, RestService.class);
			i.putExtra(RestService.ACTION, RestService.GET);
			i.putExtra(RestService.OBJECT_TYPE,
					RestService.OBJECT_TYPE_ANIMELIST);
			i.putExtra(RestService.PARAMS, new ArrayList<String>());
			// i.putExtra(RestService.ACTIVITY, );
			startService(i);

			Toast.makeText(this, R.string.animelist_refresh, Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.menu_calendar:
			startActivity(new Intent(this, CalendarActivity.class));
			break;
		case R.id.menu_update:
			if(imgAdapter != null && imgAdapter.getCursor() != null)
			if(imgAdapter.getCursor().moveToFirst())
			{
				ArrayList<String> param = new ArrayList<String>();
				do{
					String status = imgAdapter.getCursor().getString(imgAdapter.getCursor().getColumnIndexOrThrow(DBHelper.ANIME_STATUS_COL));
					if(status == null || (status != null && !status.equals("finished")))
						param.add(""+imgAdapter.getCursor().getInt(imgAdapter.getCursor().getColumnIndexOrThrow(DBHelper.ANIME_ID)));
				}while(imgAdapter.getCursor().moveToNext());
				
				Intent k = new Intent(this, RestService.class);
				k.putExtra(RestService.ACTION, RestService.GET);
				k.putExtra(RestService.OBJECT_TYPE, RestService.OBJECT_TYPE_ANIME);
				k.putExtra(RestService.PARAMS, param);
				startService(k);

				Toast.makeText(this, R.string.animelist_refresh, Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(this, "Nothing to refresh. Try a full-refresh or add an anime", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.abs__home:
			final Intent intent = new Intent(this, AnimeListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			// return super.onHandleActionBarItemClick(item, position);
		}
		return true;
	}


	public class ImageAdapter extends CursorAdapter
	{
		 
		ImageLoader imageLoader;
		ImageLoaderConfiguration config;  
		
		int height;
		
		private Cursor c;

		public ImageAdapter(Context context, Cursor c)
		{
			super(context,c);
			this.c = c;
			
			final ProgressBar spinner = new ProgressBar(getApplicationContext());
			
			imageLoader = ImageLoader.getInstance();
		
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
			if(c == null)
				return;
			Resources r = getResources();
			float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, r.getDisplayMetrics());
			float pxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, r.getDisplayMetrics());
			final Context con = context;
			RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.relativeLayoutPoster);
			
			GridView.LayoutParams paramsRl = new GridView.LayoutParams((int)px, LayoutParams.FILL_PARENT);
			rl.setLayoutParams(paramsRl);
			
			final ImageView image = (ImageView)view.findViewById(R.id.showImg);
			TextView showName = (TextView)view.findViewById(R.id.showName);
			
			image.setScaleType(ScaleType.CENTER_CROP);
			
			String animePoster = c.getString(c.getColumnIndexOrThrow(DBHelper.ANIME_IMAGE_COL));
			String imagestring = "";
			if(animePoster != null)
				imagestring = Anime.resizeImage((int)px,(int)pxHeight,animePoster);
			else
				imagestring = "http://placehold.it/" + (int)px + "x" + (int)(pxHeight);
			
			imageLoader.displayImage(imagestring, image);
			
			/*imageLoader.displayImage(imagestring, image, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(Bitmap loadedImage) {

					Animation anim = AnimationUtils.loadAnimation(con, R.anim.fade_in);
					image.setAnimation(anim);
					anim.start();
				}
			});*/
			
			String animeTitle = c.getString(c.getColumnIndexOrThrow(DBHelper.ANIME_TITLE_COL));
			
			showName.setText(animeTitle);
			
			
			// Should we show or hide the titles
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
			Boolean useAccount = sp.getBoolean("showtitles", true);
			
			if(!useAccount)
				showName.setVisibility(View.GONE);
			else
				showName.setVisibility(View.VISIBLE);
			
			return;
		}

		@Override
		public View newView(Context context, Cursor c, ViewGroup viewGroup) {
			 final View view = LayoutInflater.from(context).inflate(R.layout.gridview_single, viewGroup, false);
			 return view;
		}

	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.animelist_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	public static String truncate(String input, int length)
	{
		if(input == null)
			return "";
		if(input.length() <= length-1)
			return input;
		return input.substring(0,length) + "..";
	}

	@SuppressLint("NewApi") public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		String animeIDSQL = "";
		
		switch(itemPosition){
		// Watchlist
		case(2):
			animeIDSQL = DBHelper.ANIME_ID +" IN ("
				    + "SELECT "+DBHelper.ANIME_ID 
				    + " FROM " + DBHelper.ANIME_TABLE
				    + " WHERE " + DBHelper.ANIME_WATCHLIST + " IS NOT NULL "
				    + ")";
			break;
		// Library
		case(1):
			animeIDSQL = DBHelper.ANIME_ID +" IN ("
				    + "SELECT "+DBHelper.EPISODE_ANIME_ID_COL 
				    + " FROM " + DBHelper.EPISODE_TABLE
				    + " WHERE " + DBHelper.EPISODE_SEEN_COL + " IS NOT NULL "
				    + " GROUP BY " + DBHelper.EPISODE_ANIME_ID_COL
				    + ")";
			break;
		case(0):
		default:
			animeIDSQL = DBHelper.ANIME_ID +" IN ("
				    + "SELECT "+DBHelper.EPISODE_ANIME_ID_COL 
				    + " FROM " + DBHelper.EPISODE_TABLE
				    + " WHERE " + DBHelper.EPISODE_SEEN_COL + " IS NOT NULL "
				    + " GROUP BY " + DBHelper.EPISODE_ANIME_ID_COL
				    + ") OR " + DBHelper.ANIME_WATCHLIST + " IS NOT NULL";
			break;
		}
		Cursor shows = managedQuery(
				AnimeProvider.CONTENT_URI, Anime.projection, animeIDSQL, null, DBHelper.ANIME_TITLE_COL + " ASC");
		
		
		/**
		 * 
		 */
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			imgAdapter.changeCursor(shows);//changeCursor();
		else
			imgAdapter.swapCursor(shows);//changeCursor();
		
		//Log.e("item",""+itemPosition + " " + itemId);
		return false;
	}

	@Override
	public void onDismissScreen(Ad ad) {
		 Constants.setLastAdClick(this, Calendar.getInstance().getTimeInMillis());
	}

	@Override
	public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLeaveApplication(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPresentScreen(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceiveAd(Ad arg0) {
		// TODO Auto-generated method stub
		
	}
}