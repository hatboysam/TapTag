package com.taptag.beta.vendor;

import java.util.Arrays;
import java.util.Comparator;

import com.taptag.beta.location.TagAddress;

import android.content.Context;
import android.util.Log;

public class ProximityVendorAdapter extends AbstractVendorAdapter {

	private Double[] location;
	private Double maxDistance;
	private VendorSorter sorter;

	public ProximityVendorAdapter(Context context, int viewId, int textViewResourceId, Vendor[] objects, Double[] location, Double maxDistance) {
		super(context, viewId, textViewResourceId, objects);
		this.location = location;
		this.maxDistance = maxDistance;
		this.sorter = new VendorSorter(location);
	}

	@Override
	public void sortData() {
		//TODO implement max distance filter
		Arrays.sort(data, sorter);
	}

	public class VendorSorter implements Comparator<Vendor> {	

		Double[] sortLocation;

		public VendorSorter(Double[] sortLocation) {
			this.sortLocation = sortLocation;
		}

		@Override
		public int compare(Vendor v0, Vendor v1) {
			Double[] latLong0 = v0.getAddress().getLatLong(context);
			Double[] latLong1 = v1.getAddress().getLatLong(context);
			Double dist0 = TagAddress.getDistance(latLong0, sortLocation);
			Double dist1 = TagAddress.getDistance(latLong1, sortLocation);
			return dist0.compareTo(dist1);
		}
	}

}
