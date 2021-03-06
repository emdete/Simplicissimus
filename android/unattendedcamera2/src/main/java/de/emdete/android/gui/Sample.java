package de.emdete.android.gui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import de.emdete.cam.UnattendedPic;
import android.content.Intent;

public class Sample extends Activity {
	static final String TAG = "de.emdete.sample";
	static final boolean DEBUG = BuildConfig.DEBUG;
	static final private String LINE_END = "\r\n";
	static final private String TWO_HYPHENS = "--";
	static final private String BOUNDARY = "*****";
	static final private String SERVER_URL = "https://emdete.de/sample/upload";
	static final private int maxBufferSize = 1 * 1024 * 1024;
	Context context;

	private UnattendedPic unattendedPic = new UnattendedPic() {
		public void captured(final File file) {
			final ProgressDialog dialog = ProgressDialog.show(Sample.this, "Unattended cam", "Uploading file " + file.toString(), true);
			dialog.setProgress(ProgressDialog.STYLE_SPINNER);
			//dialog.setMax(100);
			new Thread(new Runnable() {
				public void run() {
					runOnUiThread(new Runnable() { public void run() {
						dialog.setMessage("Uploading started");
					}});
					try {
						uploadFile(file);
						setText("Uploaded");
					}
					catch (final Exception e) {
						runOnUiThread(new Runnable() { public void run() {
							Toast.makeText(Sample.this, "Got Exception: " + e, Toast.LENGTH_LONG).show();
						}});
						setText("Failed");
					}
					runOnUiThread(new Runnable() { public void run() {
						dialog.dismiss();
					}});
				}
			}).start();
		}
	};

	@Override public void onCreate(Bundle savedInstanceState) {
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
			@Override public void onClick(View view) {
				if (DEBUG) Log.d(TAG, "onClick");
				doTest(context);
			}
		});
		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_MAIN.equals(queryAction)) {
			;
		} else if ("TAKE".equals(queryAction)) {
			//String package_ = getCallingPackage();
			//String activity = getCallingActivity().flattenToString();
			//Bundle extra = queryIntent.getExtras();
			//Uri uri = queryIntent.getData();
			doTest(context);
		}
	}

	@Override protected void onStart() {
		super.onStart();
		if (DEBUG) Log.d(TAG, "onStart");
	}

	@Override protected void onRestart() {
		super.onRestart();
		if (DEBUG) Log.d(TAG, "onRestart");
	}

	@Override protected void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume");
	}

	@Override protected void onPause() {
		if (DEBUG) Log.d(TAG, "onPause");
		unattendedPic.stop();
		super.onPause();
	}

	@Override protected void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
	}

	@Override protected void onDestroy() {
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
			@Override public void run() {
				((Button)findViewById(R.id.button)).setText(text);
			}
		});
	}

	String b64(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 86, baos);
		return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
	}

	public void uploadFile(File sourceFile) throws Exception {
		if (DEBUG) Log.d(TAG, "uploadFile sourceFile=" + sourceFile);
		if (!sourceFile.isFile()) {
			throw new Exception("file does not exist");
		}
		HttpURLConnection connection = (HttpURLConnection)new URL(SERVER_URL).openConnection();
		connection.setDoInput(true); // Allow Inputs
		connection.setDoOutput(true); // Allow Outputs
		connection.setUseCaches(false); // Don't use a Cached Copy
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Enctype", "multipart/form-data");
		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
		connection.setRequestProperty("UploadedFile", sourceFile.toString());
		try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
			dos.writeBytes(TWO_HYPHENS + BOUNDARY + LINE_END);
			dos.writeBytes("Content-Disposition: form-data; name=\"UploadedFile\";filename=\"" + sourceFile + "\"" + LINE_END);
			dos.writeBytes("Content-Type: image/jpeg" + LINE_END);
			dos.writeBytes(LINE_END);
			try (FileInputStream fileInputStream = new FileInputStream(sourceFile)) {
				int bufferSize = Math.min(fileInputStream.available(), maxBufferSize);
				byte[] buffer = new byte[bufferSize];
				int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				while (bytesRead > 0) {
					dos.write(buffer, 0, bytesRead);
					bufferSize = Math.min(fileInputStream.available(), maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}
			}
			dos.writeBytes(LINE_END + TWO_HYPHENS + BOUNDARY + TWO_HYPHENS + LINE_END);
			dos.flush();
		}
		int serverResponseCode = connection.getResponseCode();
		if (serverResponseCode != HttpURLConnection.HTTP_OK) {
			String serverResponseMessage = connection.getResponseMessage();
			Log.e(TAG, "HTTP Response is: " + serverResponseMessage + ": " + serverResponseCode);
			throw new Exception(serverResponseMessage);
		}
	}
}
