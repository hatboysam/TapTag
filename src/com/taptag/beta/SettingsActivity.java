package com.taptag.beta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

	private Preference logOut;
	private Preference aboutUs;
	private SharedPreferences mPrefs;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

		logOut = getPreferenceManager().findPreference("logOut");
		mPrefs = getSharedPreferences("TapTag", MODE_PRIVATE);
		String userName = mPrefs.getString("user_name", "No Name");
		logOut.setSummary("Logged In as: " + userName);
		logOut.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				logOut();
				return true;
			}	  
		});
		aboutUs = getPreferenceManager().findPreference("aboutUs");
		aboutUs.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				return true;
			}	  
		});

	}

	public void logOut() {
		// Logging out
		Intent toLogIn = new Intent(getApplicationContext(), FacebookLogInActivity.class);
		toLogIn.setAction(FacebookLogInActivity.LOG_OUT);
		SettingsActivity.this.startActivity(toLogIn);
	}


} 
