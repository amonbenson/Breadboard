package de.amonbenson.breadboard.core.parts;

import de.amonbenson.breadboard.Colors;
import de.amonbenson.breadboard.PartChooser;
import de.amonbenson.breadboard.core.Layer;
import de.amonbenson.breadboard.core.Part;
import de.amonbenson.breadboard.core.Pin;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

public class LED extends Part {
	
	public LED(String value) {
		super(-0.5, -1, 2, 2, "LED " + value, PartChooser.GROUP_ACTIVE_COMPONENTS, "D", false, null, 0, new Pin(0, 0),
				new Pin(1, 0));

		setValue(value);
	}

	@Override
	public void render(GraphicsContext g, Layer layer) {
		if (layer == layer.TOP) {
			// Inner metal thingies
			g.setStroke(Colors.COLOR_METAL);
			g.setLineWidth(0.2);
			g.strokeLine(x + 0.5, y + 1, x + 0.6, y + 1);
			g.strokeLine(x + 1, y + 1, x + 1.5, y + 1);
			
			// Get the color and gradient
			Color color = Color.RED;
			try {
				color = Color.web(getValue());
			} catch (IllegalArgumentException ex) {
				setValue("Red");
			};
			
			RadialGradient gradient = new RadialGradient(0, 0, 0.3, 0.3, 1, true, CycleMethod.NO_CYCLE,
					new Stop(0.0, color.deriveColor(5, 0.7, 1.5, 0.6)), new Stop(1.0, color.deriveColor(0, 1.0, 0.9, 0.9)));
			
			// Fill it
			g.setFill(gradient);
			g.fillOval(x, y, width, height);
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
