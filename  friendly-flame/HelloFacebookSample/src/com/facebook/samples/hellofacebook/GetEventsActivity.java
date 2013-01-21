package com.facebook.samples.hellofacebook;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.android.Facebook;
import com.facebook.model.GraphObject;


@SuppressWarnings("deprecation")
public class GetEventsActivity extends Activity {
	
    private Handler mHandler;
    private TextView mFQLOutput;
    String events_name = "";
    private int user_attending = 0;
    private int user_declined = 0;
    private int user_not_replied = 0;
    private int all_events = 0;	
    private int all_members_count = 0;
    private int all_attending_count = 0;
    private int all_declined_count = 0;
    private int all_not_replied_count = 0;
    

	Facebook facebookManager;
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	mHandler = new Handler();
        setContentView(R.layout.geteventsactivity);
        mFQLOutput = (TextView) findViewById(R.id.fqlOutput);

   
        //TODO filter all events where I am the creator? or with JSON Object?
        
        // String query_allEvents2 = "SELECT name, creator FROM event WHERE eid IN (SELECT eid, rsvp_status FROM event_member WHERE uid = me() and start_time > 0)";
        String query_allEvents = "SELECT name, creator FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid = me() and start_time > 0)";
        System.out.println("QUERY :" + query_allEvents);
        String query = query_allEvents;
        
        // jeden eventstatus von eingeloggten user 
        //String query = "SELECT eid, rsvp_status FROM event_member WHERE uid = me()";
        
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
                    parseUserFromFQLResponse(response);
                }                  
        }); 
        Request.executeBatchAsync(request);
    }
    
    //getters for all the informations retrieved through FQL query,
    //will be needed for the flame class to calculate outgoingness
    public int getUser_attending() {
		return user_attending;
	}

	public int getUser_declined() {
		return user_declined;
	}

	public int getUser_not_replied() {
		return user_not_replied;
	}

	public int getAll_events() {
		return all_events;
	}

	public int getAll_members_count() {
		return all_members_count;
	}

	public int getAll_attending_count() {
		return all_attending_count;
	}

	public int getAll_declined_count() {
		return all_declined_count;
	}
	public int getAll_not_replied_count() {
		return all_not_replied_count;
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
			all_events = arr.length();
			System.out.println("All Events: " + all_events);
			
			for (int i = 0; i < arr.length(); i++) {
				
				JSONObject json_obj = arr.getJSONObject(i);
				
				//get the number of all attended, declined or not_replied events
				//currently not working, guess we need a multiquery here
				
				/*String rsvp_status = json_obj.getString("rsvp_status");
				if (rsvp_status.equals("attending")) {
					user_attending++;
				} else if (rsvp_status.equals("declined")) {
					user_declined++;
				} else if (rsvp_status.equals("not_replied")) {
					user_not_replied++;
				} else {
				} */
				
				//string for testing, doesn't do anything currently as there is no way right not to access rsvp_events
		        //so it does set all values currently to zero except all events
		        String result = "all events: " + all_events + ", attended: " + user_attending + ", declined: " + user_declined + ", not_replied: " + user_not_replied;
		        setText(result);

			}
			
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	//Method to set the view of the screen, maybe we
    //should change it to view?
    public void setText(final String txt) {
    	mHandler.post(new Runnable() {

			@Override
			public void run() {
				mFQLOutput.setText(txt);
				mFQLOutput.setVisibility(View.VISIBLE);
			}	
    	});
    }
    	
} //end of class
