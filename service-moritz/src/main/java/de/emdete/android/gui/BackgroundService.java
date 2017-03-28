package de.emdete.android.gui;

import android.app.Service;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Looper;
import android.os.Process;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.os.Binder;
import android.os.IBinder;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class BackgroundService extends Service {
	private static final String TAG = BackgroundService.class.getName();
	Binder binder = new BackgroundBinder();
	private Looper looper;
	private ServiceHandler handler;

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "handleMessage:");
			long endTime = System.currentTimeMillis() + 5*1000;
			while (System.currentTimeMillis() < endTime) {
				synchronized (this) {
					try {
						wait(endTime - System.currentTimeMillis());
						Log.d(TAG, "handleMessage: waited");
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
			// stopSelf(msg.arg1);
			if (!handler.sendMessage(handler.obtainMessage())) {
				Log.d(TAG, "handleMessage: ended");
			}
		}
	}

	public class BackgroundBinder extends Binder {
		public BackgroundService getService() {
			return BackgroundService.this;
		}
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate:");
		HandlerThread thread = new HandlerThread("BackgroundService", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		looper = thread.getLooper();
		handler = new ServiceHandler(looper);
	}


	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind: intent=" + intent);
		if (handler != null) {
			handler.sendMessage(handler.obtainMessage());
		}
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand: intent=" + intent + ", flags=" + flags + ", startId=" + startId);
		if (handler != null) {
			Message msg = handler.obtainMessage();
			msg.arg1 = startId;
			handler.sendMessage(msg);
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy:");
		if (looper != null) {
			looper.quit();
		}
	}

	private static final java.util.Random random = new java.util.Random();
	private void post(String url) throws Exception {
		java.net.HttpURLConnection connection = (java.net.HttpURLConnection)new java.net.URL(url).openConnection();
		try {
			String correlation_id = Long.toString(random.nextLong());
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setReadTimeout(5000);
			connection.setRequestProperty("Content-Type", "text/json");
			connection.setRequestProperty("X-Correlation-Id", correlation_id);
			connection.setRequestMethod("POST");
			java.io.Writer out = new java.io.OutputStreamWriter(connection.getOutputStream());
			try {
				out.write("".getBytes()[0]);
			}
			finally {
				out.flush();
				out.close();
			}
			int httpResponseCode = connection.getResponseCode();
			Log.d(TAG, "retrieveLocation: httpResponseCode=" + httpResponseCode);
			if (httpResponseCode != 200) {
				throw new Exception("httpResponseCode=" + httpResponseCode);
			}
			java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
			try {
				Object obj = in.read();
				Log.d(TAG, "retrieveLocation: response obj=" + obj);
			}
			finally {
				in.close();
			}
		}
		finally {
			try { connection.disconnect(); } catch (Exception ignore) { }
		}
	}
}
