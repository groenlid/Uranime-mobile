package com.banan.trakt;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.banan.entities.Constants;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

// Just an example from an old project.. Maybe we can use it?
public class RestClient {
	
	public static boolean debug = false;
	private static RestClient instance;
	private Context c;
	
	final static String LOGTAG = "RestService";
	
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
        
        String authHeader = Constants.getUsername(c)+":"+Constants.getPassword(c);
        
        request.addHeader("Authorization", authHeader); 
        String result = "";
        if(debug){
        	Log.i(LOGTAG,url);
        	Log.i(LOGTAG, authHeader);
        }
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
	
	private static JSONObject postObject = null;
	
	public static JSONObject createJSONObject()
	{
		RestClient.postObject = new JSONObject();
		return RestClient.postObject;
	}
	
	public static JSONObject addColumn(String column, JSONObject content)
	{
		if(RestClient.postObject == null)
			return null;
		try {
			RestClient.postObject.put(column, content);
			return RestClient.postObject;
		}catch(Exception ex){
			Log.i(LOGTAG, "Error when creating jsonObject");
		}
		return null;
	}
	
	public static JSONObject addColumn(String column, JSONArray content)
	{
		if(RestClient.postObject == null)
			return null;
		try {
			RestClient.postObject.put(column, content);
			return RestClient.postObject;
		}catch(Exception ex){
			Log.i(LOGTAG, "Error when creating jsonObject");
		}
		return null;
	}
	
	public static JSONObject addNullColumn(String column) 
	{
		if(RestClient.postObject == null)
			return null;
		
		try {
			
			RestClient.postObject.put(column, RestClient.postObject.NULL);
			return RestClient.postObject;
		}catch(Exception ex){
			Log.i(LOGTAG, "Error when creating jsonObject");
		}
		return null;
	}
	
	public static JSONObject addColumn(String column, String content)
	{
		if(RestClient.postObject == null)
			return null;
		
		try {
			RestClient.postObject.put(column, content);
			return RestClient.postObject;
		}catch(Exception ex){
			Log.i(LOGTAG, "Error when creating jsonObject");
		}
		return null;
	}
	
	public String PutMethod(String url, JSONObject postObject)
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
        String response = "";
        HttpPut request = new HttpPut(url);
        try {
        	
        	String postString = (RestClient.postObject == null) ? "" : RestClient.postObject.toString();
            StringEntity entity = new StringEntity(postString);
            if(debug){
            	Log.i(LOGTAG, url);
            	Log.i(LOGTAG, postString); 
            }
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            request.addHeader("Authorization", Constants.getUsername(c)+":"+Constants.getPassword(c));
            request.setEntity(entity);


            HttpResponse returnValue = client.execute(request);
            int statusCode = returnValue.getStatusLine().getStatusCode();
            HttpEntity entityResponse = returnValue.getEntity();
            //InputStream responseBody = entityResponse.getContent();
            
            response = EntityUtils.toString(entityResponse);
            
            //response = responseBody.toString();
            
            if (statusCode != 200) {
                // responseBody will have the error response
            }
                    //returnValue has the return from the server.
            if(debug)
            	Log.i(LOGTAG, response); 
            
            return response;
        
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
