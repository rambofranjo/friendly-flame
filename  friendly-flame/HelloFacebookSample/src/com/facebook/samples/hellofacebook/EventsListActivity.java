package com.facebook.samples.hellofacebook;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;


public class EventsListActivity extends ListActivity {
	
	FbEvent fbe = new FbEvent();
	
	int counter = 0; 
	String[] contactList;
	
	
	//String[] contactList = {"sadfasf", "asdfasf"};

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_events);
        
        contactList = fbe.getAllEvents();
        
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
                Toast.makeText(this, "My events", Toast.LENGTH_SHORT).show();
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

}
