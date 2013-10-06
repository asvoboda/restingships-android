package com.asvoboda.restingships;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.graphics.Color;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnHoverListener;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.asvoboda.restingships.OnSwipeTouchListener;

public class MainActivity extends Activity implements OnHoverListener {
    /** Called when the activity is first created. */
	// Declare the global text variable used across methods
	private TextView text;
	private String userName;
	volatile private String apiKey;
	private String urlBase = "http://18.111.90.66";
	
	private RelativeLayout layout;
	
	// Called whenever the activity is first created
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//don't actually do this.. its a terrible idea. only because its like 2am
		//never io block the main thread but I refuse to redesign this right now
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		setContentView(R.layout.activity_main);
		// Initialize the layout variable and listen to hover events on it
		layout = (RelativeLayout) findViewById(R.id.layout);
		layout.setOnHoverListener(this);
		//layout.setOnTouchListener(new OnSwipeTouchListener());
		// Initialize the text widget so we can edit the text inside
		text = (TextView) findViewById(R.id.text);
		
		//register new user
		userName = UUID.randomUUID().toString();
		//System.out.println(userName);
		apiKey = postRegisterUser(userName);
		final String urlPart = "/api/move.php";
		
		layout.setOnTouchListener(new OnSwipeTouchListener() {
		    public void onSwipeTop() {
		    	//System.out.println("move up");
		    	doPOST(urlPart, "Up", "Up", apiKey);
		    	
		    }
		    public void onSwipeRight() {
		    	//System.out.println("move right");
		    	doPOST(urlPart, "Right", "Right", apiKey);
		    	
		    }
		    public void onSwipeLeft() {
		    	//System.out.println("move left");
		    	doPOST(urlPart, "Left", "Left", apiKey);
		    	
		    }
		    public void onSwipeBottom() {
		    	//System.out.println("move down");
		    	doPOST(urlPart, "Down", "Down", apiKey);
		    }
		});
		
		
	}
	
	// For whenever a hover event is triggered on an element being listened to
	public boolean onHover(View v, MotionEvent e) {
		// Depending on what action is performed, set the text to that action
		final String urlPart = "/api/status.php";
		switch (e.getActionMasked()) {
		case MotionEvent.ACTION_HOVER_ENTER:
			text.setText("ACTION_HOVER_ENTER");
			if(e.getX() > 800.0 && e.getY() < 1100.0 && e.getY() > 300.0) {
				//System.out.println("hover right");
				doGET(urlPart,"Right", apiKey);
			} else if(e.getX() < 300.0 && e.getY() < 1100.0 && e.getY() > 300.0) {
				//System.out.println("hover left");
				doGET(urlPart, "Left", apiKey);
			} else if(e.getY() > 1100.0 && e.getX() < 800.0 && e.getX() > 300.0) {
				//System.out.println("hover down");
				doGET(urlPart, "Down", apiKey);
			} else if(e.getY() < 400.0 && e.getX() < 800.0 && e.getX() > 300.0) {
				//System.out.println("hover up");
				doGET(urlPart, "Up", apiKey);
			}
			
			break;
		case MotionEvent.ACTION_HOVER_MOVE:
			text.setText("ACTION_HOVER_MOVE");
			break;
		case MotionEvent.ACTION_HOVER_EXIT:
			text.setText("ACTION_HOVER_EXIT");
			break;
		}
		// Along with the event name, also print the XY location of the data
		text.setText(text.getText() + " - X: " + e.getX() + " - Y: " + e.getY());
		return true;
	}
	
	private String postRegisterUser(String handle) {
		String urlPart = "/api/register.php";
		String charset = "UTF-8";
		try {
	    	HttpURLConnection connection = (HttpURLConnection) new URL(urlBase + urlPart).openConnection();
	    	connection.setDoOutput(true); // Triggers POST.
	    	connection.setDoInput(true);
	    	connection.setRequestMethod("POST");  
	    	String query = String.format("handle=%s", 
	    		     URLEncoder.encode(handle, charset));
	    	
	    	connection.setRequestProperty("Accept-Charset", charset);
	    	connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
	    	OutputStream output = connection.getOutputStream();
	    	try {
	    	     output.write(query.getBytes(charset));
	    	} finally {
	    	     try { output.close(); } catch (IOException logOrIgnore) {}
	    	}
	    	InputStream response = connection.getInputStream();
	    	String res = convertStreamToString(response);
	    	JSONObject jsonObj = new JSONObject(res);
	    	return (String) jsonObj.get("api_key");
			
		} catch (Exception ex){
	        System.out.println("error: " + ex.getMessage() + ex.toString());
	    }
		
		return "";
	}
	
	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	private void doGET(String urlPart, String focus_scan, String api_key){
	    String charset = "UTF-8";
	    try {
	    	String query = String.format("focus_scan=%s&api_key=%s", 
	    		     URLEncoder.encode(focus_scan, charset), 
	    		     URLEncoder.encode(api_key, charset));
	    	String url = urlBase + urlPart + "?" + query;

	    	HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	    	connection.setRequestProperty("Accept-Charset", charset);

	    	//InputStream response = connection.getInputStream();
	    	InputStream response = new URL(url).openStream();
	    	
	    	String res = convertStreamToString(response);
	    	System.out.println(res);
	    	JSONObject json  = new JSONObject(res);
	    	checkReturnResultAndUpdateBackground(json);

	    } catch (Exception ex){
	        System.out.println("error: " + ex.getMessage());
	    }
	} 
	
	private void doPOST(String urlPart, String direction, String focus_scan, String apiKey){
	    String charset = "UTF-8";
	    try {
	    	
	    	HttpURLConnection connection = (HttpURLConnection) new URL(urlBase + urlPart).openConnection();
	    	
	    	connection.setDoInput(true);
    		connection.setDoOutput(true); // Triggers POST.
    		connection.setRequestMethod("POST");  
	    	String query = String.format("focus_scan=%s&api_key=%s&direction=%s", 
	    		     URLEncoder.encode(focus_scan, charset), 
	    		     URLEncoder.encode(apiKey, charset),
	    		     URLEncoder.encode(direction, charset));
	    	
	    	connection.setRequestProperty("Accept-Charset", charset);
	    	connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
	    	connection.setRequestProperty("Content-Length", "" + Integer.toString(query.getBytes().length));
	    	OutputStream output = connection.getOutputStream();
	    	try {
	    	     output.write(query.getBytes(charset));
	    	} finally {
	    	     try { output.close(); } catch (IOException logOrIgnore) {}
	    	}
	    	InputStream response = connection.getInputStream();
	    	
	    	String res = convertStreamToString(response);
	    	JSONObject json = new JSONObject(res);
	    	checkReturnResultAndUpdateBackground(json);

	    } catch (Exception ex){
	        System.out.println("error: " + ex.getMessage());
	    }
	} 
	
	private void checkReturnResultAndUpdateBackground(JSONObject json) {
		try {
	    	if(((JSONArray)json.get("asteroids")).length() > 0) {
	    		layout.setBackgroundColor(Color.RED);
	    		text.setText("ASTEROID AHEAD");
	    	} else if(((JSONArray)json.get("ships")).length() > 0) { 
	    		layout.setBackgroundColor(Color.YELLOW);
	    		text.setText("ANOTHER SHIP AHEAD");
	    	} else if(((JSONArray)json.get("starStuff")).length() > 0) {
	    		layout.setBackgroundColor(Color.GREEN);
	    		text.setText("STAR STUFF AHEAD");
	    	} else {
	    		layout.setBackgroundColor(Color.WHITE);
	    	}
		} catch(JSONException e) {
			System.out.println("error: " + e.toString());
		}
	}
}