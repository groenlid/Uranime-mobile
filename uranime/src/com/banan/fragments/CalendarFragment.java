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
import com.nostra13.universalimageloader.core.DecodingType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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
			for(int i = 0; i < 7; i++)
			 {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DAY_OF_YEAR, -calendar.get(Calendar.DAY_OF_WEEK) + (diffWeek * 7) + i);
				
				//calendar.add(Calendar.DAY_OF_YEAR, diffWeek * 7);
				//calendar.add(Calendar.DAY_OF_YEAR, i);
				//int fromDay = -weekday + i-1 + (diffWeek * 7);
				//int toDay = -weekday + i + (diffWeek * 7);
				
				//Log.e("Calendar",Constants.timeToString(calendar.getTimeInMillis()));
				final Cursor episodes = getActivity().managedQuery(
						 EpisodeProvider.CONTENT_URI, Episode.projection,
						 DBHelper.EPISODE_AIRED_COL + " == '" + Constants.timeToString(calendar.getTimeInMillis()) + "' AND ("+DBHelper.EPISODE_ANIME_ID_COL + " IN(" +
						 		"SELECT DISTINCT " + DBHelper.EPISODE_ANIME_ID_COL + " FROM " + DBHelper.EPISODE_TABLE + " WHERE " + DBHelper.EPISODE_SEEN_COL + " IS NOT NULL"+
						 		") OR "+DBHelper.EPISODE_ANIME_ID_COL+" IN(SELECT "+DBHelper.ANIME_ID+" FROM "+DBHelper.ANIME_TABLE+" WHERE "+DBHelper.ANIME_WATCHLIST+" IS NOT NULL))",
						 null,null);
				 /*final Cursor episodes = getActivity().managedQuery(
						 EpisodeProvider.CONTENT_URI, Episode.projection,
						 DBHelper.EPISODE_AIRED_COL + " == DATE(strftime('%Y-%m-%d','" + Constants.timeToString(calendar.getTimeInMillis()) + "'), '" + i + " day') AND ("+DBHelper.EPISODE_ANIME_ID_COL + " IN(" +
						 		"SELECT DISTINCT " + DBHelper.EPISODE_ANIME_ID_COL + " FROM " + DBHelper.EPISODE_TABLE + " WHERE " + DBHelper.EPISODE_SEEN_COL + " IS NOT NULL"+
						 		") OR "+DBHelper.EPISODE_ANIME_ID_COL+" IN(SELECT "+DBHelper.ANIME_ID+" FROM "+DBHelper.ANIME_TABLE+" WHERE "+DBHelper.ANIME_WATCHLIST+" IS NOT NULL))",
						 null,null);
				 */
				 if(episodes.getCount() == 0)
					 continue;
				 
				 
				 TextView date = new TextView(getActivity().getApplicationContext());
				 
				 date.setTextAppearance(getActivity(),android.R.style.TextAppearance_Holo_Large);
				 //date.setBackgroundColor(getResources().getColor(R.color.abs__background_holo_dark));
				 content.addView(date);
				 
				 episodes.moveToFirst();
				 
				 String dateString = episodes.getString(
						 episodes.getColumnIndexOrThrow(DBHelper.EPISODE_AIRED_COL));
				 
				 // TODO: Check if the date is today.
				 if(Constants.timeToString(null).equals(dateString))
					 date.setText("Today");
				 else{
				 	
				 SimpleDateFormat sdf = new SimpleDateFormat("c, dd.MM.yyyy");
				 date.setText(sdf.format(calendar.getTime()));
					 
					 
				 }
				// ArrayList<Integer> existsAnime = new ArrayList<Integer>();
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
					 
					 list.setAdapter(new AnimeAdapter(getActivity(), anime, episodes, episodes.getPosition()));

					 content.addView(list);
					 /*
					 list.setOnItemClickListener(new OnItemClickListener() {
		
							public void onItemClick(AdapterView<?> parent, View view, int pos,
									long id) {
								episodes.moveToPosition(pos);
											
								String anime_id = "" + anime.getInt(anime.getColumnIndexOrThrow(DBHelper.ANIME_ID));
								String episode_number = "" + episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_NUMBER_COL));
								boolean special_episode = episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_SPECIAL_COL))>0;
								Log.e("pos",""+pos);
								Log.e("Anime_id",anime_id);
								Log.e("episode_number", episode_number);
								Log.e("special",special_episode+"");
								
								Intent i = new Intent(getActivity().getApplicationContext(),EpisodeActivity.class);
								i.putExtra("anime_id", anime_id);
								i.putExtra("episode_number", episode_number);
								i.putExtra("special_episode", special_episode);
								startActivity(i);
							}
						
						});*/
					 //anime.close();
				 }while(episodes.moveToNext());
				 
				 // Fetch anime from watchlist
			 }
			
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
			
			public AnimeAdapter(Context context, Cursor show, Cursor episode, int epPos)
			{
				super(context,show);
				this.show = show;
				this.episode = episode;
				this.epPos = epPos;
				
				imageLoader = ImageLoader.getInstance();
				
				options = new DisplayImageOptions.Builder()
	            /*.showStubImage(R.drawable.stub_image)*/
	            .cacheOnDisc()
	            .decodingType(DecodingType.MEMORY_SAVING)
	            .build();
			}

			@Override
			public void bindView(View view, Context context, Cursor show) {
				//Log.e("cursor",""+show.getCount());
				/*final Cursor show = managedQuery(
						AnimeProvider.CONTENT_URI, Anime.projection, 
						DBHelper.ANIME_ID + "=" + c.getInt(c.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL)), 
						null, null);*/
				
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
				
				//episode_next_field
				//episode_next
				//episode_next_date
				
				
				image.setScaleType(ScaleType.CENTER_CROP);
				
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
				
				imageLoader.displayImage(episode_image, image, options);
				
				showName.setText(animeTitle);
				episodeNext.setText(episodeNextTxt);
				
				final String anime_id = "" + episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL));
				final String episode_number = "" + episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_NUMBER_COL));
				final boolean special_episode = episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_SPECIAL_COL))>0;
				
				view.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						
						/*
						String anime_id = "" + episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL));
						String episode_number = "" + episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_NUMBER_COL));
						boolean special_episode = episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_SPECIAL_COL))>0;
						*/
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
