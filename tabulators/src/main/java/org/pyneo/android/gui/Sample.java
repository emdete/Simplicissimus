package org.pyneo.android.gui;

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
	static final String TAG = Sample.class.getName();
	static boolean DEBUG = true;
	ObjectFragment[] fragments = new ObjectFragment[3];

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate:");
		setContentView(R.layout.main);
		final FragmentStatePagerAdapter adapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
			@Override
			public Fragment getItem(int position) {
				Log.d(TAG, "getItem: position=" + position);
				return fragments[position];
			}

			@Override
			public int getCount() {
				Log.d(TAG, "getCount:");
				return fragments.length;
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return fragments[position].getPageTitle();
			}
		};
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		final ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(adapter);
		for (int position=0;position<fragments.length;position++) {
			fragments[position] = new ObjectFragment(position);
			Bundle args = new Bundle();
			args.putInt("position", position);
			fragments[position].setArguments(args);
		}
	}

	public static class ObjectFragment extends Fragment {
		int position;

		ObjectFragment(int position) {
			this.position = position;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			Bundle args = getArguments();
			int position = args.getInt("position");
			Log.d(TAG, "onCreateView: position=" + position);
			View rootView = inflater.inflate(R.layout.fragment, container, false);
			((TextView)rootView.findViewById(android.R.id.text1)).setText(Integer.toString(position * 2 + 1));
			return rootView;
		}

		public CharSequence getPageTitle() {
			Log.d(TAG, "getPageTitle: position=" + position);
			return "Tab " + (position + 1);
		}
	}
}
