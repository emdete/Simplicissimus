package org.pyneo.android.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class Sample
		extends Activity {
	static final String  TAG   = Sample.class.getName();
	static       boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable("org.pyneo.android", Log.DEBUG); }

	private Animation popUpAnimation;
	private Animation popInAnimation;
	private boolean toggle;
	private View      optionsContainer;

	@Override public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) {
			Log.d(TAG, "onCreate");
		}
		setContentView(R.layout.controler);
		//		Button button = (Button)findViewById(R.id.button);
		//		button.setOnClickListener(new View.OnClickListener() {
		//			@Override
		//			public void onClick(View view) {
		//				if (DEBUG) Log.d(TAG, "onClick");
		//				doTest(context);
		//			}
		//		});

		View commandButton = findViewById(R.id.button3);
		optionsContainer = findViewById(R.id.options);
		if (commandButton != null && optionsContainer != null) {
			commandButton.setOnClickListener(new View.OnClickListener() {

				@Override public void onClick (View v) {
					toggleOptions();
					toggle = !toggle;
				}
			});
		}

		popUpAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_pop_up);
		popInAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_pop_in);
	}

	private void toggleOptions () {
		optionsContainer.startAnimation(toggle ?
				popInAnimation :
				popUpAnimation);
	}

	@Override protected void onStart () {
		super.onStart();
		if (DEBUG) {
			Log.d(TAG, "onStart");
		}
	}

	@Override protected void onRestart () {
		super.onRestart();
		if (DEBUG) {
			Log.d(TAG, "onRestart");
		}
	}

	@Override protected void onResume () {
		super.onResume();
		if (DEBUG) {
			Log.d(TAG, "onResume");
		}
	}

	@Override protected void onPause () {
		super.onPause();
		if (DEBUG) {
			Log.d(TAG, "onPause");
		}
	}

	@Override protected void onStop () {
		super.onStop();
		if (DEBUG) {
			Log.d(TAG, "onStop");
		}
	}

	@Override protected void onDestroy () {
		super.onDestroy();
		if (DEBUG) {
			Log.d(TAG, "onDestroy");
		}
	}

	public void doTest (Context context) {
		Button button = (Button) findViewById(R.id.button);
		button.setText("Started");
	}
}
