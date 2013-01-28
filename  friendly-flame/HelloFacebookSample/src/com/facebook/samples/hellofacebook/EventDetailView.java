package com.facebook.samples.hellofacebook;

import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class EventDetailView extends Activity {

	
	String eid;
	public String[] userAllEventsResult;
	public String[][] userAllEvents; 
    private int userAttributes = 0; 
	private int all_events = 0;
    public String[] returnStringResult;
    
    public TextView nameText;
    public TextView dateText;
    public TextView locationText;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_detail_view);
		
		Log.d("EventDetailView", "test");
		
		Bundle extras = getIntent().getExtras();
		eid = extras.getString("eid");
		Log.d("EventDetailView", eid);
		
		nameText = (TextView) findViewById(R.id.eventName);
		dateText = (TextView) findViewById(R.id.eventDate);
		locationText = (TextView)findViewById(R.id.location);
		getAllEvents();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_event_detail_view, menu);
		
		
		return true;
	}
	
	// methods for getting data 
	 	public void getAllEvents(){
	 		String query_allEvents = "SELECT eid, name, start_time, location FROM event WHERE eid =  " + eid;
	 		Log.d("query", query_allEvents);
	 				
	 		
	 		String query = query_allEvents;
	 		
	 		//send FQL request and retrieve informations from JSON object
	         Bundle params = new Bundle();
	         params.putString("q", query);
	         Session session = Session.getActiveSession();
	         Request request = new Request(session,
	             "/fql",                         
	             params,                         
	             HttpMethod.GET,                 
	             new Request.Callback(){         
	                 public void onCompleted(Response response) {
	                     Log.i("TAG", "Result: " + response.toString());
	                     parseUserFromFQLResponse(response); // ergebnis
	                    

	                     
	                 }                  
	         }); 
	         
	         
	         Request.executeBatchAsync(request);
	         
	 	}
	 	
	 	
	 	//method to filter needed informations from JSON Object
	    protected void parseUserFromFQLResponse(Response response) {
			try {
				//this will deliver all events where a user took some part in it,
				//attending, declined, not_replied or maybe
				GraphObject go = response.getGraphObject();
				JSONObject jso = go.getInnerJSONObject();
				JSONArray arr = jso.getJSONArray("data");
				
				//get all events by the length of the data array
				// userAttributes = eventname, date, eid
				all_events = arr.length();
				userAttributes = 4;  	
				
				userAllEvents = new String[all_events][userAttributes];
				userAllEventsResult = new String[all_events];
				returnStringResult = new String[all_events];
				
				System.out.println("All Events: " + all_events);
				
	
					
						JSONObject json_obj = arr.getJSONObject(0);
						// eid, name, start_time
						
						userAllEvents[0][0] = json_obj.getString("eid");
						userAllEvents[0][1] = json_obj.getString("name");
						userAllEvents[0][2] = json_obj.getString("start_time");
						userAllEvents[0][3] = json_obj.getString("location");
						
						Log.d("eid", userAllEvents[0][0]);
						Log.d("name", userAllEvents[0][1]);
						Log.d("start_time", userAllEvents[0][2]);
						Log.d("location", userAllEvents[0][3]);

						
						
						nameText.setText(userAllEvents[0][1]);
						dateText.setText(userAllEvents[0][2]);
						locationText.setText(userAllEvents[0][3]);
						
						
						//userAllEventsResult[i] = userAllEvents[i][1] + ", " + userAllEvents[i][2];
						
	
				
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}

}
