package de.amonbenson.breadboard.options;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Options {

	private String title;
	
	private List<Option> options;
	private List<OptionsListener> optionsListeners;

	public Options(String title) {
		this.title = title;
		
		options = new ArrayList<Option>();
		optionsListeners = new ArrayList<OptionsListener>();
	}

	public void addOption(Option option) {
		if (option == null) throw new NullPointerException("Option cannot be null");
		
		options.add(option);
		option.setContainer(this);
		
		for (OptionsListener listener : optionsListeners) listener.optionAdded(options, option, options.indexOf(option));
	}

	public void addOption(String name, String displayName, String defaultValue) {
		addOption(new Option(name, displayName, defaultValue));
	}

	public void addOption(String name, String displayName, String defaultValue, boolean editable) {
		addOption(new Option(name, displayName, defaultValue, editable));
	}

	public void addOption(String name, String displayName, String defaultValue, boolean editable, boolean onlyIntValues) {
		addOption(new Option(name, displayName, defaultValue, editable, onlyIntValues, null));
	}

	public void addOption(String name, String displayName, String defaultValue, boolean editable, boolean onlyIntValues,
			String[] availableUnits) {
		addOption(new Option(name, displayName, defaultValue, editable, onlyIntValues, availableUnits, 0));
	}

	public void addOption(String name, String displayName, String defaultValue, boolean editable, boolean onlyIntValues,
			String unit) {
		addOption(new Option(name, displayName, defaultValue, editable, onlyIntValues, new String[] {unit}, 0));
	}

	public void addOption(String name, String displayName, String defaultValue, boolean editable, boolean onlyIntValues,
			String[] availableUnits, int defaultUnitId) {
		addOption(new Option(name, displayName, defaultValue, editable, onlyIntValues, availableUnits, defaultUnitId));
	}
	
	public void removeOption(Option option) {
		if (option == null) throw new NullPointerException("Option cannot be null");
		if (!options.contains(option)) throw new IllegalArgumentException("Option is not a member of this options class");
		if (option.getContainer() != this) throw new IllegalArgumentException("Option is not a memver of this options class (But probably already in another options class)");
		
		option.setContainer(null);
		options.remove(option);
		for (OptionsListener listener : optionsListeners) listener.optionRemoved(options, option);
	}

	public int getOptionCount() {
		return options.size();
	}

	public Option getOption(int index) {
		return options.get(index);
	}
	
	public void setValue(String name, String value) {
		Option option = getOption(name);
		if (option == null) throw new NullPointerException("There is no option: " + name);
		
		option.setValue(value);
	}
	
	public String getValue(String name) {
		Option option = getOption(name);
		if (option == null) throw new NullPointerException("There is no option: " + name);
		
		return option.getValue();
	}
	
	public String getUnit(String name) {
		Option option = getOption(name);
		if (option == null) throw new NullPointerException("There is no option: " + name);
		
		return option.getUnit();
	}
	
	public Option getOption(String name) {
		for (Iterator<Option> iterator = getOptionIterator(); iterator.hasNext();) {
			Option option = iterator.next();
			if (option.getName().equals(name)) return option;
		}
		
		return null;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Iterator<Option> getOptionIterator() {
		return options.iterator();
	}
	
	public void addOptionsListener(OptionsListener listener) {
		if (listener == null) throw new NullPointerException("Options listener cannot be null");
		this.optionsListeners.add(listener);
	}
	
	public void removeOptionsListener(OptionsListener listener) {
		if (listener == null) throw new NullPointerException("Options listener cannot be null");
		this.optionsListeners.remove(listener);
	}

	protected void callOptionValueChanged(Option option) {
		int optionIndex = options.indexOf(option);
		if (optionIndex == -1) throw new IllegalStateException("The option is not part of this options class, but wants to update its value");
		
		for (OptionsListener listener : optionsListeners) {
			listener.optionValueChanged(options, option, optionIndex);
		}
	}

	protected void callOptionUnitChanged(Option option) {
		int optionIndex = options.indexOf(option);
		if (optionIndex == -1) throw new IllegalStateException("The option is not part of this options class, but wants to update its unit");
		
		for (OptionsListener listener : optionsListeners) {
			listener.optionUnitChanged(options, option, optionIndex);
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Options newObj = new Options(title);
		
		// Copy all the options
		for (int i = 0; i < getOptionCount(); i++) newObj.addOption(new Option(options.get(i)));
		
		// The listeners will have to be reassigned to the new object afterwards
		
		return newObj;
	}
}
