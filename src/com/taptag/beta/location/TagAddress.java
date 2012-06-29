package com.taptag.beta.location;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

@SuppressWarnings("serial")
public class TagAddress implements Serializable {
	
	private String streetAddress1;
	private String streetAddress2;
	private String city;
	private String state;
	private String zip;
	private Double[] latLong;
	
	public TagAddress(String streetAddress1, String streetAddress2, String city, String state, String zip, String country) {
		this.streetAddress1 = streetAddress1;
		this.streetAddress2 = streetAddress2;
		this.city = city;
		this.state = state;
		latLong = null;
	}
	
	public TagAddress(String streetAddress1, String city, String state, String zip) {
		this.streetAddress1 = streetAddress1;
		this.streetAddress2 = "";
		this.city = city;
		this.state = state;
		this.zip = zip;
		latLong = null;
	}
	
	public String toString() {
		String result = "";
		result += streetAddress1;
		if (streetAddress2 != null && !streetAddress2.equals("")) {
			result += (", " + streetAddress2);
		}
		result += (", " + city + " " + state + ", " + zip);
		return result;
	}
	
	/**
	 * Get the latitude and longitude from the TagAddress, uses the internet
	 * @param context
	 * @return
	 */
	public Double[] getLatLong(Context context) {
		if (latLong != null) {
			return latLong;
		}
		Geocoder code = new Geocoder(context);
		List<Address> addresses;
		try {
			addresses = code.getFromLocationName(this.toString(), 2);

			if(addresses.size() > 0) {
				Address bestGuess = addresses.get(0);
				latLong = new Double[] {bestGuess.getLatitude(), bestGuess.getLongitude()};
			} else {
				latLong = new Double[] {0.0, 0.0};
			}
		} catch (IOException e) {
			latLong = new Double[] {0.0, 0.0};
		}
		return latLong;
	}
	
	/**
	 * Get the distance between two {lat, long} points
	 * @param thisLatLong
	 * @param thatLatLong
	 * @return
	 */
	public static Double getDistance(Double[] thisLatLong, Double[] thatLatLong) {
		if (thisLatLong == null || thatLatLong == null || thisLatLong.length < 2 || thatLatLong.length < 2) {
			return 0.0;
		}
		float[] result = new float[5];
		Location.distanceBetween (thisLatLong[0], thisLatLong[1], thatLatLong[0], thatLatLong[1], result);
		return ((double) result[0]);
	}

}
