package com.taptag.beta;

import com.taptag.beta.nfc.NFCActions;
import com.taptag.beta.response.TapSubmitResponse;
import com.taptag.beta.reward.Reward;
import com.taptag.beta.reward.RewardAdapter;
import com.taptag.beta.vendor.Vendor;
import com.taptag.beta.network.*;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class VendorActivity extends NetworkActivity {

	public static final String VIEW_VENDOR = "View Vendor";

	private ListView rewardListView;
	private TextView vendorNameTextView;
	private TextView vendorAddressTextView;
	private ProgressBar loadingSpinner;
	private SharedPreferences mPrefs;

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
		mPrefs = getSharedPreferences("TapTag", MODE_PRIVATE);
		
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
		Thread backgroundThread = new Thread(new Runnable() {
			public void run() {
				TapSubmitTask task = new TapSubmitTask();
				task.execute(null, null);
			}
		});
		backgroundThread.run();
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
	 * Make a Toast Message with "SHORT" Length
	 * @param message
	 */
	private void toastShort(String message) {
		Toast.makeText(VendorActivity.this, message, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * Task to fetch the rewards for this Vendor
	 * @author samstern
	 */
	public class RewardsFetchTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Reward[] rewards = TapTagAPI.progressByUserAndCompany(mPrefs.getInt("user_id", 0), vendor.getCompanyId());
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
	
	/**
	 * Task to submit a Tap for this Vendor by the logged in user
	 * @author samstern
	 */
	public class TapSubmitTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			Integer userId = mPrefs.getInt("user_id", -1);
			if (userId > 0) {
				TapSubmitResponse tsr = TapTagAPI.submitTap(userId, vendor);
				return tsr.success();
			} else {
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				toastShort("Tap Submitted");
			} else {
				toastShort("Tap Failed");
			}
		}

	}

}