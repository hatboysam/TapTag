package com.taptag.beta;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Handler;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.android.*;
import com.facebook.android.Facebook.*;
import com.taptag.beta.facebook.BaseRequestListener;
import com.taptag.beta.facebook.FacebookUserInfo;
import com.taptag.beta.network.TapTagAPI;
import com.taptag.beta.response.UserFetchResponse;

public class FacebookLogInActivity extends Activity {
	public static final String APP_ID = "467907829887006";

	Facebook facebook = new Facebook(APP_ID);

	private AsyncFacebookRunner mAsyncRunner;
	private Handler mHandler;
	private SharedPreferences mPrefs;
	private TextView mWelcomeLabel;
	private Button mLoginButton;
	private static final String[] PERMISSIONS = { "email" };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_main);

		mPrefs = getSharedPreferences("TapTag", MODE_PRIVATE);
		Integer userId = mPrefs.getInt("user_id", -1);
		if (userId > 0) {
			continueToHomeScreen();
		} else {
			mWelcomeLabel = (TextView) findViewById(R.id.welcomeText);
			mLoginButton = (Button) findViewById(R.id.loginButton);
			//Facebook properties
			mAsyncRunner = new AsyncFacebookRunner(facebook);
			mHandler = new Handler();
			//Get existing saved session information
			String access_token = mPrefs.getString("access_token", null);
			long expires = mPrefs.getLong("access_expires", 0);
			if (access_token != null) {
				facebook.setAccessToken(access_token);
			}
			if (expires != 0) {
				facebook.setAccessExpires(expires);
			}
			//Change the button text and welcome label
			configureUIState();
			//Button behavior
			mLoginButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!facebook.isSessionValid()) {
						logIn();
					} else {
						logOut();
					}
				}
			});
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		super.onResume();
		//Extend the session information if it is needed
		if ((facebook != null) && facebook.isSessionValid()) {
			facebook.extendAccessTokenIfNeeded(this, null);
		}
		//Check for user and then continue
		Integer userId = mPrefs.getInt("user_id", -1);
		if (userId > 0) {
			continueToHomeScreen();
		} else {
			requestUserData();
		}
	}

	public void configureUIState() {
		if (facebook.isSessionValid()) {
			mWelcomeLabel.setText(R.string.label_welcome);
			mLoginButton.setText(R.string.button_logout);
			//Get the user's data
			requestUserData();
		} else {
			mWelcomeLabel.setText(R.string.label_login);
			mLoginButton.setText(R.string.button_login);
		}
	}

	public void continueToHomeScreen() {
		Intent toHomeScreen = new Intent(FacebookLogInActivity.this, HomeScreenActivity.class);
		FacebookLogInActivity.this.startActivity(toHomeScreen);
	}

	public void logIn() {
		facebook.authorize(FacebookLogInActivity.this, PERMISSIONS, new DialogListener() {
			@Override
			public void onComplete(Bundle values) {
				//Set the logged in UI
				mWelcomeLabel.setText(R.string.label_welcome);
				mLoginButton.setText(R.string.button_logout);
				//Save session data
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putString("access_token", facebook.getAccessToken());
				editor.putLong("access_expires", facebook.getAccessExpires());
				editor.commit();
				//Get the user's data
				requestUserData();
			}

			@Override
			public void onFacebookError(FacebookError e) {
			}
			@Override
			public void onError(DialogError e) {
			}
			@Override
			public void onCancel() {
			}
		});
	}

	public void logOut() {
		//Logging out
		mAsyncRunner.logout(FacebookLogInActivity.this, new BaseRequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				//callback should be run in the original thread, not the background thread
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						//Set the logged out UI
						mWelcomeLabel.setText(R.string.label_login);
						mLoginButton.setText(R.string.button_login);
						//Clear the token information
						SharedPreferences.Editor editor = mPrefs.edit();
						editor.putString("access_token", null);
						editor.putLong("access_expires", 0);
						editor.remove("user_id");
						editor.commit();
					}
				});
			}
		});
	}

	public void requestUserData() {
		Bundle params = new Bundle();
		params.putString("field name", "name");
		params.putString("field email", "email");

		mAsyncRunner.request("me", params, new BaseRequestListener() {
			@Override
			public void onComplete(final String response, final Object state) {
				FacebookUserInfo facebookUserInfo = TapTagAPI.userInfoFromFacebook(response);
				UserFetchResponse userFetchResponse = TapTagAPI.fetchUser(facebookUserInfo);
				if (!userFetchResponse.hasError()) {
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putInt("user_id", userFetchResponse.getId());
					editor.putString("user_name", facebookUserInfo.getFirst_name() + " " + facebookUserInfo.getLast_name());
					editor.commit();
					continueToHomeScreen();
				} else {
					final String welcomeText = "Status: " + userFetchResponse.getStatus() + ", ID: " + Integer.toString(userFetchResponse.getId());
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mWelcomeLabel.setText(welcomeText);
						}
					});
				}
			}
		});
	}

}
