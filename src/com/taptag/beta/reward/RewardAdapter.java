package com.taptag.beta.reward;

import java.util.Arrays;

import com.taptag.beta.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RewardAdapter extends ArrayAdapter<Reward> {

	Context context;
	int textViewResourceId;
	Reward[] data = null;

	public RewardAdapter(Context context, int textViewResourceId,
			Reward[] objects) {
		super(context, textViewResourceId, objects);
		this.textViewResourceId = textViewResourceId;
		this.context = context;
		this.data = objects;
	}
	
	@Override
	public void notifyDataSetChanged() {
		sortData();
		super.notifyDataSetChanged();
	}
	
	public void sortData() {
		Arrays.sort(data);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(textViewResourceId, parent, false);
		}

		TextView titleView = (TextView) row.findViewById(R.id.rewardListTitle);
		TextView descriptionView = (TextView) row.findViewById(R.id.rewardListDescription);
		ProgressBar rewardProgress = (ProgressBar) row.findViewById(R.id.rewardListProgress);

		Reward reward = data[position];
		titleView.setText(reward.getName() + " (" + reward.getProgressString() + ")");
		descriptionView.setText(reward.getDescription());
		rewardProgress.setMax(reward.getTotal());
		rewardProgress.setProgress(reward.getProgressBounded());

		return row;
	}
	
	public void replaceAllData(Reward[] data) {
		this.data = data;
	}

}
