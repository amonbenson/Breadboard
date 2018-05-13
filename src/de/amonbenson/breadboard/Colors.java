package de.amonbenson.breadboard;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class Colors {
	public static final Color COLOR_BB_SELECTION = Color.web("#f95e20");
	
	public static final Color COLOR_BB_YELLOW = Color.web("#edc17d");
	public static final Color COLOR_BB_GOLD = Color.web("#BD9150");
	public static final Color COLOR_BB_HOLES = Color.web("#f7f1e1");
	
	public static final Color COLOR_LIGHT_MARKINGS = Color.web("#FFFFFF20");
	
	public static final Color COLOR_METAL = Color.web("#a0a0a0");
	public static final Color COLOR_DARK_GRAY = Color.web("#303030");
	
	public static final Color COLOR_RESISTOR = Color.web("#e0dbac");
	public static final Color COLOR_SKY_BLUE = Color.web("#327ae5");
	
	public static Color getResistorRingColor(int id) {
		if (id == 0) return Color.BLACK;
		if (id == 1) return Color.BROWN;
		if (id == 2) return Color.RED;
		if (id == 3) return Color.ORANGE;
		if (id == 4) return Color.YELLOW;
		if (id == 5) return Color.GREEN;
		if (id == 6) return Color.BLUE;
		if (id == 7) return Color.PURPLE;
		if (id == 8) return Color.GRAY;
		if (id == 9) return Color.WHITE;
		if (id == 10) return Color.GOLD;
		if (id == 11) return Color.SILVER;
		
		return Color.TRANSPARENT;
	}
}
