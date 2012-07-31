package com.taptag.beta.vendor;

import java.util.ArrayList;
import java.util.Arrays;

import com.taptag.beta.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class AlphabeticalVendorAdapter extends AbstractVendorAdapter {

	public AlphabeticalVendorAdapter(Context context, int viewId,
			int textViewResourceId, Vendor[] objects) {
		super(context, viewId, textViewResourceId, objects);
	}

	public void sortData() {
		Arrays.sort(data);
	}
}
