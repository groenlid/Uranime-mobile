package com.banan.anime;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DataSetObserver;
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
import com.actionbarsherlock.view.Window;
import com.banan.anime.AnimeActivity;
import com.banan.anime.AnimeListActivity;
import com.banan.anime.R;
import com.banan.entities.Anime;
import com.banan.entities.AnimeGenre;
import com.banan.entities.Constants;
import com.banan.entities.Genre;
import com.banan.entities.Synonym;
import com.banan.fragments.SearchFragment.SearchLatestAdapter;
import com.banan.fragments.SearchFragment.SearchService;
import com.banan.fragments.SearchFragment.SearchResult.SearchResultInner;
import com.banan.providers.AnimeGenreProvider;
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

public class AnimeGenreActivity extends SherlockActivity{
	public static int TYPE_GENRE_ANIME = 1000;
	SearchLatestAdapter adapter;
	public static Cursor genreResults;
	public static Cursor animeResults;
	public static ListView lv;
	public static String genre_id;
	public static String anime_id;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);
		// Set to false initally
		
		long minTimeSinceUpdate = Calendar.getInstance().getTimeInMillis() - (1000 * 60 * 60 * 6); // 6 hours	
		
		genre_id = getIntent().getStringExtra("genre_id");
		TYPE_GENRE_ANIME = 1000 + Integer.parseInt(genre_id);
		
		setContentView(R.layout.genrelist_view);
		
		
		ListView lv = (ListView) findViewById(R.id.animelist);
		animeResults = getGenreCursor();
		
		ActionBar bar = getSupportActionBar();
		
		Cursor singleGenre = managedQuery(GenreProvider.CONTENT_URI, Genre.projection, DBHelper.GENRE_ID + "= ?", new String[]{""+genre_id}, null);
		// Set title
		if(singleGenre.moveToFirst())
		{
			// set title-bar
			final String genreName = WordUtils.capitalize(singleGenre.getString(singleGenre.getColumnIndexOrThrow(DBHelper.GENRE_NAME)));
			bar.setTitle(genreName);
			
			// set description
			final String desc = singleGenre.getString(singleGenre.getColumnIndexOrThrow(DBHelper.GENRE_DESC));
			TextView tv = (TextView) findViewById(R.id.genre_desc);
			if(desc != null)
			{
				tv.setText(Constants.truncate(desc, 100));
				tv.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
						builder.setTitle(genreName);
						builder.setMessage(desc);
						builder.setNeutralButton("Dismiss", null);
						builder.show();
						
					}
				});
			}
			
		}
		//stopManagingCursor(animeResults);
		
		adapter = new SearchLatestAdapter(this,
				animeResults);
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {			
				if(!adapter.getCursor().moveToPosition(pos))	
					return;
				
				int anime_id = adapter.getCursor().getInt(adapter.getCursor().getColumnIndexOrThrow(DBHelper.ANIME_ID));

				Intent i = new Intent(getApplicationContext(),
						AnimeActivity.class);
				i.putExtra("anime_id", ""+anime_id);
				startActivity(i);
			}

		});

		lv.setAdapter(adapter);

		if(Constants.getLastUpdate(getApplicationContext(), TYPE_GENRE_ANIME) < minTimeSinceUpdate)
		{
			if(RestClient.debug)
				Log.i("AnimeGenreActivity", "Refresh type genre for animes: " + genre_id + " with type_id " + TYPE_GENRE_ANIME);
			this.new SearchService(this).execute(Constants.getGenreAnimeURL(Integer.parseInt(genre_id)), 
					TYPE_GENRE_ANIME );
		}
	}
	
	private Cursor getGenreCursor(){
		genreResults = managedQuery(AnimeGenreProvider.CONTENT_URI, AnimeGenre.projection, DBHelper.ANIME_GENRE_GENRE_ID + "= ?", new String[]{""+genre_id}, null);
		
		anime_id = "";
		while(genreResults.moveToNext())
			anime_id += "" + genreResults.getInt(genreResults.getColumnIndexOrThrow(DBHelper.ANIME_GENRE_ANIME_ID)) + ",";
		
		anime_id = StringUtils.removeEnd(anime_id,",");
		
		animeResults = managedQuery(AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID + " IN(" + anime_id + ")", null, DBHelper.ANIME_TITLE_COL + " ASC");
		return animeResults;
	}
	
	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
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
				
				for(Genre g : r.getTags())
				{
					// Add the genres for the anime
					Cursor genre = c.getContentResolver().query(GenreProvider.CONTENT_URI, Genre.projection, DBHelper.GENRE_ID+"="+g.id, null, null);
					ContentValues genreContent = new ContentValues();
					genreContent.put(DBHelper.GENRE_ID, g.id);
					genreContent.put(DBHelper.GENRE_DESC, g.description);
					genreContent.put(DBHelper.GENRE_NAME, g.name);
					genreContent.put(DBHelper.GENRE_IS_GENRE, (g.is_genre != null) ? 1 : null);
					
					if(genre.getCount() == 0)
						c.getContentResolver().insert(GenreProvider.CONTENT_URI, genreContent);
					else
						c.getContentResolver().update(GenreProvider.CONTENT_URI, genreContent, DBHelper.GENRE_ID +"="+g.id, null);
	
					genre.close();
					
					// Connect the genres/tags to the anime if it is not already connected
					Cursor genreAnime = c.getContentResolver().query(
							AnimeGenreProvider.CONTENT_URI, 
							AnimeGenre.projection, 
							DBHelper.ANIME_GENRE_ANIME_ID + "= ? AND " +
							DBHelper.ANIME_GENRE_GENRE_ID + "= ?", 
							new String[]{""+r.id,""+g.id}, 
							null);
					
					ContentValues genreAnimeContent = new ContentValues();
					genreAnimeContent.put(DBHelper.ANIME_GENRE_ANIME_ID, r.id);
					genreAnimeContent.put(DBHelper.ANIME_GENRE_GENRE_ID, g.id);
					
					if(genreAnime.getCount() == 0)
						// We add the link between genre and anime to the database..
						c.getContentResolver().insert(AnimeGenreProvider.CONTENT_URI, genreAnimeContent); 
					
					genreAnime.close();
				}
				
				/*int percent = (int)(mProgress / requests.size() * 100.0);
				int progress = (Window.PROGRESS_END - Window.PROGRESS_START) / 100 * mProgress;
	            
				b.setSupportProgress(progress);
	            mProgress++;*/
				Constants.setLastUpdate(c, TYPE_GENRE_ANIME, Calendar.getInstance().getTimeInMillis());
				
			}
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
        	{
        		a.setSupportProgressBarIndeterminateVisibility(true);
        		Dialog = new ProgressDialog(a);
        	}
        	if(b != null)
        	{	
        		b.setSupportProgressBarIndeterminateVisibility(true);
        		Dialog = new ProgressDialog(b);
        	}
        	Resources res = c.getResources();
            Dialog.setMessage(res.getString(R.string.fetchanimegenre));
            if(adapter == null || adapter.isEmpty())
            	Dialog.show();
        }

        protected Integer doInBackground(Object... params) {
        	type = (Integer)(params[1]);
            return sendQuery((String)params[0], (Integer)(params[1]), c);
        }
        
        protected void onPostExecute(Integer resultAmount) {
            Dialog.dismiss();
        	if(adapter != null)
        		adapter.notifyDataSetChanged();

        	// Requery the cursor after searching. Fixes returned null on first
        	// query,
        	animeResults = getGenreCursor();
        	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
	    		adapter.changeCursor(animeResults);//changeCursor();
	    	else
	    		adapter.swapCursor(animeResults);//changeCursor();
        	adapter.notifyDataSetChanged();
        	
        	if(lv != null)
        		lv.invalidate();
        	if(a != null)
        		a.setSupportProgressBarIndeterminateVisibility(false);
        	if(b != null)
        		b.setSupportProgressBarIndeterminateVisibility(false);
        }
        
    }

}

