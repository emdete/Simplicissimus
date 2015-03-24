package org.pyneo.android.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.hardware.ConsumerIrManager;
import android.hardware.ConsumerIrManager.CarrierFrequencyRange;

public class Sample extends Activity {
	static final String TAG = Sample.class.getName();
	static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable("org.pyneo.android", Log.DEBUG); }
	ConsumerIrManager irManager = null;

	Context context;

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
		irManager = (ConsumerIrManager)getSystemService(CONSUMER_IR_SERVICE);
		boolean hasIrEmitter = irManager.hasIrEmitter();
		Log.d(TAG, "hasIrEmitter=" + hasIrEmitter);
		if (hasIrEmitter) {
			CarrierFrequencyRange[]	ranges = irManager.getCarrierFrequencies();
			for (CarrierFrequencyRange range: ranges) {
				Log.d(TAG, "range"
					+ ", max=" + range.getMaxFrequency()
					+ ", min=" + range.getMinFrequency()
					);
			}
		}
		else {
			irManager = null;
			Button button = (Button)findViewById(R.id.button);
			button.setText("No IrEmitter");
			try {
				Object c = Class.forName("com.lge.hardware.IRBlaster.IRBlaster");
			}
			catch (Exception e) {
				button.setText(e.toString());
			}
		}
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
	}

	public void doTest(Context context) {
		Button button = (Button)findViewById(R.id.button);
		if (irManager != null) {
			irManager.transmit(120, new int[]{1000});
			button.setText("Sent");
		}
	}
}
