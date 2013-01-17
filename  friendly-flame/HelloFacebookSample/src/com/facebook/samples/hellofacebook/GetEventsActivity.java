package com.facebook.samples.hellofacebook;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.samples.hellofacebook.BaseRequestListener;


@SuppressWarnings("deprecation")
public class GetEventsActivity extends Activity {
	
    private Handler mHandler;
    private TextView mFQLOutput;

	
	
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	mHandler = new Handler();
        setContentView(R.layout.geteventsactivity);
        mFQLOutput = (TextView) findViewById(R.id.fqlOutput);

        // alle events wo ich involviert bin
        String query = "SELECT eid, name, start_time, end_time, location, venue, host, description FROM event WHERE eid IN ( SELECT eid FROM event_member WHERE uid = me() )";
      
        Bundle params = new Bundle();
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
            }
        });
    }
    
  }

    	
}
