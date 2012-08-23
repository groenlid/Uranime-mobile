package com.banan.anime;

import java.io.File;

import com.banan.providers.DBHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build;
import android.os.Environment;

public class TraktApplication extends Application
{
		@SuppressLint("NewApi")
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

			/*ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .maxImageWidthForMemoryCache(1024)
            .maxImageHeightForMemoryCache(768)
            .httpConnectTimeout(5000)
            .httpReadTimeout(30000)
            .threadPoolSize(5)
            .threadPriority(Thread.MIN_PRIORITY + 2)
            .discCache(new UnlimitedDiscCache(cacheDir))
            .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
            .build();*/
			DisplayImageOptions options = new DisplayImageOptions.Builder()
			.cacheInMemory()
			.cacheOnDisc()
	        .imageScaleType(ImageScaleType.POWER_OF_2)
	        .build();
			
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .memoryCacheExtraOptions(480, 800)
            .threadPoolSize(5)
            .threadPriority(Thread.MIN_PRIORITY + 2)
            .denyCacheImageMultipleSizesInMemory()
            .discCache(new UnlimitedDiscCache(cacheDir)) // You can pass your own disc cache implementation
            .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
            //.imageDownloader(new DefaultImageDownloader(5 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)
            .defaultDisplayImageOptions(options)
            //.enableLogging()
            .build();
			
			// Initialize ImageLoader with created configuration. Do it once.
			imageLoader.init(config);
			DBHelper helper = new DBHelper(this);
			helper.getWritableDatabase();
			helper.close();
			
		}
}
