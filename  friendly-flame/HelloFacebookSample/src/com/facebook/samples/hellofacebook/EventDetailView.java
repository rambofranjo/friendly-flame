package com.facebook.samples.hellofacebook;

import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.samples.hellofacebook.R;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

//detailview for events
public class EventDetailView extends Activity {

	
	public String eid;
	public String[] userAllEventsResult;
	public String[][] userAllEvents; 
	public String[][] userRSVP;
	public String[] userRSVPResult;
    private int userAttributes = 0; 
	private int all_events = 0;
    public String[] returnStringResult;
    
    public TextView nameText;
    public TextView dateText;
    public TextView locationText;
    public TextView rsvpText;
    
    public String query_allEvents;
    public String query_eventStatus;
		
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_detail_view);
		
		Log.d("EventDetailView", "test");
		
		Bundle extras = getIntent().getExtras();
		eid = extras.getString("eid");
		Log.d("EventDetailView", eid);
		
		query_allEvents = "SELECT eid, name, start_time, location FROM event WHERE eid =  " + eid;
	    query_eventStatus = "SELECT eid, rsvp_status FROM event_member WHERE uid = me() and start_time > 0 and eid =  " + eid;
		
		//setup text fields for GUI
		nameText = (TextView) findViewById(R.id.eventName);
		dateText = (TextView) findViewById(R.id.eventDate);
		locationText = (TextView)findViewById(R.id.location);
		rsvpText = (TextView)findViewById(R.id.rsvp);
		
		
		Log.d("query", query_allEvents);
 		Log.d("query", query_eventStatus);
 		
		//get details for current event
		getAllEvents(query_allEvents);
		getAllEvents(query_eventStatus);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_event_detail_view, menu);
		return true;
	}
	
		//method to execute FQL query, get the event with the given eid
	 	public void getAllEvents(final String query){
	 		
	 		
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
	                     
	                     //parse output from FQL query
	                     
	                     if (query.equals(query_allEvents)) {
	                    	 parseUserFromFQLResponse(response); // 
	                     }
	                     else  {
	                    	 parseUserDetailsFromFQLResponse(response);
	                     }
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
				// userAttributes = eventname, date, eid and location
				all_events = arr.length();
				
				userAttributes = 4;  	
				
				//array to find all informations of event
				userAllEvents = new String[all_events][userAttributes];
				
				//array for string concatenation
				userAllEventsResult = new String[all_events];
				
				//array for final results
				returnStringResult = new String[all_events];
				JSONObject json_obj = arr.getJSONObject(0);

				//save attributes in multidimensional arrays
				userAllEvents[0][0] = json_obj.getString("eid");
				userAllEvents[0][1] = json_obj.getString("name");
				userAllEvents[0][2] = json_obj.getString("start_time");
				userAllEvents[0][3] = json_obj.getString("location");
				
				
				userAllEvents[0][0] = json_obj.getString("eid");
				/**
				Log.d("eid", userAllEvents[0][0]);
				Log.d("name", userAllEvents[0][1]);
				Log.d("start_time", userAllEvents[0][2]);
				Log.d("location", userAllEvents[0][3]);
				**/

				//set textviews with results from FQL query to certain event
				nameText.setText("Event: " + userAllEvents[0][1]);
				dateText.setText("Datum: " + userAllEvents[0][2]);
				
				//hide location if no location was entered
				if (!userAllEvents[0][3].equals("null")) {
					locationText.setText("Ort: " + userAllEvents[0][3]);
				}	
				else {
					locationText.setText("Ort: nicht bekannt");
				}
	
				
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
	    
	  //method to filter needed informations from JSON Object
	    protected void parseUserDetailsFromFQLResponse(Response response) {
			try {
				//this will deliver all events where a user took some part in it,
				//attending, declined, not_replied or maybe
				GraphObject go = response.getGraphObject();
				JSONObject jso = go.getInnerJSONObject();
				JSONArray arr = jso.getJSONArray("data");
				
				//get all events by the length of the data array
				// userAttributes = eventname, date, eid and location
				all_events = arr.length();
				
				userAttributes = 2;  	
				
				//array to find all informations of event
				userRSVP = new String[all_events][userAttributes];
				
				//array for string concatenation
				userRSVPResult = new String[all_events];
				
				//array for final results
				JSONObject json_obj = arr.getJSONObject(0);

				//save attributes in multidimensional arrays
				userRSVP[0][0] = json_obj.getString("rsvp_status");
				String status = "";
				
				if (userRSVP[0][0].equals("attending")) {
					status = "zugesagt";
				} else if (userRSVP[0][0].equals("declined")) {
					status = "abgesagt";
				} else if (userRSVP[0][0].equals("unsure")) {
					status = "unsicher";
				} else {
					status = "noch nicht beantwortet";
				}
				
				
				Log.d("rsvp", userRSVP[0][0]);
				
				rsvpText.setText("Status: " + status);

				//set textviews with results from FQL query to certain event
//				nameText.setText("Event: " + userAllEvents[0][1]);
//				dateText.setText("Datum: " + userAllEvents[0][2]);					
	
				
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}

} //end of class