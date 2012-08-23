package com.banan.fragments;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Build;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
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
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.banan.anime.AnimeActivity;
import com.banan.anime.AnimeListActivity;
import com.banan.anime.R;
import com.banan.anime.SearchActivity;
import com.banan.entities.Anime;
import com.banan.entities.AnimeRequest;
import com.banan.entities.Constants;
import com.banan.entities.Synonym;
import com.banan.providers.AnimeProvider;
import com.banan.providers.DBHelper;
import com.banan.providers.SynonymProvider;
import com.banan.trakt.RestClient;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class SearchFragment extends SherlockFragment{

	public static final int TYPE_SEARCH = 0;
	
	SearchLatestAdapter adapter;
	public static Cursor searchResults;
	public static Cursor latestResults;
	public static ListView lv;
	public static String textSearch;
	
	public static Fragment newInstance(String content) {
		SearchFragment frag = new SearchFragment();
		
		frag.content = content;
		
		return frag;
	}
	
	/*public AnimeFragment(){
		super();
	}*/
	
	public String content = null;
	
	
	
	public void onResume(){
		super.onResume();
		if(latestResults == null || latestResults.isClosed())
			latestResults = getActivity().managedQuery(AnimeProvider.CONTENT_URI, Anime.projection, null, null, DBHelper.ANIME_ID + " DESC LIMIT 0,10");
		if(adapter != null)
			adapter.notifyDataSetChanged();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setRetainInstance(true);
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
			  adapter = new SearchLatestAdapter(getSherlockActivity(),null);
		  else
			  adapter = new SearchLatestAdapter(getSherlockActivity(),searchResults);
		  
		  adapter.registerDataSetObserver(this);
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
	
	private Cursor getSearchCursor(String searchText){
		Cursor synonymResults = getSherlockActivity().managedQuery(SynonymProvider.CONTENT_URI, Synonym.projection, DBHelper.SYNONYM_TITLE + " LIKE ?", new String[]{"%" + searchText + "%"}, null);
		
		if(!synonymResults.moveToFirst())
			return null;
		String ids = "";
		
		while(!synonymResults.isAfterLast()){
			ids += synonymResults.getInt(synonymResults.getColumnIndex(DBHelper.SYNONYM_ANIME_ID)) + ",";
			synonymResults.moveToNext();
		}
		ids = ids.substring(0,ids.length()-1);
		
		searchResults = getSherlockActivity().managedQuery(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID + " IN("+ids+")", null , DBHelper.ANIME_ID + " DESC");
		//adapter.changeCursor(searchResults);
		return searchResults;
	}
	
	private boolean startSearch(String searchText, Button searchBtn){
		//searchText = DatabaseUtils.sqlEscapeString(searchText);
		searchBtn.requestFocus();
		this.textSearch = searchText; 
		//searchText = searchText.replace("'","\'");
		new SearchService(getSherlockActivity()).execute(Constants.REST_SEARCH + searchText.replace(" ", "%20"),
	    		TYPE_SEARCH);
	    //searchResults = getActivity().managedQuery(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_TITLE_COL + " LIKE '%" + searchText + "%'", null, DBHelper.ANIME_ID + " DESC");
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			adapter.changeCursor(getSearchCursor(searchText));//changeCursor();
		else
			adapter.swapCursor(getSearchCursor(searchText));//changeCursor();
		
		Toast.makeText(this.getSherlockActivity(), R.string.searching, Toast.LENGTH_LONG).show();
		
		//Log.e("search",searchResults.);
	    return true;
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
			.cacheInMemory()
			.cacheOnDisc()
	        .imageScaleType(ImageScaleType.POWER_OF_2)
	        .build();
			
			imageLoader = ImageLoader.getInstance();
		}

		public void registerDataSetObserver(SearchFragment searchFragment) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
			if(c.isClosed() || c == null)
				return;
			
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
		
		// Since the json includes arraylist with string numbers
		// we need to add a type for gson
		
		Type gsonType = new TypeToken<Map<String, Anime>>() {}.getType();
		
		try{
	
			
			Map<String, Anime> requests = gson.fromJson(response,gsonType);
			
			/*
			 * 
			 * */
			if(requests == null || requests.isEmpty())
				return 0;
			//for(Anime r : requests.getAnimelist())
			for(Map.Entry<String, Anime> entry : requests.entrySet())
			{
				Anime r = entry.getValue();
				/**
				 * TODO: NEED to remove this in the next version.. Use cursoradapter instead.
				 */
				// Check if the anime exists in the database
					
				Cursor shows = c.getContentResolver().query(
						AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID+"="+r.id, null, null);
				
				
				
				ContentValues result = new ContentValues();
				
				result.put(DBHelper.ANIME_DESC_COL, r.desc);
				result.put(DBHelper.ANIME_FANART_COL, r.fanart);
				result.put(DBHelper.ANIME_IMAGE_COL, r.image);
				result.put(DBHelper.ANIME_STATUS_COL, r.status); // NOTHING HERE?
				result.put(DBHelper.ANIME_TITLE_COL, r.title);
				result.put(DBHelper.ANIME_ID, r.id);
				result.put(DBHelper.ANIME_BAYES_RATING, r.bayes);
				
				// If it exists.. we do not add it to the database but update it
				if(shows.getCount() == 0)
					c.getContentResolver().insert(AnimeProvider.CONTENT_URI, result); // We add the anime to the database..
				else
					c.getContentResolver().update(AnimeProvider.CONTENT_URI, result, DBHelper.ANIME_ID +"="+r.id, null);
				
				shows.close();
				
				for(Synonym s : r.getSynonyms())
				{
					Cursor synonym = c.getContentResolver().query(SynonymProvider.CONTENT_URI, Synonym.projection, DBHelper.SYNONYM_ID+"="+s.id, null, null);
					ContentValues synonymContent = new ContentValues();
					synonymContent.put(DBHelper.SYNONYM_ID, s.id);
					synonymContent.put(DBHelper.SYNONYM_ANIME_ID, s.anime_id);
					synonymContent.put(DBHelper.SYNONYM_TITLE, s.title);
					synonymContent.put(DBHelper.SYNONYM_LANG, s.lang);
					
					if(synonym.getCount() == 0)
						c.getContentResolver().insert(SynonymProvider.CONTENT_URI, synonymContent);
					else
						c.getContentResolver().update(SynonymProvider.CONTENT_URI, synonymContent, DBHelper.SYNONYM_ID +"="+s.id, null);
					if(DBHelper.Debug)
						Log.e("SearchFragment", "Anime " + r.title + " is also called " + s.title);
					synonym.close();
				}
				
			}
			return requests.size();
		} catch (JsonParseException e){
		  return 0;
		}
		//Log.e("tag",query);
	}
	
	/*private class FetchLatestAnimeService extends AsyncTask<String, Void, Void> {
		
	}*/
	
	public class SearchService extends AsyncTask<Object, Void, Integer> {
        private String Content;
        private String Error = null;
        private ProgressDialog Dialog;
        private SherlockActivity a;
        private SherlockFragmentActivity b;
        private Context c;
        
        int type = 0;
        public SearchService(SherlockActivity a)
        {
        	this.a = a;
        	c = a.getApplicationContext();
        }
        
        public SearchService(SherlockFragmentActivity b)
        {
        	this.b = b;
        	c = b.getApplicationContext();
        }
        
        protected void onPreExecute() {
        	if(a != null)
        		a.setSupportProgressBarIndeterminateVisibility(true);
        	if(b != null)
        		b.setSupportProgressBarIndeterminateVisibility(true);
        	/*Dialog = new ProgressDialog(c);
            Resources res = c.getResources();
            Dialog.setMessage(res.getString(R.string.searching));
            Dialog.show();*/
        }

        protected Integer doInBackground(Object... params) {
        	type = (Integer)(params[1]);
            return sendQuery((String)params[0], (Integer)(params[1]),c);
        }
        
        protected void onPostExecute(Integer resultAmount) {
            //Dialog.dismiss();
        	if(adapter != null)
        		adapter.notifyDataSetChanged();

        	// Requery the cursor after searching. Fixes returned null on first
        	// query,
        	if(SearchFragment.textSearch != null && type == TYPE_SEARCH)
	        	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
	    			adapter.changeCursor(getSearchCursor(SearchFragment.textSearch));//changeCursor();
	    		else
	    			adapter.swapCursor(getSearchCursor(SearchFragment.textSearch));//changeCursor();
        	
        	if(lv != null)
        		lv.invalidate();
        	if(a != null)
        		a.setSupportProgressBarIndeterminateVisibility(false);
        	if(b != null)
        		b.setSupportProgressBarIndeterminateVisibility(false);
        	
        	if(type == TYPE_SEARCH)
        		Toast.makeText(c, "Found " + resultAmount + " anime(s) online.", Toast.LENGTH_SHORT).show();
        }
        
    }

}