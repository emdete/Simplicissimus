package org.pyneo.android.gui;

import android.util.Log;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import java.util.ArrayList;

public class MessengerService extends Service {
	NotificationManager mNM;
	ArrayList<Messenger> mClients = new ArrayList<>();
	int mValue = 0;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	class IncomingHandler extends Handler {
		@Override public void handleMessage(Message msg) {
			switch (msg.what) {
				case R.id.msg_register_client:
					mClients.add(msg.replyTo);
					break;
				case R.id.msg_unregister_client:
					mClients.remove(msg.replyTo);
					break;
				case R.id.msg_set_value:
					mValue = msg.arg1;
					for (int i=mClients.size()-1; i>=0; i--) {
						try {
							mClients.get(i).send(Message.obtain(null, R.id.msg_set_value, mValue, 0, new Bundle()));
						}
						catch (RemoteException e) {
							mClients.remove(i);
						}
					}
					break;
				default:
					super.handleMessage(msg);
					return;
			}
			if (mClients.size() == 0) {
				stopSelf();
			}
		}
	}

	@Override public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification();
	}

	@Override public void onDestroy() {
		mNM.cancel(R.id.notification_id);
		Log.d(Sample.TAG, "remote service stopped");
	}

	@Override public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	private void showNotification() {
		CharSequence text = getText(R.string.remote_service_started);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, Sample.class), 0);
		Notification notification = new Notification.Builder(this)
			.setSmallIcon(R.drawable.stat_sample) // the status icon
			.setTicker(text) // the status text
			.setWhen(System.currentTimeMillis()) // the time stamp
			.setContentTitle(getText(R.string.local_service_label)) // the label of the entry
			.setContentText(text) // the contents of the entry
			.setContentIntent(contentIntent) // The intent to send when the entry is clicked
			.build();
		mNM.notify(R.id.notification_id, notification);
	}
}
