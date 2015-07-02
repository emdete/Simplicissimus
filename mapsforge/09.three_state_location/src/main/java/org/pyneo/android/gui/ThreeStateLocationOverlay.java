/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2014 Ludwig M Brinckmann
 * Copyright © 2014 devemux86
 * Copyright © 2015 M. Dietrich
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.pyneo.android.gui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.os.SystemClock;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.model.MapViewPosition;

/**
 * A thread-safe {@link Layer} implementation to display the current location. NOTE: This code really does not reflect
 * Android best practice and used in production leads to bad user experience (e.g. long time to first fix, excessive
 * battery use, non-compliance with the Android lifecycle...). Best use the new location services provided by Google
 * Play Services. Also note that ThreeStateLocationOverlay needs to be added to a view before requesting location updates
 * (otherwise no DisplayModel is set).
 */
public class ThreeStateLocationOverlay extends Layer implements LocationListener {
	static final protected String TAG = Sample.TAG;
	static final protected boolean DEBUG = Sample.DEBUG;

	protected static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;
	protected float minDistance = 0.0f;
	protected long minTime = 0;

	protected static Paint getDefaultCircleFill() {
		return getPaint(GRAPHIC_FACTORY.createColor(48, 0, 0, 255), 0, Style.FILL);
	}

	protected static Paint getDefaultCircleStroke() {
		return getPaint(GRAPHIC_FACTORY.createColor(160, 0, 0, 255), 2, Style.STROKE);
	}

	protected static Paint getPaint(int color, int strokeWidth, Style style) {
		Paint paint = GRAPHIC_FACTORY.createPaint();
		paint.setColor(color);
		paint.setStrokeWidth(strokeWidth);
		paint.setStyle(style);
		return paint;
	}

	protected boolean centerAtNextFix;
	protected final Circle circle;
	protected Location lastLocation;
	protected final LocationManager locationManager;
	protected final MapViewPosition mapViewPosition;
	protected Marker marker;
	protected final Marker map_needle_pinned;
	protected final Marker map_needle_off;
	protected final RotatingMarker map_needle;
	protected boolean myLocationEnabled;
	protected boolean snapToLocationEnabled;

	/**
	 * Constructs a new {@code ThreeStateLocationOverlay} with the default circle paints.
	 *
	 * @param context
	 *            a reference to the application context.
	 * @param mapViewPosition
	 *            the {@code MapViewPosition} whose location will be updated.
	 */
	public ThreeStateLocationOverlay(Context context, MapViewPosition mapViewPosition) {
		this(context, mapViewPosition, getDefaultCircleFill(), getDefaultCircleStroke());
	}

	/**
	 * Constructs a new {@code ThreeStateLocationOverlay} with the given circle paints.
	 *
	 * @param context
	 *            a reference to the application context.
	 * @param mapViewPosition
	 *            the {@code MapViewPosition} whose location will be updated.
	 * @param circleFill
	 *            the {@code Paint} used to fill the circle that represents the accuracy of the current location (might be null).
	 * @param circleStroke
	 *            the {@code Paint} used to stroke the circle that represents the accuracy of the current location (might be null).
	 */
	public ThreeStateLocationOverlay(Context context, MapViewPosition mapViewPosition, Paint circleFill,
							Paint circleStroke) {
		super();

		this.mapViewPosition = mapViewPosition;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		map_needle_pinned = new Marker(null, AndroidGraphicFactory.convertToBitmap(
			context.getResources().getDrawable(R.drawable.map_needle_pinned)), 0, 0);
		map_needle = new RotatingMarker(null, AndroidGraphicFactory.convertToBitmap(
			context.getResources().getDrawable(R.drawable.map_needle)), 0, 0);
		map_needle_off = new Marker(null, AndroidGraphicFactory.convertToBitmap(
			context.getResources().getDrawable(R.drawable.map_needle_off)), 0, 0);
		marker = map_needle_off;
		circle = new Circle(null, 0, circleFill, circleStroke);
	}

	/**
	 * Stops the receiving of location updates. Has no effect if location updates are already disabled.
	 */
	public synchronized void disable() {
		if (myLocationEnabled) {
			myLocationEnabled = false;
			locationManager.removeUpdates(this);
			// TODO trigger redraw?
		}
	}

	@Override
	public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		if (!myLocationEnabled) {
			return;
		}
		circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
		marker.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
	}

	/**
	 * Enables the receiving of location updates from the most accurate {@link LocationProvider} available.
	 *
	 * @param centerAtFirstFix
	 *            whether the map should be centered to the first received location fix.
	 * @return true if at least one location provider was found, false otherwise.
	 */
	public synchronized boolean enable(boolean centerAtFirstFix) {
		if (!enableBestAvailableProvider()) {
			return false;
		}

		centerAtNextFix = centerAtFirstFix;
		circle.setDisplayModel(displayModel);
		map_needle_pinned.setDisplayModel(displayModel);
		map_needle.setDisplayModel(displayModel);
		map_needle_off.setDisplayModel(displayModel);
		return true;
	}

	/**
	 * @return the most-recently received location fix (might be null).
	 */
	public synchronized Location getLastLocation() {
		return lastLocation;
	}

	/**
	 * @return true if the map will be centered at the next received location fix, false otherwise.
	 */
	public synchronized boolean isCenterAtNextFix() {
		return centerAtNextFix;
	}

	/**
	 * @return true if the receiving of location updates is currently enabled, false otherwise.
	 */
	public synchronized boolean isEnabled() {
		return myLocationEnabled;
	}

	/**
	 * @return true if the snap-to-location mode is enabled, false otherwise.
	 */
	public synchronized boolean isSnapToLocationEnabled() {
		return snapToLocationEnabled;
	}

	@Override
	public void onDestroy() {
		map_needle_pinned.onDestroy();
		map_needle.onDestroy();
		map_needle_off.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) {
		synchronized (this) {
			long age = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / 1000000;
			if (age > 5 || !location.hasAccuracy() || location.getAccuracy() == 0) {
				if (DEBUG) { Log.d(TAG, "off: age=" + age); }
				marker = map_needle_off;
				circle.setRadius(0);
			}
			else {
				float accuracy = location.getAccuracy();
				if (DEBUG) { Log.d(TAG, "circle: accuracy=" + accuracy); }
				circle.setRadius(accuracy);
				if (!location.hasSpeed() || !location.hasBearing()) {
					marker = map_needle_pinned;
					if (DEBUG) { Log.d(TAG, "pinned: no speed or bearing"); }
				}
				else {
					float speed = location.getSpeed();
					if (speed < 2.0) {
						if (DEBUG) { Log.d(TAG, "pinned: speed=" + speed); }
						marker = map_needle_pinned;
					}
					else {
						float bearing = location.getBearing();
						map_needle.setDegree(bearing);
						marker = map_needle;
					}
				}
			}
			LatLong latLong = new LatLong(location.getLatitude(), location.getLongitude(), true);
			marker.setLatLong(latLong);
			circle.setLatLong(latLong);
			if (centerAtNextFix || snapToLocationEnabled) {
				centerAtNextFix = false;
				mapViewPosition.setCenter(latLong);
			}
			requestRedraw();
			lastLocation = location;
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		enableBestAvailableProvider();
	}

	@Override
	public void onProviderEnabled(String provider) {
		enableBestAvailableProvider();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// do nothing
	}

	/**
	 * Minimum distance between location updates, in meters.
	 * You should call this before calling {@link ThreeStateLocationOverlay#enable(boolean)}.
	 */
	public void setMinDistance(float minDistance) {
		this.minDistance = minDistance;
	}

	/**
	 * Minimum time interval between location updates, in milliseconds.
	 * You should call this before calling {@link ThreeStateLocationOverlay#enable(boolean)}.
	 */
	public void setMinTime(long minTime) {
		this.minTime = minTime;
	}

	/**
	 * @param snapToLocationEnabled
	 *            whether the map should be centered at each received location fix.
	 */
	public synchronized void setSnapToLocationEnabled(boolean snapToLocationEnabled) {
		this.snapToLocationEnabled = snapToLocationEnabled;
	}

	protected synchronized boolean enableBestAvailableProvider() {
		disable();

		boolean result = false;
		for (String provider : locationManager.getProviders(true)) {
			if (LocationManager.GPS_PROVIDER.equals(provider)
					|| LocationManager.NETWORK_PROVIDER.equals(provider)) {
				result = true;
				locationManager.requestLocationUpdates(provider, minTime, minDistance, this);
			}
		}
		myLocationEnabled = result;
		return result;
	}
}
