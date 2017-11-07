package de.emdete.android.gui;
// see http://www.grokkingandroid.com/adding-files-to-androids-media-library-using-the-mediascanner/

import android.app.Activity;
import android.app.Fragment;
import android.app.ListActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
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
	static final String TAG = "de.emdete.sample";
	static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable(TAG, Log.DEBUG); }

	Context context;

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

	void setText(final String s) {
		runOnUiThread(new Runnable() {
			public void run() {
				Button button = (Button)findViewById(R.id.button);
				button.setText(s);
			}
		});
	}

	public void doTest(final Context context) {
		setText("Started");
		//add_scanner(context, new File("/sdcard/Pictures/test.jpg"));
		new Thread() {
			@Override
			public void run() {
				recurseMediaDb(context);
				recurseFilesystem(context, new File("/storage"), 0);
			}
		}.run();
	}

	/**
		Method to recurse through the filesystem and check if the file exists
		in the MediaStore.
	*/
	void recurseFilesystem(Context context, File file, int depth) {
		if (depth > 100)
			return;
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File current: files) {
					if (!current.isHidden()) {
						if (current.isDirectory()) {
							if (!new File(current, ".nomedia").exists()) {
								recurseFilesystem(context, current, depth+1);
							}
						}
						else {
							if (DEBUG) Log.d(TAG, "add current=" + current);
							add_scanner(context, current);
						}
					}
				}
			}
		}
		else {
			if (DEBUG) Log.d(TAG, "OUPS");
		}
	}

	/**
		Method to show code how to get the Thumbnail from the MediaStore.
	*/
	File getThumbnail(Context context, long id) throws IOException {
		Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(
			context.getContentResolver(),
			id,
			MediaStore.Images.Thumbnails.MINI_KIND,
			new String[]{
				MediaStore.Images.Thumbnails.DATA
				});
		if (cursor.moveToFirst()) {
			return new File(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA)));
		}
		return null;
	}

	void add_by_intent(Context context, File file) {
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intent.setData(Uri.fromFile(file));
		sendBroadcast(intent);
	}

	/**
		Method to add a file to the MediaScanner (either cause it exists or it
		doesnt - we have no other choice).
	*/
	void add_scanner(Context context, File file) {
		Button button = (Button)findViewById(R.id.button);
		button.setText("Found: " + file);
		MediaScannerConnection.scanFile(
			context,
			new String[]{
				file.getAbsolutePath()},
			null,
			new MediaScannerConnection.OnScanCompletedListener() {
				@Override public void onScanCompleted(String path, Uri uri) {
					Log.v(TAG, "rescanned file=" + path + " uri=" + uri);
				}
			});
	}

	MediaScannerConnection connection;
	void add_connection(Context context, File file) {
		if (connection == null) {
			connection = new MediaScannerConnection(
				context, new MediaScannerConnection.MediaScannerConnectionClient(){
					@Override public void onMediaScannerConnected() {
					}
					@Override public void onScanCompleted(String path, Uri uri) {
						//connection.disconnect(); connection = null;
					}
				}
			);
		}
	}

	/**
		Method to iterate through all media mentioned in the MediaStore and
		check if they still exist.
	*/
	void recurseMediaDb(Context context) {
		for (String volumeName: new String[]{"external", "internal", }) {
			if (DEBUG) Log.d(TAG, "recurseMediaDb contentUri=" + MediaStore.Files.getContentUri(volumeName));
			Cursor cursor = context.getContentResolver().query(
					MediaStore.Files.getContentUri(volumeName),
					new String[]{
						MediaStore.MediaColumns._ID, //
						MediaStore.MediaColumns.DATA, // Path to the file on disk.
						MediaStore.MediaColumns.DATE_ADDED, // The time the file was added to the media provider Units are seconds since 1970.
						MediaStore.MediaColumns.DATE_MODIFIED, // The time the file was last modified Units are seconds since 1970.
						MediaStore.MediaColumns.DISPLAY_NAME, // The display name of the file Type: TEXT
						MediaStore.MediaColumns.HEIGHT, // The height of the image/video in pixels.
						MediaStore.MediaColumns.MIME_TYPE, // The MIME type of the file Type: TEXT
						MediaStore.MediaColumns.SIZE, // The size of the file in bytes Type: INTEGER (long)
						MediaStore.MediaColumns.TITLE, // The title of the content Type: TEXT
						MediaStore.MediaColumns.WIDTH, // The width of the image/video in pixels.
						},
					null,
					null,
					null);
			if (cursor != null) {
				try {
					int data_column = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
					int date_modified_column = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED);
					int id_column = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
					while (cursor.moveToNext()) {
						try {
							File file = new File(cursor.getString(data_column)).getCanonicalFile();
							//if (DEBUG) Log.d(TAG, "file=" + file);
							//File thumbnail = getThumbnail(context, cursor.getLong(id_column));
							//if (DEBUG) Log.d(TAG, "thumbnail=" + thumbnail);
							if (file.exists() && !file.isHidden()) { // && file.isFile()
								if (file.lastModified() / 1000L > cursor.getLong(date_modified_column)) {
									if (DEBUG) Log.d(TAG, "changed file=" + file);
									add_scanner(context, file);
								}
								else {
									//if (DEBUG) Log.d(TAG, "done file=" + file);
								}
							}
							else {
								if (DEBUG) Log.d(TAG, "remove file=" + file);
								add_scanner(context, file);
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
