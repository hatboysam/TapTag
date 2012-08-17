package com.taptag.beta;

import com.taptag.beta.R;
import com.taptag.beta.network.TapTagAPI;
import com.taptag.beta.reward.Reward;
import com.taptag.beta.reward.RewardAdapter;
import com.taptag.beta.reward.RewardTabFragment;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;


public class RewardsActivity extends Activity {

	public static Context appContext;
	private SharedPreferences mPrefs;
	
	public static String IN_PROGRESS = "In Progress";
	public static String COMPLETED = "Completed";
	public static String NEARBY = "Nearby";
	
	private Reward[] completedRewards;
	private Reward[] inProgressRewards;
	private Reward[] nearbyRewards;
	
	private ListFragment completedFragment;
	private ListFragment inProgressFragment;
	private ListFragment nearbyFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rewards);
		
		mPrefs = getSharedPreferences("TapTag", MODE_PRIVATE);
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		ActionBar.Tab inProgressTab = actionBar.newTab().setText(IN_PROGRESS);
		ActionBar.Tab completedTab = actionBar.newTab().setText(COMPLETED);
		ActionBar.Tab nearbyTab = actionBar.newTab().setText(NEARBY);
		
		inProgressFragment = new RewardTabFragment(IN_PROGRESS);
		completedFragment = new RewardTabFragment(COMPLETED);
		nearbyFragment = new RewardTabFragment(NEARBY);
		
		inProgressTab.setTabListener(new RewardTabListener(inProgressFragment));
		completedTab.setTabListener(new RewardTabListener(completedFragment));
		nearbyTab.setTabListener(new RewardTabListener(nearbyFragment));		
		
		actionBar.addTab(inProgressTab);
		actionBar.addTab(completedTab);
		actionBar.addTab(nearbyTab);
		
		setupLists();
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
				loadNearbyTask.execute(new String[] {NEARBY});
			}	
		});
		backgroundThread.run();
	}
	
	public class RewardLoadTask extends AsyncTask<String, Void, Void> {

		RewardAdapter adapter;
		ListFragment toUpdate;
		
		@Override
		protected Void doInBackground(String... params) {
			String type = params[0];
			Reward[] rewards = new Reward[0];
			if (IN_PROGRESS.equals(type)) {
				rewards = TapTagAPI.progressByUser(mPrefs.getInt("user_id", -1));
				toUpdate = inProgressFragment;
			}
			if (COMPLETED.equals(type)) {
				//TODO
				toUpdate = completedFragment;
			}
			if (NEARBY.equals(type)) {
				//TODO
				toUpdate = nearbyFragment;
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
