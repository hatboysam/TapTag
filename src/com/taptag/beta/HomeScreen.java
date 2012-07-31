package com.taptag.beta;


import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.taptag.beta.R;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class HomeScreen extends Activity {
	private Facebook facebook = new Facebook("467907829887006");
	private SharedPreferences mPrefs;
	private static final boolean USE_FACEBOOK = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		// Set the click behavior for the My Places button
		ImageButton toVendors = (ImageButton) findViewById(R.id.myPlacesButton);
		toVendors.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent toVendorList = new Intent(HomeScreen.this,
						VendorListActivity.class);
				toVendorList.setAction(VendorListActivity.IN_ORDER);
				HomeScreen.this.startActivity(toVendorList);
			}
		});

		// Set the click behavior for the Places Near Me button
		ImageButton toNearbyVendors = (ImageButton) findViewById(R.id.nearMeButton);
		toNearbyVendors.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent toVendorList = new Intent(HomeScreen.this,
						VendorListActivity.class);
				toVendorList.setAction(VendorListActivity.NEAR_ME);
				HomeScreen.this.startActivity(toVendorList);
			}
		});

		// Set the click behavior for the tag writing button
		Button toWrite = (Button) findViewById(R.id.writeButton);
		toWrite.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent toVendor = new Intent(HomeScreen.this,
						TapTagActivity.class);
				HomeScreen.this.startActivity(toVendor);
			}
		});

		/*
		 * Get existing access_token if any
		 */
		mPrefs = getPreferences(MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);
		if (access_token != null) {
			facebook.setAccessToken(access_token);
		}
		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		/*
		 * Only call authorize if the access_token has expired.
		 */
		if (USE_FACEBOOK && !facebook.isSessionValid()) {
			facebook.authorize(this, new String[] {}, new DialogListener() {
				public void onComplete(Bundle values) {
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("access_token", facebook.getAccessToken());
					editor.putLong("access_expires",
							facebook.getAccessExpires());
					editor.commit();
				}
				public void onFacebookError(FacebookError error) {
				}
				public void onError(DialogError e) {
				}
				public void onCancel() {
				}
			});
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
		facebook.authorize(this, new String[] { "email" },
		new DialogListener() {
			public void onComplete(Bundle values) {
			}
			public void onFacebookError(FacebookError error) {
			}
			public void onError(DialogError e) {
			}
			public void onCancel() {
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		facebook.extendAccessTokenIfNeeded(this, null);
	}
}
