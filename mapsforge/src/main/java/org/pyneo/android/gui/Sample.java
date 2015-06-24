package org.pyneo.android.gui;

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
	static final String TAG = "org.pyneo.sample";
	private static final boolean DEBUG = true;
	// get one from http://download.mapsforge.org/maps/ and adapt path to your needs:
	private static final String MAPFILE = "/storage/sdcard1/germany.map";
	// leave out when not wanted:
	private static final String THEMEFILE = "/storage/sdcard1/theme.xml";

	MapView mapView;
	TileCache tileCache;
	TileRendererLayer tileRendererLayer;
	Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
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
		mapView.setClickable(true);
		mapView.getMapScaleBar().setVisible(true);
		mapView.setBuiltInZoomControls(false);
		//mapView.getMapZoomControls().setShowMapZoomControls(false);
		//mapView.getMapZoomControls().setZoomLevelMin((byte)8);
		//mapView.getMapZoomControls().setZoomLevelMax((byte)20);
		mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		tileCache = AndroidUtil.createTileCache(this, "mapcache", mapView.getModel().displayModel.getTileSize(),
			1f, mapView.getModel().frameBufferModel.getOverdrawFactor());
		MapDataStore mapDataStore = new MapFile(new File(MAPFILE));
		tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore, mapView.getModel().mapViewPosition,
			false, true, AndroidGraphicFactory.INSTANCE);
		try {
			tileRendererLayer.setXmlRenderTheme(new ExternalRenderTheme(new File(THEMEFILE)));
		}
		catch (FileNotFoundException ignore) {
			tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		}
		mapView.getLayerManager().getLayers().add(tileRendererLayer);
		mapView.getModel().mapViewPosition.setZoomLevel((byte)12);
		// warp to 'unter den linden'
		mapView.getModel().mapViewPosition.setCenter(new LatLong(52.517037, 13.38886));
	}

	@Override
	protected void onStart() {
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
		mapView.getLayerManager().getLayers().remove(tileRendererLayer);
		tileRendererLayer.onDestroy();
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
		tileCache.destroy();
		mapView.getModel().mapViewPosition.destroy();
		mapView.destroy();
		AndroidGraphicFactory.clearResourceMemoryCache();
	}
}
