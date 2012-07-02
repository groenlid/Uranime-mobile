package com.banan.anime;

import java.io.File;

import com.banan.providers.DBHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Build;
import android.os.Environment;

public class TraktApplication extends Application
{
		public void onCreate()
		{
			super.onCreate();
			
			File cacheDir;
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ECLAIR_MR1)
			{	
				cacheDir = new File(Environment.getExternalStorageDirectory(),"/Android/data/com.banan.anime/cache/");
				cacheDir.mkdirs();
			}//changeCursor();
			else
				cacheDir = getApplicationContext().getExternalFilesDir(null);
			//File cacheDir = getApplicationContext().getExternalCacheDir();
			//File cacheDir = Environment.getExternalStorageDirectory();
			
			ImageLoader imageLoader = ImageLoader.getInstance();

			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .maxImageWidthForMemoryCache(1024)
            .maxImageHeightForMemoryCache(768)
            .httpConnectTimeout(5000)
            .httpReadTimeout(30000)
            .threadPoolSize(5)
            .threadPriority(Thread.MIN_PRIORITY + 2)
            .discCache(new UnlimitedDiscCache(cacheDir))
            .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
            .build();
			
			// Initialize ImageLoader with created configuration. Do it once.
			imageLoader.init(config);
			DBHelper helper = new DBHelper(this);
			helper.getWritableDatabase();
			helper.close();
		}
}
