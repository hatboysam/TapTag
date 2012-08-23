package com.taptag.beta.reward;

import com.taptag.beta.RewardsActivity;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class RewardTabFragment extends ListFragment {

	private String tag;
	private AlertDialog ad;
	private ListView listView;
	private boolean readMode;
	private Reward clicked;
	
	public RewardTabFragment() {
		this.tag = "None";
		readMode = false;
	}
	
	public RewardTabFragment(String tag) {
		this.tag = tag;
		readMode = false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		this.listView = getListView();
		if (RewardsActivity.COMPLETED.equals(tag)) {
			setCompletedActivity();
		}
	}
	
	public void setCompletedActivity() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				readMode = true;
				Object object = RewardTabFragment.this.getListAdapter().getItem(position);
				clicked = (Reward) object;
				showCompletedDialog(clicked);
			}
		});
	}
	
	private void showCompletedDialog(Reward reward) {
		ad = (new AlertDialog.Builder(this.getActivity())).create();
		ad.setCancelable(false);
		ad.setCanceledOnTouchOutside(false);
		ad.setTitle("Redeem Reward");
		ad.setMessage("Place phone on TapTag to redeem '" + reward.getName() + "'");
		ad.setButton(AlertDialog.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ad.dismiss();
				readMode = false;
			}
		});
		ad.show();
	}
	
	public void dismissDialog() {
		if (ad != null) {
			ad.dismiss();
		}
	}
	
	public boolean inReadMode() {
		return readMode;
	}
	
	public Reward getSelectedReward() {
		if (readMode) {
			return clicked;
		} else {
			return null;
		}
	}
	
}
