package com.facebook.samples.hellofacebook;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


public class EventsListActivity extends ListActivity {
	
	
	int counter = 0; 
	//String[] contactList;
	String[] contactList = {"sadfasf", "asdfasf"};

	private String[][] userAllEvents; 
    private int userAttributes = 0; 
    public String[] userAllEventsResult;
	private int all_events = 0;
    public String[] returnStringResult; 
    
    public ListAdapter adapter;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
    	super.onCreate(icicle);
        
        setContentView(R.layout.main_events);
        getAllEvents();
        
        ListView lv = getListView();
        // listening to single list item on click
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
 
              // selected item
              String product = ((TextView) view).getText().toString();
 
              // Launching new Activity on selecting single List Item
              Intent i = new Intent(getApplicationContext(), EventDetailView.class);
              // sending data to new activity
              i.putExtra("product", product);
              startActivity(i);
 
          }
        });
		
        
    }

    /**
     * Creates and returns a list adapter for the current list activity
     * @return
     */
    protected ListAdapter createAdapter(String[] eventsArray)
    {
    	
    	Log.d("eventsArray", Integer.toString(eventsArray.length));
    	
        // Create a simple array adapter (of type string) with the test values
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, eventsArray);

        return adapter;
    }
    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_events, menu);
		return super.onCreateOptionsMenu(menu);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.all_events:
            	startActivity(new Intent(this, EventsListActivity.class));

            	
            	break;

            case R.id.my_events:
            	startActivity(new Intent(this, MyEventsListActivity.class));
                break;
              
            case R.id.create_event:
                Toast.makeText(this, "Create Events", Toast.LENGTH_SHORT).show();
                break;
                
            case R.id.logout:
            	Toast.makeText(this, "logout", Toast.LENGTH_SHORT).show();
            	break; 
            	
        }
        return true;
    }
    
    
    
    
 // methods for getting data 
 	public void getAllEvents(){
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
                     userAllEventsResult = parseUserFromFQLResponse(response); // ergebnis
                    
                     adapter = createAdapter(userAllEventsResult);
                     setListAdapter(adapter);

                     
                 }                  
         }); 
         
         
         Request.executeBatchAsync(request);
         
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
			
			System.out.println("All Events: " + all_events);
			
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
		
		//Log.d("userAllEventsResultReturn", returnStringResult[2]);
		
		return userAllEventsResult;
	}
    

}
