package com.banan.trakt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.content.*;

public class ImageThreadLoader {
	private static final String TAG = "ImageThreadLoader";

	// Global cache of images.
	// Using SoftReference to allow garbage collector to clean cache if needed
	private final HashMap<String, SoftReference<Bitmap>> Cache = new HashMap<String,  SoftReference<Bitmap>>();

	private final class QueueItem {
		public URL url;
		public ImageLoadedListener listener;
	}
	private final ArrayList<QueueItem> Queue = new ArrayList<QueueItem>();

	private final Handler handler = new Handler();	// Assumes that this is started from the main (UI) thread
	private Thread thread;
	private QueueRunner runner = new QueueRunner();
	private static Context context;

	/** Creates a new instance of the ImageThreadLoader */
	public ImageThreadLoader(Context context) {
		thread = new Thread(runner);
		this.context = context;
	}

	/**
	 * Defines an interface for a callback that will handle
	 * responses from the thread loader when an image is done
	 * being loaded.
	 */
	public interface ImageLoadedListener {
		public void imageLoaded(Bitmap imageBitmap );
	}

	/**
	 * Provides a Runnable class to handle loading
	 * the image from the URL and settings the
	 * ImageView on the UI thread.
	 */
	private class QueueRunner implements Runnable {
		public void run() {
			synchronized(this) {
				while(Queue.size() > 0) {
					final QueueItem item = Queue.remove(0);

					// If in the cache, return that copy and be done
					if( Cache.containsKey(item.url.toString()) && Cache.get(item.url.toString()).get() != null){
						// Use a handler to get back onto the UI thread for the update
						handler.post(new Runnable() {
							public void run() {
								if( item.listener != null ) {
									// NB: There's a potential race condition here where the cache item could get
									//     garbage collected between when we post the runnable and it's executed.
									//     Ideally we would re-run the network load or something.
									SoftReference<Bitmap> ref = Cache.get(item.url.toString());
									if( ref != null ) {
										item.listener.imageLoaded(ref.get());
									}
								}
							}
						});
					} else {
						final Bitmap bmp = readBitmapFromNetwork(item.url);
						if( bmp != null ) {
							Cache.put(item.url.toString(), new SoftReference<Bitmap>(bmp));

							// Use a handler to get back onto the UI thread for the update
							handler.post(new Runnable() {
								public void run() {
									if( item.listener != null ) {
										item.listener.imageLoaded(bmp);
									}
								}
							});
						}

					}

				}
			}
		}
	}

	
	
	public void clearQueue(){
		for(int i = 0; i < Queue.size(); i++){
			Queue.remove(i);
		}
	}
	/**
	 * Queues up a URI to load an image from for a given image view.
	 *
	 * @param uri	The URI source of the image
	 * @param callback	The listener class to call when the image is loaded
	 * @throws MalformedURLException If the provided uri cannot be parsed
	 * @return A Bitmap image if the image is in the cache, else null.
	 */
	public Bitmap loadImage( final String uri, final ImageLoadedListener listener) throws MalformedURLException {
		// If it's in the cache, just get it and quit it
		if( Cache.containsKey(uri)) {
			SoftReference<Bitmap> ref = Cache.get(uri);
			Bitmap tmpbitmap = ref.get();
			if( tmpbitmap != null ) {
				return tmpbitmap;
			}
		}
		Log.i("****", "Queue size: " + Queue.size());
		if(Queue.size() > 20)
			clearQueue();
		QueueItem item = new QueueItem();
		item.url = new URL(uri);
		item.listener = listener;
		Queue.add(item);

		// start the thread if needed
		if( thread.getState() == State.NEW) {
			thread.start();
		} else if( thread.getState() == State.TERMINATED) {
			thread = new Thread(runner);
			thread.start();
		}
		return null;
	}

	/**
	 * Convenience method to retrieve a bitmap image from
	 * a URL over the network. The built-in methods do
	 * not seem to work, as they return a FileNotFound
	 * exception.
	 *
	 * Note that this does not perform any threading --
	 * it blocks the call while retrieving the data.
	 *
	 * @param url The URL to read the bitmap from.
	 * @return A Bitmap image or null if an error occurs.
	 */
	public static Bitmap readBitmapFromNetwork( URL url ) {
		Log.i("ImageThreadLoader","Image: " + url.getPath());
		
		String FILEPATH = url.getPath();
		String FILENAME = FILEPATH.substring(FILEPATH.lastIndexOf("/")+1, FILEPATH.length());
		String SAVEPATH = Environment.getExternalStorageDirectory() + "/Android/data/com.banan.trakt/images/";
		//getPath
		
		File folder = new File(SAVEPATH);
		folder.mkdirs();
		
		
		ContextWrapper cw = new ContextWrapper(context);
		File file = new File(SAVEPATH, FILENAME);
		//File file = new File(FILENAME);
		
		Bitmap bmp = null;
		
		// Check if the file exists in the internal storage
		
		//try {
			
			//FileInputStream in = cw.openFileInput("/sdcard/Android/data/com.groenlid.animelist/images/"+file.toString());
			
			//BufferedInputStream buf = new BufferedInputStream(in);
			bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
			//bmp = BitmapFactory.decodeStream(buf);
			
			/*if (in != null) {
	         	in.close();
	        }
	        if (buf != null) {
	         	buf.close();
	        }*/
			
		//} catch (FileNotFoundException ex) {
		if(bmp == null){	
			//The file does not exists local
			Log.i("ImageThreadLoader","The file does not exists locally: '" + FILENAME + "'");
			InputStream is = null;
			BufferedInputStream bis = null;
			bmp = null;
			try {
				URLConnection conn = url.openConnection();
				conn.connect();
				is = conn.getInputStream();
				bis = new BufferedInputStream(is);
				bmp = BitmapFactory.decodeStream(bis);
				
				try {
					 
				    FileOutputStream out = new FileOutputStream(file);
				       
				    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
				    out.flush();
				    out.close();
				} catch (Exception e) {
						Log.e("ImageThreadLoader","Unable to save image: '"+ FILENAME + "'" + e);
				       e.printStackTrace();
				}
				
			} catch (MalformedURLException e) {
				Log.e(TAG, "Bad ad URL", e);
			} catch (IOException e) {
				Log.e(TAG, "Could not get remote ad image", e);
			} finally {
				try {
					if( is != null )
						is.close();
					if( bis != null )
						bis.close();
				} catch (IOException e) {
					Log.w(TAG, "Error closing stream.");
				}
			}
		/*} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("ImageThreadLoader",""+ e);
			e.printStackTrace();*/
		}
		return bmp;
	}

}
