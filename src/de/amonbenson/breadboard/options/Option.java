package de.amonbenson.breadboard.options;

import java.util.Arrays;

public class Option {
	
	private String name, displayName, value;
	private int unitId;
	private String[] units;
	
	private boolean editable, onlyNumricValues;
	
	private Options container;
	
	public Option(Option other) {
		this.name = other.name;
		this.displayName = other.displayName;
		this.value = other.value;
		this.unitId = other.unitId;
		
		// Clone all units
		if (other.units !=null) {
			this.units = new String[other.units.length];
			for (int i = 0; i < this.units.length; i++) this.units[i] = other.units[i];
		} else {
			this.units = null;
		}
		
		this.editable = other.editable;
		this.onlyNumricValues = other.onlyNumricValues;
	}
	
	public Option(String name, String displayName, String defaultValue) {
		this(name, displayName, defaultValue, true);
	}
	
	public Option(String name, String displayName, String defaultValue, boolean editable) {
		this(name, displayName, defaultValue, editable, false);
	}
	
	public Option(String name, String displayName, String defaultValue, boolean editable, boolean onlyNumricValues) {
		this(name, displayName, defaultValue, editable, onlyNumricValues, null);
	}
	
	public Option(String name, String displayName, String defaultValue, boolean editable, boolean onlyNumricValues, String[] availableUnits) {
		this(name, displayName, defaultValue, editable, onlyNumricValues, availableUnits, 0);
	}
	
	public Option(String name, String displayName, String defaultValue, boolean editable, boolean onlyNumricValues, String[] availableUnits, int defaultUnitId) {
		// Set the units to null if the length is null to avoid empty arrays.
		if (availableUnits != null) if (availableUnits.length == 0) availableUnits = null;
		
		this.name = name;
		this.displayName = displayName;
		this.units = availableUnits;
		
		this.editable = editable;
		this.onlyNumricValues = onlyNumricValues;
		
		this.container = null;
		
		setValue(defaultValue);								// This will also check if the given value is an integer (if onlyIntValues is enabled)
		if (availableUnits != null) setUnit(defaultUnitId);	// This will also check if the given unit id is in the correct range
	}
	
	public void setValue(String value) {
		if (isOnlyNumricValues()) {
			// Int value
			try {
				double doubleValue = Double.parseDouble(value);
				this.value = String.valueOf(doubleValue);
			} catch (Exception ex) {
				throw new NumberFormatException("Value (" + value + ") cannot be read as integer");
			}
		} else {
			// Non-Int value
			this.value = value;
		}
		
		// Notify the container that the value has changed
		if (container != null) container.callOptionValueChanged(this);
	}
	
	public String[] getAvailableUnits() {
		return Arrays.copyOf(units, units.length);
	}
	
	public void setUnit(int id) {
		if (units == null) throw new IllegalStateException("This option has no units");
		if (id < 0 || id >= units.length) throw new ArrayIndexOutOfBoundsException("Unit id is out of bounds (length: " + units.length + ", id: " + id + ")");
		
		unitId = id;
		
		// Notify the container that the unit has changed
		if (container != null) container.callOptionUnitChanged(this);
	}
	
	public void setUnit(String unit) {
		if (units == null) throw new IllegalStateException("This option has no units");
		
		for (int id = 0; id < units.length; id++) {
			if (units[id].equals(unit)) setUnit(id);
		}
		
		throw new IllegalArgumentException("Unit (" + unit + ") is not available for this option");
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getUnit() {
		if (units == null) throw new IllegalStateException("This option has no units");
		return units[unitId];
	}
	
	public int getUnitId() {
		if (units == null) throw new IllegalStateException("This option has no units");
		return unitId;
	}
	
	public boolean hasUnit() {
		return units != null;
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public boolean isOnlyNumricValues() {
		return onlyNumricValues;
	}
	
	protected void setContainer(Options container) {
		this.container = container;
	}
	
	protected Options getContainer() {
		return container;
	}
}
