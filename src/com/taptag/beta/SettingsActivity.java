package com.taptag.beta;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	
	private TextView mLogOutLabel;
	private Button mLogoutButton;
	private Button mAboutUsButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		mLogoutButton = (Button) findViewById(R.id.logOutButton);
		mAboutUsButton = (Button) findViewById(R.id.aboutUsButton);
		mLogOutLabel = (TextView) findViewById(R.id.logOutText);

		// Button log-out
		mLogoutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					logOut();
			}
		});
		
		// Button aboutUs
		mAboutUsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
	}

	public void logOut() {
		// Logging out
		Intent toLogIn = new Intent(getApplicationContext(),
				FacebookLogInActivity.class);
		toLogIn.putExtra("Log out from TapTag", true);
		SettingsActivity.this.startActivity(toLogIn);
	}
}
