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

public class HomeScreenActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		// Set the click behavior for the My Places button
		ImageButton toVendors = (ImageButton) findViewById(R.id.myPlacesButton);
		toVendors.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent toVendorList = new Intent(HomeScreenActivity.this,
						VendorListActivity.class);
				toVendorList.setAction(VendorListActivity.IN_ORDER);
				HomeScreenActivity.this.startActivity(toVendorList);
			}
		});

		// Set the click behavior for the Places Near Me button
		ImageButton toNearbyVendors = (ImageButton) findViewById(R.id.nearMeButton);
		toNearbyVendors.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent toVendorList = new Intent(HomeScreenActivity.this,
						VendorListActivity.class);
				toVendorList.setAction(VendorListActivity.NEAR_ME);
				HomeScreenActivity.this.startActivity(toVendorList);
			}
		});
		
		// Set the click behavior for the Rewards button
		ImageButton toRewards = (ImageButton) findViewById(R.id.rewardsButton);
		toRewards.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent toRewards = new Intent(HomeScreenActivity.this,
						RewardsActivity.class);
				HomeScreenActivity.this.startActivity(toRewards);
			}
		});
		
		//Button settings
		ImageButton toSettings  = (ImageButton) findViewById(R.id.settingsButton);
		toSettings .setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent toSettings = new Intent(HomeScreenActivity.this, 
						SettingsActivity.class);
				HomeScreenActivity.this.startActivity(toSettings);
			}
		});

		// Set the click behavior for the tag writing button
		Button toWrite = (Button) findViewById(R.id.writeButton);
		toWrite.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent toVendor = new Intent(HomeScreenActivity.this,
						TapTagActivity.class);
				HomeScreenActivity.this.startActivity(toVendor);
			}
		});	
	}
}
