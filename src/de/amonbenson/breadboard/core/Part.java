package de.amonbenson.breadboard.core;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.amonbenson.breadboard.PartChooser;
import de.amonbenson.breadboard.options.Option;
import de.amonbenson.breadboard.options.Options;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public abstract class Part implements Cloneable {

	public boolean selected;
	
	public int xPos, yPos, rotation; // Drawed x / y position (must be a grid value in the pin unit, therefore an integer), rotation (0 to 3, corresponds to 90 deg. steps)
	protected double x, y, width, height; // Bounds of the part. Usually, all the render contents lay inside this borders.
	private Pin[] pins;
	
	private String generalName;
	private Options options;
	private int generalGroupId, boardDesignator;	// Unique for every part
	
	public Part(double x, double y, double w, double h, String generalName, int generalGroupId, String boardDesignatorPrefix, boolean onlyNumricValues, String[] units, int defaultUnitId, Pin... pins) {
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		this.pins = pins;
		
		this.generalName = generalName;
		this.generalGroupId = generalGroupId;
		
		options = new Options(generalName);
		options.addOption("generalGroup", "Group", PartChooser.getGroupName(generalGroupId), false);
		options.addOption("boardDesignatorPrefix", "Board Desg.", boardDesignatorPrefix);
		options.addOption("value", "Value", "0", true, onlyNumricValues, units, defaultUnitId);
		
		setBoardDesignator(1);
		if (onlyNumricValues) setValue("0");
		else setValue("");
		
		selected = false;
	}
	
	public Pin[] getPins() {
		return pins;
	}
	
	public Rectangle2D getBoundingBox() {
		return new Rectangle2D.Double(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());
	}
	
	public boolean contains(Point2D point) {
		return getBoundingBox().contains(point);
	}
	
	public boolean intersects(Rectangle2D rect) {
		return getBoundingBox().intersects(rect);
	}
	
	public void rotate90() {
		rotation++;
		if (rotation >= 4) rotation = 0;
		
		// Flip the x and y package positions
		double a = x;
		x = y;
		y = a;
		
		// Update the pin rotation
		int pinminx = Integer.MAX_VALUE, pinmaxx = Integer.MIN_VALUE;
		for (Pin pin : pins) {
			double r = Math.hypot(pin.x, pin.y);
			double t = Math.atan2(pin.y, pin.x) + Math.PI / 2;
			
			pin.x = (int) Math.round(r * Math.cos(t));
			pin.y = (int) Math.round(r * Math.sin(t));

			if (pin.x < pinminx) pinminx = pin.x;
			if (pin.x > pinmaxx) pinmaxx = pin.x;
		}
		
		// Update the pin translation
		int pinw = pinmaxx - pinminx;
		for (Pin pin : pins) {
			pin.x += pinw;
		}
	}

	public int getRotation() {
		return rotation;
	}

	public Pin getPin(int index) {
		if (pins == null)
			return null;
		if (index < 0 || index >= pins.length)
			throw new IndexOutOfBoundsException("Pin index out of bounds.");
		return pins[index];
	}

	public int getPinCount() {
		if (pins == null)
			return 0;
		return pins.length;
	}
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getWidth() {
		if (rotation % 2 == 0)
			return width;
		else
			return height;
	}

	public double getHeight() {
		if (rotation % 2 == 0)
			return height;
		else
			return width;
	}

	public double getAbsoluteX() {
		return xPos + x;
	}
	
	public double getAbsoluteY() {
		return yPos + y;
	}

	public String getBoardDesignatorString() {
		return options.getValue("boardDesignatorPrefix") + boardDesignator;
	}

	public void setBoardDesignator(int boardDesignator) {
		this.boardDesignator = boardDesignator;
	}

	public String getValueString() {
		Option valueOption = options.getOption("value");
		String valString = valueOption.getValue();
		if (valString.endsWith(".0")) valString = valString.substring(0, valString.length() - 2);
		if (valueOption.hasUnit()) valString += " " + options.getUnit("value");
		
		return valString;
	}

	public void setValue(String value) {
		options.setValue("value", value);
	}
	
	public double getDoubleValue() {
		if (!options.getOption("value").isOnlyNumricValues()) throw new IllegalArgumentException("Cannot get double value from non numric part");
		
		return Double.parseDouble(options.getValue("value"));
	}
	
	public String getValue() {
		return options.getValue("value");
	}

	public String getGeneralName() {
		return generalName;
	}

	public int getGeneralGroupId() {
		return generalGroupId;
	}

	public String getGeneralGroup() {
		return PartChooser.getGroupName(generalGroupId);
	}

	public String getGeneralBoardDesignatorPrefix() {
		return options.getValue("boardDesignatorPrefix");
	}

	public Options getOptions() {
		return options;
	}

	public abstract void render(GraphicsContext g, Layer layer);
	
	public Canvas createPreview(double cw, double ch) {
		// Make a preview canvas
		Canvas c = new Canvas(cw, ch);
		
		// Calc the size and bounds
		final double border = 10;
		cw -= border * 2;
		ch -= border * 2;
		
		double dx, dy, dw, dh;
        if (cw / width > ch / height) {
        	dw = ch / height * width;
        	dh = ch;
        } else {
        	dw = cw;
        	dh = cw / width * height;
        }

    	dx = (cw - dw) / 2 + border;
    	dy = (ch - dh) / 2 + border;
		
    	// Draw our part
		GraphicsContext g = c.getGraphicsContext2D();
		g.setFont(new Font("Sans Serif", 1));
		
		g.translate(dx, dy);
		g.scale(dw / (width), dh / (height));
		g.translate(-x, -y);
		g.setStroke(Color.BLACK);
		g.setLineWidth(0.1);
		render(g, Layer.TOP);
		
		return c;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
	    Part newObj = (Part) super.clone();
	    
	    // Clone the pins array manually
	    Pin[] newPins = new Pin[this.pins.length];
	    for (int i = 0; i < newPins.length; i++) {
	    	newPins[i] = (Pin) this.pins[i].clone();
	    }
	    newObj.pins = newPins;
	    
	    // Clone the options manually
	    newObj.options = (Options) getOptions().clone();
	    
	    return newObj;
	}
}
