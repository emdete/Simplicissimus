package org.pyneo.android.gui;

import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

public class Controller extends Base {
	static final private String TAG = Sample.TAG;
	static final private boolean DEBUG = Sample.DEBUG;
	private Animation popOutAnimation;
	private Animation popInAnimation;
	private View optionsContainer;
	private boolean optionsOut;

	public void inform(int event, Bundle extra) {
		if (DEBUG) { Log.d(TAG, "Controller.inform event=" + event); }
		if (optionsOut) {
			optionsContainer.startAnimation(popInAnimation);
			optionsOut = false;
		}
		switch (event) {
			case R.id.event_attribute_red: ((ImageButton)getActivity().findViewById(R.id.event_attribute)).setImageResource(R.drawable.attribute_red); break;
			case R.id.event_attribute_yellow: ((ImageButton)getActivity().findViewById(R.id.event_attribute)).setImageResource(R.drawable.attribute_yellow); break;
			case R.id.event_attribute_green: ((ImageButton)getActivity().findViewById(R.id.event_attribute)).setImageResource(R.drawable.attribute_green); break;
			case R.id.event_attribute_blue: ((ImageButton)getActivity().findViewById(R.id.event_attribute)).setImageResource(R.drawable.attribute_blue); break;
			case R.id.event_attribute_white: ((ImageButton)getActivity().findViewById(R.id.event_attribute)).setImageResource(R.drawable.attribute_white); break;
		}
	}

	private boolean onDrag(View v, DragEvent event) {
		if (DEBUG) { Log.d(TAG, "Controller.onClick event=" + event); }
		return true;
	}

	private void onClick(View view) {
		int e = view.getId();
		if (DEBUG) { Log.d(TAG, "Controller.onClick e=" + e); }
		switch(e) {
			case R.id.event_attribute: {
				if (!optionsOut) {
					optionsContainer.startAnimation(popOutAnimation);
					optionsOut = true;
					return;
				}
			}
		}
		((Sample) getActivity()).inform(e, null);
	}

	@Override public void onAttach(Activity activity) {
		if (DEBUG) { Log.d(TAG, "Controller.onAttach"); }
		super.onAttach(activity);
	}

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) { Log.d(TAG, "Controller.onCreate"); }
		super.onCreate(bundle);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) { Log.d(TAG, "Controller.onCreateView"); }
		View view = inflater.inflate(R.layout.controller, container, false);
		View.OnClickListener clickListener = new View.OnClickListener() {
			@Override public void onClick(View view) {
				Controller.this.onClick(view);
			}
		};
		View.OnDragListener dragListener = new View.OnDragListener() {
			@Override public boolean onDrag(View v, DragEvent event) {
				return Controller.this.onDrag(v, event);
			}
		};
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
			view.findViewById(resourceId).setOnClickListener(clickListener);
		}
		view.findViewById(R.id.drag).setOnDragListener(dragListener);
		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) { Log.d(TAG, "Controller.onActivityCreated"); }
		optionsContainer = getActivity().findViewById(R.id.attributes);
		popOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.attributes_open);
		popInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.attributes_close);
	}
}
