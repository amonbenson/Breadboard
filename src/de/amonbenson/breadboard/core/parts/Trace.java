package de.amonbenson.breadboard.core.parts;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.amonbenson.breadboard.Colors;
import de.amonbenson.breadboard.PartChooser;
import de.amonbenson.breadboard.core.Layer;
import de.amonbenson.breadboard.core.Part;
import de.amonbenson.breadboard.core.Pin;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Trace extends Part {

	public static final double SELECTION_MIN_RADIUS = 0.5;
	
	private Layer layer;

	public Trace(int sx, int sy, int ex, int ey, Layer layer) {
		super(0, 0, ex - sx, ey - sy, "Trace", PartChooser.GROUP_UNKNOWN, "", false, null, 0, new Pin(0, 0), new Pin(ex - sx, ey - sy));
		this.layer = layer;

		if (width < 0) {
			width = -width;
			x -= width;
		}
		if (height < 0) {
			height = -height;
			y -= height;
		}

		xPos = sx;
		yPos = sy;
	}

	public Pin getStartPin() {
		return getPin(0);
	}

	public Pin getEndPin() {
		return getPin(getPinCount() - 1);
	}

	public Line2D getAsLine() {
		return new Line2D.Double(xPos + getStartPin().x, yPos + getStartPin().y, xPos + getEndPin().x,
				yPos + getEndPin().y);
	}
	
	@Override
	public void rotate90() {
		// Does nothing here
	}

	@Override
	public boolean contains(Point2D point) {
		// Check for a point collision (we'll assume a rectangle with 1x1 size
		// around the cursor point)
		return intersects(new Rectangle2D.Double(point.getX() - SELECTION_MIN_RADIUS,
				point.getY() - SELECTION_MIN_RADIUS, SELECTION_MIN_RADIUS * 2, SELECTION_MIN_RADIUS * 2));
	}

	@Override
	public boolean intersects(Rectangle2D rect) {
		// Check for a line collision
		return getAsLine().intersects(rect);
	}

	@Override
	public void render(GraphicsContext g, Layer layer) {
		if (this.layer == layer) {
			g.setStroke(Colors.COLOR_METAL);
			g.setLineWidth(0.2);
		} else {
			g.setStroke(Color.BLACK);
			g.setLineWidth(0.05);
		}

		for (int i = 0; i < getPinCount() - 1; i++) {
			Pin p1 = getPin(i);
			Pin p2 = getPin(i + 1);
			g.strokeLine(p1.x, p1.y, p2.x, p2.y);
		}
	}
}
