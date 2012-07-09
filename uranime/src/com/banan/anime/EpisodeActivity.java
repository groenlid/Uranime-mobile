package com.banan.anime;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.banan.entities.Constants;
import com.banan.entities.Episode;
import com.banan.fragments.EpisodeFragment;
import com.banan.providers.DBHelper;
import com.banan.providers.EpisodeProvider;
import com.banan.providers.RestService;
import com.viewpagerindicator.TitlePageIndicator;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class EpisodeActivity extends BaseActivity implements ActionBar.OnNavigationListener{
	private String anime_id;
	private ViewPager pager;
	private Cursor episodes;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.episode_tabs);
		
		anime_id = getIntent().getStringExtra("anime_id");
		String episode_number = getIntent().getStringExtra("episode_number");
		boolean special = getIntent().getBooleanExtra("special_episode", false);
		
		episodes = managedQuery(
				EpisodeProvider.CONTENT_URI, Episode.projection, DBHelper.ANIME_ID + " = " + anime_id , null, DBHelper.EPISODE_NUMBER_COL + " DESC");
		
		
		// Find the current episode to show.
		int i = 0;
		for(episodes.moveToFirst(); !episodes.isAfterLast(); episodes.moveToNext()) {
			if((episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_NUMBER_COL)) == Integer.parseInt(episode_number))
					&& (episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_SPECIAL_COL))>0) == special)
				break;
			i++;
		}
		TitleFragmentAdapter adapter = new TitleFragmentAdapter(episodes, getSupportFragmentManager());
		
		pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(adapter);
		
		TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		indicator.setCurrentItem(i);
		
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	
		switch(item.getItemId()) {
		case R.id.menu_add:
			int pos = pager.getCurrentItem();
			if(episodes.moveToPosition(pos))
			{
				ArrayList<String> param = new ArrayList<String>();
				param.add(""+episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_ID_COL)));
				
				param.add(Constants.timeToString(null));
				//param.add("true"); // Should rather be timestamp.
				
				/** TODO: Convert to timestamp */
				
				Intent i = new Intent(this, RestService.class);
				i.putExtra(RestService.ACTION, RestService.PUT);
				i.putExtra(RestService.OBJECT_TYPE, RestService.OBJECT_TYPE_EPISODE);
				i.putExtra(RestService.PARAMS, param);
				startService(i);
				pager.getAdapter().notifyDataSetChanged();
			}
			break;
			case R.id.menu_preferences:
				startActivity(new Intent(this, TvdbPreferenceActivity.class));
				break;
			case R.id.menu_update:
				ArrayList<String> param = new ArrayList<String>();
				param.add(anime_id);
				Intent i = new Intent(this, RestService.class);
				i.putExtra(RestService.ACTION, RestService.GET);
				i.putExtra(RestService.OBJECT_TYPE, RestService.OBJECT_TYPE_EPISODE);
				i.putExtra(RestService.PARAMS, param);
				startService(i);
	            
	            Toast.makeText(this, R.string.animelist_refresh, Toast.LENGTH_SHORT).show();
	            //Log.e("page",""+pager.getCurrentItem());
				break;
			 case R.id.abs__home:
	                final Intent intent = new Intent(this, AnimeListActivity.class);
	                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                startActivity(intent);
	                return true;
			default:
				//return super.onHandleActionBarItemClick(item, position);
		}
		return true;
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.episode_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
	
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		return false;
	}
	
	private class TitleFragmentAdapter extends FragmentPagerAdapter{
		private Cursor episodes;
		
		public TitleFragmentAdapter(Cursor episodes,FragmentManager fm) {
			super(fm);
			this.episodes = episodes;
		}

		public CharSequence getPageTitle(int position) {
			if(episodes.moveToPosition(position))
				if(episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_SPECIAL_COL))>0)
					return "SPECIAL "+episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_NUMBER_COL));
				else
					return ""+episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_NUMBER_COL));
			return "NO_POS";
		}

		@Override
		public Fragment getItem(int position) {
			if(episodes.moveToPosition(position))
				return EpisodeFragment.newInstance(episodes.getInt(episodes.getColumnIndexOrThrow(DBHelper.EPISODE_ID_COL)));
			else
				return EpisodeFragment.newInstance(0);
		}
		
		public int getItemPosition(Object object) {
		    return POSITION_NONE;
		}

		@Override
		public int getCount() {
			return episodes.getCount();
		}
	}
}
