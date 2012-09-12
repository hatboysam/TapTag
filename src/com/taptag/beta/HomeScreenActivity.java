package com.taptag.beta;


import org.codehaus.jackson.map.ObjectMapper;

import com.taptag.beta.R;
import com.taptag.beta.vendor.Vendor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class HomeScreenActivity extends Activity {
	
	public static final int REQUEST_CODE = 1;
	
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
		toSettings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent toSettings = new Intent(HomeScreenActivity.this, 
						SettingsActivity.class);
				HomeScreenActivity.this.startActivity(toSettings);
			}
		});
		
		//Button settings
		ImageButton toScan  = (ImageButton) findViewById(R.id.scanButton);
		toScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchQrScanner();
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
	
	/**
	 * QR STUFF
	 */
	
	private void launchQrScanner() {
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, 0);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				Vendor vendor = vendorFromQr(contents);
				continueAfterScan(vendor);
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "QR Scan Failed", Toast.LENGTH_SHORT).show();
				continueAfterScan(null);
			}
		}
	}
	
	private Vendor vendorFromQr(String contents) {
		ObjectMapper om = new ObjectMapper();
		try {
			VendorWrapper vw = om.readValue(contents.getBytes(), VendorWrapper.class);
			return vw.getVendor();
		} catch (Exception e) {
			return new Vendor();
		}
	}
	
	public void continueAfterScan(Vendor vendor) {
		if (vendor != null) {
			Intent intent = new Intent(HomeScreenActivity.this, VendorActivity.class);
			intent.setAction(VendorActivity.FROM_QR);
			intent.putExtra("vendor", vendor);
			startActivity(intent);
		}
	}
	
	@SuppressWarnings("unused")
	private static class VendorWrapper {
		private Vendor vendor;
		public VendorWrapper() {
			vendor = new Vendor();
		}
		public void setVendor(Vendor vendor){
			this.vendor = vendor;
		}
		public Vendor getVendor(){
			return vendor;
		}
	}
	
}
