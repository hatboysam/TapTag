package com.taptag.beta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;

import com.taptag.beta.nfc.NFCActions;
import com.taptag.beta.reward.Reward;
import com.taptag.beta.reward.RewardAdapter;
import com.taptag.beta.vendor.Vendor;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

public class VendorActivity extends Activity {

	public static final String VIEW_VENDOR = "View Vendor";

	private ListView rewardListView;
	private TextView vendorNameTextView;
	private TextView vendorAddressTextView;

	private NfcAdapter nfcAdapter;
	private PendingIntent nfcIntent;

	private ObjectMapper objectMapper = new ObjectMapper();
	private JsonFactory jsonFactory = new JsonFactory();
	private JsonParser jp = null;

	private Vendor vendor = new Vendor();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vendor);

		Reward[] sampleRewards = new Reward[] {
				new Reward("Free Sammich", 3, 9),
				new Reward("Come 10 Times", 9, 10),
				new Reward("Be Totally Awesome", 2, 11) };

		// Getting the json.
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		connect();
		new Connection().execute();

		RewardAdapter adapter = new RewardAdapter(this,
				R.layout.rewardlistitem, sampleRewards);

		vendorNameTextView = (TextView) findViewById(R.id.vendorName);
		vendorAddressTextView = (TextView) findViewById(R.id.vendorAddress);
		rewardListView = (ListView) findViewById(R.id.rewardlist);
		rewardListView.setAdapter(adapter);

		// Check for NFC
		nfcIntent = PendingIntent.getActivity(this, 0, (new Intent(this,
				getClass())).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			setInfoFromNFCIntent(intent);
			setIntent(new Intent()); // Consume this intent.
		} else if (VendorActivity.VIEW_VENDOR.equals(intent.getAction())) {
			Bundle extras = intent.getExtras();
			//Vendor v = (Vendor) extras.get("vendor");
			setInfoFromVendor(vendor);
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			setInfoFromNFCIntent(intent);
		} else if (VendorActivity.VIEW_VENDOR.equals(intent.getAction())) {
			Bundle extras = intent.getExtras();
			//Vendor v = (Vendor) extras.get("vendor");
			setInfoFromVendor(vendor);
		}
	}

	/**
	 * Load the Vendor information from an NFC tag
	 * 
	 * @param intent
	 */
	private void setInfoFromNFCIntent(Intent intent) {
		NdefMessage firstMessage = NFCActions.getFirstMessage(intent);
		NdefRecord[] records = firstMessage.getRecords();
		String firstString = new String(records[0].getPayload());
		String secondString = new String(records[1].getPayload());
		// Set title and address
		vendorNameTextView.setText(firstString);
		vendorAddressTextView.setText(secondString);
	}

	/**
	 * Load the Vendor information from the intent Bundle
	 * 
	 * @param vendor
	 */
	private void setInfoFromVendor(Vendor vendor) {
		// Set title and address
		vendorNameTextView.setText(vendor.getName());
		vendorAddressTextView.setText(vendor.getAddress().toString());
	}

	private class Connection extends AsyncTask {

		@Override
		protected Object doInBackground(Object... arg0) {
			connect();
			return null;
		}
	}

	private void connect() {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(
					"https://taptag.herokuapp.com/vendors/44.json");
			HttpResponse response = client.execute(request);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();

			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}

			String json = builder.toString();

			try {
				jp = jsonFactory.createJsonParser(json);
				objectMapper.configure(Feature.UNWRAP_ROOT_VALUE, true);
				vendor = objectMapper.readValue(jp, Vendor.class);
				Log.e(vendor.getName(), vendor.toString());
			} catch (JsonParseException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}