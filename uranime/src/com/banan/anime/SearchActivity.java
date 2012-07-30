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
import com.banan.fragments.SearchFragment;
import com.banan.pagers.PagerShowAdapter;
import com.banan.providers.AnimeProvider;
import com.banan.providers.DBHelper;
import com.banan.providers.RestService;
import com.banan.trakt.RestClient;
import com.banan.anime.R;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.nostra13.universalimageloader.core.DecodingType;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class SearchActivity extends BaseActivity implements ActionBar.OnNavigationListener{

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.search_tabs);
		
		SearchPageAdapter adapter = new SearchPageAdapter(getSupportFragmentManager());
		
		
		
		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(adapter);
		
		TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		final SherlockFragmentActivity a = this;
		
		// So the user can get logged in without doing an action.
		Constants.getUserID(this);
		
		indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
		long minTimeSinceUpdate = Calendar.getInstance().getTimeInMillis() - (1000 * 60 * 60);	
		
			public void onPageSelected(int pos) {
				// TODO Auto-generated method stub
				// Position 1 is the Latest page.
				if (pos == 1 && Constants.getLastUpdate(a.getApplicationContext(), SearchFragment.TYPE_LATEST) < minTimeSinceUpdate)
				{
					SearchFragment s = new SearchFragment();
					s.new SearchService(a).execute(Constants.getLatestAnimeURL(10, 0), 
							 SearchFragment.TYPE_LATEST);
				}
				// Position 2 is the Trending page.
				else if(pos == 2 && Constants.getLastUpdate(a.getApplicationContext(), SearchFragment.TYPE_TRENDING) < minTimeSinceUpdate)
				{
					SearchFragment s = new SearchFragment();
					s.new SearchService(a).execute(Constants.getTopAnimeURL(10,0), 
							 SearchFragment.TYPE_TRENDING);
				}
					

			}
			
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		setSupportProgressBarIndeterminateVisibility(false);
		
	}
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_update:
				SearchFragment s = new SearchFragment();
				s.new SearchService(this).execute(Constants.getLatestAnimeURL(10, 0), 
						 SearchFragment.TYPE_LATEST);;
				s.new SearchService(this).execute(Constants.getTopAnimeURL(10,0), 
						 SearchFragment.TYPE_TRENDING);
			Toast.makeText(this, R.string.animelist_refresh, Toast.LENGTH_SHORT)
					.show();

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

		protected final String[] CONTENT = new String[]{ "Search", "Latest", "Trending"};
		
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
