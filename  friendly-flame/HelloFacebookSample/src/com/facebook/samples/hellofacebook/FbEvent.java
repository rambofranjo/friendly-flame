package com.facebook.samples.hellofacebook;

import org.json.JSONArray;
import org.json.JSONObject;
import android.os.Bundle;
import android.util.Log;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

//class to implement fb methods
public class FbEvent {
	private String id;
	private String title;
	private String startTime;
	private String endTime;
	private String location;
	
	private int all_events = 0;
	private String[][] userAllEvents; 
    private int userAttributes = 0; 
    public String[] userAllEventsResult;
    public String[] returnStringResult; 
    public JSONObject data; 

	public FbEvent() {
    	//Log.d("length:getallevents", Integer.toString(getAllEvents().length));
    	//this.userAllEventsResult = new String[getAllEvents().length];
    	//this.userAllEventsResult = getAllEvents();
    }
	//constructor and getters/setters
	public FbEvent(String _id, String _title, String _sT, String _eT, String _loc){
		this.id = _id;
		this.title = _title;
		this.startTime = _sT;
		this.endTime = _eT;
		this.location = _loc;
	}
	
	public String getId(){
		return id;
	}
 
	public String getTitle(){
		return title;
	}
 
	public String getStartTime(){
		return startTime;
	}
 
	public String getEndTime(){
		return endTime;
	}
 
	public String getLocation(){
		return location;
	}
	
	public String[] getReturnStringResult() {
		return returnStringResult;
	}
	
    //method to execute FQL query, receive all events of the logged in user
	public String[] getAllEvents(){
		String query_allEvents = "SELECT eid, name, start_time FROM event WHERE eid IN " +
				"(SELECT eid FROM event_member WHERE uid = me() and start_time > 0)";
		
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
                    
                    int len = parseUserFromFQLResponse(response).length;
                    userAllEventsResult = new String[len];
                    userAllEventsResult = parseUserFromFQLResponse(response);
                    Log.d("lengthArray", Integer.toString(userAllEventsResult.length));
                    Log.d("userAllEventsResult", userAllEventsResult[2]); 
                }                  
        });
        Request.executeBatchAsync(request);
        
        /*
        String reqstr = request.getParameters().toString();
        Log.d("reqstr", reqstr);
        String ss = Request.executeBatchAsync(request).toString();
        RequestAsyncTask res = Request.executeBatchAsync(request);
        Log.d("ausg", res.toString());
        */
        
		return userAllEventsResult;
	}
	
	//method to filter needed informations from JSON Object
    protected String[] parseUserFromFQLResponse(Response response) {
		try {
			//this will deliver all events where a user took some part in it,
			//attending, declined, not_replied or maybe
			GraphObject go = response.getGraphObject();
			JSONObject jso = go.getInnerJSONObject();
			JSONArray arr = jso.getJSONArray("data");
			
			//get all events by the length of the data array
			// userAttributes = eventname, date, eid
			all_events = arr.length();
			userAttributes = 3;  	
			
			userAllEvents = new String[all_events][userAttributes];
			userAllEventsResult = new String[all_events];
			returnStringResult = new String[all_events];
						
			for (int i = 0; i < all_events; i++) {
				
					JSONObject json_obj = arr.getJSONObject(i);
					// eid, name, start_time
					
					userAllEvents[i][0] = json_obj.getString("eid");
					userAllEvents[i][1] = json_obj.getString("name");
					userAllEvents[i][2] = json_obj.getString("start_time");
					
					/*
					Log.d("test", userAllEvents[i][0]);
					Log.d("test", userAllEvents[i][1]);
					Log.d("test", userAllEvents[i][2]);
					*/
					
					userAllEventsResult[i] = userAllEvents[i][1] + ", " + userAllEvents[i][2];	
					returnStringResult[i] = userAllEventsResult[i];
					Log.d("FbEvent-nachArrayZuweisung", returnStringResult[i]);
					
			}
			
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		Log.d("userAllEventsResultReturn", returnStringResult[2]);
		
		return userAllEventsResult;
	}
} //end of class