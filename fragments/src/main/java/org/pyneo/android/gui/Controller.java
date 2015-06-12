package org.pyneo.android.gui;

import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

public class Controller extends Base {
	static final private String TAG = Sample.TAG;
	static final private boolean DEBUG = Sample.DEBUG;
	private Activity activity;
	private Context context;
	private Animation popOutAnimation;
	private Animation popInAnimation;
	private View optionsContainer;
	private boolean optionsOut;

	public void inform(int event, Bundle extra) {
		if (optionsOut) {
			optionsContainer.startAnimation(popInAnimation);
			optionsOut = false;
		}
		switch (event) {
		case R.id.event_attribute_red: ((ImageButton)activity.findViewById(R.id.event_attribute)).setImageResource(R.drawable.attribute_red); break;
		case R.id.event_attribute_yellow: ((ImageButton)activity.findViewById(R.id.event_attribute)).setImageResource(R.drawable.attribute_yellow); break;
		case R.id.event_attribute_green: ((ImageButton)activity.findViewById(R.id.event_attribute)).setImageResource(R.drawable.attribute_green); break;
		case R.id.event_attribute_blue: ((ImageButton)activity.findViewById(R.id.event_attribute)).setImageResource(R.drawable.attribute_blue); break;
		case R.id.event_attribute_white: ((ImageButton)activity.findViewById(R.id.event_attribute)).setImageResource(R.drawable.attribute_white); break;
		}
	}

	private void onClick(View view) {
		int e = view.getId();
		switch(e) {
			case R.id.event_attribute: {
				if (!optionsOut) {
					optionsContainer.startAnimation(popOutAnimation);
					optionsOut = true;
					return;
				}
			}
		}
		((Sample) activity).inform(e, null);
	}

	@Override public void onAttach(Activity activity) {
		if (DEBUG) { Log.d(TAG, "onAttach"); }
		super.onAttach(activity);
		this.activity = activity;
		context = activity.getApplicationContext();
	}

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) { Log.d(TAG, "onCreate"); }
		super.onCreate(bundle);

	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) { Log.d(TAG, "onCreateView"); }
		return inflater.inflate(R.layout.controller, container, false);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) { Log.d(TAG, "onActivityCreated"); }
		optionsContainer = activity.findViewById(R.id.attributes);
		popOutAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_pop_out);
		popInAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_pop_in);
		for (int resourceId: new int[]{
				R.id.event_attribute_blue,
				R.id.event_attribute_green,
				R.id.event_attribute_red,
				R.id.event_attribute_white,
				R.id.event_attribute_yellow,
				R.id.event_attribute,
				R.id.event_autofollow,
				R.id.event_overlay,
				R.id.event_zoom_in,
				R.id.event_zoom_out,
		}) {
			View imageButton = activity.findViewById(resourceId);
			imageButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (DEBUG) { Log.d(TAG, "onClick"); }
					Controller.this.onClick(view);
				}
			});
		}
	}
}
