package com.taptag.beta;

import com.taptag.beta.nfc.NFCActions;
import com.taptag.beta.reward.Reward;
import com.taptag.beta.reward.RewardAdapter;
import com.taptag.beta.vendor.Vendor;
import com.taptag.beta.network.*;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class VendorActivity extends Activity {

	public static final String VIEW_VENDOR = "View Vendor";

	private ListView rewardListView;
	private TextView vendorNameTextView;
	private TextView vendorAddressTextView;
	private ProgressBar loadingSpinner;

	private NfcAdapter nfcAdapter;
	private PendingIntent nfcIntent;

	private Vendor vendor = null;
	private Reward[] allRewards = new Reward[0];
	private RewardAdapter adapter;
	
	private boolean rewardsLoaded;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vendor);
		
		vendorNameTextView = (TextView) findViewById(R.id.vendorName);
		vendorAddressTextView = (TextView) findViewById(R.id.vendorAddress);
		rewardListView = (ListView) findViewById(R.id.rewardlist);
		loadingSpinner = (ProgressBar) findViewById(R.id.vendorRewardProgress);

		// Check for NFC
		nfcIntent = PendingIntent.getActivity(this, 0, (new Intent(this, getClass())).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		
		rewardsLoaded = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			setInfoFromNFCIntent(intent);
			setIntent(new Intent()); // Consume this intent.
		} else if (VendorActivity.VIEW_VENDOR.equals(intent.getAction())) {
			Bundle extras = intent.getExtras();
			vendor = (Vendor) extras.get("vendor");
			setInfoFromVendor(vendor);
		}
		rewardsLoaded = false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			setInfoFromNFCIntent(intent);
		} else if (VendorActivity.VIEW_VENDOR.equals(intent.getAction())) {
			Bundle extras = intent.getExtras();
			vendor = (Vendor) extras.get("vendor");
			setInfoFromVendor(vendor);
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// Load all places in background
		if (hasFocus && !rewardsLoaded) {
			loadingSpinner.setVisibility(View.VISIBLE);
			final Intent intent = getIntent();
			Thread backgroundThread = new Thread(new Runnable() {
				public void run() {
					RewardsFetchTask task = new RewardsFetchTask();
					task.execute(null, null, null);
				}
			});
			backgroundThread.run();
		}
		rewardsLoaded = true;
	}

	/**
	 * Load the Vendor information from an NFC tag
	 * 
	 * @param intent
	 */
	private void setInfoFromNFCIntent(Intent intent) {
		NdefMessage firstMessage = NFCActions.getFirstMessage(intent);
		vendor = NFCActions.vendorFromNdef(firstMessage);
		setInfoFromVendor(vendor);
	}

	/**
	 * Load the Vendor information from the intent Bundle
	 * 
	 * @param vendor
	 */
	private void setInfoFromVendor(Vendor vendor) {
		// Set title and address
		vendorNameTextView.setText(vendor.getName());
		vendorAddressTextView.setText(vendor.getAddress().toString());
	}
	
	/**
	 * Task to fetch the rewards for this Vendor
	 * @author samstern
	 */
	public class RewardsFetchTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Reward[] rewards = TapTagAPI.progressByUserAndCompany(201, vendor.getCompanyId());
			VendorActivity.this.allRewards = rewards;
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			adapter = new RewardAdapter(VendorActivity.this, R.layout.rewardlistitem, allRewards);
			rewardListView.setAdapter(adapter);
			loadingSpinner.setVisibility(View.GONE);
		}	
	}

}