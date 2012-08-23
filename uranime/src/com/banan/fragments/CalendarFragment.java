package com.banan.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.banan.anime.AnimeActivity;
import com.banan.anime.EpisodeActivity;
import com.banan.anime.R;
import com.banan.entities.Anime;
import com.banan.entities.Constants;
import com.banan.entities.Episode;
import com.banan.providers.AnimeProvider;
import com.banan.providers.DBHelper;
import com.banan.providers.EpisodeProvider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;

public class CalendarFragment extends SherlockFragment implements ActionBar.OnNavigationListener{
		// The difference between current week and wanted week.
		private int diffWeek;
		
		public static Fragment newInstance(int diffWeek) {
			CalendarFragment frag = new CalendarFragment();
			
			frag.diffWeek = diffWeek;
			
			return frag;
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			setRetainInstance(true);
			View v = inflater.inflate(R.layout.calendar_main, container, false);
			
			ActionBar ab = getSherlockActivity().getSupportActionBar();
			
			// So the user can get logged in without doing an action.
			Constants.getUserID(getActivity().getApplicationContext());
			
			LinearLayout content = (LinearLayout)v.findViewById(R.id.calendar);
			// Sqlite uses DATE('2010-07-29', '-1 day')
			// DATE() (== SQL NOW())	
			
			// DATE(DATE(), '1 day') for use in between
			
			String calendarDATESQL = "(";
			
			for(int i = 0; i < 7; i++)
			 {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DAY_OF_YEAR, -calendar.get(Calendar.DAY_OF_WEEK) + (diffWeek * 7) + i);
				calendarDATESQL += DBHelper.EPISODE_AIRED_COL + "='" + Constants.timeToString(calendar.getTimeInMillis()) + "' OR ";
				
			 }
			calendarDATESQL = Constants.truncate(calendarDATESQL, calendarDATESQL.length()-3, false) + ")";
			
			final Cursor episodes = getActivity().managedQuery(
					 EpisodeProvider.CONTENT_URI, Episode.projection,
					 calendarDATESQL + " AND ("+DBHelper.EPISODE_ANIME_ID_COL + " IN(" +
					 		"SELECT DISTINCT " + DBHelper.EPISODE_ANIME_ID_COL + " FROM " + DBHelper.EPISODE_TABLE + " WHERE " + DBHelper.EPISODE_SEEN_COL + " IS NOT NULL"+
					 		") OR "+DBHelper.EPISODE_ANIME_ID_COL+" IN(SELECT "+DBHelper.ANIME_ID+" FROM "+DBHelper.ANIME_TABLE+" WHERE "+DBHelper.ANIME_WATCHLIST+" IS NOT NULL))",
					 null,DBHelper.EPISODE_AIRED_COL + " ASC");
				 
				if(episodes.getCount() == 0)
					return v;
			
				 episodes.moveToFirst();
				 
				 String lastDate = "";
				 
				 do
				 {
					 
					 final Cursor anime = getActivity().managedQuery(
							 AnimeProvider.CONTENT_URI, Anime.projection,
							 DBHelper.ANIME_ID + " = " + 
							 episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL)),
							 null,null
							 );
					 
					 //existsAnime.add(episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL)));
					 
					 ListView list = new ListView(getActivity().getApplicationContext());
					 list.setLayoutParams(
							 new ListView.LayoutParams(
									 LayoutParams.MATCH_PARENT,
									 LayoutParams.MATCH_PARENT
									 )
							 );
					 
					 // Check if the header with date needs to be displayed.
					 boolean showHeader = false;
					 String episodeDate = episodes.getString(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_AIRED_COL));

					 if(!lastDate.equals(episodeDate))
					 {
						 showHeader = true;
						 lastDate = episodeDate;
					 }
					 
					 
					 list.setAdapter(new AnimeAdapter(getActivity(), anime, episodes, episodes.getPosition(), showHeader));

					 content.addView(list);

				 }while(episodes.moveToNext());
				 
				 // Fetch anime from watchlist
			
			return v;
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
		}
		
		public boolean onNavigationItemSelected(int itemPosition, long itemId) {
			return false;
		}
		
		public class AnimeAdapter extends CursorAdapter
		{
			 
			ImageLoader imageLoader;
			ImageLoaderConfiguration config;  
			DisplayImageOptions options;
			
			int height;
			
			private Cursor show;
			private Cursor episode;
			private int epPos;
			private boolean header;
			
			public AnimeAdapter(Context context, Cursor show, Cursor episode, int epPos, boolean header)
			{
				super(context,show);
				this.show = show;
				this.episode = episode;
				this.epPos = epPos;
				this.header = header;
				
				imageLoader = ImageLoader.getInstance();
				
				options = new DisplayImageOptions.Builder()
				.cacheInMemory()
				.cacheOnDisc()
		        .imageScaleType(ImageScaleType.POWER_OF_2)
		        .build();
			}

			@Override
			public void bindView(View view, Context context, Cursor show) {
				
				if(!show.moveToFirst())
					return;
				
				if(!episode.moveToPosition(epPos))
					return;
				
				Resources r = getResources();
				float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, r.getDisplayMetrics());
				//float pxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, r.getDisplayMetrics());
				
				final ImageView image = (ImageView)view.findViewById(R.id.anime_image);
				
				
				TextView showName = (TextView)view.findViewById(R.id.anime_title);
				
				TextView episodeNext = (TextView)view.findViewById(R.id.episode_next);
				
				TextView date = (TextView)view.findViewById(R.id.separator);
				
				if(!header)
					date.setVisibility(View.GONE);
				//episode_next_field
				//episode_next
				//episode_next_date
				
				 String dateString = episode.getString(
						 episode.getColumnIndexOrThrow(DBHelper.EPISODE_AIRED_COL));
				 
				 // TODO: Check if the date is today.
				 if(Constants.timeToString(null).equals(dateString))
					 date.setText("Today");
				 else{
					 SimpleDateFormat sdf = new SimpleDateFormat("E, dd.MM.yyyy");
					 date.setText(sdf.format(Constants.stringToTime(dateString))); 
				 }
				
				
				image.setScaleType(ScaleType.CENTER_INSIDE);
				
				String episode_image = show.getString(show.getColumnIndexOrThrow(DBHelper.ANIME_FANART_COL));
				if(episode_image == null)
					episode_image = "http://placehold.it/" + (int)px + "x" + (int)(0.56*px);
				else
					episode_image = Anime.resizeImage((int)px, 0, episode_image);
				
				int animeID = episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL));
				
				if(!episode.isNull(episode.getColumnIndexOrThrow(DBHelper.EPISODE_IMAGE_COL)))
					episode_image = Constants.EPISODE_IMAGE_PATH + animeID +"/" + episode.getString(episode.getColumnIndexOrThrow(DBHelper.EPISODE_IMAGE_COL));
				
				//String imageUrl = Anime.resizeImage((int)px,0,show.getString(show.getColumnIndexOrThrow(DBHelper.ANIME_FANART_COL)) );
				
				String animeTitle = show.getString(show.getColumnIndexOrThrow(DBHelper.ANIME_TITLE_COL));
				String episodeNextTxt = episode.getString(episode.getColumnIndexOrThrow(DBHelper.EPISODE_TITLE_COL));
				
				imageLoader.displayImage(episode_image, image);
				
				showName.setText(animeTitle);
				episodeNext.setText(episodeNextTxt);
				
				final String anime_id = "" + episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL));
				final String episode_number = "" + episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_NUMBER_COL));
				final boolean special_episode = episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_SPECIAL_COL))>0;
				
				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						
						Log.e("Anime_id",anime_id);
						Log.e("episode_number", episode_number);
						Log.e("special",special_episode+"");
						
						Intent i = new Intent(getActivity().getApplicationContext(),EpisodeActivity.class);
						i.putExtra("anime_id", anime_id);
						i.putExtra("episode_number", episode_number);
						i.putExtra("special_episode", special_episode);
						startActivity(i);
						
					}
				
				});
				
				return;
			}

			@Override
			public View newView(Context context, Cursor c, ViewGroup viewGroup) {
				 final View view = LayoutInflater.from(context).inflate(R.layout.calendar_item, viewGroup, false);
				 return view;
			}

		}
		
		public static String truncate(String input, int length)
		{
			if(input.length() <= length-1)
				return input;
			return input.substring(0,length) + "..";
		}
}
