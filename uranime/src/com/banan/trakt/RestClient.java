package com.banan.trakt;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import com.banan.entities.Constants;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

// Just an example from an old project.. Maybe we can use it?
public class RestClient {
	
	private boolean debug = true;
	private static RestClient instance;
	private Context c;
	
	final String LOGTAG = "RestService";
	
	public RestClient(){}
	
	public static RestClient getInstance(Context c) {
		if(instance == null)
			instance = new RestClient();
		instance.c = c;
		return instance;
	}
	
	public String ReadMethod(String url)
	{
		if(!isNetworkAvailable())
		{
			/*if(c != null)
				Toast.makeText(c, "Could not connect to server. Please check your internet connectivity", Toast.LENGTH_LONG);*/
            return "";
		}
		DefaultHttpClient httpclient = new DefaultHttpClient();  
        HttpGet request = new HttpGet(url);  
        request.addHeader("Authorization", "TRUEREST email="+Constants.getUsername(c)+"&password="+Constants.getPassword(c)); 
        String result = "";
        if(debug)
        	Log.i(LOGTAG,url);
        ResponseHandler<String> handler = new BasicResponseHandler();  
        try {  
           result = httpclient.execute(request, handler);  
         } catch (ClientProtocolException e) {  
        	 result=e.toString(); 
        	 result = "";
        	 /*if(c != null)
        		 Toast.makeText(c, "Could not connect to server. Please check your internet connectivity", Toast.LENGTH_LONG);*/
         } catch (IOException e) {  
             result=e.toString();
             result = "";
             /*if(c != null)
            	 Toast.makeText(c, "Could not connect to server. Please check your internet connectivity", Toast.LENGTH_LONG);*/
         }  
         httpclient.getConnectionManager().shutdown();  
        if(debug)
        	Log.i(LOGTAG, result);  
        
        return result;
	}
	
	public String PostMethod(String postString)
	{
		if(!isNetworkAvailable())
		{
			/*if(c != null)
				Toast.makeText(c, "Could not connect to server. Please check your internet connectivity", Toast.LENGTH_LONG);*/
            return "";
		}
		HttpParams httpParams = new BasicHttpParams();
		int TIMEOUT_MILLISEC = 10000; // = 10 seconds

	    HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT_MILLISEC);
        HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT_MILLISEC);
        HttpClient client = new DefaultHttpClient(httpParams);

        JSONObject obj = new JSONObject();
        try {
        	obj.put("jsonrpc", "2.0");
        
        obj.put("method", "getSomething");

        }
        catch(Exception ex){
        	Log.i(LOGTAG, "Error when creating jsonObject.");
        }

        HttpPost request = new HttpPost("server address");
        try {
            StringEntity entity = new StringEntity(obj.toString());
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            request.addHeader("Authorization", "TRUEREST email="+Constants.getUsername(c)+"&password="+Constants.getPassword(c));
            request.setEntity(entity);

            ResponseHandler<String> handler = new BasicResponseHandler();
            String returnValue = client.execute(request, handler);
                    //returnValue has the return from the server.
            return returnValue;
        } catch (Throwable e) {
            e.printStackTrace();
           /* if(c != null)
            	Toast.makeText(c, "Could not connect to server. Please check your internet connectivity", Toast.LENGTH_LONG);*/
            return "";
        }
	}
	
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
		  = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}
}
