package com.taptag.beta;

import java.util.Date;
import com.taptag.beta.vendor.AbstractVendorAdapter;
import com.taptag.beta.vendor.ProximityVendorAdapter;
import com.taptag.beta.vendor.Vendor;
import com.taptag.beta.vendor.AlphabeticalVendorAdapter;
import com.taptag.beta.location.LatLong;
import com.taptag.beta.location.TagAddress;
import com.taptag.beta.network.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class VendorListActivity extends NetworkActivity {

	private TextView titleView;
	private ListView vendorListView;
	private AutoCompleteTextView vendorAutoComplete;
	private TextWatcher filterTextWatcher;
	private AbstractVendorAdapter adapter;
	private ArrayAdapter<String> autoCompleteAdapter;
	private Vendor[] allData;
	private ProgressBar loadingSpinner;
	private SharedPreferences mPrefs;
	private boolean placesLoaded;
	private Date lastPlacesLoad;
	
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location recentLocation;
	private String mode;

	public static final String NEAR_ME = "Places Nearby";
	public static final String IN_ORDER = "My Places";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vendorlist);
		mPrefs = getSharedPreferences("TapTag", MODE_PRIVATE);

		allData = new Vendor[0];
		
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		vendorListView = (ListView) findViewById(R.id.vendorList);
		titleView = (TextView) findViewById(R.id.vendorListTitle);
		loadingSpinner = (ProgressBar) findViewById(R.id.vendorListProgress);
		placesLoaded = false;
		lastPlacesLoad = new Date(0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		placesLoaded = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// Load all places in background
		if (hasFocus && !placesLoaded && shouldLoadPlaces()) {
			loadingSpinner.setVisibility(View.VISIBLE);
			final Intent intent = getIntent();
			Thread backgroundThread = new Thread(new Runnable() {
				public void run() {
					configureByIntent(intent);
				}
			});
			backgroundThread.run();
		}
		placesLoaded = true;
	}

	/**
	 * Load the list contents based on the intent type, and set the window title
	 * 
	 * @param intent
	 */
	public void configureByIntent(Intent intent) {
		String action = intent.getAction();
		titleView.setText(action);
		mode = action;
		if (NEAR_ME.equals(action)) {
			Log.i("TapTag", "PLACES NEAR ME");
			setupLocationListener();
			getRecentLocation();
		} else {
			Log.i("TapTag", "PLACES IN ORDER");
			LoadPlacesTask loadPlacesTask = new LoadPlacesTask();
			loadPlacesTask.execute(null, null);
		}

	}

	/**
	 * Instantiate the necessary TextWatcher for AutoComplete to work
	 */
	public void configureAutoComplete() {
		vendorAutoComplete = (AutoCompleteTextView) findViewById(R.id.vendorFilter);
		autoCompleteAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, adapter.getNames());
		vendorAutoComplete.setAdapter(autoCompleteAdapter);

		filterTextWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				adapter.getFilter().filter(s);
			}
		};

		vendorAutoComplete.addTextChangedListener(filterTextWatcher);
	}

	/**
	 * Configure the behavior when a list item is clicked
	 */
	public void configureOnClickListener() {
		vendorListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Vendor clicked = adapter.getItem(position);
				Intent toVendor = new Intent(VendorListActivity.this, VendorActivity.class);
				toVendor.setAction(VendorActivity.VIEW_VENDOR);
				Bundle extras = new Bundle();
				extras.putSerializable("vendor", clicked);
				toVendor.putExtras(extras);
				VendorListActivity.this.startActivity(toVendor);
			}
		});
	}

	/**
	 * Check if places have been loaded too recently (under 2 minutes)
	 * 
	 * @return
	 */
	private boolean shouldLoadPlaces() {
		Date now = new Date();
		Long twoMinutes = (long) (2 * 60 * 1000);
		return ((now.getTime() - lastPlacesLoad.getTime()) > twoMinutes);
	}

	/**
	 * Asynchronously populate the ListView. All behavior in onPostExecute
	 * because doInBackground can't access the UI thread
	 */
	public class LoadPlacesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			lastPlacesLoad = new Date();
			if (NEAR_ME.equals(mode)) {
				adapter = new ProximityVendorAdapter(VendorListActivity.this, R.layout.vendorlistitem, R.id.vendorListItemName, allData, getLatLong());
			} else {
				adapter = new AlphabeticalVendorAdapter(VendorListActivity.this, R.layout.vendorlistitem, R.id.vendorListItemName, allData);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (NEAR_ME.equals(mode)) {
				allData = TapTagAPI.vendorsNear(getLatLong(), 500);
			} else {
				allData = TapTagAPI.vendorsVisitedBy(mPrefs.getInt("user_id", 0));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			vendorListView.setAdapter(adapter);
			adapter.replaceAllData(allData);
			VendorListActivity.this.configureAutoComplete();
			VendorListActivity.this.configureOnClickListener();
			adapter.notifyDataSetChanged();
			loadingSpinner.setVisibility(View.GONE);
		}
	}
	
	//=========================================================
	//========================LOCATION=========================
	//=========================================================
	
	public void setupLocationListener() {
		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				recentLocation = location;
				LoadPlacesTask loadPlacesTask = new LoadPlacesTask();
				loadPlacesTask.execute(null, null);
			}
			@Override
			public void onProviderDisabled(String provider) {
			}
			@Override
			public void onProviderEnabled(String provider) {
			}
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {	
			}
		};
	}
 	
	public void getRecentLocation() {
		locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
	}
	
	public LatLong getLatLong() {
		if (recentLocation == null) {
			return new LatLong();
		} else {
			return new LatLong(recentLocation.getLatitude(), recentLocation.getLongitude());
		}
	}

}
