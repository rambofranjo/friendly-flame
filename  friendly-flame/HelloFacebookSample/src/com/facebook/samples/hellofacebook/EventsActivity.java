package com.facebook.samples.hellofacebook;

import java.util.ArrayList;


import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.ListActivity;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class EventsActivity extends Activity  {
	
	int clickCounter=0;
    ArrayList<String> listItems=new ArrayList<String>();
    ArrayAdapter<String> adapter;
    
    private Button eventButton = null; 


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_events);
		
		
		  
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
            	//Toast.makeText(this, "All events", Toast.LENGTH_SHORT).show();
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
