package com.taptag.beta.location;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class Locator {
	
	private Context context;
	private LocationManager locationManager;
	private Location recentLocation;
	private LocationListener locationListener;
	private List<String> providers;
	
	public Locator(Context context) {
		this.context = context;
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		providers = locationManager.getProviders(true);
		initListener();
	}
	
	/**
	 * Initialize the Location Change listener service
	 */
	private void initListener() {
		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				acceptLocation(location);		
			}

			@Override
			public void onProviderDisabled(String provider) {
				providers.remove(provider);		
			}

			@Override
			public void onProviderEnabled(String provider) {
				if (!providers.contains(provider)) {
					providers.add(provider);
				}
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub	
			}			
		};
		startLocating();
	}
	
	/**
	 * Get a location from the listener, and store it
	 * @param location
	 */
	private void acceptLocation(Location location) {
		recentLocation = location;	
	}
	
	/**
	 * Return the most recent location object
	 * @return
	 */
	public Location getLocation() {
		return recentLocation;
	}
	
	/**
	 * Get the latitude and longitude of the User's location
	 * @return
	 */
	public Double[] getLatLong() {
		if (recentLocation != null) {
			Log.i("TapTag", "Getting Good Location");
			return new Double[] {recentLocation.getLatitude(), recentLocation.getLongitude()};
		} else {
			Log.i("TapTag", "Getting Last Known Location");
			return getLastKnownLocation();
		}
	}
	
	/**
	 * Begin the locator service
	 */
	public void startLocating() {
		//Wait at minimum 1second and/or 100m
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 100, locationListener);
	}
	
	/**
	 * Stop the locator service
	 */
	public void stopLocating() {
		locationManager.removeUpdates(locationListener);
	}
	
	/**
	 * Get the last known location from any provider (most accurate first), in case
	 * there has not been a location reported to this service yet
	 * @return
	 */
	private Double[] getLastKnownLocation() {
		Location location = null;
		//Check the providers in reverse order
		for (int i = (providers.size() - 1); i >= 0; i--) {
			location = locationManager.getLastKnownLocation(providers.get(i));
			if (location != null) {
				break;
			}
		}
		//Empire State Building
		Double[] result = new Double[] {40.713956, -74.003906};
		if (location != null) {
			result[0] = location.getLatitude();
			result[1] = location.getLongitude();
		}
		return result;
	}

}
