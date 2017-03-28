package de.emdete.android.gui;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Sample extends FragmentActivity {
	static final String TAG = "de.emdete.sample";
	static boolean DEBUG = true;
	static final String POSITION = "position";

	public static class ObjectFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
			Log.d(TAG, "onCreateView: bundle=" + bundle);
			Bundle args = getArguments();
			int position = args.getInt(POSITION);
			Log.d(TAG, "onCreateView: position=" + position);
			View rootView = inflater.inflate(R.layout.fragment, container, false);
			((TextView)rootView.findViewById(android.R.id.text1)).setText(Integer.toString(position * 3 + 2));
			return rootView;
		}
	}

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				Log.e(TAG, "error e=" + e, e);
				finish();
			}
		});
		Log.d(TAG, "onCreate: bundle=" + bundle);
		setContentView(R.layout.main);
		final FragmentStatePagerAdapter adapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
			@Override
			public Fragment getItem(int position) {
				Log.d(TAG, "getItem: position=" + position);
				Fragment fragment = new ObjectFragment();
				Bundle args = new Bundle();
				args.putInt(POSITION, position);
				fragment.setArguments(args);
				return fragment;
			}

			@Override
			public int getCount() {
				Log.d(TAG, "getCount:");
				return 6;
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return "Tab " + (position + 1);
			}
		};
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		final ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(adapter);
	}
}
