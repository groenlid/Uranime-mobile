package com.banan.anime;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager.LayoutParams;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;

/**
 * This activity handles the calendar page on trakt, where upcoming episodes can
 * be viewed
 * 
 * @author aliyhuss
 * 
 */
public class CalendarActivity extends SherlockActivity implements ActionBar.OnNavigationListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 setContentView(R.layout.calendar_main);
         
		// So the user can get logged in without doing an action.
		Constants.getUserID(this);
		 
		 // Sqlite uses DATE('2010-07-29', '-1 day')
		 // DATE() (== SQL NOW())
		 
		 // DATE(DATE(), '1 day') for use in between
		 // Testing the cursors
		 LinearLayout content = (LinearLayout)findViewById(R.id.calendar);
		 /*final Cursor animes = managedQuery(
				 EpisodeProvider.CONTENT_URI, Episode.projection_distinct,
				 DBHelper.EPISODE_SEEN_COL +" IS NOT NULL ",
				 null,null);*/
		 
		 for(int i = 0; i < 7; i++)
		 {
			 final Cursor episodes = managedQuery(
					 EpisodeProvider.CONTENT_URI, Episode.projection,
					 DBHelper.EPISODE_AIRED_COL + " == DATE(DATE(), '"+i+" day') AND ("+DBHelper.EPISODE_ANIME_ID_COL + " IN(" +
					 		"SELECT DISTINCT " + DBHelper.EPISODE_ANIME_ID_COL + " FROM " + DBHelper.EPISODE_TABLE + " WHERE " + DBHelper.EPISODE_SEEN_COL + " IS NOT NULL"+
					 		") OR "+DBHelper.EPISODE_ANIME_ID_COL+" IN(SELECT "+DBHelper.ANIME_ID+" FROM "+DBHelper.ANIME_TABLE+" WHERE "+DBHelper.ANIME_WATCHLIST+" IS NOT NULL))",
					 null,null);
			 
			 if(episodes.getCount() == 0)
				 continue;
			 
			 
			 TextView date = new TextView(this);
			 
			 date.setTextAppearance(this,android.R.style.TextAppearance_Medium);
			 date.setBackgroundColor(getResources().getColor(R.color.abs__background_holo_dark));
			 content.addView(date);
			 
			 episodes.moveToFirst();
			 String dateStr = episodes.getString(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_AIRED_COL));
			 if(i == 0)
				 date.setText("Today");
			 else if(i == 1)
				 date.setText("Tomorrow");
			 else
				 date.setText(dateStr);
			 
			// ArrayList<Integer> existsAnime = new ArrayList<Integer>();
			 do
			 {
				 
				 final Cursor anime = managedQuery(
						 AnimeProvider.CONTENT_URI, Anime.projection,
						 DBHelper.ANIME_ID + " = " + 
						 episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL)),
						 null,null
						 );
				 
				 //existsAnime.add(episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL)));
				 
				 ListView list = new ListView(this);
				 list.setLayoutParams(
						 new ListView.LayoutParams(
								 LayoutParams.MATCH_PARENT,
								 LayoutParams.MATCH_PARENT
								 )
						 );
				 
				 list.setAdapter(new AnimeAdapter(this, anime, episodes, episodes.getPosition()));

				 content.addView(list);
				 
				 list.setOnItemClickListener(new OnItemClickListener() {
	
						public void onItemClick(AdapterView<?> parent, View view, int pos,
								long id) {
							episodes.moveToPosition(pos);
							String anime_id = "" + anime.getInt(anime.getColumnIndexOrThrow(DBHelper.ANIME_ID));
							
							Intent i = new Intent(getApplicationContext(),AnimeActivity.class);
							i.putExtra("anime_id", anime_id);
							startActivity(i);
						}
					
					});
				 //anime.close();
			 }while(episodes.moveToNext());
			 
			 // Fetch anime from watchlist
		 }
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
			
			String imageUrl = Anime.resizeImage((int)px,0,show.getString(show.getColumnIndexOrThrow(DBHelper.ANIME_FANART_COL)) );
			
			String animeTitle = show.getString(show.getColumnIndexOrThrow(DBHelper.ANIME_TITLE_COL));
			String episodeNextTxt = episode.getString(episode.getColumnIndexOrThrow(DBHelper.EPISODE_TITLE_COL));
			
			imageLoader.displayImage(imageUrl, image, options);
			
			showName.setText(animeTitle);
			episodeNext.setText(episodeNextTxt);
			
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
