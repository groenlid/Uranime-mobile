package com.banan.anime;

import com.banan.anime.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;
public class TvdbPreferenceActivity extends PreferenceActivity
{
      @Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.traktpreference);
    	setContentView(R.layout.preferences);
    	Button login = (Button)findViewById(R.id.login);
    	login.setVisibility(View.INVISIBLE);
    }
}
