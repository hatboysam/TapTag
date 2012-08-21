package com.taptag.beta.vendor;

import java.util.Arrays;
import java.util.Comparator;

import com.taptag.beta.location.LatLong;
import com.taptag.beta.location.TagAddress;

import android.content.Context;
import android.util.Log;

public class ProximityVendorAdapter extends AbstractVendorAdapter {

	private LatLong location;
	private VendorSorter sorter;

	public ProximityVendorAdapter(Context context, int viewId, int textViewResourceId, Vendor[] objects, LatLong location) {
		super(context, viewId, textViewResourceId, objects);
		this.location = location;
		this.sorter = new VendorSorter(this.location);
	}

	@Override
	public void sortData() {
		Arrays.sort(data, sorter);
	}
	
	public void setLocation(LatLong newLocation) {
		this.location = newLocation;
		sorter.setLocation(newLocation);
	}

	public class VendorSorter implements Comparator<Vendor> {

		LatLong sortLocation;

		public VendorSorter(LatLong sortLocation) {
			this.sortLocation = sortLocation;
		}
		
		public void setLocation(LatLong newLocation) {
			sortLocation = newLocation;
		}

		@Override
		public int compare(Vendor v0, Vendor v1) {
			LatLong latLong0 = v0.getAddress().getCoordinates();
			LatLong latLong1 = v0.getAddress().getCoordinates();
			Double dist0 = TagAddress.getDistance(latLong0, sortLocation);
			Double dist1 = TagAddress.getDistance(latLong1, sortLocation);
			return dist0.compareTo(dist1);
		}

	}

}
