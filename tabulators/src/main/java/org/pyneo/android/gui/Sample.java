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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final FragmentStatePagerAdapter mDemoCollectionPagerAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
			@Override
			public Fragment getItem(int i) {
				Fragment fragment = new DemoObjectFragment();
				Bundle args = new Bundle();
				args.putInt("no", i + 1);
				fragment.setArguments(args);
				return fragment;
			}

			@Override
			public int getCount() {
				return 10;
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return "Tab " + (position + 1);
			}
		};
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		final ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mDemoCollectionPagerAdapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				Intent upIntent = new Intent(this, Sample.class);
				if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
					TaskStackBuilder.from(this).addNextIntent(upIntent).startActivities();
					finish();
				} else {
					NavUtils.navigateUpTo(this, upIntent);
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class DemoObjectFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment, container, false);
			Bundle args = getArguments();
			((TextView)rootView.findViewById(android.R.id.text1)).setText(Integer.toString(args.getInt("no") * 2 + 1));
			return rootView;
		}
	}
}
