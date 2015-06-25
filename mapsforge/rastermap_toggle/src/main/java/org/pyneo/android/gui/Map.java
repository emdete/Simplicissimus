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
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.ExternalRenderTheme;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeStyleMenu;

public class Map extends Base {
	static final private String TAG = Sample.TAG;
	static final private boolean DEBUG = Sample.DEBUG;
	// get one from http://download.mapsforge.org/maps/ and adapt path to your needs:
	private static final String MAPFILE = "/storage/sdcard1/mapsforge/germany.map";
	// leave out when not wanted:
	private static final String THEMEFILE = "/storage/sdcard1/mapsforge/Tiramisu_3_0_beta1.xml";
	protected MapView mapView;
	protected TileLayer[] tileLayers = new TileLayer[3];
	protected TileCache[] tileCaches = new TileCache[3];
	Layers layers;

	public void inform(int event, Bundle extra) {
		if (DEBUG) { Log.d(TAG, "Map.inform event=" + event); }
		switch (event) {
			case R.id.event_mapquest:
				mapView.getLayerManager().getLayers().clear();
				mapView.getLayerManager().getLayers().add(tileLayers[0]);
				break;
			case R.id.event_vector:
				mapView.getLayerManager().getLayers().clear();
				mapView.getLayerManager().getLayers().add(tileLayers[1]);
				break;
			case R.id.event_satellite:
				mapView.getLayerManager().getLayers().clear();
				mapView.getLayerManager().getLayers().add(tileLayers[2]);
				break;
		}
	}

	@Override public void onAttach(Activity activity) {
		if (DEBUG) { Log.d(TAG, "Map.onAttach"); }
		super.onAttach(activity);
	}

	@Override public void onCreate(Bundle bundle) {
		if (DEBUG) { Log.d(TAG, "Map.onCreate"); }
		super.onCreate(bundle);
		AndroidGraphicFactory.createInstance(getActivity().getApplication());
		mapView = new MapView(getActivity());
		// mapquest:
		tileCaches[0]= AndroidUtil.createTileCache(getActivity(), "mapcache-0", mapView.getModel().displayModel.getTileSize(),
			1f, mapView.getModel().frameBufferModel.getOverdrawFactor());
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
		// vector:
		tileCaches[1] = AndroidUtil.createTileCache(getActivity(), "mapcache-1", mapView.getModel().displayModel.getTileSize(),
			1f, mapView.getModel().frameBufferModel.getOverdrawFactor());
		tileLayers[1] = new TileRendererLayer(tileCaches[1], new MapFile(new File(MAPFILE)), mapView.getModel().mapViewPosition,
			false, true, AndroidGraphicFactory.INSTANCE);
		try {
			((TileRendererLayer)tileLayers[1]).setXmlRenderTheme(new ExternalRenderTheme(new File(THEMEFILE)));
		}
		catch (FileNotFoundException ignore) {
			((TileRendererLayer)tileLayers[1]).setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		}
		// satellite
		tileCaches[2]= AndroidUtil.createTileCache(getActivity(), "mapcache-2", mapView.getModel().displayModel.getTileSize(),
			1f, mapView.getModel().frameBufferModel.getOverdrawFactor());
		tileLayers[2] = new TileDownloadLayer(tileCaches[2], mapView.getModel().mapViewPosition, new SatTileSource(), AndroidGraphicFactory.INSTANCE);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) { Log.d(TAG, "Map.onCreateView"); }
		//mapView.getModel().init(preferencesFacade);
		mapView.setClickable(true);
		mapView.getMapScaleBar().setVisible(true);
		mapView.setBuiltInZoomControls(true);
		mapView.getMapZoomControls().setShowMapZoomControls(true);
		mapView.getMapZoomControls().setZoomLevelMin((byte)2);
		mapView.getMapZoomControls().setZoomLevelMax((byte)18);
		mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		// activate one:
		mapView.getLayerManager().getLayers().add(tileLayers[2]);
		mapView.getModel().mapViewPosition.setZoomLevel((byte)12);
		mapView.getModel().mapViewPosition.setCenter(new LatLong(52.517037, 13.38886));
		return mapView;
	}

	@Override public void onResume() {
		super.onResume();
		((TileDownloadLayer)tileLayers[0]).onResume();
		((TileDownloadLayer)tileLayers[2]).onResume();
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DEBUG) { Log.d(TAG, "Map.onActivityCreated"); }
	}

	@Override public void onPause() {
		((TileDownloadLayer)tileLayers[0]).onPause();
		((TileDownloadLayer)tileLayers[2]).onPause();
	}

	@Override public void onDestroy() {
		//for (TileLayer tileLayer: tileLayers) tileLayer.destroy();
		//for (TileCache tileCache: tileCaches) tileCache.destroy();
		//mapView.getModel().mapViewPosition.destroy();
		//mapView.destroy();
		//AndroidGraphicFactory.clearResourceMemoryCache();
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
}
