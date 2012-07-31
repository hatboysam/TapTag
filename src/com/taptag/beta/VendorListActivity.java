package com.taptag.beta;

import java.util.Date;
import com.taptag.beta.location.TagAddress;
import com.taptag.beta.vendor.AbstractVendorAdapter;
import com.taptag.beta.vendor.ProximityVendorAdapter;
import com.taptag.beta.vendor.Vendor;
import com.taptag.beta.vendor.AlphabeticalVendorAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.TextView;

public class VendorListActivity extends Activity {

	private TextView titleView;
	private ListView vendorListView;
	private AutoCompleteTextView vendorAutoComplete;
	private TextWatcher filterTextWatcher;
	private AbstractVendorAdapter adapter;
	private ArrayAdapter<String> autoCompleteAdapter;
	private Vendor[] allData;
	private ProgressDialog loading;
	// private Locator locator;
	private boolean placesLoaded;
	private Date lastPlacesLoad;

	public static final String NEAR_ME = "Places Nearby";
	public static final String IN_ORDER = "My Places";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vendorlist);
		// locator = new Locator(this);

		allData = new Vendor[] {
				new Vendor("Pizza Palace", new TagAddress(
						"140 East 14th Street", "New York", "NY", "10003")),
				new Vendor("Dunkin Donuts", new TagAddress("425 Corte Sur",
						"Novato", "CA", "94949")),
				new Vendor("Chipotle", new TagAddress("3706 Locust Walk",
						"Philadelphia", "PA", "19104")),
				new Vendor("Duane Reade", new TagAddress("1147 Barbara Drive",
						"Cherry Hill", "NJ", "08003")),
				new Vendor("Varun's Curry Emporium", new TagAddress(
						"10475 Crosspoint Boulevard", "Indianapolis", "IN",
						"46256")) };

		vendorListView = (ListView) findViewById(R.id.vendorList);
		titleView = (TextView) findViewById(R.id.vendorListTitle);
		placesLoaded = false;
		lastPlacesLoad = new Date(0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// locator.startLocating();
		placesLoaded = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		// locator.stopLocating();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// locator.stopLocating();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// Load all places in background
		if (hasFocus && !placesLoaded && shouldLoadPlaces()) {
			showLoadingDialog();
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
	 * Get the user's location
	 * 
	 * @return
	 */
	private Double[] getLocation() {
		Double[] result = null; // = locator.getLatLong();
		// Log.i("TapTag", result[0].toString() + ", " + result[1].toString());
		return result;
	}

	/**
	 * Display a loading dialog, for long operations
	 */
	public void showLoadingDialog() {
		if (loading == null) {
			loading = ProgressDialog.show(VendorListActivity.this, "TapTag",
					"Loading Places...", true);
			loading.setCancelable(true);
		} else {
			loading.show();
		}
	}

	/**
	 * Load the list contents based on the intent type, and set the window title
	 * 
	 * @param intent
	 */
	public void configureByIntent(Intent intent) {
		String action = intent.getAction();
		titleView.setText(action);
		Log.i("TapTag", "Making Adapter...");
		if (NEAR_ME.equals(action)) {
			Log.i("TapTag", "PLACES NEAR ME");
			adapter = new ProximityVendorAdapter(VendorListActivity.this,
					R.layout.vendorlistitem, R.id.vendorListItemName, allData,
					getLocation(), 10.0);
		} else {
			Log.i("TapTag", "PLACES IN ORDER");
			adapter = new AlphabeticalVendorAdapter(VendorListActivity.this,
					R.layout.vendorlistitem, R.id.vendorListItemName, allData);
		}
		LoadPlacesTask loadPlacesTask = new LoadPlacesTask();
		loadPlacesTask.execute(null, null, null);
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
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Vendor clicked = adapter.getItem(position);
				Intent toVendor = new Intent(VendorListActivity.this,
						VendorActivity.class);
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
	 * Asynchronously populate the ListView. All beahvior in onPostExecute
	 * because doInBackground can't access the UI thread
	 */
	public class LoadPlacesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			lastPlacesLoad = new Date();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Log.i("TapTag", "Setting Adapter...");
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			vendorListView.setAdapter(adapter);
			VendorListActivity.this.configureAutoComplete();
			VendorListActivity.this.configureOnClickListener();
			adapter.notifyDataSetChanged();
			loading.dismiss();
		}
	}

}
