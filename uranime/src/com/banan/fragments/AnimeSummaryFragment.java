package com.banan.fragments;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.internal.widget.IcsAdapterView.AdapterContextMenuInfo;
import com.banan.UIElements.CheckBox;
import com.banan.anime.EpisodeActivity;
import com.banan.anime.R;
import com.banan.entities.Anime;
import com.banan.entities.Constants;
import com.banan.entities.Episode;
import com.banan.providers.AnimeProvider;
import com.banan.providers.DBHelper;
import com.banan.providers.EpisodeProvider;
import com.banan.providers.RestService;
import com.banan.trakt.RestClient;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AnimeSummaryFragment extends SherlockFragment{

		final String count_column = "count_column";
		private long selectedEpisodeId = -1L;
		private Cursor episodes;
		
		public String content = null;
		
		public static AnimeSummaryFragment newInstance(){
			return new AnimeSummaryFragment();
		}
		
		@Override		
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
		
			
			setRetainInstance(true);
			
			View v = inflater.inflate(R.layout.anime, container, false);
			
			ActionBar ab = getSherlockActivity().getSupportActionBar();
			
			ab.setTitle("hello");
			
			final String anime_id = getActivity().getIntent().getStringExtra("anime_id");
			
			Cursor shows = getActivity().managedQuery(
			AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID+"="+anime_id, null, "title ASC");
			
			Cursor stats = getActivity().managedQuery(
			EpisodeProvider.CONTENT_URI, Episode.projection, DBHelper.EPISODE_ANIME_ID_COL + "="+anime_id, null, DBHelper.EPISODE_NUMBER_COL + " ASC");
			
			if(!shows.moveToFirst())
				return null;
			String fanart = shows.getString(shows.getColumnIndexOrThrow(DBHelper.ANIME_FANART_COL));
			String title = shows.getString(shows.getColumnIndexOrThrow(DBHelper.ANIME_TITLE_COL));
			String description = shows.getString(shows.getColumnIndexOrThrow(DBHelper.ANIME_DESC_COL));
			String status = shows.getString(shows.getColumnIndexOrThrow(DBHelper.ANIME_STATUS_COL));
			int runtimeInt = shows.getInt(shows.getColumnIndexOrThrow(DBHelper.ANIME_RUNTIME_COL));
			String watchlist = shows.getString(shows.getColumnIndexOrThrow(DBHelper.ANIME_WATCHLIST));
			
			getActivity().setTitle(title);
			
			TextView descriptionView = (TextView) v.findViewById(R.id.anime_desc);
			ImageView imageView = (ImageView) v.findViewById(R.id.anime_fanart);
			TextView statusView = (TextView) v.findViewById(R.id.anime_status);
			TextView runtime = (TextView) v.findViewById(R.id.anime_runtime);
			TextView episodes = (TextView) v.findViewById(R.id.anime_episodes);
			TextView time = (TextView) v.findViewById(R.id.anime_time);
			TextView aired = (TextView) v.findViewById(R.id.anime_aired);
			
			
			Display display = getActivity().getWindowManager().getDefaultDisplay();
			int width = display.getWidth();
			
			/*
			if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE)
				width = width/2;*/
			
			//Log.e("WIDTH","" + width);
			String imageUrl = "";
			if(fanart == null)
				imageUrl = "http://placehold.it/" + width + "x" + (int)(0.56*width);
			else
				imageUrl = Anime.resizeImage(width, 0, fanart);
			// or "file:///mnt/sdcard/images/image.jpg"
			
			// Get singletone instance of ImageLoader
			ImageLoader imageLoader = ImageLoader.getInstance();

			// Load and display image asynchronously
			imageLoader.displayImage(imageUrl, imageView);
			if(RestClient.debug)
				Log.i("AnimeImage", imageUrl);
			String[] statusShortArray = new String[]{"finished","currently","unaired"};
			
			Resources r = getResources();
			String[] statusArray = r.getStringArray(R.array.anime_status);
			
			int pos = 0;
			for(String s : statusShortArray)
				if(s.equals(status))
					break;
				else
					pos++;
			
			if(stats != null && status != null)
			{
				episodes.setText(stats.getCount() + " episodes");
				time.setText((stats.getCount() * runtimeInt) + " min");
				if(stats.getCount() != 0 && stats.moveToFirst())
				{
					String startDate = stats.getString(stats.getColumnIndexOrThrow(DBHelper.EPISODE_AIRED_COL));
					aired.setText("Aired: " + startDate + " - ");
					
					if(stats.moveToLast() && status.equals("finished"))
					{
						String endDate = stats.getString(stats.getColumnIndexOrThrow(DBHelper.EPISODE_AIRED_COL));
						aired.append(endDate);
					}
				}
			}
			
			runtime.setText("Runtime: " + runtimeInt + " min");
			if(pos < statusArray.length)
				statusView.setText(statusArray[pos]);
			descriptionView.setText(description);
			
			// Show the user seen progress
			
			Cursor user_seen = getActivity().managedQuery(EpisodeProvider.CONTENT_URI,new String[]{"count(*) AS "+count_column},DBHelper.EPISODE_ANIME_ID_COL +
					"=" + anime_id + " AND " + DBHelper.EPISODE_SEEN_COL + " IS NOT NULL",null,null);
			
			
			Cursor anime_episodes = getActivity().managedQuery(EpisodeProvider.CONTENT_URI,new String[]{"count(*) AS " + count_column},DBHelper.EPISODE_ANIME_ID_COL +
					"=" + anime_id,null,null);
			
			// Register on dataset Change event.
			/*user_seen.registerContentObserver(new MyContentObserver(new Handler(), v, user_seen, anime_episodes));
			anime_episodes.registerContentObserver(new MyContentObserver(new Handler(), v, user_seen, anime_episodes));
			*/
			
			updateProcessValues(v, user_seen, anime_episodes, count_column);
			
			v.invalidate();
			return v;
		}
		
		public static void updateProcessValues(View v, Cursor user_seen, Cursor anime_episodes,String count_column){
	    	user_seen.moveToFirst();
			anime_episodes.moveToFirst();
			
			// Update progressbar with the user seen episodes
			// 
			int user_seen_results = user_seen.getInt(user_seen.getColumnIndexOrThrow(count_column));
			int anime_episodes_results = anime_episodes.getInt(anime_episodes.getColumnIndexOrThrow(count_column));
			
			ProgressBar progressBar = (ProgressBar)v.findViewById(R.id.user_seen_progress);
			TextView progressText = (TextView)v.findViewById(R.id.user_progress);
			int percent = (int)(user_seen_results / (float)anime_episodes_results * 100);
			
			
			progressText.setText("You have seen " + user_seen_results + " of " + anime_episodes_results + " episodes (" + percent + "%)");
			
			progressBar.setProgress(percent);

			v.invalidate();
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
		    ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			
			AdapterView.AdapterContextMenuInfo info =
	                (AdapterView.AdapterContextMenuInfo) menuInfo;

			
		    MenuInflater inflater = getActivity().getMenuInflater();
		    inflater.inflate(R.menu.context_episode, menu);
		    
		    
		    //Log.e("ID",""+v.getId());
			//menu.setHeaderTitle("Episode Menu");
			//menu.add("Mark as seen");
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item){
			 AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		      //String[] names = getResources().getStringArray(R.array.names);
		      switch(item.getItemId()) {
		      case R.id.watch_episode:
		    	  int pos = info.position;
		    	  if(episodes != null && episodes.moveToPosition(pos))
		    	  {
		    		  markEpisodeAsSeen(episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_ID_COL)), true);
		    	  }else
		    		  Toast.makeText(getActivity().getApplicationContext(), "Something went wrong. We're sorry :(",
		                        Toast.LENGTH_SHORT).show();
		            return true;
		      default:
		            return super.onContextItemSelected(item);
		      }
		}
		
		private void markEpisodeAsSeen(int episodeID, boolean seen)
		{
			ArrayList<String> param = new ArrayList<String>();
			param.add(""+episodeID);
			param.add(Constants.timeToString(null));
			//param.add("true"); // Should rather be timestamp.
			
			Intent i = new Intent(this.getActivity(), RestService.class);
			if(seen)
				i.putExtra(RestService.ACTION, RestService.PUT);
			else
				i.putExtra(RestService.ACTION, RestService.DELETE);
			i.putExtra(RestService.OBJECT_TYPE, RestService.OBJECT_TYPE_EPISODE);
			i.putExtra(RestService.PARAMS, param);
			getActivity().startService(i);
	        /*Toast.makeText(getActivity().getApplicationContext(), "Changed watched status for episode",
	                    Toast.LENGTH_SHORT).show();*/
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
		}
		
		public class RelatedAnimeAdapter extends CursorAdapter {
			
			public RelatedAnimeAdapter(Context context, Cursor c) {
				super(context, c);
			}

			@Override
			public void bindView(View view, Context context, Cursor c) {
				
				return;
			}

			@Override
			public View newView(Context context, Cursor c, ViewGroup viewGroup) {
				 final View view = LayoutInflater.from(context).inflate(R.layout.episodelist_item, viewGroup, false);
				 return view;
			}
			
		}
}
