package com.banan.anime;

import java.util.ArrayList;
import java.util.Calendar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.banan.anime.R;
import com.banan.entities.Anime;
import com.banan.entities.Constants;
import com.banan.entities.Episode;
import com.banan.fragments.CalendarFragment;
import com.banan.fragments.EpisodeFragment;
import com.banan.providers.AnimeProvider;
import com.banan.providers.DBHelper;
import com.banan.providers.EpisodeProvider;
import com.viewpagerindicator.TitlePageIndicator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.LayoutParams;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView.ScaleType;

/**
 * This activity handles the calendar page on trakt, where upcoming episodes can
 * be viewed
 * 
 * @author aliyhuss
 * 
 */
public class CalendarActivity extends BaseActivity implements ActionBar.OnNavigationListener {
	
	private ViewPager pager;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.calendar_tabs);

	
		TitleFragmentAdapter adapter = new TitleFragmentAdapter(getSupportFragmentManager());
		
		pager = (ViewPager)findViewById(R.id.calendar_pager);
		pager.setAdapter(adapter);
		
		TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.calendar_indicator);
		indicator.setViewPager(pager);
		indicator.setCurrentItem((int)Constants.CALENDAR_WEEKS/2);
		
		
	}
	
	private class TitleFragmentAdapter extends FragmentPagerAdapter{
		
		public TitleFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		public CharSequence getPageTitle(int position) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.WEEK_OF_YEAR, position-(Constants.CALENDAR_WEEKS / 2));
			return "Week " + cal.get(Calendar.WEEK_OF_YEAR);
		}

		@Override
		public Fragment getItem(int position) {
				return CalendarFragment.newInstance(position-(Constants.CALENDAR_WEEKS / 2));
		}
		
		public int getItemPosition(Object object) {
		    return POSITION_NONE;
		}

		@Override
		public int getCount() {
			return Constants.CALENDAR_WEEKS;
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}
}
