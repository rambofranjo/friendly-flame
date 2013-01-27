/**
 * Copyright 2012 Facebook
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.samples.hellofacebook;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import com.facebook.*;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.samples.hellofacebook.Utility;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.*;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;



public class HelloFacebookSampleActivity extends Activity {



    private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";

    // TAG is used to debug in Android logcat console
 	private static final String TAG = "ArduinoAccessory";
	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

    private Button createEvent;
    private Button showFriendsEvents;
    private Button showMyEvents;
    private LoginButton loginButton;
    private ToggleButton lightButton;
    private ProfilePictureView profilePictureView;
    private ImageView logo;
    private TextView greeting;
    private TextView appTitle;
    private TextView welcome;
    private PendingAction pendingAction = PendingAction.NONE;
    private ViewGroup controlsContainer;
    private GraphUser user;
    private View layoutView;
    private Flame flame;
    
    private Handler mHandler;
    private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	
	private BaseAdapter userPermissionsAdapter;
    
    final static int AUTHORIZE_ACTIVITY_RESULT_CODE = 0;
    
    String[] permissions = { "offline_access", "publish_stream", "user_photos", "publish_checkins",
    "photo_upload" };
    
    String[] user_permissions = { "user_about_me", "user_activities", "user_birthday",
            "user_checkins", "user_education_history", "user_events", "friends_events", "user_groups",
            "user_hometown", "user_interests", "user_likes", "user_location", "user_notes",
            "user_online_presence", "user_photos", "user_photo_video_tags", "user_relationships",
            "user_relationship_details", "user_religion_politics", "user_status", "user_videos",
            "user_website", "user_work_history"};
    
    UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;
	


    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }
    private UiLifecycleHelper uiHelper;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory "
								+ accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = UsbManager.getAccessory(intent);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
		}
	};
	
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        
        Session session = Session.getActiveSession();
        
        

        
        if (session == null) {      
            // Check if there is an existing token to be migrated 
            if(access_token != null) {                              
                // Clear the token info
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString("access_token", null);
                editor.commit();    
                // Create an AccessToken object for importing
                // just pass in the access token and take the
                // defaults on other values
                AccessToken accessToken = AccessToken.createFromExistingAccessToken(
                                            access_token,
                                            null, null, null, null);
                // statusCallback: Session.StatusCallback implementation
                session.open(accessToken, callback);
                Session.setActiveSession(session);
            }
        }
        
        
        
        
        
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }
        
        mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
 
		if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(mAccessory);
		}

        setContentView(R.layout.main);
        
        //userPermissionsAdapter = new PermissionsListAdapter(user_permissions);
        
        //jetzt dann hier irgendwie die Verbindung machen zum Login Button
        mHandler = new Handler();

        
        // Create the Facebook Object using the app id.
        Utility.mFacebook = new Facebook("101522410025545");
        // Instantiate the asynrunner object for asynchronous api calls. 
        Utility.mAsyncRunner = new AsyncFacebookRunner(Utility.mFacebook);
        

        //LOGIN BUTTON
        loginButton = (LoginButton) findViewById(R.id.login_button);
        List<String> permissionslist = new ArrayList<String>();
        permissionslist = Arrays.asList(user_permissions);
        loginButton.clearPermissions();
        loginButton.setReadPermissions(permissionslist);
        
        
        //loginButton.setApplicationId("101522410025545");
        
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                HelloFacebookSampleActivity.this.user = user;
                //updateUI();
                
                changeToEventActivity();
                
                // It's possible that we were waiting for this.user to be populated in order to post a
                // status update.
                //handlePendingAction();
            }
        });

        //PROFILE PICTURE
        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        greeting = (TextView) findViewById(R.id.greeting);
        appTitle = (TextView) findViewById(R.id.app_title);
        welcome = (TextView) findViewById(R.id.welcome);
        logo = (ImageView) findViewById(R.id.logo);
  
    }
    


    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        //updateUI();
        changeToEventActivity();
        if (mInputStream != null && mOutputStream != null) {
			return;
		}
 
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);

        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        closeAccessory();
    }

    @Override
    public void onDestroy() {
    	unregisterReceiver(mUsbReceiver);
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    private void openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			Log.d(TAG, "accessory opened");
		} else {
			Log.d(TAG, "accessory open fail");
		}
	}
    
    private void closeAccessory() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
		}
	}

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
                new AlertDialog.Builder(HelloFacebookSampleActivity.this)
                    .setTitle(R.string.cancelled)
                    .setMessage(R.string.permission_not_granted)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            //handlePendingAction();
        }
        //dateUI();
        changeToEventActivity();
    }

    private void updateUI() {
        Session session = Session.getActiveSession();
        boolean enableButtons = (session != null && session.isOpened());
        
        
        createEvent.setEnabled(enableButtons);
        showFriendsEvents.setEnabled(enableButtons);
        showMyEvents.setEnabled(enableButtons);
        

        //show or hide content, depending on login-status
        if (enableButtons && user != null) {
        	showContentLoggedIn();
        	
            
        } else {
        	hideContentLoggedOut();
        	
        }
    }
    
    @Override
	public Object onRetainNonConfigurationInstance() {
		if (mAccessory != null) {
			return mAccessory;
		} else {
			return super.onRetainNonConfigurationInstance();
		}
	}
 
    /*
    @SuppressWarnings("incomplete-switch")
    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case POST_PHOTO:
                break;
            case POST_STATUS_UPDATE:
                break;
        }
    }
    
    */

    
    private void changeToEventActivity() {
    	   Session session = Session.getActiveSession();
           if (session != null && session.isOpened()) {
        	  Intent eventsActivity = new Intent(this, EventsListActivity.class);
         	  startActivity(eventsActivity); 
           }
    	
    	  
    }
   
    
    //show content if logged in
    private void showContentLoggedIn() {
    	//make profile picture and buttons visible and enabled
        profilePictureView.setProfileId(user.getId());
        profilePictureView.setVisibility(View.VISIBLE);
        greeting.setText(getString(R.string.hello_user, user.getFirstName()));
        welcome.setText(null);
        appTitle.setText(getString(R.string.app_name));
        createEvent.setVisibility(View.VISIBLE);
        showFriendsEvents.setVisibility(View.VISIBLE);
        showMyEvents.setVisibility(View.VISIBLE);
        logo.setVisibility(View.GONE);
        
        //set color of the background according to outgoingness
        flame = new Flame();
        layoutView= findViewById(R.id.main_ui_container); 
        layoutView.setBackgroundColor(Color.HSVToColor(flame.calculateOutgoingness()));

        
    }
    
    //hide content if user is not logged in
    private void hideContentLoggedOut() {
    	//hide profile picture and buttons
    	profilePictureView.setVisibility(View.GONE);
        profilePictureView.setProfileId(null);
        greeting.setText(null);
        appTitle.setText(null);
        welcome.setText("Welcome To Friendly Flame Please log in");
        createEvent.setVisibility(View.GONE);
        showFriendsEvents.setVisibility(View.GONE);
        showMyEvents.setVisibility(View.GONE);
        logo.setVisibility(View.VISIBLE);
        
        //set color of the background according to outgoingness
        flame = new Flame();
        layoutView= findViewById(R.id.main_ui_container); 
        layoutView.setBackgroundColor(Color.WHITE);
        
        
    }
    
    private void bufferWrite(byte[] buffer) {
    	if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
    }
   
    
    
    

} //end class