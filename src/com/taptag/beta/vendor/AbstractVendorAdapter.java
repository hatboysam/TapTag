package com.taptag.beta.vendor;

import java.util.ArrayList;

import com.taptag.beta.R;
import com.taptag.beta.list.AbstractCustomAdapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public abstract class AbstractVendorAdapter extends
		AbstractCustomAdapter<Vendor> {

	Filter filter;

	public AbstractVendorAdapter(Context context, int viewId,
			int textViewResourceId, Vendor[] objects) {
		super(context, viewId, textViewResourceId, objects);
		this.filter = new VendorFilter();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(viewId, parent, false);
		}

		TextView nameView = (TextView) row
				.findViewById(R.id.vendorListItemName);
		TextView addressView = (TextView) row
				.findViewById(R.id.vendorListItemAddress);

		Vendor vendor = data[position];
		nameView.setText(vendor.getName());
		addressView.setText(vendor.getAddress().toString());

		return row;
	}

	public String[] getNames() {
		String[] results = new String[originalData.length];
		for (int i = 0; i < originalData.length; i++) {
			results[i] = originalData[i].getName();
		}
		return results;
	}

	public Filter getFilter() {
		return filter;
	}
	
	public void replaceAllData(Vendor[] objects) {
		this.data = objects;
		this.originalData = objects;
	}

	public abstract void sortData();

	private class VendorFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			if (constraint == null || constraint.length() == 0) {
				results.values = originalData;
				results.count = originalData.length;
			} else {
				String filterString = constraint.toString().toLowerCase();
				ArrayList<Vendor> matches = new ArrayList<Vendor>();
				for (Vendor v : originalData) {
					String lowerName = v.getName().toLowerCase();
					if (lowerName.contains(filterString)) {
						matches.add(v);
					}
				}
				int resultSize = matches.size();
				Vendor[] matchingVendors = matches
						.toArray(new Vendor[resultSize]);
				results.count = resultSize;
				results.values = matchingVendors;
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			Vendor[] vendors = (Vendor[]) results.values;
			AbstractVendorAdapter.this.data = vendors;
			notifyDataSetChanged();
			// clear();
		}

	}

}
