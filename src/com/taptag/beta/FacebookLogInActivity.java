package com.taptag.beta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Handler;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.android.*;
import com.facebook.android.Facebook.*;
import com.taptag.beta.facebook.BaseRequestListener;
import com.taptag.beta.facebook.FacebookUserInfo;
import com.taptag.beta.network.TapTagAPI;
import com.taptag.beta.response.UserFetchResponse;

public class FacebookLogInActivity extends NetworkActivity {
	public static final String APP_ID = "467907829887006";
	public static final String LOG_OUT = "Log Out";

	Facebook facebook = new Facebook(APP_ID);

	private AsyncFacebookRunner mAsyncRunner;
	private Handler mHandler;
	private SharedPreferences mPrefs;
	private Button mLoginButton;
	private static final String[] PERMISSIONS = { "email" };

	private EditText firstName;
	private EditText lastName;
	private EditText email;
	private Button signupButton;
	
	private TextView errorView;
	private ProgressBar facebookSpinner;
	private ProgressBar signupSpinner;
	
	//Taken from mkyong
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private Pattern emailPattern;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_main);

		mPrefs = getSharedPreferences("TapTag", MODE_PRIVATE);
		Integer userId = mPrefs.getInt("user_id", -1);
		
		mLoginButton = (Button) findViewById(R.id.loginButton);

		// Facebook properties
		mAsyncRunner = new AsyncFacebookRunner(facebook);
		mHandler = new Handler();
		// Get existing saved session information
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);

		if (access_token != null) {
			facebook.setAccessToken(access_token);
		}
		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		errorView = (TextView) findViewById(R.id.signupError);
		facebookSpinner = (ProgressBar) findViewById(R.id.facebookSpinner);
		signupSpinner = (ProgressBar) findViewById(R.id.signupSpinner);
		
		hideSpinners();
		errorView.setVisibility(View.GONE);

		emailPattern = Pattern.compile(EMAIL_PATTERN);
		firstName = (EditText) findViewById(R.id.signupFirstName);
		lastName = (EditText) findViewById(R.id.signupLastName);
		email = (EditText) findViewById(R.id.signupEmail);
		signupButton = (Button) findViewById(R.id.signupButton);

		signupButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean isValid = validateInputs();
				if (isValid) {
					SignupTask signupTask = new SignupTask();
					signupTask.execute(null, null);
				}
			}
		});

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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		super.onResume();
		// Extend the session information if it is needed
		//if (facebook != null && !facebook.isSessionValid()) {
			facebook.extendAccessTokenIfNeeded(this, null);
		//}
		if (LOG_OUT.equals(getIntent().getAction())) {
			logOut();
		} else  {
			Integer userId = mPrefs.getInt("user_id", -1);
			if (userId > 0) {
				continueToHomeScreen();
			}
		}
	}

	@Override
	public void onBackPressed() {
		//Do nothing, don't want people getting back into the app
	}

	/**
	 * Validate the signup form.  True if valid, false otherwise.
	 * @return
	 */
	public boolean validateInputs() {
		boolean firstNameValid = validateFirstName();
		boolean lastNameValid = validateLastName();
		boolean emailValid = validateEmail();

		return (firstNameValid && lastNameValid && emailValid);
	}

	private FacebookUserInfo userFromForm() {
		String firstS = firstName.getText().toString();
		String lastS = lastName.getText().toString();
		String emailS = email.getText().toString();
		return new FacebookUserInfo(firstS.trim(), lastS.trim(), emailS.trim());
	}

	private boolean validateFirstName() {
		String firstNameString = firstName.getText().toString();
		if (firstNameString == null || firstNameString.length() == 0) {
			firstName.setError("First name can't be blank");
			return false;
		}
		return true;
	}

	private boolean validateLastName() {
		String lastNameString = lastName.getText().toString();
		if (lastNameString == null || lastNameString.length() == 0) {
			lastName.setError("Last name can't be blank");
			return false;
		}
		return true;
	}

	private boolean validateEmail() {
		String emailString = email.getText().toString();
		Matcher emailMatcher = emailPattern.matcher(emailString);
		if (!emailMatcher.matches()) {
			email.setError("Invalid Email address");
			return false;
		}
		return true;
	}
	
	public void hideSpinners() {
		facebookSpinner.setVisibility(View.GONE);
		signupSpinner.setVisibility(View.GONE);
	}

	public void continueToHomeScreen() {
		Intent toHomeScreen = new Intent(FacebookLogInActivity.this, HomeScreenActivity.class);
		FacebookLogInActivity.this.startActivity(toHomeScreen);
	}

	public void logIn() {
		facebookSpinner.setVisibility(View.VISIBLE);
		mLoginButton.setVisibility(View.GONE);
		facebook.authorize(FacebookLogInActivity.this, PERMISSIONS, Facebook.FORCE_DIALOG_AUTH, new DialogListener() {
			@Override
			public void onComplete(Bundle values) {
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putString("access_token", facebook.getAccessToken());
				editor.putLong("access_expires", facebook.getAccessExpires());
				editor.commit();
				requestUserData();
			}

			@Override
			public void onFacebookError(FacebookError e) {
				mLoginButton.setVisibility(View.VISIBLE);
				hideSpinners();

			}
			@Override
			public void onError(DialogError e) {
				mLoginButton.setVisibility(View.VISIBLE);
				hideSpinners();
			}
			@Override
			public void onCancel() {
				mLoginButton.setVisibility(View.VISIBLE);
				hideSpinners();
			}
		});
	}

	public void logOut() {
		// Logging out
		mAsyncRunner.logout(FacebookLogInActivity.this, new BaseRequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				// callback should be run in the original thread, not
				// the background thread
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// Clear the token information
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
					commitUserInfo(userFetchResponse);
					continueToHomeScreen();
				} else {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							hideSpinners();
							mLoginButton.setVisibility(View.VISIBLE);
						}
					});
				}
			}
		});
	}
	
	public void commitUserInfo(UserFetchResponse userFetchResponse) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putInt("user_id", userFetchResponse.getId());
		editor.putString("user_name", userFetchResponse.getFirst() + " " + userFetchResponse.getLast());
		editor.commit();
	}

	public class SignupTask extends AsyncTask<Void, Void, UserFetchResponse> {
		@Override
		protected void onPreExecute() {
			signupSpinner.setVisibility(View.VISIBLE);
			signupButton.setVisibility(View.GONE);
		}
		
		@Override
		protected UserFetchResponse doInBackground(Void... arg0) {
			FacebookUserInfo user = userFromForm();
			UserFetchResponse response = TapTagAPI.fetchUser(user);
			return response;
		}
		
		@Override
		protected void onPostExecute(UserFetchResponse response) {
			if (response.success()) {
				commitUserInfo(response);
				continueToHomeScreen();
			} else {
				errorView.setVisibility(View.VISIBLE);
			}
		}
	}

}
