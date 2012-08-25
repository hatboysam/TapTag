package com.taptag.beta;

import com.taptag.beta.R;
import com.taptag.beta.network.TapTagAPI;
import com.taptag.beta.nfc.NFCActions;
import com.taptag.beta.redemption.Redemption;
import com.taptag.beta.response.RedemptionResponse;
import com.taptag.beta.reward.Reward;
import com.taptag.beta.reward.RewardAdapter;
import com.taptag.beta.reward.RewardTabFragment;
import com.taptag.beta.vendor.Vendor;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;


public class RewardsActivity extends Activity {

	public static Context appContext;
	private SharedPreferences mPrefs;

	public static String IN_PROGRESS = "In Progress";
	public static String COMPLETED = "Completed";
	public static String REDEEMED = "Redeemed";

	private RewardTabFragment completedFragment;
	private RewardTabFragment inProgressFragment;
	private RewardTabFragment redeemedFragment;

	private ActionBar actionBar;

	private PendingIntent nfcIntent;
	private IntentFilter ndefFilter;
	private IntentFilter[] intentFiltersArray;
	private String[][] techListsArray;
	private NfcAdapter adapter;
	private Tag recentTag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rewards);

		mPrefs = getSharedPreferences("TapTag", MODE_PRIVATE);

		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.Tab inProgressTab = actionBar.newTab().setText(IN_PROGRESS);
		ActionBar.Tab completedTab = actionBar.newTab().setText(COMPLETED);
		ActionBar.Tab nearbyTab = actionBar.newTab().setText(REDEEMED);

		inProgressFragment = new RewardTabFragment(IN_PROGRESS);
		completedFragment = new RewardTabFragment(COMPLETED);
		redeemedFragment = new RewardTabFragment(REDEEMED);

		inProgressTab.setTabListener(new RewardTabListener(inProgressFragment));
		completedTab.setTabListener(new RewardTabListener(completedFragment));
		nearbyTab.setTabListener(new RewardTabListener(redeemedFragment));		

		actionBar.addTab(inProgressTab);
		actionBar.addTab(completedTab);
		actionBar.addTab(nearbyTab);

		setupLists();

		//NFC
		adapter = NfcAdapter.getDefaultAdapter(this);
		nfcIntent = PendingIntent.getActivity(this, 0, 
				new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		ndefFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefFilter.addDataType(NFCActions.TAG_MIME);
		} catch (MalformedMimeTypeException e) {
			e.printStackTrace();
		}
		intentFiltersArray = new IntentFilter[] {ndefFilter};
		techListsArray = new String[][] { new String[] { Ndef.class.getName() } };

	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		String action = intent.getAction();
		if (COMPLETED.equals(action)) {
			actionBar.setSelectedNavigationItem(1);
		}
		if (adapter != null) {
			adapter.enableForegroundDispatch(this, nfcIntent, intentFiltersArray, techListsArray);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (adapter != null) {
			adapter.disableForegroundDispatch(this);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		recentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (recentTag != null) {
			NdefMessage message = NFCActions.getFirstMessage(intent);
			Vendor vendor = NFCActions.vendorFromNdef(message);
			if (completedFragment.inReadMode()) {
				Reward clicked = completedFragment.getSelectedReward();
				redeemReward(clicked, vendor);
			}
		}
	}

	public void redeemReward(Reward reward, Vendor vendor) {
		if (reward == null || vendor == null) {
			return;
		}
		int userId = mPrefs.getInt("user_id", -1);
		final Redemption redemption = new Redemption(userId, vendor.getId(), reward);
		Thread backgroundThread = new Thread(new Runnable() {
			@Override
			public void run() {
				RedeemRewardTask redeemRewardTask = new RedeemRewardTask();
				redeemRewardTask.execute(new Redemption[] { redemption });
			}	
		});
		backgroundThread.run();
	}

	public void setupLists() {
		Thread backgroundThread = new Thread(new Runnable() {
			@Override
			public void run() {
				RewardLoadTask loadInProgressTask = new RewardLoadTask();
				loadInProgressTask.execute(new String[] {IN_PROGRESS});

				RewardLoadTask loadCompletedTask = new RewardLoadTask();
				loadCompletedTask.execute(new String[] {COMPLETED});

				RewardLoadTask loadNearbyTask = new RewardLoadTask();
				loadNearbyTask.execute(new String[] {REDEEMED});
			}	
		});
		backgroundThread.run();
	}

	/**
	 * Make a Toast Message with "SHORT" Length
	 * @param message
	 */
	private void toastShort(String message) {
		Toast.makeText(RewardsActivity.this, message, Toast.LENGTH_SHORT).show();
	}

	public class RedeemRewardTask extends AsyncTask<Redemption, Void, RedemptionResponse> {

		@Override
		protected RedemptionResponse doInBackground(Redemption... params) {
			Redemption toRedeem = params[0];
			return TapTagAPI.redeemReward(toRedeem);
		}

		@Override
		protected void onPostExecute(RedemptionResponse response) {
			if (response.success()) {
				toastShort("Reward Redeemed");
				setupLists();
			} else {
				toastShort("Redemption Failed");
			}
			completedFragment.dismissDialog();
		}

	}

	public class RewardLoadTask extends AsyncTask<String, Void, Void> {

		RewardAdapter adapter;
		RewardTabFragment toUpdate;
		String type;

		@Override
		protected Void doInBackground(String... params) {
			type = params[0];
			Reward[] rewards = new Reward[0];
			if (IN_PROGRESS.equals(type)) {
				rewards = TapTagAPI.progressByUser(mPrefs.getInt("user_id", -1));
				rewards = RewardAdapter.filterInProgress(rewards);
				toUpdate = inProgressFragment;
			}
			if (COMPLETED.equals(type)) {
				rewards = TapTagAPI.completedByUser(mPrefs.getInt("user_id", -1));
				toUpdate = completedFragment;
			}
			if (REDEEMED.equals(type)) {
				rewards = TapTagAPI.redeemedByUser(mPrefs.getInt("user_id", -1));
				toUpdate = redeemedFragment;
			}
			adapter = new RewardAdapter(RewardsActivity.this, R.layout.rewardlistitem, rewards);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			toUpdate.setListAdapter(adapter);
			adapter.notifyDataSetChanged();
		}

	}

	public class RewardTabListener implements ActionBar.TabListener {

		public Fragment fragment;

		public RewardTabListener(Fragment fragment) {
			this.fragment = fragment;
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub	
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			ft.replace(R.id.fragment_container, fragment);

		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.remove(fragment);		
		}

	}

}
