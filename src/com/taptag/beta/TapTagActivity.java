package com.taptag.beta;

import com.taptag.beta.R;
import com.taptag.beta.nfc.NFCActions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
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
	private String toWrite;

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
		toWrite = "No Selection Made";

		// Handle NFC Pending Intents
		nfcIntent = PendingIntent.getActivity(this, 0, (new Intent(this,
				getClass())).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// Add intent filter for reading from a tag
		IntentFilter ndefDetected = new IntentFilter(
				NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefDetected.addDataType(TAG_MIME);
		} catch (MalformedMimeTypeException e) {
			Log.e(TAG, "Failed to add MIME type");
		}
		ndefExchangeFilters = new IntentFilter[] { ndefDetected };
		// Intent filter for writing to a tag
		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		writeTagFilters = new IntentFilter[] { tagDetected };

		// Initialize the list view
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
		if (writeMode
				&& NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			boolean writeResult = NFCActions.writeTag(detectedTag, "My App",
					toWrite);
			if (writeResult) {
				toastShort("Tag Written");
			} else {
				toastShort("Tag Not Written");
			}
		}
	}

	/**
	 * Populate the List view and set the OnItemClickListener
	 */
	private void initListView() {
		ListView writeList = (ListView) findViewById(R.id.writelist);
		String[] sampleValues = { "140 East 14th Street, New York, NY 10003",
				"1147 Barbara Drive, Cherry Hill, NJ 08003",
				"3706 Locust Walk, Philadelphia PA 19104" };
		// First - context
		// Second - layout for the row
		// Third - ID of the View to which the data is written
		// Fourth - array of data
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				sampleValues);

		writeList.setAdapter(adapter);
		writeList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Get the text from the current selected view
				TextView clickedView = (TextView) view;
				String clickedText = (String) clickedView.getText();
				toWrite = clickedText;
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
		adapter.enableForegroundNdefPush(this, NFCActions.stringAsNdef(toWrite));
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
		ad.setMessage(toWrite);
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

	private void toastShort(String message) {
		Toast.makeText(TapTagActivity.this, message, Toast.LENGTH_SHORT).show();
	}
}