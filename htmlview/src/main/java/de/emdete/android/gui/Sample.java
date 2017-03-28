package de.emdete.android.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class Sample extends Activity {
	static final String TAG = "de.emdete.sample";
	static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable(TAG, Log.DEBUG); }

	Context context;
	HtmlViewer browser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				Log.e(TAG, "error e=" + e, e);
				finish();
			}
		});
		if (DEBUG) Log.d(TAG, "onCreate");
		//setContentView(R.layout.main);
		context = getBaseContext();
		//browser = (HtmlViewer) findViewById(R.id.webview);
		browser = new HtmlViewer(context, null);
		browser.loadUrl("file:///android_asset/index.html");
		setContentView(browser);
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
		super.onPause();
		if (DEBUG) Log.d(TAG, "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		if (DEBUG) Log.d(TAG, "onSaveInstanceState bundle=" + bundle);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "onDestroy");
	}

	@Override public void onBackPressed() {
		if (!browser.back()) {
			finish();
		}
	}
}
