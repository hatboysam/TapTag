package com.taptag.beta;


import com.taptag.beta.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class HomeScreen extends Activity {

	private TextView loggedInLabel;
	private SharedPreferences mPrefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		//Display user name
		loggedInLabel = (TextView) findViewById(R.id.loggedInLabel);
		mPrefs = getSharedPreferences("TapTag", MODE_PRIVATE);
		String userName = mPrefs.getString("user_name", "No Name");
		loggedInLabel.setText("Welcome, " + userName + "!");
		
		// Set the click behavior for the My Places button
		ImageButton toVendors = (ImageButton) findViewById(R.id.myPlacesButton);
		toVendors.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent toVendorList = new Intent(HomeScreen.this,
						VendorListActivity.class);
				toVendorList.setAction(VendorListActivity.IN_ORDER);
				HomeScreen.this.startActivity(toVendorList);
			}
		});

		// Set the click behavior for the Places Near Me button
		ImageButton toNearbyVendors = (ImageButton) findViewById(R.id.nearMeButton);
		toNearbyVendors.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent toVendorList = new Intent(HomeScreen.this,
						VendorListActivity.class);
				toVendorList.setAction(VendorListActivity.NEAR_ME);
				HomeScreen.this.startActivity(toVendorList);
			}
		});

		// Set the click behavior for the tag writing button
		Button toWrite = (Button) findViewById(R.id.writeButton);
		toWrite.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent toVendor = new Intent(HomeScreen.this,
						TapTagActivity.class);
				HomeScreen.this.startActivity(toVendor);
			}
		});
	}
}
