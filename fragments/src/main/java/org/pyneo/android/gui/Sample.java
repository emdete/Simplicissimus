package org.pyneo.android.gui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class Sample extends Activity {
	static public final String TAG = Sample.class.getName();
	static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable("org.pyneo.android", Log.DEBUG); }
	private Base[] fragments;

	@Override public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) { Log.d(TAG, "Sample.onCreate"); }
		Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler(){
			@Override public void uncaughtException (Thread thread, Throwable e) {
				Log.e(TAG, "error e=" + e, e);
				finish();
			}
		});
		setContentView (R.layout.base);
		fragments = new Base[]{
				new Controller(),
				new Dashboard(),
		};
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction tx = fragmentManager.beginTransaction();
		for (Base b: fragments) {
			tx.add(R.id.base, b, b.getClass().getSimpleName());
		}
		tx.commit();
	}

	@Override protected void onStart () {
		super.onStart();
		if (DEBUG) { Log.d(TAG, "Sample.onStart"); }
	}

	@Override protected void onRestart () {
		super.onRestart();
		if (DEBUG) { Log.d(TAG, "Sample.onRestart"); }
	}

	@Override protected void onResume () {
		super.onResume();
		if (DEBUG) { Log.d(TAG, "Sample.onResume"); }
	}

	@Override protected void onPause () {
		super.onPause();
		if (DEBUG) { Log.d(TAG, "Sample.onPause"); }
	}

	@Override protected void onStop () {
		super.onStop();
		if (DEBUG) { Log.d(TAG, "Sample.onStop"); }
	}

	@Override protected void onDestroy () {
		super.onDestroy();
		if (DEBUG) { Log.d(TAG, "Sample.onDestroy"); }
	}

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main_option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		inform(item.getItemId(), null);
		return true;
	}

	public void inform(int event, Bundle extra) {
		for (Base b: fragments) {
			b.inform(event, extra);
		}
	}
}
