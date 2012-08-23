package com.banan.fragments;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.banan.anime.R;
import com.banan.entities.Anime;
import com.banan.entities.Constants;
import com.banan.entities.Episode;
import com.banan.providers.AnimeProvider;
import com.banan.providers.DBHelper;
import com.banan.providers.EpisodeProvider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class EpisodeFragment extends SherlockFragment{
		private int episodeID;
		public static Fragment newInstance(int episodeID) {
			EpisodeFragment frag = new EpisodeFragment();
			
			frag.episodeID = episodeID;
			
			return frag;
		}
		
		/*public EpisodeFragment(){
			super();
		}*/	
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			setRetainInstance(true);
			View v = inflater.inflate(R.layout.episode, container, false);
			
			ActionBar ab = getSherlockActivity().getSupportActionBar();
			
			Cursor episode = getActivity().managedQuery(
					EpisodeProvider.CONTENT_URI, Episode.projection, DBHelper.EPISODE_ID_COL+"="+episodeID, null,null);
			// Use cursorloader for api level over 11
			
			if(!episode.moveToFirst())
				return null;
			
			int animeid = episode.getInt(episode.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL));
			
			Cursor anime = getActivity().managedQuery(AnimeProvider.CONTENT_URI,
					Anime.projection, DBHelper.ANIME_ID + "=" + animeid, null, null);
			
			
			
			Display display = getActivity().getWindowManager().getDefaultDisplay();
			int width = display.getWidth();
			String episode_image = "";
			
			if(anime.moveToFirst())
			{
				String animetitle = anime.getString(anime.getColumnIndexOrThrow(DBHelper.ANIME_TITLE_COL));
				
				TextView animeTitleView = (TextView) v.findViewById(R.id.series_name);
				animeTitleView.setText(animetitle);
				
				//if the episodeimage does not exist, we use the anime fanart.
				episode_image = anime.getString(anime.getColumnIndexOrThrow(DBHelper.ANIME_FANART_COL));
				if(episode_image == null)
					episode_image = "http://placehold.it/" + width + "x" + (int)(0.56*width);
				else
					episode_image = Anime.resizeImage(width, 0, episode_image);
				getActivity().setTitle(animetitle);
			}
			
			String title = episode.getString(episode.getColumnIndexOrThrow(DBHelper.EPISODE_TITLE_COL));
			String description = episode.getString(episode.getColumnIndexOrThrow(DBHelper.EPISODE_DESC_COL));
			String date = episode.getString(episode.getColumnIndexOrThrow(DBHelper.EPISODE_AIRED_COL));
			if(!episode.isNull(episode.getColumnIndexOrThrow(DBHelper.EPISODE_IMAGE_COL)))
				episode_image = Constants.EPISODE_IMAGE_PATH + animeid +"/" + episode.getString(episode.getColumnIndexOrThrow(DBHelper.EPISODE_IMAGE_COL));

			
			
			
			
			ImageView seen = (ImageView) v.findViewById(R.id.seen);
			ImageView epImageView = (ImageView) v.findViewById(R.id.episode_image);
			
			
			// Show the image
			ImageLoader imageLoader = ImageLoader.getInstance();

			// Load and display image asynchronously
			imageLoader.displayImage(episode_image, epImageView);
			
			
			if(episode.isNull(episode.getColumnIndexOrThrow(DBHelper.EPISODE_SEEN_COL))){
				seen.setVisibility(View.INVISIBLE);
			}
			else{
				seen.setVisibility(View.VISIBLE);
			}
			
			TextView titleView = (TextView) v.findViewById(R.id.episode_title);
			TextView descriptionView = (TextView) v.findViewById(R.id.episode_desc);
			TextView dateView = (TextView) v.findViewById(R.id.episode_date);
			
			titleView.setText(title);
			descriptionView.setText(description);
			dateView.setText(date);
			
			
			return v;
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
		}
}
