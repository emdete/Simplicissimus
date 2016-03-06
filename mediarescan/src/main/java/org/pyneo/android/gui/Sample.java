package org.pyneo.android.gui;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class Sample extends Activity {
	static final String TAG = "org.pyneo.sample";
	static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable(TAG, Log.DEBUG); }

	Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "onCreate");
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				Log.e(TAG, "error e=" + e, e);
				finish();
			}
		});
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

	public void doTest(Context context) {
		Button button = (Button)findViewById(R.id.button);
		//recurseMediaDb();
		recurseFilesystem(new File("/storage"));
		button.setText("Started");
	}

	int c = 100;
	protected void recurseFilesystem(File file) {
		if (c-- <= 0)
			return;
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File current: files) {
					if (!current.isHidden()) {
						if (current.isDirectory()) {
							if (!new File(current, ".nomedia").exists()) {
								recurseFilesystem(current);
							}
						}
						else {
							if (DEBUG) Log.d(TAG, "add current=" + current);
						}
					}
				}
			}
		}
		else {
			if (DEBUG) Log.d(TAG, "OUPS");
		}
	}

	protected void recurseMediaDb() {
		c = 100;
		for (String volumeName: new String[]{"external", "internal", }) {
			if (DEBUG) Log.d(TAG, "recurseMediaDb contentUri=" + MediaStore.Files.getContentUri(volumeName));
			Cursor cursor = context.getContentResolver().query(
					MediaStore.Files.getContentUri(volumeName),
					new String[]{
						MediaStore.MediaColumns.DATA,
						MediaStore.MediaColumns.DATE_MODIFIED,
						},
					null,
					null,
					null);
			if (cursor != null) {
				try {
					int data_column = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
					int modified_column = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);
					while (cursor.moveToNext()) {
						try {
							File file = new File(cursor.getString(data_column)).getCanonicalFile();
							if (file.exists() && !file.isHidden()) { // && file.isFile()
								if (file.lastModified() / 1000L > cursor.getLong(modified_column)) {
									if (DEBUG) Log.d(TAG, "add file=" + file);
								}
								else {
									//if (DEBUG) Log.d(TAG, "done file=" + file);
								}
							}
							else {
								if (DEBUG) Log.d(TAG, "remove file=" + file);
							}
						}
						catch (IOException e) {
							if (DEBUG) Log.e(TAG, "recurseMediaDb e=" + e);
						}
					}
				}
				finally {
					cursor.close();
				}
			}
		}
	}
}
