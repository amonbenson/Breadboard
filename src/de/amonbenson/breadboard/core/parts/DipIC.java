package de.amonbenson.breadboard.core.parts;

import de.amonbenson.breadboard.Colors;
import de.amonbenson.breadboard.PartChooser;
import de.amonbenson.breadboard.core.Layer;
import de.amonbenson.breadboard.core.Part;
import de.amonbenson.breadboard.core.Pin;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Text;

public class DipIC extends Part {
	
	public DipIC(int pinsPerSide, String value) {
		super(-0.5, 0, pinsPerSide, 3, "DIP " + (pinsPerSide * 2) + " Package", PartChooser.GROUP_ICS, "Q", false, null, 0, createPins(pinsPerSide));
		
		setValue(value);
	}

	private static Pin[] createPins(int pinsPerSide) {
		Pin[] pins = new Pin[pinsPerSide * 2];
		for (int i = 0; i < pinsPerSide; i++) {
			pins[i * 2] = new Pin(i, 0);
			pins[i * 2 + 1] = new Pin(i, 3);
		}
		return pins;
	}

	@Override
	public void render(GraphicsContext g, Layer layer) {
		if (layer == Layer.TOP) {
			// Legs
			g.setStroke(Colors.COLOR_METAL);
			g.setLineWidth(0.2);
			
			for (double lx = 0.5; lx < width; lx++) {
				g.strokeLine(x + lx, y + 0, x + lx, y + 0.5);
				g.strokeLine(x + lx, y + height, x + lx, y + height - 0.5);
			}

			// Body
			g.setFill(Colors.COLOR_DARK_GRAY);
			g.fillRect(x, y + 0.2, width, height - 0.4);

			double notchSize = 0.6;
			g.setFill(Colors.COLOR_LIGHT_MARKINGS);
			g.fillRect(x + width - notchSize / 2, y + (height - notchSize) / 2, notchSize / 2, notchSize);
			g.fillArc(x + width - notchSize, y + (height - notchSize) / 2, notchSize, notchSize, 90, 180, ArcType.CHORD);

			// IC name
			Text text = new Text(getValue());
			text.setFont(g.getFont());
			double textW = text.getLayoutBounds().getWidth();
			double textH = text.getLayoutBounds().getHeight();
			
			g.setFill(Colors.COLOR_LIGHT_MARKINGS);
			g.fillText(text.getText(), x + (width - textW) / 2, y + height / 2 + textH / 4);
		} else {
			g.setStroke(Color.BLACK);
			g.setLineWidth(0.05);

			g.strokeRect(x, y + 0.2, width, height - 0.4);
		}
	}
}
