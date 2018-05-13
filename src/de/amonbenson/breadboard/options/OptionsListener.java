package de.amonbenson.breadboard.options;

import java.util.List;

public interface OptionsListener {
	public void optionAdded(List<Option> allOptions, Option addedOption, int addedOptionIndex);
	public void optionRemoved(List<Option> allOptions, Option removedOption);
	public void optionValueChanged(List<Option> allOptions, Option changedOption, int changedOptionIndex);
	public void optionUnitChanged(List<Option> allOptions, Option changedOption, int changedOptionIndex);
}
