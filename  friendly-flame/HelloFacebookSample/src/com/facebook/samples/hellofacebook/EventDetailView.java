package com.facebook.samples.hellofacebook;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class EventDetailView extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_detail_view);
		
		Log.d("EventDetailView", "test");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_event_detail_view, menu);
		return true;
	}

}
