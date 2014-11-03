package org.pyneo.android.gui;

import android.app.Activity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.nio.charset.Charset;

public class Sample extends Activity implements CreateNdefMessageCallback {
	// see https://developer.android.com/guide/topics/connectivity/nfc/nfc.html#p2p
	static final String TAG = Sample.class.getName();
	static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable("org.pyneo.android", Log.DEBUG); }
	static final String text = "xmpp://mdt@emdete.de";

	Context context;
	NfcAdapter nfcAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "onCreate");
		setContentView(R.layout.main);
		context = getBaseContext();
		Button button = (Button)findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (DEBUG) Log.d(TAG, "onClick");
				doTest(context);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (DEBUG) Log.d(TAG, "onStart");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (DEBUG) Log.d(TAG, "onRestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume nfcAdapter=" + nfcAdapter);
		Button button = (Button)findViewById(R.id.button);
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (DEBUG) Log.d(TAG, "onCreate nfcAdapter=" + nfcAdapter);
		if (nfcAdapter != null) {
			if (nfcAdapter.isEnabled() || nfcAdapter.isNdefPushEnabled()) {
				// only if nfc and nde/beam is enabled we can proceed
				button.setText("NFC and NDE available");
				nfcAdapter.setNdefPushMessageCallback(this, this);
				Intent intent = getIntent();
				// this is the moment where we actually notice that we received a beam event:
				if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
					Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
					NdefMessage msg = (NdefMessage)rawMsgs[0];
					button.setText(new String(msg.getRecords()[0].getPayload()));
				}
			}
			else {
				// if either is swithed off ask the user to turn it on, it may
				// well be that we land here without any nfc in the device
				button.setText(nfcAdapter.isEnabled() ?
					"NFC and Beam not enabled" :
					"Beam not enabled");
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(nfcAdapter.isEnabled() ?
					"For this operation you need NFC and Beam which is currently disabled, you have to enable it in the settings." :
					"For this operation you need Beam which is currently disabled, you have to enable it in the settings."
					);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
						startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
					}
				});
				builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int i) {
					}
				});
				builder.create().show();
			}
		}
		else {
			// devices without any nfc capability often just return null
			button.setText("NFC not available");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "onDestroy");
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		NdefMessage msg = new NdefMessage(new NdefRecord[] { NdefRecord.createMime(
			"application/vnd.org.pyneo.android.sample", text.getBytes()),
			// The Android Application Record (AAR) is commented out. When a
			// device receives a push with an AAR in it, the application
			// specified in the AAR is guaranteed to run. The AAR overrides the
			// tag dispatch system. You can add it back in to guarantee that
			// this activity starts when receiving a beamed message. For now,
			// this code uses the tag dispatch system.
			//NdefRecord.createApplicationRecord("com.example.android.beam"),
			});
		return msg;
	}

	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
	}

	public void doTest(Context context) {
		Button button = (Button)findViewById(R.id.button);
		button.setText(text);
	}
}
