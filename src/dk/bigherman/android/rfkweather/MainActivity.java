package dk.bigherman.android.rfkweather;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Calendar;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

//Activity to fetch and display weather information from Randers Flyveklubs Weather Station
public class MainActivity extends Activity 
{
	Timer timer;
	//Use a boolean value to keep track of whether the App is in the foreground and whether it is worthwhile fetching form the network 
	boolean active = true;
	boolean language_english = true;
	//Use a boolean value to keep track of whether the latest fetched file is fresher than 15 minutes
	boolean time_expired = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		//StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		//StrictMode.setThreadPolicy(policy);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		final Handler handler = new Handler();
		timer = new Timer("weatherFetch", true);
		//Attach asynchronous network task to a timer
	    TimerTask updateWeatherfromServer = new TimerTask() 
	    {       
	        public void run() 
	        {
	            handler.post(new Runnable() 
	            {
	                public void run() 
	                {       
	                    try 
	                    {
	                        AsynchNetworkFetch networkFetch = new AsynchNetworkFetch();
	                        networkFetch.execute();
	                    } 
	                    catch (Exception e) 
	                    {
	                        e.printStackTrace();
	                    }
	                }
	            });
	        }
	    };
	    timer.schedule(updateWeatherfromServer, 0, 15000); //execute every 15 s
	}

	@Override
    public void onPause() 
	{
		active = false;
		
		super.onPause();
    }
	
	@Override
    public void onResume() 
	{		
		active = true;
		
		super.onResume();
    }
	
	private class AsynchNetworkFetch extends AsyncTask<String, Void, String> 
	{
		String download;
		
		//Start background network task
		@Override
        protected String doInBackground(String... params) 
		{
			if(active)
			{
				download = Downloader.DownloadFromUrl(getResources().getString(R.string.randers_weather_server));
	    		Log.d("mytag", download);
			}
			else
			{
				Log.d("mytag", "Still running");
			}

    		return "Executed";
        }      

        //Action on completion of background network task 
		@Override
		protected void onPostExecute(String result) 
        {
        	if(active)
        	{
				//If network connection error
        		if(download.contentEquals("Weather information not available at this time"))
	    		{					
        			if(language_english)
    				{
        				Toast.makeText(getApplicationContext(), getResources().getString(R.string.failure_message_eng), Toast.LENGTH_LONG).show();
    				}
	    			else
	    			{
	    				Toast.makeText(getApplicationContext(), getResources().getString(R.string.failure_message_dan), Toast.LENGTH_LONG).show();
	    			} 
	    		}
	    		//If file is downloaded
        		else
	    		{
	    			try
	    			{
	        			//Read individual elements from text file into an array
		    			String[] weatherArray;  
		    		    String delimiter = " ";  
		    		    weatherArray = download.split(delimiter);
		    		    
		    		    //Split date (day/month/year) into elements
		    		    String[] dateArray;  
		    		    delimiter = "/";  
		    		    dateArray = weatherArray[74].split(delimiter);
		    		    int year = Integer.parseInt(dateArray[2]);
		    		    int month = Integer.parseInt(dateArray[1])-1;
		    		    int day = Integer.parseInt(dateArray[0]);
		    		    //Find hours, minutes and seconds for report from text file elements 
		    		    int hour = Integer.parseInt(weatherArray[29]);
		    		    int minute = Integer.parseInt(weatherArray[30]);
		    		    int second = Integer.parseInt(weatherArray[31]);
		    			
		    			//Calculate time of report in UNIX format and get current UNIX time  
		    		    long unixTimeOfReport = componentTimeToTimestamp(year, month, day, hour, minute, second);
		    			long currentTime = System.currentTimeMillis();
		    			
		    			//Check if report is timestamped more than 15 minutes before current time
		    			if ((long)(currentTime - unixTimeOfReport)>900000)
		    			{
		    				time_expired = true;
		    				
		    				setInvalidUI();
		    			}
		    			else
		    			{
		    				time_expired = false;
		    				
		    				setActiveUI(weatherArray);
		    			}
	    			}
	    			catch (Exception ex)
	    			{
	    				if(language_english)
	    				{
	        				Toast.makeText(getApplicationContext(), getResources().getString(R.string.failure_message_eng), Toast.LENGTH_LONG).show();
	    				}
		    			else
		    			{
		    				Toast.makeText(getApplicationContext(), getResources().getString(R.string.failure_message_dan), Toast.LENGTH_LONG).show();
		    			} 
	    			}
	    		}
        	}
        }

        @Override
        protected void onPreExecute() 
        {
        }

        @Override
        protected void onProgressUpdate(Void... values) 
        {
        }
	}   
	
	void setInvalidUI()
	{
		TextView windDirectionUI = (TextView) findViewById(R.id.textWindDirectionValue);
		windDirectionUI.setText(R.string.not_available);
		
		TextView windSpeedUI = (TextView) findViewById(R.id.textWindSpeedValue);
		windSpeedUI.setText(R.string.not_available);
		
		TextView pressureQFE_UI = (TextView) findViewById(R.id.textPressureQFEValue);
		pressureQFE_UI.setText(R.string.not_available);
		
		TextView cloudbaseUI = (TextView) findViewById(R.id.textCloudbaseValue);
		cloudbaseUI.setText(R.string.not_available);
		
		TextView temperatureUI = (TextView) findViewById(R.id.textTemperatureValue);
		temperatureUI.setText(R.string.not_available);
		
		TextView dewPointUI = (TextView) findViewById(R.id.textDewPointValue);
		dewPointUI.setText(R.string.not_available);	
		
		TextView timeOfUpdateUIValue = (TextView) findViewById(R.id.textTimeOfUpdateValue);
		if(language_english)
		{
			timeOfUpdateUIValue.setText(R.string.time_expired_eng);
		}
		else
		{
			timeOfUpdateUIValue.setText(R.string.time_expired_dan);
		}
	}
	
	void setActiveUI(String[] weatherArray)
	{
		String windDirection = weatherArray[3] + " deg";
		TextView windDirectionUI = (TextView) findViewById(R.id.textWindDirectionValue);
		windDirectionUI.setText(windDirection);
		
		String windSpeed = Math.round(Float.parseFloat(weatherArray[1])) + " knots";
		TextView windSpeedUI = (TextView) findViewById(R.id.textWindSpeedValue);
		windSpeedUI.setText(windSpeed);
		
		String pressureQFE = Math.round(Float.parseFloat(weatherArray[6])) + " hPa";
		TextView pressureQFE_UI = (TextView) findViewById(R.id.textPressureQFEValue);
		pressureQFE_UI.setText(pressureQFE);
		
		String cloudbase = Math.round(Float.parseFloat(weatherArray[73])/100)*100 + " feet";
		TextView cloudbaseUI = (TextView) findViewById(R.id.textCloudbaseValue);
		cloudbaseUI.setText(cloudbase);
		
		String temperature = Math.round(Float.parseFloat(weatherArray[4])) + " Celsius";
		TextView temperatureUI = (TextView) findViewById(R.id.textTemperatureValue);
		temperatureUI.setText(temperature);
		
		String dewPoint = Math.round(Float.parseFloat(weatherArray[72])) + " Celsius";
		TextView dewPointUI = (TextView) findViewById(R.id.textDewPointValue);
		dewPointUI.setText(dewPoint);		
		
		String timeOfUpdate;
		TextView timeOfUpdateUIValue = (TextView) findViewById(R.id.textTimeOfUpdateValue);
		timeOfUpdate = weatherArray[29] + ":" + weatherArray[30] + ":" + weatherArray[31] + " L";
		timeOfUpdateUIValue.setText(timeOfUpdate);
	}
	
	//Calculate UNIX timestamp from elemental input
	long componentTimeToTimestamp(int year, int month, int day, int hour, int minute, int second)
	{
		Calendar c = Calendar.getInstance();
		c.set(year, month, day, hour, minute, second);
		
		return c.getTimeInMillis();
	}

	//Localise UI text labels and UI time of update, if expired 
	private void updateLanguage()
	{
		if(language_english)
		{
			TextView windDirectionUI = (TextView) findViewById(R.id.textWindDirection);
			windDirectionUI.setText(R.string.wind_direction_eng);
			
			TextView windSpeedUI = (TextView) findViewById(R.id.textWindSpeed);
			windSpeedUI.setText(R.string.wind_speed_eng);
			
			TextView pressureQFE_UI = (TextView) findViewById(R.id.textPressureQFE);
			pressureQFE_UI.setText(R.string.air_pressure_qfe_eng);
			
			TextView cloudbaseUI = (TextView) findViewById(R.id.textCloudbase);
			cloudbaseUI.setText(R.string.cloudbase_eng);
			
			TextView temperatureUI = (TextView) findViewById(R.id.textTemperature);
			temperatureUI.setText(R.string.temperature_eng);
			
			TextView dewPointUI = (TextView) findViewById(R.id.textDewPoint);
			dewPointUI.setText(R.string.dew_point_eng);

			TextView timeOfUpdateUI = (TextView) findViewById(R.id.textTimeOfUpdate);
			timeOfUpdateUI.setText(R.string.time_of_update_eng);
			
			TextView timeOfUpdateUIValue = (TextView) findViewById(R.id.textTimeOfUpdateValue);
			if(time_expired)
			{
				timeOfUpdateUIValue.setText(R.string.time_expired_eng);
			}
		}
		else
		{
			TextView windDirectionUI = (TextView) findViewById(R.id.textWindDirection);
			windDirectionUI.setText(R.string.wind_direction_dan);
			
			TextView windSpeedUI = (TextView) findViewById(R.id.textWindSpeed);
			windSpeedUI.setText(R.string.wind_speed_dan);
			
			TextView pressureQFE_UI = (TextView) findViewById(R.id.textPressureQFE);
			pressureQFE_UI.setText(R.string.air_pressure_qfe_dan);
			
			TextView cloudbaseUI = (TextView) findViewById(R.id.textCloudbase);
			cloudbaseUI.setText(R.string.cloudbase_dan);
			
			TextView temperatureUI = (TextView) findViewById(R.id.textTemperature);
			temperatureUI.setText(R.string.temperature_dan);
			
			TextView dewPointUI = (TextView) findViewById(R.id.textDewPoint);
			dewPointUI.setText(R.string.dew_point_dan);

			TextView timeOfUpdateUI = (TextView) findViewById(R.id.textTimeOfUpdate);
			timeOfUpdateUI.setText(R.string.time_of_update_dan);
			
			TextView timeOfUpdateUIValue = (TextView) findViewById(R.id.textTimeOfUpdateValue);
			if(time_expired)
			{
				timeOfUpdateUIValue.setText(R.string.time_expired_dan);
			}
		}
		return;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_english, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) 
	{
	    menu.clear();

	    if (language_english) 
	    {
	    	getMenuInflater().inflate(R.menu.main_english, menu);
	    }
	    else 
	    { 
	    	getMenuInflater().inflate(R.menu.main_danish, menu);
	    }

	    return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
	{
        switch (item.getItemId()) 
        {
	        case R.id.action_language:
	        	/*if(language_english)
        		{
        			language_english = false;
        			updateLanguage();
        		}
	        	else
	        	{
	        		language_english = true;
	        		updateLanguage();
	        	}*/
	        	language_english=!language_english;
	        	updateLanguage();
	        	return true;
	        	
	        case R.id.action_help:
	        	Intent i = new Intent(getApplicationContext(), HelpActivity.class);
	        	i.putExtra("language_english", language_english);
	        	startActivity(i);
	        	return true;
	        
	        default:
	        	return super.onOptionsItemSelected(item);
        }
    }

}
