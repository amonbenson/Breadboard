package de.amonbenson.breadboard.core.parts;

import de.amonbenson.breadboard.Colors;
import de.amonbenson.breadboard.PartChooser;
import de.amonbenson.breadboard.core.Layer;
import de.amonbenson.breadboard.core.Part;
import de.amonbenson.breadboard.core.Pin;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class ElectrolyticCapacitor extends Part {

	public ElectrolyticCapacitor() {
		this(100, 2);
	}
	
	public ElectrolyticCapacitor(double value, int unitId) {
		super(-1, -1.5, 3, 3, "Electrolytic Capacitor", PartChooser.GROUP_PASSIVE_COMPONENTS, "C", true, new String[] {"F", "mF", "\u03BCF", "nF", "pF"}, unitId, new Pin(0, 0),
				new Pin(1, 0));
		
		setValue("" + value);
	}

	@Override
	public void render(GraphicsContext g, Layer layer) {
		if (layer == Layer.TOP) {
			g.setFill(Colors.COLOR_SKY_BLUE);
			g.fillOval(x, y, width, height);
			
			g.setFill(Colors.COLOR_METAL);
			g.fillArc(x, y, width, height, -25, 50, ArcType.ROUND);
			g.fillOval(x + width * 0.2, y + height * 0.2, width * 0.6, height * 0.6);
			
		} else {
			g.setStroke(Color.BLACK);
			g.setLineWidth(0.05);
			
			// Outline
			g.strokeOval(x, y, width, height);
			
			// Plus sign
			g.strokeLine(-0.1, -0.5, -0.7, -0.5);
			g.strokeLine(-0.4, -0.2, -0.4, -0.8);
		}
	}
}
