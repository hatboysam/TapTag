package com.taptag.beta.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

public class NFCActions {
	
	private static final String TAG = "TapTag";
	private static final String TAG_MIME = "application/com.taptag.tag";
	
    /**
     * Convert a string to a writeable NdefMessage (single NdefRecord)
     * @param toTranslate
     * @return
     */
    public static NdefMessage stringAsNdef(String toTranslate) {
    	byte[] stringBytes = toTranslate.getBytes();
    	NdefRecord stringRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, TAG_MIME.getBytes(), new byte[] {}, stringBytes);
    	return new NdefMessage(new NdefRecord[] { stringRecord });
    }
    
    /**
     * Overloaded.
     * Convert a series of strings to a multi-NdefRecord NdefMessage
     * @param strings
     * @return
     */
     public static NdefMessage stringAsNdef(String... strings) {
    	int numRecords = strings.length;
    	NdefRecord[] allRecords = new NdefRecord[numRecords];
    	for (int i = 0; i < numRecords; i++) {
    		byte[] stringBytes = strings[i].getBytes();
    		NdefRecord stringRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, TAG_MIME.getBytes(), new byte[] {}, stringBytes);
    		allRecords[i] = stringRecord;
    	}
    	return new NdefMessage(allRecords);
    }
     
     /**
      * Write a tag with a variable number of records
      * @param tag
      * @param messages
      * @return
      */
     public static boolean writeTag(Tag tag, String... messages) {
     	NdefMessage fromString = NFCActions.stringAsNdef(messages);
     	Ndef ndef = Ndef.get(tag);
     	try {
 	    	if (ndef != null) {
 	    		ndef.connect();
 	    		if (!ndef.isWritable()) {
 	    			Log.i(TAG, "This tag is read only, sorry!");
 	    			return false;
 	    		}
 	    		if (ndef.getMaxSize() < fromString.toByteArray().length) {
 	    			Log.i(TAG, "The message is too big!");
 	    			return false;
 	    		}
 	    		ndef.writeNdefMessage(fromString);
 	    		return true;
 	    	} else {
 	    		NdefFormatable format = NdefFormatable.get(tag);
 	    		if (format != null) {
 	    			try {
 	    				format.connect();
 	    				format.format(fromString);
 	    				Log.i(TAG, "Formatted Tag and Wrote Message");
 	    				return true;
 	    			} catch (Exception e) {
 	    				Log.e(TAG, e.getMessage());
 	    				return false;
 	    			}
 	    		} else {
 	    			Log.i(TAG, "NDEF not supported, it seems");
 	    			return false;
 	    		}
 	    	}
     	} catch (Exception e) {
     		Log.e("TagTap", e.getMessage());
     		return false;
     	}
     }
     
     /**
      * Copied straight from Google Sticky Notes
      * @param intent
      * @return
      */
     public static NdefMessage[] getNdefMessages(Intent intent) {
         // Parse the intent
         NdefMessage[] msgs = null;
         String action = intent.getAction();
         if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
             Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
             if (rawMsgs != null) {
                 msgs = new NdefMessage[rawMsgs.length];
                 for (int i = 0; i < rawMsgs.length; i++) {
                     msgs[i] = (NdefMessage) rawMsgs[i];
                 }
             } else {
                 // Unknown tag type
                 byte[] empty = new byte[] {};
                 NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                 NdefMessage msg = new NdefMessage(new NdefRecord[] {
                     record
                 });
                 msgs = new NdefMessage[] {
                     msg
                 };
             }
         } else {
             Log.e(TAG, "Unknown intent");
         }
         return msgs;
     }
     
     /**
      * Get the message from a tag known to have a single message
      * @param intent
      * @return
      */
     public static NdefMessage getFirstMessage(Intent intent) {
     	return (getNdefMessages(intent))[0];
     }

}
