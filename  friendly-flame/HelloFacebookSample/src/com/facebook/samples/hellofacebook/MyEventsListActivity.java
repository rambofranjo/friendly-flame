package com.facebook.samples.hellofacebook;

import org.json.JSONArray;
import org.json.JSONObject;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.samples.hellofacebook.R;

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
import android.widget.AdapterView.OnItemClickListener;

//class to implement the list view of all events
//where the loggend in user was the creator
public class MyEventsListActivity extends ListActivity {
	
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
        
        //get the listView
        ListView lv = getListView();
        
      //listening to single list item on click
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              int position, long id) {
 
              //create new intent depending on position of click
              Intent i = new Intent(getApplicationContext(), EventDetailView.class);
              
              //sending data to new activity with the eid of the event
              i.putExtra("eid", userAllEvents[position][0]);
              Log.d("EventsListActivity-ListView", userAllEvents[position][0]);
              startActivity(i);
          }
        });
    }

  //create the list needed for this view
    protected ListAdapter createAdapter(String[] eventsArray) {
    	
    	Log.d("eventsArray", Integer.toString(eventsArray.length));
        // Create a simple array adapter (of type string)
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, eventsArray);
        return adapter;
    }
    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_events, menu);
		return super.onCreateOptionsMenu(menu);
	}
    
	//listener for menu
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
 		String query_allEvents = "SELECT eid, name, start_time FROM event WHERE creator = me() and eid IN " +
 				"(SELECT eid FROM event_member WHERE uid = me() and start_time > 0)";
 		
 		String query = query_allEvents;
 		
 	    //method to execute FQL query, receive all events where the logged in user
 		//was the creator
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
                     
                     //get all events from the user and add them to the list
                     adapter = createAdapter(userAllEventsResult);
                     setListAdapter(adapter);  
                 }                  
         }); 
         Request.executeBatchAsync(request);
 	}
 	
 	
 	//method to filter needed informations from JSON Object
    protected String[] parseUserFromFQLResponse(Response response) {
		try {
			//search json object for needed data
			GraphObject go = response.getGraphObject();
			JSONObject jso = go.getInnerJSONObject();
			JSONArray arr = jso.getJSONArray("data");
			
			//get all events by the length of the data array
			// userAttributes = eventname, date, eid
			all_events = arr.length();
			userAttributes = 3;  	
			
			//create multidimensional array to store all attributes of FQL query
			userAllEvents = new String[all_events][userAttributes];
			userAllEventsResult = new String[all_events];
			returnStringResult = new String[all_events];
			
			//loop through whole json object			
			for (int i = 0; i < all_events; i++) {
				
					JSONObject json_obj = arr.getJSONObject(i);
					userAllEvents[i][0] = json_obj.getString("eid");
					userAllEvents[i][1] = json_obj.getString("name");
					userAllEvents[i][2] = json_obj.getString("start_time");
					
					/*
					Log.d("test", userAllEvents[i][0]);
					Log.d("test", userAllEvents[i][1]);
					Log.d("test", userAllEvents[i][2]);
					*/
					
					//combine all attributes of one event to a single string for the string array
					userAllEventsResult[i] = userAllEvents[i][1] + "\n " + userAllEvents[i][2];
					returnStringResult[i] = userAllEventsResult[i];
					Log.d("FbEvent-nachArrayZuweisung", returnStringResult[i]);
			}
			
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return userAllEventsResult;
	}

} //end of class