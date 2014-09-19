package org.pyneo.android.gui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Sample extends Activity {
	static final String TAG = Sample.class.getName();
	static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable("org.pyneo.android", Log.DEBUG); }

	Context context;
	ServiceConnection serviceConnection;
	BackgroundService.BackgroundBinder binder;

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
				Sample.this.onClick(view);
			}
		});
		button.setText("click me!");
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
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "onDestroy");
		if (serviceConnection != null) {
			unbindService(serviceConnection);
		}
	}

	void onServiceConnected(ComponentName name, IBinder service) {
		if (DEBUG) Log.d(TAG, "onServiceConnected: name=" + name + ", service=" + service);
		binder = (BackgroundService.BackgroundBinder)service;
	}

	void onServiceDisconnected(ComponentName name) {
		if (DEBUG) Log.d(TAG, "onServiceDisconnected: name=" + name);
		binder = null;
	}

	void onClick(View view) {
		if (DEBUG) Log.d(TAG, "onClick");
		Button button = (Button)findViewById(R.id.button);
		if (false) {
			startService(new Intent(this, BackgroundService.class));
		}
		else {
			if (serviceConnection == null) {
				serviceConnection = new ServiceConnection() {
						@Override
						public void onServiceConnected(ComponentName name, IBinder service) {
							Sample.this.onServiceConnected(name, service);
						}
						@Override
						public void onServiceDisconnected(ComponentName name) {
							Sample.this.onServiceDisconnected(name);
						}
					};
				if (this.bindService(new Intent(this, BackgroundService.class), serviceConnection, BIND_AUTO_CREATE)) {
					button.setText("service started");
				}
				else {
					button.setText("couldnt start service");
				}
			}
			else {
				unbindService(serviceConnection);
				serviceConnection = null;
				button.setText("service stopped");
			}
		}
	}
}
