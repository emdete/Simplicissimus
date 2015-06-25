package org.pyneo.android.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.File;
import org.pyneo.android.cam.UnattendedPic;
import android.content.Intent;
import android.net.Uri;

public class Sample extends Activity {
	private static final String TAG = "org.pyneo.sample";
	private static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable(TAG, Log.DEBUG); }
	private Context context;
	private UnattendedPic unattendedPic = new UnattendedPic() {
		public void captured(File file) {
			show(file);
		}
	};

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
		if (DEBUG) Log.d(TAG, "onResume");
	}

	@Override
	protected void onPause() {
		if (DEBUG) Log.d(TAG, "onPause");
		unattendedPic.stop();
		super.onPause();
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

	public void doTest(Context context) {
		if (DEBUG) Log.d(TAG, "doTest");
		unattendedPic.capture(this);
		setText("Triggered");
	}

	void setText(final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((Button)findViewById(R.id.button)).setText(text);
			}
		});
	}

	void show(File file) {
		setText("uri=" + Uri.fromFile(file));
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/jpeg");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		intent.putExtra("contact", "anne@droeselwitz.de");
		intent.putExtra("account", "mdt@emdete.de");
		//intent.putExtra("latitude", 0.0);
		//intent.putExtra("longitude", 0.0);
		//intent.putExtra("accuracy", 0.0);
		//intent.putExtra("altitude", 0.0);
		try {
			startActivity(intent);
		}
		catch (Exception e) {
			Log.e(TAG, "caught an exception", e);
		}
		if (DEBUG) Log.d(TAG, "show intent=" + intent);
	}
}
