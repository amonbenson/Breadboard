package de.amonbenson.breadboard.core.parts;

import de.amonbenson.breadboard.Colors;
import de.amonbenson.breadboard.PartChooser;
import de.amonbenson.breadboard.core.Layer;
import de.amonbenson.breadboard.core.Part;
import de.amonbenson.breadboard.core.Pin;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Resistor extends Part {

	public Resistor() {
		this(10, 2);
	}
	
	public Resistor(double value, int unitId) {
		super(0, -0.5, 5, 1, "Resistor", PartChooser.GROUP_PASSIVE_COMPONENTS, "R", true, new String[] {"G\u03A9", "M\u03A9", "K\u03A9", "\u03A9"}, unitId, new Pin(0, 0),
				new Pin(5, 0));

		setValue("" + value);
	}

	private Color[] getRingColors(double value) {
		int power = 1;
		int val = (int) getDoubleValue();
		int val1 = val / 10;
		int val2 = val % 10;
		
		Color[] colors = new Color[] {
				Colors.getResistorRingColor(val1),					// Value 1
				Colors.getResistorRingColor(val2),					// Value 2
				Colors.getResistorRingColor(Math.max(power, 0)),	// Multiplicator
				Colors.getResistorRingColor(10)						// Tolerance (5%)
		};
		
		return colors;
	}

	@Override
	public void render(GraphicsContext g, Layer layer) {
		if (layer == Layer.TOP) {
			// Legs
			g.setStroke(Colors.COLOR_METAL);
			g.setLineWidth(0.1);
			g.strokeLine(x, y + height / 2, x + 0.5, y + height / 2);
			g.strokeLine(x + width - 0.5, y + height / 2, x + width, y + height / 2);

			// Body
			g.setFill(Colors.COLOR_RESISTOR);
			g.fillRect(x + 1, y, width - 2, height);
			g.fillOval(x + 0.5, y, height, height);
			g.fillOval(x + width - height - 0.5, y, height, height);

			// Rings
			Color[] colors = getRingColors(getDoubleValue());
			int numStripes = colors.length * 2 + 1;
			double ringsWidth = width - 2;
			for (int i = 0; i < colors.length; i++) {
				g.setFill(colors[i]);
				g.fillRect(x + 1 + (ringsWidth * (i * 2 + 1) / numStripes), y, ringsWidth / numStripes, height);
			}
		} else {
			g.setStroke(Color.BLACK);
			g.setLineWidth(0.05);

			g.strokeLine(x, y + height / 2, x + 0.5, y + height / 2);
			g.strokeLine(x + width - 0.5, y + height / 2, x + width, y + height / 2);
			g.strokeRect(x + 0.5, y, width - 1, height);
		}
	}
}