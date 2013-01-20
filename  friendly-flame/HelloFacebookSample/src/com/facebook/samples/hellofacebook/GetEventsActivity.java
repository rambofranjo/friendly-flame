package com.facebook.samples.hellofacebook;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Session;
import com.facebook.android.Facebook;
import com.facebook.samples.hellofacebook.BaseRequestListener;


@SuppressWarnings("deprecation")
public class GetEventsActivity extends Activity {
	
    private Handler mHandler;
    private TextView mFQLOutput;

	Facebook facebookManager;
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	mHandler = new Handler();
        setContentView(R.layout.geteventsactivity);
        mFQLOutput = (TextView) findViewById(R.id.fqlOutput);

        // alle events wo ich involviert bin
        //String query = "SELECT eid, all_members_count, attending_count, declined_count, name, start_time, end_time, location, venue, host, description FROM event WHERE eid IN ( SELECT eid FROM event_member WHERE uid = me() )";
        
        String query_attending = "SELECT '' FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid = me() and start_time > 0 AND rsvp_status="+"\"attending\""+")";
        String query_declined = "SELECT '' FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid = me() and start_time > 0 AND rsvp_status="+"\"declined\""+")";
        String query_notreplied = "SELECT '' FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid = me() and start_time > 0 AND rsvp_status="+"\"not_replied\""+")";
        String query_countAllEvents = "SELECT '' FROM event WHERE eid IN (SELECT eid FROM event_member WHERE uid = me() and start_time > 0)";
        
        String query = query_attending;
        
        // jeden eventstatus von eingeloggten user 
        //String query = "SELECT eid, rsvp_status FROM event_member WHERE uid = me()";
        
        Bundle params = new Bundle();
        params.putString("access_token", Session.getActiveSession().getAccessToken());
        params.putString("method", "fql.query");
        params.putString("query", query);
        Utility.mAsyncRunner.request(null, params, new FQLRequestListener());
        
    }
    
    public class FQLRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
            /*
             * Output can be a JSONArray or a JSONObject.
             * Try JSONArray and if there's a JSONException, parse to JSONObject
             */
            try {
                JSONArray json = new JSONArray(response);
                setText(json.toString(2));
            } catch (JSONException e) {
                try {
                    /*
                     * JSONObject probably indicates there was some error
                     * Display that error, but for end user you should parse the
                     * error and show appropriate message
                     */
                    JSONObject json = new JSONObject(response);
                    setText(json.toString(2));
                } catch (JSONException e1) {
                    System.out.println(e1.getMessage());
                }
            }
        }
    
    
    
    
    
    public void setText(final String txt) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFQLOutput.setText(txt);
                mFQLOutput.setVisibility(View.VISIBLE);
                
                int count = countEvents(txt);
                System.out.println("count: " + count);
                Log.d("count", "dasfaf " +count);
                
            }
        });
    }
    
    public int countEvents(String query) {
    	
    	String[] splitquery = query.split(":");
    	int count = splitquery.length;
    	
    	return count-1;
    }
    
  }

    	
}
