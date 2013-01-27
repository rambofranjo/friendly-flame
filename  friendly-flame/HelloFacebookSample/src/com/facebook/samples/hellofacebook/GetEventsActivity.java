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
    
    private String[][] userAllEvents = null; 
    private int userAttributes = 0; 

	Facebook facebookManager;
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	mHandler = new Handler();
        setContentView(R.layout.geteventsactivity);
        mFQLOutput = (TextView) findViewById(R.id.fqlOutput);

   
        //TODO filter all events where I am the creator? or with JSON Object?
        
        // String query_allEvents2 = "SELECT name, creator FROM event WHERE eid IN (SELECT eid, rsvp_status FROM event_member WHERE uid = me() and start_time > 0)";
        
        // all events of the user who created and get invited to events
        String query_allEvents = "SELECT eid, name, start_time FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid = me() and start_time > 0)"; 
        
        // events where logged in user = creator
        //String query_allEvents = "SELECT eid, name, creator FROM event WHERE creator = me() AND eid IN (SELECT eid FROM event_member WHERE uid = me() and start_time > 0)";
        
        // events where I got invited AND I'm not the creator  --> fkt noch noch nicht, inviter == null, anscheinend privacy problem
        // get rsvp_status, also events where I'm creator 
        //String query_myFriendsEvents = "SELECT eid, inviter, uid, rsvp_status FROM event_member WHERE uid = me() and start_time > 0"; 

        
        // find uid from users who are invited to the event
        //String query_invitedUser = "SELECT uid, eid, name FROM event_member WHERE eid = \"515985715108944\"";
        
        
        
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
			// userAttributes = eventname, date, eid
			all_events = arr.length();
			userAttributes = 2;  	
			userAllEvents = new String[all_events][userAttributes];
			
			
	
			
			
			System.out.println("All Events: " + all_events);
			
			for (int i = 0; i < all_events; i++) {
				
					JSONObject json_obj = arr.getJSONObject(i);
					// eid, name, start_time
					userAllEvents[i][0] = json_obj.getString("eid");
					userAllEvents[i][1] = json_obj.getString("name");
					userAllEvents[i][2] = json_obj.getString("start_time");
					
					//string for testing, doesn't do anything currently as there is no way right now to access rsvp_events
			        //so it does set all values currently to zero except all events
			        //String result = "all events: " + all_events + ", attended: " + user_attending + ", declined: " + user_declined + ", not_replied: " + user_not_replied;
			        setText("eid: "+userAllEvents[i][0] +" name: "+userAllEvents[i][1]+" start_time: "+userAllEvents[i][2]);
				

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
