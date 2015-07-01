package org.pyneo.android.gui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.AndroidPreferences;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.FixedPixelCircle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.layer.TileLayer;
import org.mapsforge.map.model.common.PreferencesFacade;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.map.reader.MapDataStore;
import org.mapsforge.map.reader.MultiMapDataStore;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;

public class Map extends Base {
	static final private String TAG = Sample.TAG;
	static final private boolean DEBUG = Sample.DEBUG;
	// get one from http://download.mapsforge.org/maps/ and adapt path to your needs:
	private static final String MAPFILE1 = "/storage/sdcard1/mapsforge/germany.map";
	private static final String MAPFILE2 = "/storage/sdcard1/mapsforge/netherlands.map";
	// get the track from a gpx file (adjust for your needs):
	private static final String GPXFILE = "/sdcard/tabulae/export/track50.gpx";

	MapView mapView;
	TileRendererLayer tileLayer;
	TileCache tileCache;
	AlternatingLine polyline;

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

		polyline = new AlternatingLine(AndroidGraphicFactory.INSTANCE);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) { Log.d(TAG, "Map.onCreateView"); }
		return mapView;
	}

	@Override public void onStart() {
		super.onStart();
		if (DEBUG) { Log.d(TAG, "Map.onStart"); }
		List<LatLong> latLongs = polyline.getLatLongs();
		LatLong latLong = new LatLong(52.5, 13.4);
		try {
			for (TrackGpxParser.TrackPoint trackPoint : new TrackGpxParser(new File(GPXFILE))) {
				latLong = trackPoint;
				latLongs.add(latLong);
			}
			// add: mapView.getModel().mapViewPosition.setMapPosition(new MapPosition(bb.getCenterPoint(), LatLongUtils.zoomForBounds(dimension, bb, mapView.getModel().displayModel.getTileSize())));
		}
		catch (Exception e) {
			Log.e(TAG, "Map.onStart", e);
		}
		// warp to track
		mapView.getModel().mapViewPosition.setCenter(latLong);
		mapView.getModel().mapViewPosition.setZoomLevel((byte)12);
		MultiMapDataStore multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.DEDUPLICATE);
		tileLayer = new TileRendererLayer(tileCache, multiMapDataStore, mapView.getModel().mapViewPosition,
			false, true, AndroidGraphicFactory.INSTANCE);
		multiMapDataStore.addMapDataStore(new MapFile(new File(MAPFILE1)), true, true);
		multiMapDataStore.addMapDataStore(new MapFile(new File(MAPFILE2)), false, false);
		tileLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		mapView.getLayerManager().getLayers().add(tileLayer);
		mapView.getLayerManager().getLayers().add(polyline);
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

	public void inform(int event, Bundle extra) {
		if (DEBUG) { Log.d(TAG, "Map.inform event=" + event); }
	}
}
