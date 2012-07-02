package com.banan.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.banan.anime.AnimeActivity;
import com.banan.anime.AnimeListActivity;
import com.banan.anime.R;
import com.banan.anime.SearchActivity;
import com.banan.entities.Anime;
import com.banan.entities.AnimeRequest;
import com.banan.entities.Constants;
import com.banan.providers.AnimeProvider;
import com.banan.providers.DBHelper;
import com.banan.trakt.RestClient;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.nostra13.universalimageloader.core.DecodingType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class SearchFragment extends SherlockFragment{

	public static final int TYPE_SEARCH = 0;
	public static final int TYPE_LATEST = 1;
	public static final int TYPE_TRENDING = 2;
	
	SearchLatestAdapter adapter;
	public static Cursor searchResults;
	public static Cursor latestResults;
	public static Cursor topResults;
	public static ListView lv;
	public static Fragment newInstance(String content) {
		SearchFragment frag = new SearchFragment();
		
		frag.content = content;
		
		return frag;
	}
	
	/*public AnimeFragment(){
		super();
	}*/
	
	public String content = null;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setRetainInstance(true);
		//Log.e("content",""+content);
		if(content == null)
			return null;
		
		
		if(content.equals("Search"))
			return createSearchPage(inflater, container, savedInstanceState);
		else if(content.equals("Latest"))
			return createLatestPage(inflater, container, savedInstanceState);
		else if(content.equals("Trending"))
			return createTopPage(inflater, container, savedInstanceState);
		else
			return null;
		
	}
	
	public View createSearchPage(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.search, container, false);
		
		ActionBar ab = getSherlockActivity().getSupportActionBar();
		final EditText e = (EditText) v.findViewById(R.id.searchquery);
		final Button searchBtn = (Button) v.findViewById(R.id.btnSearch);
		
		e.setOnEditorActionListener(
			        new EditText.OnEditorActionListener() {

			        	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			        	    if (event != null) {
			        	        return false;
			        	    }
			        	    String searchText = v.getText().toString();
			        	    return startSearch(searchText,searchBtn);
			        	}
			});
		  
		searchBtn.setOnClickListener(
				new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						String searchText = e.getText().toString();
						startSearch(searchText,searchBtn);
						return;
					}
				});
	    
		  
		  lv = (ListView)v.findViewById(R.id.searchresults);
		  // Preparing the list
		  /*if(!e.getText().toString().equals(""))
		  {
			  searchResults = getActivity().managedQuery(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_TITLE_COL + " LIKE '%" + e.getText().toString() + "%'", null, DBHelper.ANIME_ID + " DESC");
		  }*/
		  if(searchResults == null || searchResults.isClosed())
			  adapter = new SearchLatestAdapter(getActivity(),null);
		  else
			  adapter = new SearchLatestAdapter(getActivity(),searchResults);
		  lv.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view, int pos,
						long id) {
					if(!searchResults.moveToPosition(pos))	
						return;
					
					int anime_id = searchResults.getInt(searchResults.getColumnIndexOrThrow(DBHelper.ANIME_ID));

					Intent i = new Intent(getActivity().getApplicationContext(),
							AnimeActivity.class);
					i.putExtra("anime_id", ""+anime_id);
					startActivity(i);
				}
			
			});
		  
		  lv.setAdapter(adapter);
		return v;
	}
	
	private boolean startSearch(String searchText, Button searchBtn){
		//searchText = DatabaseUtils.sqlEscapeString(searchText);
		searchBtn.requestFocus();
		//searchText = searchText.replace("'","\'");
		new SearchService(getActivity().getApplicationContext()).execute(Constants.REST_SEARCH + searchText.replace(" ", "%20") + ".json",
	    		TYPE_SEARCH);
	    //searchResults = getActivity().managedQuery(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_TITLE_COL + " LIKE '%" + searchText + "%'", null, DBHelper.ANIME_ID + " DESC");
		searchResults = getActivity().managedQuery(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_TITLE_COL + " LIKE ?", new String[]{"%" + searchText + "%"}, DBHelper.ANIME_ID + " DESC");
		adapter.changeCursor(searchResults);
		
		Toast.makeText(this.getActivity(), R.string.searching, Toast.LENGTH_LONG).show();
		
		//Log.e("search",searchResults.);
	    return true;
	}

	public View createLatestPage(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.animelist_view, container, false);

		ListView lv = (ListView) v.findViewById(R.id.animelist);
		latestResults = getActivity().managedQuery(AnimeProvider.CONTENT_URI, Anime.projection, null, null, DBHelper.ANIME_ID + " DESC LIMIT 0,10");
		adapter = new SearchLatestAdapter(getActivity(),
				latestResults);
		/*if (latestResults.size() == 0)
			new SearchService().execute(Constants.REST_LATEST_ANIME, ""
					+ TYPE_LATEST, this.getActivity());*/

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {			
				if(!latestResults.moveToPosition(pos))	
					return;
				
				int anime_id = latestResults.getInt(latestResults.getColumnIndexOrThrow(DBHelper.ANIME_ID));

				Intent i = new Intent(getActivity().getApplicationContext(),
						AnimeActivity.class);
				i.putExtra("anime_id", ""+anime_id);
				startActivity(i);
			}

		});

		lv.setAdapter(adapter);

		return v;
	}
	
	public View createTopPage(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.animelist_view, container, false);

		ListView lv = (ListView) v.findViewById(R.id.animelist);
		topResults = getActivity().managedQuery(AnimeProvider.CONTENT_URI, Anime.projection, null, null, DBHelper.ANIME_BAYES_RATING + " DESC LIMIT 0,10");;
		adapter = new SearchLatestAdapter(getActivity(),
				topResults);
		/*if (latestResults.size() == 0)
			new SearchService().execute(Constants.REST_LATEST_ANIME, ""
					+ TYPE_LATEST, this.getActivity());*/

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {			
				if(!topResults.moveToPosition(pos))	
					return;
				
				int anime_id = topResults.getInt(topResults.getColumnIndexOrThrow(DBHelper.ANIME_ID));

				Intent i = new Intent(getActivity().getApplicationContext(),
						AnimeActivity.class);
				i.putExtra("anime_id", ""+anime_id);
				startActivity(i);
			}

		});

		lv.setAdapter(adapter);

		return v;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	public class SearchResult{
		@SerializedName("data")
		public SearchResultInner data;
		
		public class SearchResultInner{
			@SerializedName("anime")
			public Collection<Anime> anime;
		}
		
	}
	
	public class SearchLatestAdapter extends CursorAdapter{
		ImageLoader imageLoader;
		ImageLoaderConfiguration config;  
		DisplayImageOptions options;
		
		public SearchLatestAdapter(Context context, Cursor c) {
			super(context, c);
			
			options = new DisplayImageOptions.Builder()
            /*.showStubImage(R.drawable.stub_image)*/
            .cacheOnDisc()
            .decodingType(DecodingType.MEMORY_SAVING)
            .build();
			
			imageLoader = ImageLoader.getInstance();
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
	 
			TextView tv = (TextView) view.findViewById(R.id.anime_title);
			TextView animeDesc = (TextView)view.findViewById(R.id.episode_next);
			ImageView image = (ImageView)view.findViewById(R.id.anime_image);
			
			String animeDescText = c.getString(c.getColumnIndexOrThrow(DBHelper.ANIME_DESC_COL));
			String animeTitleText = c.getString(c.getColumnIndexOrThrow(DBHelper.ANIME_TITLE_COL));
			String animeImageText = c.getString(c.getColumnIndexOrThrow(DBHelper.ANIME_IMAGE_COL));
			
			tv.setText(animeTitleText);
			animeDesc.setText(AnimeListActivity.truncate(animeDescText,100));

			
			Resources r = getResources();
			float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, r.getDisplayMetrics());
			String imagestring = "";
			if(animeImageText != null)
				imagestring = Anime.resizeImage((int)px,0,animeImageText);
			else
				imagestring = "http://placehold.it/" + (int)px + "x" + (int)(px*1.56);
			imageLoader.displayImage(imagestring, image, options);
			return;
		}
		
		@Override
		public View newView(Context context, Cursor c, ViewGroup viewGroup) {
			 View view =  LayoutInflater.from(context).inflate(R.layout.animelist_item, null);
				TextView episodeNextDate = (TextView)view.findViewById(R.id.episode_next_date);
				TextView episodeNextDateField = (TextView)view.findViewById(R.id.episode_next_field);
				
				((LinearLayout)episodeNextDateField.getParent()).removeView(episodeNextDateField);
				((LinearLayout)episodeNextDate.getParent()).removeView(episodeNextDate);
			return view;
		}
	}
	
	public int sendQuery(String query,int type, Context c){
		RestClient rest = RestClient.getInstance(c);
		
		String response = rest.ReadMethod(query);
		
		Gson gson = new Gson();
		
		AnimeRequest requests = gson.fromJson(response,AnimeRequest.class);
		
		if(type== TYPE_TRENDING)
			Constants.setLastUpdate(c, TYPE_TRENDING, Calendar.getInstance().getTimeInMillis());
		else if(type == TYPE_LATEST)
			Constants.setLastUpdate(c, TYPE_LATEST, Calendar.getInstance().getTimeInMillis());
		
		/*
		 * 
		 * */
		if(requests == null || requests.getAnimelist() == null)
			return 0;
		for(Anime r : requests.getAnimelist())
		{
			/**
			 * TODO: NEED to remove this in the next version.. Use cursoradapter instead.
			 */
			/*if(type== TYPE_SEARCH)
				searchResults.add(r); // for backwards-compatibility
			else if(type == TYPE_LATEST)
				latestResults.add(r);*/
			
			// Check if the anime exists in the database
				
			Cursor shows = c.getContentResolver().query(
					AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID+"="+r.id, null, null);
			
			// If it exists.. we do not add it to the database
			if(shows.getCount() != 0)
				continue;
			
			ContentValues result = new ContentValues();
			
			result.put(DBHelper.ANIME_DESC_COL, r.desc);
			result.put(DBHelper.ANIME_FANART_COL, r.fanart);
			result.put(DBHelper.ANIME_IMAGE_COL, r.image);
			result.put(DBHelper.ANIME_STATUS_COL, r.status); // NOTHING HERE?
			result.put(DBHelper.ANIME_TITLE_COL, r.title);
			result.put(DBHelper.ANIME_ID, r.id);
			result.put(DBHelper.ANIME_BAYES_RATING, r.bayes);
			
			c.getContentResolver().insert(AnimeProvider.CONTENT_URI, result); // We add the anime to the database..
		}
		return requests.getAnimelist().size();
		//Log.e("tag",query);
	}
	
	/*private class FetchLatestAnimeService extends AsyncTask<String, Void, Void> {
		
	}*/
	
	public class SearchService extends AsyncTask<Object, Void, Integer> {
        private String Content;
        private String Error = null;
        private ProgressDialog Dialog;
        private Context c;
        
        public SearchService(Context c)
        {
        	this.c = c;
        }
        
        protected void onPreExecute() {
        	if(c == null)
        		Log.e("SearchService","Context is null");
        	/*Dialog = new ProgressDialog(c);
            Resources res = c.getResources();
            Dialog.setMessage(res.getString(R.string.searching));
            Dialog.show();*/
        }

        protected Integer doInBackground(Object... params) {
            return sendQuery((String)params[0], (Integer)(params[1]),c);
        }
        
        protected void onPostExecute(Integer resultAmount) {
            //Dialog.dismiss();
        	if(adapter != null)
        		adapter.notifyDataSetChanged();
        	if(lv != null)
        		lv.invalidate();
        	Toast.makeText(c, "Found " + resultAmount + " anime(s) online.", Toast.LENGTH_SHORT).show();
        }
        
    }
	
}