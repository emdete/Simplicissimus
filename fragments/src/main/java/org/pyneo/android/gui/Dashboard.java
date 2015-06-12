package org.pyneo.android.gui;

import android.util.Log;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class Dashboard extends Base {
	static final private String TAG = Sample.TAG;
	static final private boolean DEBUG = Sample.DEBUG;
	private Activity activity;
	private Context context;

	public void inform(int event, Bundle extra) {
	}

	@Override public void onAttach(Activity activity) {
		if (DEBUG) { Log.d(TAG, "Dashboard.onAttach"); }
		super.onAttach(activity);
		this.activity = activity;
		context = activity.getApplicationContext();
	}

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) { Log.d(TAG, "Dashboard.onCreate"); }
		super.onCreate(bundle);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) { Log.d(TAG, "Dashboard.onCreateView"); }
		return inflater.inflate(R.layout.dashboard, container, false);
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) { Log.d(TAG, "Dashboard.onActivityCreated"); }
		ViewGroup viewGroup = (ViewGroup)activity.findViewById(R.id.dashboard).findViewById(R.id.dashboard_list);
		viewGroup.removeAllViews();
		for (int i=0;i<3;i++) {
			View item = (View)LayoutInflater.from(context).inflate(R.layout.dashboard_item, viewGroup, false);
			((TextView)item.findViewById(R.id.data_header)).setText("Speed");
			((TextView)item.findViewById(R.id.data_value)).setText("52." + i);
			((TextView)item.findViewById(R.id.data_unit)).setText("km/h");
			viewGroup.addView(item);
		}
	}
}
