package com.banan.anime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.banan.entities.Anime;
import com.banan.entities.AnimeRequest;
import com.banan.entities.Constants;
import com.banan.fragments.AnimeFragment;
import com.banan.fragments.AnimeSummaryFragment;
import com.banan.fragments.EpisodeListFragment;
import com.banan.fragments.GenreBrowseFragment;
import com.banan.fragments.LatestAnimeFragment;
import com.banan.fragments.SearchFragment;
import com.banan.pagers.PagerShowAdapter;
import com.banan.providers.AnimeProvider;
import com.banan.providers.DBHelper;
import com.banan.providers.RestService;
import com.banan.providers.TabListener;
import com.banan.trakt.RestClient;
import com.banan.anime.R;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SearchActivity extends BaseActivity implements ActionBar.OnNavigationListener{

	// Variables for the tabs
	TabHost mTabHost;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.anime_tabs);
		setSupportProgressBarIndeterminateVisibility(false);
		
		ActionBar bar = getSupportActionBar();
		
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
	    
	    bar.addTab(bar.newTab()
	            .setText("Search")
	            .setTabListener(new TabListener<SearchFragment>(
	                    this, "search", SearchFragment.class, null)));
	    
	    bar.addTab(bar.newTab()
	            .setText("Latest")
	            .setTabListener(new TabListener<LatestAnimeFragment>(
	                    this, "latest", LatestAnimeFragment.class, null)));
	    
	    bar.addTab(bar.newTab()
	            .setText("Genre")
	            .setTabListener(new TabListener<GenreBrowseFragment>(
	                    this, "genre", GenreBrowseFragment.class, null)));
	    
	    if (savedInstanceState != null) {
	        bar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
	    }
		
		
		// So the user can get logged in without doing an action.
		Constants.getUserID(this);
		
	}
	
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_update:
			LatestAnimeFragment s = (LatestAnimeFragment) LatestAnimeFragment.newInstance(null);
			s.new SearchService(this).execute(Constants.getLatestAnimeURL(10, 0), 
					 LatestAnimeFragment.TYPE_LATEST);
			GenreBrowseFragment g = (GenreBrowseFragment) GenreBrowseFragment.newInstance(null);
			g.new SearchService(this).execute(Constants.REST_GENRE, 
					GenreBrowseFragment.TYPE_GENRE);
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
        getSupportMenuInflater().inflate(R.menu.search_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	public class SearchPageAdapter extends FragmentPagerAdapter{

		public SearchPageAdapter(FragmentManager fm) {
			super(fm);
		}

		protected final String[] CONTENT = new String[]{ "Search", "Latest"};
		
		@Override
		public Fragment getItem(int position) {
			return SearchFragment.newInstance(CONTENT[position % CONTENT.length]);
		}

		@Override
		public int getCount() {
			return CONTENT.length;
		}
		
		/*public void setCount(int count){
			if(count > 0 && count <= 10){
				mCount = count;
				notifyDataSetChanged();
			}
		}*/
		
		public CharSequence getPageTitle(int position) {
			return CONTENT[position % CONTENT.length].toUpperCase();
		}
	}
}
