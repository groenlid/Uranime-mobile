package com.banan.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.banan.UIElements.CheckBox;
import com.banan.anime.EpisodeActivity;
import com.banan.anime.R;
import com.banan.entities.Constants;
import com.banan.entities.Episode;
import com.banan.fragments.AnimeFragment.EpisodeListAdapter;
import com.banan.providers.DBHelper;
import com.banan.providers.EpisodeProvider;
import com.banan.providers.RestService;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class EpisodeListFragment extends SherlockFragment{
	
	final String count_column = "count_column";
	private long selectedEpisodeId = -1L;
	private Cursor episodes;
	
	public String content = null;
	
	public static EpisodeListFragment newInstance(){
		return new EpisodeListFragment();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setRetainInstance(true);
		
		View v = inflater.inflate(R.layout.animelist_view, container, false);
		
		String anime_id = getActivity().getIntent().getStringExtra("anime_id");
		
		final Cursor episodes = getActivity().managedQuery(
				EpisodeProvider.CONTENT_URI, Episode.projection, DBHelper.ANIME_ID + " = " + anime_id , null, DBHelper.EPISODE_NUMBER_COL + " DESC");
		
		this.episodes = episodes;
		// Check if episode count is 0
		if(episodes.getCount() == 0)
		{
			ArrayList<String> params = new ArrayList<String>();
			params.add(""+anime_id);
			// Fetch new episode... User is possibly using search.
			Intent i = new Intent(getActivity(), RestService.class);
			i.putExtra(RestService.ACTION, RestService.GET);
			i.putExtra(RestService.OBJECT_TYPE, RestService.OBJECT_TYPE_ANIME);
			i.putExtra(RestService.PARAMS, params);
			getActivity().startService(i);
            
            Toast.makeText(getActivity(), "Looking for episodes", Toast.LENGTH_SHORT).show();
		}
		
		ListView listview = (ListView) v.findViewById(R.id.animelist);

		final EpisodeListAdapter episodeAdapter = new EpisodeListAdapter(getActivity().getApplicationContext(),episodes);
		
		listview.setAdapter(episodeAdapter);
		
		registerForContextMenu(listview);
		
		listview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				if(!episodes.moveToFirst())
					return;
				episodes.moveToPosition(pos);
				
				String anime_id = "" + episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_ANIME_ID_COL));
				String episode_number = "" + episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_NUMBER_COL));
				boolean special_episode = episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_SPECIAL_COL))>0;
				
				Intent i = new Intent(getActivity().getApplicationContext(),EpisodeActivity.class);
				i.putExtra("anime_id", anime_id);
				i.putExtra("episode_number", episode_number);
				i.putExtra("special_episode", special_episode);
				startActivity(i);
			}
		
		});
		v.invalidate();
		return v;
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
	
	public class EpisodeListAdapter extends CursorAdapter
	{
		 
		ImageLoader imageLoader;
		ImageLoaderConfiguration config;  
		DisplayImageOptions options;
		
		private Cursor c;

		public EpisodeListAdapter(Context context, Cursor c)
		{
			super(context,c);
			this.c = c;
			
			imageLoader = ImageLoader.getInstance();
			
			options = new DisplayImageOptions.Builder()
			.cacheInMemory()
			.cacheOnDisc()
	        .imageScaleType(ImageScaleType.POWER_OF_2)
	        .build();
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
			
			TextView episodeTitleTxt = (TextView)view.findViewById(R.id.episode_title);
			//TextView episodeDescTxt = (TextView)view.findViewById(R.id.episode_desc);
			TextView episodeNumberTxt = (TextView)view.findViewById(R.id.episode_number);
			CheckBox checkBox = (CheckBox)view.findViewById(R.id.watchedCheckbox);
			
			final int episodeID = c.getInt(c.getColumnIndexOrThrow(DBHelper.EPISODE_ID_COL));
			
			//TextView episodeNumberBigTxt = (TextView)view.findViewById(R.id.episode_number_big);
			
			TextView episodeDateTxt = (TextView)view.findViewById(R.id.episode_date);
			//ImageView episodeSeenImage = (ImageView)view.findViewById(R.id.ep_seen);
			Resources r = getResources();
			float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, r.getDisplayMetrics());
			
			//String imageUrl = Anime.resizeImage((int)px,0,c.getString(c.getColumnIndexOrThrow(DBHelper.ANIME_IMAGE_COL)) );
			final String episodeTitle = c.getString(c.getColumnIndexOrThrow(DBHelper.EPISODE_TITLE_COL));
			String episodeNumber = c.getString(c.getColumnIndexOrThrow(DBHelper.EPISODE_NUMBER_COL));
			String episodeDesc = c.getString(c.getColumnIndexOrThrow(DBHelper.EPISODE_DESC_COL));
			String episodeDate = c.getString(c.getColumnIndexOrThrow(DBHelper.EPISODE_AIRED_COL));
			boolean episodeNotSeen = c.isNull(c.getColumnIndexOrThrow(DBHelper.EPISODE_SEEN_COL));
			boolean special = episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_SPECIAL_COL))>0;
			//imageLoader.displayImage(imageUrl, image, options);
			
			episodeTitleTxt.setText(Constants.truncate(episodeTitle,30));
			//episodeDescTxt.setText(Constants.truncate(episodeDesc,70));
			if(special)
				episodeNumberTxt.setText("Special " + episodeNumber);
			else
				episodeNumberTxt.setText("Episode " + episodeNumber);
			
			//episodeNumberBigTxt.setText(episodeNumber);
			
			episodeDateTxt.setText(episodeDate);
			
			checkBox.setChecked(!episodeNotSeen);
			
			checkBox.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    ((CheckBox) v).toggle();
                    markEpisodeAsSeen(episodeID, ((CheckBox) v).isChecked());
                }
            });

			
			return;
		}

		@Override
		public View newView(Context context, Cursor c, ViewGroup viewGroup) {
			 final View view = LayoutInflater.from(context).inflate(R.layout.episodelist_item, viewGroup, false);
			 return view;
		}

	}
}
