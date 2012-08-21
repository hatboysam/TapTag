package com.taptag.beta;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Settings extends PreferenceActivity {
    boolean CheckboxPreference;
	
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
      PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
      final CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("logOutFromFaceBook");

      checkboxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {            
      public boolean onPreferenceChange(Preference preference, Object newValue) {
          if (newValue.toString().equals("true"))
          {  
             logOut();
          } 
          else 
          {  
              Toast.makeText(getApplicationContext(), "CB: " + "false", Toast.LENGTH_SHORT).show();
          }
          return true;
      }
    });
  
  
  }
  
	public void logOut() {
		// Logging out
		Intent toLogIn = new Intent(getApplicationContext(),
				FacebookLogInActivity.class);
		toLogIn.putExtra("Log out", true);
		Settings.this.startActivity(toLogIn);
	}
  
  
} 
