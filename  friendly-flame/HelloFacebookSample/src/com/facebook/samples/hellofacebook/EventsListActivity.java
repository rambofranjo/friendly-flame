package com.facebook.samples.hellofacebook;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class EventsListActivity extends ListActivity {
	
	int counter = 0; 
	String[] contactList = {"saf", "asf"}; 
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_events);

        ListAdapter adapter = createAdapter();
        setListAdapter(adapter);
    }

    /**
     * Creates and returns a list adapter for the current list activity
     * @return
     */
    protected ListAdapter createAdapter()
    {
        // Create a simple array adapter (of type string) with the test values
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactList);

        return adapter;
    }

}
