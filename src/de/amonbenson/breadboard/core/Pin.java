package de.amonbenson.breadboard.core;

public class Pin implements Cloneable {
	public int x, y;
	
	public Pin(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
