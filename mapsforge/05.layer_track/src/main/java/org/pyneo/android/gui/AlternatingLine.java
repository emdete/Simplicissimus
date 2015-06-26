package org.pyneo.android.gui;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Path;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.overlay.Polyline;

import java.util.HashMap;
import java.util.Iterator;

public class AlternatingLine extends Polyline {
	GraphicFactory graphicFactory;

	public AlternatingLine(GraphicFactory graphicFactory) {
		super(null, graphicFactory);
		this.graphicFactory = graphicFactory;
	}

	@Override public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
		if (getLatLongs().isEmpty()) {
			return;
		}
		Iterator<LatLong> iterator = getLatLongs().iterator();
		if (!iterator.hasNext()) {
			return;
		}
		long mapSize = MercatorProjection.getMapSize(zoomLevel, displayModel.getTileSize());
		java.util.Map<Paint,Path> paints = new HashMap<Paint,Path>();
		LatLong from = iterator.next();
		while (iterator.hasNext()) {
			LatLong to = iterator.next();
			Paint paint = getPaintStroke(from, to);
			Path path = paints.get(paint);
			if (path == null) {
				path = graphicFactory.createPath();
				paints.put(paint, path);
			}
			float x = (float) (MercatorProjection.longitudeToPixelX(from.longitude, mapSize) - topLeftPoint.x);
			float y = (float) (MercatorProjection.latitudeToPixelY(from.latitude, mapSize) - topLeftPoint.y);
			path.moveTo(x, y);
			x = (float) (MercatorProjection.longitudeToPixelX(to.longitude, mapSize) - topLeftPoint.x);
			y = (float) (MercatorProjection.latitudeToPixelY(to.latitude, mapSize) - topLeftPoint.y);
			path.lineTo(x, y);
			from = to;
		}
		for (Paint paint: paints.keySet()) {
			canvas.drawPath(paints.get(paint), paint);
		}
	}

	public synchronized Paint getPaintStroke() {
		throw new RuntimeException("getPaintStroke called with no parms"); // i hate to do that, it's just to validate my code
	}

	public synchronized Paint getPaintStroke(LatLong from, LatLong to) {
		Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
		paint.setStrokeWidth(16);
		paint.setStyle(Style.STROKE);
		int alt = to == null? 0: ((TrackGpxParser.TrackPoint)to).getAltitude();
		switch (alt % 3) {
			case 0: paint.setColor(AndroidGraphicFactory.INSTANCE.createColor(Color.GREEN)); break;
			case 1: paint.setColor(AndroidGraphicFactory.INSTANCE.createColor(Color.RED)); break;
			default: paint.setColor(AndroidGraphicFactory.INSTANCE.createColor(Color.BLUE)); break;
		}
		return paint;
	}
}
