package com.facebook.samples.hellofacebook;

//class for showing all events of the user, including the ones
//where he was invited and those where he is the creator himself
import org.json.JSONArray;
import org.json.JSONObject;

import com.android.future.usb.UsbManager;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.samples.hellofacebook.R;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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


public class EventsListActivity extends ListActivity {
	
	public String[][] userAllEvents; 
    private int userAttributes = 0; 
    public String[] userAllEventsResult;
	private int all_events = 0;
    public String[] returnStringResult; 
    private String[][] userRSVP;
	private String[] userRSVPResult;
    public ListAdapter adapter;
	public int len = 0; 
	public FbEvent fb = new FbEvent();
	int attended = 0;
	int declined = 0;
	int not_replied = 0;
	int unsure = 0;
	String query_allEvents = "SELECT eid, name, start_time FROM event WHERE eid IN " +
				"(SELECT eid FROM event_member WHERE uid = me() and start_time > 0)";
	String query_RSVP = "SELECT eid, rsvp_status FROM event_member WHERE uid = me() and start_time > 0";

 final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			 if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
			    unbindService(arduinoConnection);
			    Log.d("MainActivity","Service unbound");
				finish();
			}
		}
	}; 
	
    private Handler messageHandler = new Handler();
    private ArduinoService.MyServiceBinder arduinoBinder = null;
	private ServiceConnection arduinoConnection = 
		new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			arduinoBinder = (ArduinoService.MyServiceBinder) arg1;
			//arduinoBinder.setRunnable(new RunnableForArduinoService());
			arduinoBinder.setActivityCallbackHandler(messageHandler);
			Log.d("MainActivity","Arduino Service is connected!");				
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {		
			Log.d("MainActivity","Arduino Service is disconnected!");	
		}

	};
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
    	super.onCreate(icicle);
        
        setContentView(R.layout.main_events);
        getAllEvents(query_allEvents);
        getAllEvents(query_RSVP);
        
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
        
        IntentFilter filter = new IntentFilter("com.google.android.BeyondTheDesktop.action.USB_PERMISSION");
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);	
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
    
    
    
    
    //method to execute FQL query, receive all events of the logged in user
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
                     
                     if (query.equals(query_allEvents)) {
	                     len = parseUserFromFQLResponse(response).length;
	                     
	                     //create new array depending on number of events
	                     userAllEventsResult = new String[len];
	                     userAllEventsResult = parseUserFromFQLResponse(response);
	                    
	                     //get all events from the user and add them to the list
	                     adapter = createAdapter(userAllEventsResult);
	                     setListAdapter(adapter);   
                     } else {
                    	 parseUserRSVPFromFQLResponse(response);
                     }
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
    
    public void parseUserRSVPFromFQLResponse(Response response) {
    	try {
			//this will deliver all events where a user took some part in it,
			//attending, declined, not_replied or maybe
			GraphObject go = response.getGraphObject();
			JSONObject jso = go.getInnerJSONObject();
			JSONArray arr = jso.getJSONArray("data");
			
			//get all events by the length of the data array
			// userAttributes = eventname, date, eid and location
			all_events = arr.length();
			Log.d("number of events :", Integer.toString(all_events));
			userAttributes = 1;  	
			
			//array to find all informations of event
			userRSVP = new String[all_events][userAttributes];
			
			//array for string concatenation
			userRSVPResult = new String[all_events];
			
			//array for final results
			for (int i = 0; i < all_events; i++) {
				JSONObject json_obj = arr.getJSONObject(i);
	
				//save attributes in multidimensional arrays
				userRSVP[i][0] = json_obj.getString("rsvp_status");
			
				String rsvp = userRSVP[i][0];
				
				if (rsvp.equals("attending")) {
					attended++;
				} else if (rsvp.equals("declined")) {
					declined++;
				} else if (rsvp.equals("unsure")) {
					unsure++;
				} else if (rsvp.equals("not_replied")){
					not_replied++;
				}
				
			}
			
			//calculate outgoingness and return byte value for Arduino
			byte[] buffer = calculateOutgoingness(all_events, attended, declined, unsure, not_replied);
			Log.d("outgoingness: ", Byte.valueOf(buffer[0]).toString());
			arduinoBinder.sendMessageToArduino(buffer);

			
		} catch(Throwable t) {
			t.printStackTrace();
		}
    }
    
    @Override
    protected void onResume() {
    	
    	final Intent netzwerkIntent = new Intent(getApplicationContext(), ArduinoService.class);
		bindService(netzwerkIntent, arduinoConnection, Context.BIND_AUTO_CREATE);
		    
        super.onResume();
    }
    
    public byte[] calculateOutgoingness(int all_events, int attending, int declined, int unsure, int not_replied) {
    	
    	//outgoingness is a color that ranges between yellow and black
    	//yellow being very outgoing, black not outgoing at all
    	    	
    	
    	float outgoingness = ((attending - declined - (not_replied/2)) / all_events * 2) + 0.5f;
    	Log.d("myoutgoingness", Float.toString(outgoingness));
    	byte[] buffer = new byte[1];
    	
    	//000000 - black
    	if (outgoingness <= 0.20) {
    		buffer[0] = 4;
    	}
    	//666600 - black/yellow
    	if (outgoingness > 0.20 && outgoingness <= 0.40) {
    		buffer[0] = 5;
    	}
    	//FFFFFF - white
    	if (outgoingness > 0.40 && outgoingness <= 0.60) {
    		buffer[0] = 6;
    	}
    	//FF400 - orange
    	if (outgoingness > 0.60 && outgoingness <= 0.80) {
    		buffer[0] = 7;
    	}
    	//FFFF00 - yellow
    	if (outgoingness > 0.80 && outgoingness <= 1.00) {
    		buffer[0] = 8;
    	}
    	
    	return buffer;
    }
} //end of class