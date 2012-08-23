package com.banan.anime;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TabHost;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.banan.pagers.PagerShowAdapter;
import com.banan.providers.AnimeProvider;
import com.banan.providers.DBHelper;
import com.banan.providers.RestService;
import com.banan.providers.TabListener;
import com.banan.anime.R;
import com.banan.entities.Anime;
import com.banan.entities.Constants;
import com.banan.fragments.AnimeSummaryFragment;
import com.banan.fragments.EpisodeListFragment;

public class AnimeActivity extends BaseActivity implements ActionBar.OnNavigationListener {
	
	// Variables for the tabs
	TabHost mTabHost;
	String anime_id;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		//if(savedInstanceState == null)
			setContentView(R.layout.anime_tabs);
		
		anime_id = getIntent().getStringExtra("anime_id");
		
		ActionBar bar = getSupportActionBar();
		//bar.removeAllTabs();
		
		// This is for tablet support
		/*if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE)
        {
			bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			bar.setDisplayOptions(1, ActionBar.DISPLAY_SHOW_TITLE);*/
			/*
			AnimeSummaryFragment summary = AnimeSummaryFragment.newInstance();
			EpisodeListFragment list = EpisodeListFragment.newInstance();
			
			//getSupportFragmentManager().beginTransaction().replace(R.id.animeSummary_frag, summary).commit();
			//getSupportFragmentManager().beginTransaction().replace(R.id.episodeList_frag, list).commit();
			
			FragmentManager fragmentManager = getSupportFragmentManager();
	       FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

	       //add the fragments
	       fragmentTransaction.add(R.id.frags, summary);
	       fragmentTransaction.add(R.id.frags, list);
	       fragmentTransaction.commit();*/
			
        //}
		/*if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT)
		{*/
			bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		    bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
		    
		    bar.addTab(bar.newTab()
		            .setText("Summary")
		            .setTabListener(new TabListener<AnimeSummaryFragment>(
		                    this, "summary", AnimeSummaryFragment.class, null)));
	
		    bar.addTab(bar.newTab()
		            .setText("Episodes")
		            .setTabListener(new TabListener<EpisodeListFragment>(
		                    this, "episodes", EpisodeListFragment.class, null)));
		    
		    if (savedInstanceState != null) {
		        bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		    }
		//}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  //setContentView(R.layout.anime_tabs);
	  
	}
	
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String anime_id = getIntent().getStringExtra("anime_id");
		switch (item.getItemId()) {
		case R.id.menu_add:
			Toast.makeText(this, "Not implemented yet!", Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.menu_search:
			startActivity(new Intent(this, SearchActivity.class));
			break;
		case R.id.menu_preferences:
			startActivity(new Intent(this, TvdbPreferenceActivity.class));
			break;
		case R.id.menu_seenall:
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				/**
				 * This is the dialoginterface for the "Mark all episodes as watched button"
				 */
				@Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			            //Yes button clicked
			        	String anime_id = getIntent().getStringExtra("anime_id");
			        	ArrayList<String> param = new ArrayList<String>();
						param.add(anime_id);
						param.add("true");
						Intent i = new Intent(getApplicationContext(), RestService.class);
						i.putExtra(RestService.ACTION, RestService.PUT);
						i.putExtra(RestService.OBJECT_TYPE, RestService.OBJECT_TYPE_ANIME);
						i.putExtra(RestService.PARAMS, param);
						startService(i);
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            //No button clicked
			        	dialog.dismiss();
			            break;
			        }
			    }
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("This will mark the entire anime as watched. Are you sure?").setPositiveButton("Yes", dialogClickListener)
		    		.setNegativeButton("No", dialogClickListener).show();
			break;
		case R.id.menu_unseeall:
			DialogInterface.OnClickListener dialogClickListener2 = new DialogInterface.OnClickListener() {
				/**
				 * This is the dialoginterface for the "Mark all episodes as watched button"
				 */
				@Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			            //Yes button clicked
			        	String anime_id = getIntent().getStringExtra("anime_id");
			        	ArrayList<String> param = new ArrayList<String>();
						param.add(anime_id);
						param.add("false");
						Intent i = new Intent(getApplicationContext(), RestService.class);
						i.putExtra(RestService.ACTION, RestService.PUT);
						i.putExtra(RestService.OBJECT_TYPE, RestService.OBJECT_TYPE_ANIME);
						i.putExtra(RestService.PARAMS, param);
						startService(i);
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            //No button clicked
			        	dialog.dismiss();
			            break;
			        }
			    }
			};
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setMessage("This will mark the entire anime as unwatched and thus remove it from your library. Are you sure?").setPositiveButton("Yes", dialogClickListener2)
		    		.setNegativeButton("No", dialogClickListener2).show();
			break;
		case R.id.menu_update:
			
			ArrayList<String> params = new ArrayList<String>();
			params.add(anime_id);
			Intent is = new Intent(this, RestService.class);
			is.putExtra(RestService.ACTION, RestService.GET);
			is.putExtra(RestService.OBJECT_TYPE, RestService.OBJECT_TYPE_ANIME);
			is.putExtra(RestService.PARAMS, params);
			startService(is);

			Toast.makeText(this, R.string.animelist_refresh, Toast.LENGTH_SHORT)
					.show();

			break;
		case R.id.menu_watchlist:
			Cursor anime = this.managedQuery(
	    			AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID+"= ? AND " + DBHelper.ANIME_WATCHLIST + " IS NOT NULL", new String[]{anime_id}, "title ASC");
	        
			ArrayList<String> param = new ArrayList<String>();
			param.add(anime_id);
			
	        if(anime.getCount() == 0){
	        	param.add("true");
	        	item.setIcon(R.drawable.ic_action_remove);
	        	item.setTitle("Remove from watchlist");
	        }
	        else{
	        	param.add("false");
	        	item.setIcon(R.drawable.ic_action_add);
	        	item.setTitle("Add to watchlist");
	        }
	        
	        Intent i = new Intent(getApplicationContext(), RestService.class);
			i.putExtra(RestService.ACTION, RestService.PUT);
			i.putExtra(RestService.OBJECT_TYPE, RestService.OBJECT_TYPE_WATCHLIST);
			i.putExtra(RestService.PARAMS, param);
			this.startService(i);
			Toast.makeText(getApplicationContext(), "Updated watchlist status of anime", Toast.LENGTH_LONG).show();
			
			break;
		case R.id.abs__home:
			return true;
		default:
			// return super.onHandleActionBarItemClick(item, position);
		}
		return true;
	}
	
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.anime_menu, menu);
        
        MenuItem watchmenu = menu.getItem(3); // the watchlist menu item
        
        Cursor anime = this.managedQuery(
    			AnimeProvider.CONTENT_URI, Anime.projection, DBHelper.ANIME_ID+"= ? AND " + DBHelper.ANIME_WATCHLIST + " IS NOT NULL", new String[]{anime_id}, "title ASC");
        
        if(anime.getCount() != 0)
        {
        	watchmenu.setIcon(R.drawable.ic_action_remove);
        	watchmenu.setTitle("Remove from watchlist");
        }else
        {
        	watchmenu.setIcon(R.drawable.ic_action_add);
        	watchmenu.setTitle("Add to watchlist");
        }
        
        
        return super.onCreateOptionsMenu(menu);
    }
	
}
