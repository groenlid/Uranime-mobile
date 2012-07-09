package com.banan.anime;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.banan.pagers.PagerShowAdapter;
import com.banan.providers.RestService;
import com.banan.anime.R;
import com.banan.entities.Constants;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

public class AnimeActivity extends BaseActivity implements ActionBar.OnNavigationListener {
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.anime_tabs);
		
		PagerShowAdapter adapter = new PagerShowAdapter(getSupportFragmentManager());
		
		// So the user can get logged in without doing an action.
		Constants.getUserID(this);
		
		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(adapter);
		
		TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		
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
        return super.onCreateOptionsMenu(menu);
    }
	
}
