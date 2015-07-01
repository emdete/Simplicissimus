package org.pyneo.android.gui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.model.common.Observer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.reader.MultiMapDataStore;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;

public class Map extends Base {
	static final private String TAG = Sample.TAG;
	static final private boolean DEBUG = Sample.DEBUG;
	// get one from http://download.mapsforge.org/maps/ and adapt path to your needs:
	private static final String MAPFILE0 = "/storage/sdcard1/mapsforge/world.map";
	private static final String MAPFILE1 = "/storage/sdcard1/mapsforge/germany.map";
	private static final String MAPFILE2 = "/storage/sdcard1/mapsforge/netherlands.map";

	MapView mapView;
	TileRendererLayer tileLayer;
	TileCache tileCache;

	@Override public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		if (DEBUG) { Log.d(TAG, "Map.onCreate"); }
		AndroidGraphicFactory.createInstance(getActivity().getApplication());
		mapView = new MapView(getActivity());
		//
		mapView.setClickable(true);
		mapView.getMapScaleBar().setVisible(true);
		mapView.setBuiltInZoomControls(true);
		mapView.getMapZoomControls().setZoomLevelMin((byte)2);
		mapView.getMapZoomControls().setZoomLevelMax((byte)18);
		mapView.getMapZoomControls().setShowMapZoomControls(true);
		mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		tileCache = AndroidUtil.createTileCache(getActivity(), "mapcache", mapView.getModel().displayModel.getTileSize(),
			1f, mapView.getModel().frameBufferModel.getOverdrawFactor());
		mapView.getModel().displayModel.setUserScaleFactor(1.3f);
		final MapViewPosition mapViewPosition = mapView.getModel().mapViewPosition;
		mapViewPosition.addObserver(new Observer() {
			LatLong lastLatLong = null;
			byte lastZoom = -1;
			@Override public void onChange() {
				LatLong currentLatLong = mapViewPosition.getCenter();
				if (!currentLatLong.equals(lastLatLong)) {
					Bundle extra = new Bundle();
					extra.putSerializable("latlong", currentLatLong);
					inform(0, extra);
					lastLatLong = currentLatLong;
				}
				byte currentZoom = mapViewPosition.getZoomLevel();
				if (lastZoom != currentZoom) {
					Bundle extra = new Bundle();
					extra.putByte("zoom", currentZoom);
					inform(0, extra);
					lastZoom = currentZoom;
				}
			}
		});
		final MapViewPosition mapViewPosition = mapView.getModel().mapViewPosition;
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) { Log.d(TAG, "Map.onCreateView"); }
		return mapView;
	}

	@Override public void onStart() {
		super.onStart();
		if (DEBUG) { Log.d(TAG, "Map.onStart"); }
		// warp to 'unter den linden'
		mapView.getModel().mapViewPosition.setCenter(new LatLong(52.517037, 13.38886));
		mapView.getModel().mapViewPosition.setZoomLevel((byte)12);
		MultiMapDataStore multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.DEDUPLICATE);
		tileLayer = new TileRendererLayer(tileCache, multiMapDataStore, mapView.getModel().mapViewPosition,
			false, true, AndroidGraphicFactory.INSTANCE) {
			@Override public boolean onLongPress(LatLong tapLatLong, Point thisXY, Point tapXY) {
				return Map.this.onLongPress(tapLatLong, thisXY, tapXY);
			}
			};
		multiMapDataStore.addMapDataStore(new MapFile(new File(MAPFILE1)), true, true);
		multiMapDataStore.addMapDataStore(new MapFile(new File(MAPFILE2)), false, false);
		multiMapDataStore.addMapDataStore(new MapFile(new File(MAPFILE0)), false, false);
		tileLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		mapView.getLayerManager().getLayers().add(tileLayer);
	}

	@Override public void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
		mapView.getLayerManager().getLayers().remove(tileLayer);
		tileLayer.onDestroy();
	}

	@Override public void onDestroy() {
		super.onDestroy();
		if (DEBUG) { Log.d(TAG, "Map.onDestroy"); }
		tileCache.destroy();
		mapView.getModel().mapViewPosition.destroy();
		mapView.destroy();
		AndroidGraphicFactory.clearResourceMemoryCache();
	}

	public boolean onLongPress(LatLong tapLatLong, Point thisXY, Point tapXY) {
		if (DEBUG) { Log.d(TAG, "Map.onLongPress tapLatLong=" + tapLatLong + ", thisXY=" + thisXY + ", tapXY=" + tapXY); }
		return true;
	}

	public void inform(int event, Bundle extra) {
		if (DEBUG) { Log.d(TAG, "Map.inform event=" + event + ", extra=" + extra); }
	}
}
