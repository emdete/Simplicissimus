package de.emdete.android.gui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
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
import org.mapsforge.map.layer.cache.FileSystemTileCache;
import org.mapsforge.map.layer.cache.InMemoryTileCache;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.cache.TwoLevelTileCache;
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
	private static final String MAPFILE0 = "/storage/sdcard1/mapsforge/maps/world.map";
	private static final String MAPFILE1 = "/storage/sdcard1/mapsforge/maps/germany.map";
	private static final String MAPFILE2 = "/storage/sdcard1/mapsforge/maps/netherlands.map";
	private static final File storage = new File("/storage/sdcard1/mapsforge/tilecache");

	MapView mapView;
	TileRendererLayer tileLayer;
	TileCache tileCache;
	PreferencesFacade preferencesFacade;

	@SuppressWarnings("deprecation")
	@TargetApi(18)
	static long getAvailableCacheSlots(String directory, int fileSize) {
		long ret = 0;
		StatFs statfs = new StatFs(directory);
		if (android.os.Build.VERSION.SDK_INT >= 18) {
			ret = statfs.getAvailableBytes() / fileSize;
		}
		else {
			// problem is overflow with devices with large storage, so order is important here
			// additionally avoid division by zero in devices with a large block size
			int blocksPerFile = Math.max(fileSize / statfs.getBlockSize(), 1);
			ret = statfs.getAvailableBlocks() / blocksPerFile;
		}
		Log.d(TAG, "getAvailableCacheSlots ret=" + ret);
		return ret;
	}

	static int estimateSizeOfFileSystemCache(String cacheDirectoryName, int firstLevelSize, int tileSize) {
		// assumption on size of files in cache, on the large side as not to eat
		// up all free space, real average probably 50K compressed
		final int tileCacheFileSize = 4 * tileSize * tileSize;
		final int maxCacheFiles = 2000; // arbitrary, probably too high
		// result cannot be bigger than maxCacheFiles
		int result = (int) Math.min(maxCacheFiles, getAvailableCacheSlots(cacheDirectoryName, tileCacheFileSize));
		if (firstLevelSize > result) {
			// no point having a file system cache that does not even hold the memory cache
			result = 0;
		}
		Log.d(TAG, "estimateSizeOfFileSystemCache result=" + result);
		return result;
	}

	static TileCache createExternalStorageTileCache(Context c, String id, int firstLevelSize, int tileSize) {
		Log.d(TAG, "createExternalStorageTileCache firstLevelSize=" + firstLevelSize);
		TileCache firstLevelTileCache = new InMemoryTileCache(firstLevelSize);
		if (storage != null) { // storage will be null if full
			String cacheDirectoryName = storage.getAbsolutePath() + File.separator + id;
			File cacheDirectory = new File(cacheDirectoryName);
			if (cacheDirectory.exists() || cacheDirectory.mkdirs()) {
				int tileCacheFiles = estimateSizeOfFileSystemCache(cacheDirectoryName, firstLevelSize, tileSize);
				if (cacheDirectory.canWrite() && tileCacheFiles > 0) {
					try {
						Log.d(TAG, "createExternalStorageTileCache tileCacheFiles=" + tileCacheFiles);
						TileCache secondLevelTileCache = new FileSystemTileCache(tileCacheFiles,
							cacheDirectory, AndroidGraphicFactory.INSTANCE, true, 25, true);
						return new TwoLevelTileCache(firstLevelTileCache, secondLevelTileCache);
					} catch (IllegalArgumentException e) {
						Log.w(TAG, "createExternalStorageTileCache e=" + e);
					}
				}
			}
			else {
				Log.w(TAG, "createExternalStorageTileCache can't");
			}
		}
		return firstLevelTileCache;
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		if (DEBUG) {
			Log.d(TAG, "Map.onCreate");
		}
		AndroidGraphicFactory.createInstance(getActivity().getApplication());
		mapView = new MapView(getActivity());
		preferencesFacade = new AndroidPreferences(getActivity().getSharedPreferences("map", Context.MODE_PRIVATE));
		mapView.getModel().init(preferencesFacade);
		if (mapView.getModel().mapViewPosition.getZoomLevel() == 0) {
			// warp to 'unter den linden'
			mapView.getModel().mapViewPosition.setCenter(new LatLong(52.517037, 13.38886));
			mapView.getModel().mapViewPosition.setZoomLevel((byte)12);
		}
		mapView.setClickable(true);
		mapView.getMapScaleBar().setVisible(true);
		mapView.setBuiltInZoomControls(true);
		mapView.getMapZoomControls().setZoomLevelMin((byte) 2);
		mapView.getMapZoomControls().setZoomLevelMax((byte) 18);
		mapView.getMapZoomControls().setShowMapZoomControls(true);
		mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		tileCache = createExternalStorageTileCache(getActivity(), "osmarender", 50, mapView.getModel().displayModel.getTileSize());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (DEBUG) {
			Log.d(TAG, "Map.onCreateView");
		}
		return mapView;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (DEBUG) {
			Log.d(TAG, "Map.onStart");
		}
		MultiMapDataStore multiMapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.DEDUPLICATE);
		tileLayer = new TileRendererLayer(tileCache, multiMapDataStore, mapView.getModel().mapViewPosition,
				false, true, AndroidGraphicFactory.INSTANCE);
		multiMapDataStore.addMapDataStore(new MapFile(new File(MAPFILE1)), true, true);
		multiMapDataStore.addMapDataStore(new MapFile(new File(MAPFILE2)), false, false);
		multiMapDataStore.addMapDataStore(new MapFile(new File(MAPFILE0)), false, false);
		tileLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);
		mapView.getLayerManager().getLayers().add(tileLayer);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
		mapView.getLayerManager().getLayers().remove(tileLayer);
		tileLayer.onDestroy();
		mapView.getModel().save(preferencesFacade);
		preferencesFacade.save();
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
