package com.taptag.beta.reward;

import android.app.ListFragment;

public class RewardTabFragment extends ListFragment {

	private String tag;
	
	public RewardTabFragment() {
		this.tag = "None";
	}
	
	public RewardTabFragment(String tag) {
		this.tag = tag;
	}
	
}
