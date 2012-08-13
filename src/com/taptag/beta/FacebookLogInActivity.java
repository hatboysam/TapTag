package com.taptag.beta;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.os.Handler;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.android.*;
import com.facebook.android.Facebook.*;
import com.facebook.info.FacebookUserInfo;
import com.taptag.beta.network.TapTagAPI;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

public class FacebookLogInActivity extends Activity {
	public static final String APP_ID = "467907829887006";

	Facebook facebook = new Facebook(APP_ID);

	private AsyncFacebookRunner mAsyncRunner;
	private Handler mHandler;
	private SharedPreferences mPrefs;
	private TextView mWelcomeLabel;
	private Button mLoginButton;
	private Button mPostFeedButton;
	private Button mSendRequestButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_main);

		// UI properties
		mWelcomeLabel = (TextView) findViewById(R.id.welcomeText);
		mLoginButton = (Button) findViewById(R.id.loginButton);

		// Facebook properties
		mAsyncRunner = new AsyncFacebookRunner(facebook);
		mHandler = new Handler();

		/*
		 * Get existing saved session information
		 */
		mPrefs = getPreferences(MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);
		if (access_token != null) {
			facebook.setAccessToken(access_token);
		}
		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		// Parse any incoming notifications and save
		Uri intentUri = getIntent().getData();
		if (intentUri != null) {
			String alertMessage = null;
			// Check if incoming request. If not, an incoming deep link
			if (intentUri.getQueryParameter("request_ids") != null
					&& intentUri.getQueryParameter("ref").equals("notif")) {
				alertMessage = "Incoming Request: "
						+ intentUri.getQueryParameter("request_ids");
			} else {
				alertMessage = "Incoming Deep Link: " + intentUri.toString();
			}
			if (alertMessage != null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(alertMessage)
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}

		if (facebook.isSessionValid()) {
			// Set the logged in UI
			mWelcomeLabel.setText(R.string.label_welcome);
			mLoginButton.setText(R.string.button_logout);

			// Get the user's data
			requestUserData();
		} else {
			// Set the logged out UI
			mWelcomeLabel.setText(R.string.label_login);
			mLoginButton.setText(R.string.button_login);
		}

		mLoginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Logging in
				if (!facebook.isSessionValid()) {
					facebook.authorize(FacebookLogInActivity.this,
							new DialogListener() {
								@Override
								public void onComplete(Bundle values) {
									// Set the logged in UI
									mWelcomeLabel
											.setText(R.string.label_welcome);
									mLoginButton
											.setText(R.string.button_logout);

									// Save session data
									SharedPreferences.Editor editor = mPrefs
											.edit();
									editor.putString("access_token",
											facebook.getAccessToken());
									editor.putLong("access_expires",
											facebook.getAccessExpires());
									editor.commit();

									Intent toHomeScreen = new Intent(
											FacebookLogInActivity.this,
											HomeScreen.class);
									FacebookLogInActivity.this
											.startActivity(toHomeScreen);

									// Get the user's data
									requestUserData();
								}

								@Override
								public void onFacebookError(FacebookError error) {
								}

								@Override
								public void onError(DialogError e) {
								}

								@Override
								public void onCancel() {
								}
							});
				} else {
					// Logging out
					mAsyncRunner.logout(FacebookLogInActivity.this,
							new BaseRequestListener() {
								@Override
								public void onComplete(String response,
										Object state) {
									/*
									 * callback should be run in the original
									 * thread, not the background thread
									 */
									mHandler.post(new Runnable() {
										@Override
										public void run() {
											// Set the logged out UI
											mWelcomeLabel
													.setText(R.string.label_login);
											mLoginButton
													.setText(R.string.button_login);

											// Clear the token information
											SharedPreferences.Editor editor = mPrefs
													.edit();
											editor.putString("access_token",
													null);
											editor.putLong("access_expires", 0);
											editor.commit();
										}
									});
								}
							});
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Callback to handle the post-authorization flow.
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Extend the session information if it is needed
		if ((facebook != null) && facebook.isSessionValid()) {
			facebook.extendAccessTokenIfNeeded(this, null);
		}
	}

	public void requestUserData() {
		/**
		 * { "user" : { "first" : ..., "last" : ..., "facebook" : ..., "email" :
		 * ... }}
		 */
		Bundle params = new Bundle();
		params.putString("field name", "name");
		params.putString("field email", "email");

		mAsyncRunner.request("me", params, new BaseRequestListener() {

			@Override
			public void onComplete(final String response, final Object state) {
				JSONObject jsonObject;
				try {
					//Get user info from facebook.
					FacebookUserInfo facebookUserInfo = TapTagAPI.userInfoFromFacebook(response);
					
					/**
					//post user info to the server
					try {
						String userInforesponse = TapTagAPI.jsonPostFacebookUserInfo(facebookUserInfo);
						userInforesponse.toString();
					} catch (Exception e) {
						e.printStackTrace();
					}
					*/
					
					jsonObject = new JSONObject(response);
					final String welcomeText = getString(R.string.label_welcome)
							+ ", " + jsonObject.getString("name");

					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mWelcomeLabel.setText(welcomeText);
						}
					});

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
