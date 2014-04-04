package dk.bigherman.android.rfkweather;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.content.res.Resources;
import android.util.Log;

public class Downloader 
{
    public final static String DownloadFromUrl(String fileURL) 
    {  
    	String responseBody="Weather information not available at this time"; 
    	try 
        {
			HttpClient httpclient = new DefaultHttpClient();
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000);
		    httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 6000);
		    
			try 
		    {
				HttpGet httpget = new HttpGet(fileURL);
	 
	            Log.d("mytag", "executing request " + httpget.getURI());
	 
	            // Create a response handler
	            ResponseHandler<String> responseHandler = new BasicResponseHandler();
	            responseBody = httpclient.execute(httpget, responseHandler);
	             
	            //
	            Log.i("mytag", "DOWNLOADED " + responseBody);
	        }
	        catch (ClientProtocolException e)
	        {
	            Log.i("net_fail",e.getMessage());
	            //responseBody="Weather information not available at this time";
	        }
			catch(IOException e)
			{
				Log.i("net_fail",e.getMessage());
				//responseBody="Weather information not available at this time";
			}
	        finally
	        {
	            // When HttpClient instance is no longer needed,
	            // shut down the connection manager to ensure
	            // immediate deallocation of all system resources
	            httpclient.getConnectionManager().shutdown();
	        }
        }
		catch(RuntimeException e)
		{
			Log.i("net_fail",e.getMessage());
			//responseBody="Weather information not available at this time";
		}
    	return responseBody;
    }
}

