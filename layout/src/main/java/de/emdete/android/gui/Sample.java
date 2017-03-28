package de.emdete.android.gui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class Sample
		extends Activity {
	static final String  TAG   = "de.emdete.sample";
	static       boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable(TAG, Log.DEBUG); }

	private Animation popUpAnimation;
	private Animation popInAnimation;
	private View      optionsContainer;
	private boolean   optionsToggled;

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
		setContentView(R.layout.controler);

		View commandButton = findViewById(R.id.button_attribute);
		optionsContainer = findViewById(R.id.options);
		if (commandButton != null && optionsContainer != null) {
			commandButton.setOnClickListener(new View.OnClickListener() {

				@Override public void onClick (View view) {
					toggleOptions();
				}
			});
		}

		popUpAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_pop_out);
		popInAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_pop_in);
	}

	private void toggleOptions () {
		optionsContainer.startAnimation(optionsToggled ?
				popInAnimation :
				popUpAnimation);
		optionsToggled = !optionsToggled;
	}

	@Override
	protected void onStart () {
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
	}
}
