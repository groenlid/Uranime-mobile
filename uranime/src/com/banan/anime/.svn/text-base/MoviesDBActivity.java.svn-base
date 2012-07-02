package com.banan.anime;

import com.banan.anime.R;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;

public class MoviesDBActivity extends Activity
{
	private static final int ACTION_BAR_SEARCH=0;
	 private static final int ACTION_BAR_REFRESH=1;
	 @Override
		protected void onCreate(Bundle savedInstanceState)
		{
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.gridview);
				
			Cursor movies = null;/*= managedQuery(
					MovieProvider.CONTENT_URI, Movie.projection, null, null, null);*/

			
			GridView moviesGV =(GridView)findViewById(R.id.gridview);
			ImageAdapter imgAdapter = new ImageAdapter(this, movies);
			moviesGV.setAdapter(imgAdapter);
				
		}
	 
	 
	 public class ImageAdapter extends CursorAdapter
		{
			private LayoutInflater minflater;
			private Cursor c;

			public ImageAdapter(Context context, Cursor c)
			{
				super(context,c);
				this.c = c;
			}

			@Override
			public void bindView(View view, Context context, Cursor c) {
				
				/*String imagestring = Image.getSmallPoster(c.getString(c.getColumnIndexOrThrow(DBHelper.MOVIE_IMAGE_POSTER_COL)));
				String movienamestring = c.getString(c.getColumnIndexOrThrow(DBHelper.MOVIE_TITLE_COL));
				
				AsyncImageView image = (AsyncImageView)view.findViewById(R.id.showImg);
				TextView movieName = (TextView)view.findViewById(R.id.showName);
				
			
				
				movieName.setText(movienamestring);
				image.setUrl(imagestring);*/
				
				return;

			}

			@Override
			public View newView(Context context, Cursor c, ViewGroup viewGroup) {
				 final View view = LayoutInflater.from(context).inflate(R.layout.tvdblist, viewGroup, false);
				 return view;
			}

		}
	 
	 
	 
}
