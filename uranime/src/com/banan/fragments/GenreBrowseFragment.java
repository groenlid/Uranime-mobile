package com.banan.fragments;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.banan.anime.AnimeActivity;
import com.banan.anime.AnimeGenreActivity;
import com.banan.anime.AnimeListActivity;
import com.banan.anime.R;
import com.banan.entities.Anime;
import com.banan.entities.Constants;
import com.banan.entities.Genre;
import com.banan.entities.Synonym;
import com.banan.fragments.SearchFragment.SearchLatestAdapter;
import com.banan.fragments.SearchFragment.SearchService;
import com.banan.fragments.SearchFragment.SearchResult.SearchResultInner;
import com.banan.providers.AnimeProvider;
import com.banan.providers.DBHelper;
import com.banan.providers.GenreProvider;
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

public class GenreBrowseFragment extends SherlockFragment{
	public static final int TYPE_GENRE = 9;
	SearchLatestAdapter adapter;
	public static Cursor genreResult;
	public static ListView lv;
	
	public static Fragment newInstance(String content) {
		GenreBrowseFragment frag = new GenreBrowseFragment();
		return frag;
	}
	
	public void onResume(){
		super.onResume();
		if(genreResult == null || genreResult.isClosed())
			genreResult = getGenreResults(this.getSherlockActivity());
		if(adapter != null)
			adapter.notifyDataSetChanged();
	}

	private Cursor getGenreResults(SherlockFragmentActivity a) {
		
		if(a != null)
			return a.managedQuery(GenreProvider.CONTENT_URI, Genre.projection, DBHelper.GENRE_IS_GENRE + " IS NOT NULL", null, "lower("+DBHelper.GENRE_NAME + ") ASC");
		return getActivity().managedQuery(GenreProvider.CONTENT_URI, Genre.projection, DBHelper.GENRE_IS_GENRE + " IS NOT NULL", null, "lower("+DBHelper.GENRE_NAME + ") ASC");
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setRetainInstance(true);
		
		long minTimeSinceUpdate = Calendar.getInstance().getTimeInMillis() - (1000 * 60 * 60);	
		
		if(Constants.getLastUpdate(this.getActivity().getApplicationContext(), GenreBrowseFragment.TYPE_GENRE) < minTimeSinceUpdate)
		{
			GenreBrowseFragment s = new GenreBrowseFragment();
			s.new SearchService(this.getSherlockActivity()).execute(Constants.REST_GENRE, 
					GenreBrowseFragment.TYPE_GENRE);
		}
		
		View v = inflater.inflate(R.layout.animelist_view, container, false);

		ListView lv = (ListView) v.findViewById(R.id.animelist);
		genreResult = getGenreResults(this.getSherlockActivity());
		//getActivity().stopManagingCursor(genreResult);
		//genreResult.registerDataSetObserver(this);
		
		adapter = new SearchLatestAdapter(getActivity(),
				genreResult);
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {			
				if(!adapter.getCursor().moveToPosition(pos))	
					return;
				
				int genre_id = adapter.getCursor().getInt(adapter.getCursor().getColumnIndexOrThrow(DBHelper.GENRE_ID));

				Intent i = new Intent(getActivity().getApplicationContext(),
						AnimeGenreActivity.class);
				i.putExtra("genre_id", ""+genre_id);
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
	
	public class SearchLatestAdapter extends CursorAdapter{
		
		public SearchLatestAdapter(Context context, Cursor c) {
			super(context, c);
		}

		public void registerDataSetObserver(SearchFragment searchFragment) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
			if(c.isClosed() || c == null)
				return;
			
			TextView genre = (TextView) view.findViewById(R.id.genre);
			String genreText = c.getString(c.getColumnIndexOrThrow(DBHelper.GENRE_NAME));
			genre.setText(WordUtils.capitalize(genreText));
			return;
		}
		
		@Override
		public View newView(Context context, Cursor c, ViewGroup viewGroup) {
			 View view =  LayoutInflater.from(context).inflate(R.layout.genre_list_item, null);
			return view;
		}
	}
	
	public int sendQuery(String query,int type, Context c){
		RestClient rest = RestClient.getInstance(c);
		
		String response = rest.ReadMethod(query);
		
		Gson gson = new Gson();
		
		// Since the json includes arraylist with string numbers
		// we need to add a type for gson
		
		Type gsonType = new TypeToken<Map<String, Genre>>() {}.getType();
		
		try{
	
			
			Map<String, Genre> requests = gson.fromJson(response,gsonType);
			
			/*
			 * 
			 * */
			if(requests == null || requests.isEmpty())
				return 0;
			//for(Anime r : requests.getAnimelist())
			for(Map.Entry<String, Genre> entry : requests.entrySet())
			{
				Genre r = entry.getValue();
				/**
				 * TODO: NEED to remove this in the next version.. Use cursoradapter instead.
				 */
				// Check if the anime exists in the database
					
				Cursor genre = c.getContentResolver().query(
						GenreProvider.CONTENT_URI, Genre.projection, DBHelper.GENRE_ID+"="+r.id, null, null);
				
				
				
				ContentValues result = new ContentValues();
				
				result.put(DBHelper.GENRE_ID, r.id);
				result.put(DBHelper.GENRE_NAME, r.name);
				result.put(DBHelper.GENRE_DESC, r.description);
				result.put(DBHelper.GENRE_IS_GENRE, (r.is_genre != null) ? 1 : null);
				
				// If it exists.. we do not add it to the database but update it
				if(genre.getCount() == 0)
					c.getContentResolver().insert(GenreProvider.CONTENT_URI, result); // We add the anime to the database..
				else
					c.getContentResolver().update(GenreProvider.CONTENT_URI, result, DBHelper.GENRE_ID +"="+r.id, null);
				
				genre.close();
				
			}
			Constants.setLastUpdate(c, TYPE_GENRE, Calendar.getInstance().getTimeInMillis());
			return requests.size();
		} catch (JsonParseException e){
		  return 0;
		}
		//Log.e("tag",query);
	}

	
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
        	
        	
        	if(lv != null)
        		lv.invalidate();
        	if(a != null)
        		a.setSupportProgressBarIndeterminateVisibility(false);
        	if(b != null)
        		b.setSupportProgressBarIndeterminateVisibility(false);
        }
        
    }
}

