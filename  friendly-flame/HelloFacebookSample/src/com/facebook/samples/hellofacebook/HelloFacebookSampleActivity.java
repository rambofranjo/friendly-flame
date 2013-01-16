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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.content.Intent;


import com.facebook.*;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.*;

import java.util.*;


public class HelloFacebookSampleActivity extends Activity implements OnClickListener {

    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final Location SEATTLE_LOCATION = new Location("") {
        {
            setLatitude(47.6097);
            setLongitude(-122.3331);
        }
    };

    private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";

    private Button createEvent;
    private Button showFriendsEvents;
    private Button showMyEvents;
    private LoginButton loginButton;
    private ProfilePictureView profilePictureView;
    private TextView greeting;
    private TextView appTitle;
    private TextView welcome;
    private PendingAction pendingAction = PendingAction.NONE;
    private ViewGroup controlsContainer;
    private GraphUser user;
    private View layoutView;
    private Flame flame;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }

        setContentView(R.layout.main);

        //LOGIN BUTTON
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                HelloFacebookSampleActivity.this.user = user;
                updateUI();
                // It's possible that we were waiting for this.user to be populated in order to post a
                // status update.
                handlePendingAction();
            }
        });

        //PROFILE PICTURE
        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        greeting = (TextView) findViewById(R.id.greeting);
        appTitle = (TextView) findViewById(R.id.app_title);
        welcome = (TextView) findViewById(R.id.welcome);
        
        //BUTTONS
        createEvent = (Button) findViewById(R.id.createEventButton);
        createEvent.setOnClickListener(this);
        showFriendsEvents = (Button) findViewById(R.id.showFriendsEventsButton);
        showFriendsEvents.setOnClickListener(this);
        showMyEvents = (Button) findViewById(R.id.showMyEventsButton);
        showMyEvents.setOnClickListener(this);
        
        controlsContainer = (ViewGroup) findViewById(R.id.main_ui_container);  
    }
    
    @Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.createEventButton:
			case R.id.showFriendsEventsButton:
			case R.id.showMyEventsButton:
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();

        updateUI();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
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
            handlePendingAction();
        }
        updateUI();
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

    private interface GraphObjectWithId extends GraphObject {
        String getId();
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
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
        
        //set color of the background according to outgoingness
        flame = new Flame();
        layoutView= findViewById(R.id.main_ui_container); 
        layoutView.setBackgroundColor(Color.WHITE);
    }
   
} //end class
