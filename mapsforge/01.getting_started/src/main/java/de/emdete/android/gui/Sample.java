package de.emdete.android.gui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.io.FileNotFoundException;

public class Sample extends Activity {
	static final String TAG = "de.emdete.sample";
	private static final boolean DEBUG = true;
	// get one from http://download.mapsforge.org/maps/ and adapt path to your needs:
	private static final String MAPFILE = "/storage/sdcard1/mapsforge/germany.map";

	MapView mapView;
	TileRendererLayer tileLayer;
	TileCache tileCache;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "onCreate");
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable e) {
				Log.e(TAG, "error e=" + e, e);
				finish();
			}
		});
		AndroidGraphicFactory.createInstance(getApplication());
		mapView = new MapView(this);
		setContentView(mapView);
		mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		mapView.setClickable(true);
		mapView.getMapScaleBar().setVisible(true);
		mapView.setBuiltInZoomControls(true);
		mapView.getMapZoomControls().setZoomLevelMin((byte)2);
		mapView.getMapZoomControls().setZoomLevelMax((byte)18);
		mapView.getMapZoomControls().setShowMapZoomControls(true);
		// create a tile cache of suitable size
		tileCache = AndroidUtil.createTileCache(this, "mapcache",
			mapView.getModel().displayModel.getTileSize(), 1f,
			mapView.getModel().frameBufferModel.getOverdrawFactor());
	}

	@Override protected void onStart() {
		super.onStart();
		if (DEBUG) Log.d(TAG, "onStart");
		// warp to 'unter den linden'
		mapView.getModel().mapViewPosition.setCenter(new LatLong(52.517037, 13.38886));
		mapView.getModel().mapViewPosition.setZoomLevel((byte)12);
		tileLayer = new TileRendererLayer(tileCache, new MapFile(new File(MAPFILE)), mapView.getModel().mapViewPosition,
			false, true, AndroidGraphicFactory.INSTANCE);
		tileLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		mapView.getLayerManager().getLayers().add(tileLayer);
	}

	@Override protected void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
		mapView.getLayerManager().getLayers().remove(tileLayer);
		tileLayer.onDestroy();
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "onDestroy");
		tileCache.destroy();
		mapView.getModel().mapViewPosition.destroy();
		mapView.destroy();
		AndroidGraphicFactory.clearResourceMemoryCache();
	}
}
