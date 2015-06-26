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
import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.model.Tile;
import org.mapsforge.map.android.AndroidPreferences;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OnlineTileSource;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.Layer;
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
	// leave out when not wanted:
	private static final String THEMEFILE = "/storage/sdcard1/mapsforge/Tiramisu_3_0_beta1.xml";
	MapView mapView;
	TileLayer[] tileLayers = new TileLayer[3];
	TileCache[] tileCaches = new TileCache[3];
	int current = -1;

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
		for (int i=0;i<tileCaches.length;i++) {
			tileCaches[i]= AndroidUtil.createTileCache(getActivity(),
				"mapcache-" + i, mapView.getModel().displayModel.getTileSize(), 1f,
				mapView.getModel().frameBufferModel.getOverdrawFactor());
		}
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
		// mapquest:
		OnlineTileSource onlineTileSource = new OnlineTileSource(new String[]{"otile1.mqcdn.com", "otile2.mqcdn.com", "otile3.mqcdn.com", "otile4.mqcdn.com"}, 80){
			@Override public URL getTileUrl(Tile tile) throws MalformedURLException {
				URL url = super.getTileUrl(tile);
				Log.d(TAG, "getTileUrl url=" + url);
				return url;
			}
		};
		onlineTileSource
			.setAlpha(false)
			.setBaseUrl("/tiles/1.0.0/map/")
			.setExtension("png")
			.setName("MapQuest")
			.setParallelRequestsLimit(8)
			.setProtocol("http")
			.setTileSize(256)
			.setZoomLevelMax((byte) 18)
			.setZoomLevelMin((byte) 2)
			;
		tileLayers[0] = new TileDownloadLayer(tileCaches[0], mapView.getModel().mapViewPosition, onlineTileSource, AndroidGraphicFactory.INSTANCE);
		mapView.getLayerManager().getLayers().add(tileLayers[0]);
		tileLayers[0].setVisible(false);
		// vector:
		MultiMapDataStore multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.DEDUPLICATE);
		tileLayers[1] = new TileRendererLayer(tileCaches[1], multiMapDataStore, mapView.getModel().mapViewPosition,
			false, true, AndroidGraphicFactory.INSTANCE);
		multiMapDataStore.addMapDataStore(new MapFile(new File(MAPFILE1)), true, true);
		multiMapDataStore.addMapDataStore(new MapFile(new File(MAPFILE2)), false, false);
		try {
			((TileRendererLayer)tileLayers[1]).setXmlRenderTheme(new ExternalRenderTheme(new File(THEMEFILE)));
		}
		catch (FileNotFoundException ignore) {
			((TileRendererLayer)tileLayers[1]).setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		}
		mapView.getLayerManager().getLayers().add(tileLayers[1]);
		tileLayers[1].setVisible(false);
		// satellite
		tileLayers[2] = new TileDownloadLayer(tileCaches[2], mapView.getModel().mapViewPosition, new SatTileSource(), AndroidGraphicFactory.INSTANCE);
		mapView.getLayerManager().getLayers().add(tileLayers[2]);
		tileLayers[2].setVisible(false);
		enable(0);
	}

	@Override public void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "onPause");
		for (TileLayer tileLayer: tileLayers)
			if (tileLayer instanceof TileDownloadLayer)
				((TileDownloadLayer)tileLayers[current]).onPause();
	}

	@Override public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume");
		for (TileLayer tileLayer: tileLayers)
			if (tileLayer instanceof TileDownloadLayer)
				((TileDownloadLayer)tileLayer).onResume();
	}

	@Override public void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
		mapView.getLayerManager().getLayers().clear();
		for (TileLayer tileLayer: tileLayers)
			if (tileLayer instanceof TileDownloadLayer)
				((TileDownloadLayer)tileLayer).onPause();
	}

	@Override public void onDestroy() {
		super.onDestroy();
		if (DEBUG) { Log.d(TAG, "Map.onDestroy"); }
		for (TileLayer tileLayer: tileLayers)
			tileLayer.onDestroy();
		for (TileCache tileCache: tileCaches)
			tileCache.destroy();
		mapView.getModel().mapViewPosition.destroy();
		mapView.destroy();
		AndroidGraphicFactory.clearResourceMemoryCache();
	}

	static class SatTileSource extends OnlineTileSource {
		protected static final char[] NUM_CHAR = {'0', '1', '2', '3'};
		private String encodeQuadTree(int zoom, int tilex, int tiley) {
			char[] tileNum = new char[zoom];
			for (int i = zoom - 1; i >= 0; i--) {
				// Binary encoding using ones for tilex and twos for tiley. if a
				// bit is set in tilex and tiley we get a three.
				int num = (tilex % 2) | ((tiley % 2) << 1);
				tileNum[i] = NUM_CHAR[num];
				tilex >>= 1;
				tiley >>= 1;
			}
			return new String(tileNum);
		}

		SatTileSource() {
			super(new String[]{"ecn.t1.tiles.virtualearth.net", }, 80);
			setAlpha(false);
			setBaseUrl("/tiles/a");
			setExtension(".jpeg?g=1134&n=z");
			setName("Satellite");
			setParallelRequestsLimit(8);
			setProtocol("http");
			setTileSize(256);
			setZoomLevelMax((byte)19);
			setZoomLevelMin((byte)2);
		}

		@Override public URL getTileUrl(Tile tile) throws MalformedURLException {
			StringBuilder stringBuilder = new StringBuilder()
				.append(getBaseUrl())
				.append(encodeQuadTree(tile.zoomLevel, tile.tileX, tile.tileY))
				.append(getExtension());
			URL url = new URL(getProtocol(), getHostName(), port, stringBuilder.toString());
			Log.d(TAG, "getTileUrl url=" + url);
			return url;
		}
	}

	void enable(int newlayer) {
		if (current >= 0) tileLayers[current].setVisible(false);
		current = newlayer;
		if (current >= 0) tileLayers[current].setVisible(true);
		mapView.getLayerManager().redrawLayers();
	}

	public void inform(int event, Bundle extra) {
		if (DEBUG) { Log.d(TAG, "Map.inform event=" + event); }
		switch (event) {
			case R.id.event_mapquest:
				enable(0);
				break;
			case R.id.event_vector:
				enable(1);
				break;
			case R.id.event_satellite:
				enable(2);
				break;
		}
	}
}
