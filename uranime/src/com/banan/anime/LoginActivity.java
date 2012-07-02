package com.banan.anime;

import com.banan.anime.R;
import android.app.Activity;
import android.os.Bundle;


public class LoginActivity extends Activity{
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		/*
		final EditText user = (EditText)findViewById(R.id.username);
		if(Constants.getUsername(getApplicationContext()) != null && !Constants.getUsername(getApplicationContext()).equals(""))
			user.setText(Constants.getUsername(getApplicationContext()));
		
		
			
		final EditText password = (EditText)findViewById(R.id.password);
		
		if(Constants.getPassword(getApplicationContext()) != null && !Constants.getPassword(getApplicationContext()).equals(""))
			password.setText(Constants.getPassword(getApplicationContext()));	
		
		if(!Constants.getUserID(getApplicationContext()).equals(Constants.NO_ID))
			startActivity(new Intent(getApplicationContext(),DashboardActivity.class));
		
		Button btn = (Button)findViewById(R.id.login);
		btn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				new LoginTask().execute(getApplicationContext(),user.getText().toString(),password.getText().toString());
			}
		});
		
		
	}
	
	private class LoginTask extends AsyncTask<Object, String, Boolean> {
		
		private final String TAG = LoginTask.class.getName();
		
		@Override
		protected Boolean doInBackground(Object... params){
			Context c = (Context) params[0];
			String email = (String) params[1];
			String pwd = (String) params[2];
			
			Constants.setUsername(c,email);
			Constants.setPassword(c,pwd);

			RestClient client = RestClient.getInstance(c);
			
			String response = client.ReadMethod(Constants.REST_LOGIN);
			
			Gson gson = new Gson();
			
			User user = gson.fromJson(response,User.class);
			
			Log.e(TAG,user.data.id);
			
			if(user.data.id != null)
			{
				Constants.setUserID(c,user.data.id);
				return true;
			}
			return false;
		}
		
		@Override
		protected void onPreExecute(){
			
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(Boolean result){
			if(result)
			{
				Toast.makeText(getApplicationContext(), "Logged inn", Toast.LENGTH_SHORT).show();
				Intent i = new Intent(getApplicationContext(),DashboardActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(i);
			}
			else
				Toast.makeText(getApplicationContext(), "Wrong username or password", Toast.LENGTH_SHORT).show();
			
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			Toast.makeText(getApplicationContext(), values[0] , Toast.LENGTH_SHORT).show();
		}*/
	}
}
