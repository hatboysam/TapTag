package com.taptag.beta;

import com.taptag.beta.R;
import com.taptag.beta.network.TapTagAPI;
import com.taptag.beta.nfc.NFCActions;
import com.taptag.beta.vendor.AlphabeticalVendorAdapter;
import com.taptag.beta.vendor.Vendor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.view.View;

public class TapTagActivity extends Activity {

	/**
	 * NFC fields
	 */
	private NfcAdapter adapter;
	private boolean writeMode;
	private boolean resumed;
	private PendingIntent nfcIntent;
	private IntentFilter[] writeTagFilters;
	private IntentFilter[] ndefExchangeFilters;
	private Vendor toWrite;
	private Tag recentTag;
	private Vendor[] allData;
	private ProgressDialog loading;
	private ListView writeList;
	private AlphabeticalVendorAdapter listAdapter;

	/**
	 * Static/Final Things
	 */
	private static final String TAG = "TapTag";
	private static final String TAG_MIME = "application/com.taptag.tag";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Initialize NFC fields
		adapter = NfcAdapter.getDefaultAdapter(this);
		writeMode = false;
		resumed = false;
		toWrite = new Vendor();

		// Handle NFC Pending Intents
		nfcIntent = PendingIntent.getActivity(this, 0, (new Intent(this, getClass())).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// Add intent filter for reading from a tag
		IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefDetected.addDataType(TAG_MIME);
		} catch (MalformedMimeTypeException e) {
			Log.e(TAG, "Failed to add MIME type");
		}
		ndefExchangeFilters = new IntentFilter[] { ndefDetected };
		// Intent filter for writing to a tag
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		writeTagFilters = new IntentFilter[] { tagDetected };

		// Initialize the list view
		writeList = (ListView) findViewById(R.id.writelist);
		initListView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		enableExchangeMode();
	}

	@Override
	protected void onPause() {
		super.onPause();
		resumed = false;
		adapter.disableForegroundNdefPush(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Writing mode
		if (writeMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			this.recentTag = detectedTag;
			WriteTagTask writeTagTask = new WriteTagTask();
			writeTagTask.execute(null, null);
		}
	}

	/**
	 * Populate the List view and set the OnItemClickListener
	 */
	private void initListView() {
		showLoadingDialog();
		Thread backgroundThread = new Thread(new Runnable() {
			public void run() {
				LoadPlacesTask task = new LoadPlacesTask();
				task.execute(null, null, null);
			}
		});
		backgroundThread.run();
	}
	
	private void configureOnClickListener() {
		writeList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Get the text from the current selected view
				Vendor vendor = listAdapter.getItem(position);
				toWrite = vendor;
				// Popup with write prompt
				disableExchangeMode();
				showWriteDialog();
			}
		});
	}

	/**
	 * Enable writing to NFC tags
	 */
	private void enableWriteMode() {
		writeMode = true;
		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		writeTagFilters = new IntentFilter[] { tagDetected };
		adapter.enableForegroundDispatch(this, nfcIntent, writeTagFilters, null);
	}

	/**
	 * Enable NFC tag exchange mode
	 */
	private void enableExchangeMode() {
		adapter.enableForegroundNdefPush(this, NFCActions.vendorAsNdef(toWrite));
		adapter.enableForegroundDispatch(this, nfcIntent, ndefExchangeFilters,
				null);
	}

	/**
	 * Disable NFC tag exchange mode
	 */
	private void disableExchangeMode() {
		adapter.disableForegroundNdefPush(this);
		adapter.disableForegroundDispatch(this);
	}

	/**
	 * Disable writing to NFC tags
	 */
	private void disableWriteMode() {
		writeMode = false;
		adapter.disableForegroundDispatch(this);
	}

	/**
	 * Enable write mode and show a dialog
	 */
	private void showWriteDialog() {
		enableWriteMode();
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle("Write Tag");
		ad.setMessage(toWrite.getName());
		ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				disableWriteMode();
				// Re-enable read
				enableExchangeMode();
			}
		});
		ad.create();
		ad.show();
	}
	
	/**
	 * Display a loading dialog, for long operations
	 */
	public void showLoadingDialog() {
		if (loading == null) {
			loading = ProgressDialog.show(TapTagActivity.this, "TapTag", "Loading Places...", true);
			loading.setCancelable(true);
		} else {
			loading.show();
		}
	}

	private void toastShort(String message) {
		Toast.makeText(TapTagActivity.this, message, Toast.LENGTH_SHORT).show();
	}
	
	public class LoadPlacesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			allData = TapTagAPI.vendorsVisitedBy(402);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			//TODO create and set adapter
			listAdapter = new AlphabeticalVendorAdapter(TapTagActivity.this,
					R.layout.vendorlistitem, R.id.vendorListItemName, allData);
			writeList.setAdapter(listAdapter);
			configureOnClickListener();
			listAdapter.notifyDataSetChanged();
			loading.dismiss();
		}
	}
	
	public class WriteTagTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... messages) {
			Tag detectedTag = TapTagActivity.this.recentTag;
			boolean writeResult = NFCActions.writeTag(detectedTag, toWrite);
			return writeResult;
		}
		
		@Override
		protected void onPostExecute(Boolean writeResult) {
			if (writeResult) {
				toastShort("Tag Written: " + toWrite.getName());
			} else {
				toastShort("Tag Not Written");
			}
		}
		
	}
}