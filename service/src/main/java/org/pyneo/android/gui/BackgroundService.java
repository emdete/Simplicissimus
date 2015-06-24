package org.pyneo.android.gui;

import android.telephony.PhoneStateListener;
import android.telephony.CellLocation;
import android.app.Service;
import android.os.HandlerThread;
import android.os.Message;
import android.telephony.CellInfo;
import android.content.Context;
import android.os.Looper;
import android.os.Process;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import java.util.List;

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
			CellLocation.requestLocationUpdate();
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
		if (true) { //
			HandlerThread thread = new HandlerThread("BackgroundService", Process.THREAD_PRIORITY_BACKGROUND);
			thread.start();
			looper = thread.getLooper();
			handler = new ServiceHandler(looper);
		}
		if (true) {
			TelephonyManager telephonyManager = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
			telephonyManager.listen(new PhoneStateListener() {
				@Override
				public void onCellInfoChanged(List<CellInfo> cellInfos) {
					Log.d(TAG, "onCellInfoChanged: cellInfos=" + cellInfos);
				}
				@Override
				public void onCellLocationChanged (CellLocation location) {
					Log.d(TAG, "onCellLocationChanged: location=" + location);
				}
			}, PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_CELL_LOCATION);
		}
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
}
