package de.amonbenson.breadboard;

import java.util.List;

import de.amonbenson.breadboard.options.Option;
import de.amonbenson.breadboard.options.Options;
import de.amonbenson.breadboard.options.OptionsListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class OptionPane extends VBox implements OptionsListener {
	
	public static final int UNIT_WIDTH = 60;
	public static final int GAP = 3;
	
	private Label titleLabel;
	private GridPane optionGrid;
	
	private Options options;
	
	public OptionPane() {
		// TItle
		titleLabel = new Label();
		titleLabel.setId("title-text");
		
		// Init the grid pane
		optionGrid = new GridPane();
		
		ColumnConstraints colName = new ColumnConstraints();
		ColumnConstraints colValue = new ColumnConstraints();
		ColumnConstraints colUnit = new ColumnConstraints();
		
		colValue.setHgrow(Priority.ALWAYS);
		colUnit.setPrefWidth(UNIT_WIDTH);
		
		optionGrid.getColumnConstraints().addAll(colName, colValue, colUnit);
		optionGrid.setVgap(GAP);
		optionGrid.setHgap(GAP);
		
		// Add the children
		getChildren().addAll(titleLabel, optionGrid);
		
		// Init with no options
		setOptions(null);
	}

	public void setOptions(Options newOptions) {
		if (options != null) options.removeOptionsListener(this);
		options = newOptions;
		if (newOptions != null) newOptions.addOptionsListener(this);
		
		updateOptionsDisplay();
	}
	
	public void updateOptionsDisplay() {
		optionGrid.getChildren().clear();
		
		if (options == null) {
			titleLabel.setText("No options available.");
			return;
		}

		titleLabel.setText(options.getTitle());
		
		// Make a fancy grid layout
		for (int i = 0; i < options.getOptionCount(); i++) {
			Option option = options.getOption(i);
			
			// Option name label
			Label nameLabel = new Label(option.getDisplayName() + ":");
			GridPane.setHalignment(nameLabel, HPos.LEFT);
			optionGrid.add(nameLabel, 0, i);
			
			// Option value field
			if (option.isEditable()) {
				TextField valueField = new TextField(option.getValue());
				valueField.setStyle("-fx-background-color: #ffffff15; -fx-text-fill: white;");
				valueField.setPrefWidth(1);
				
				valueField.setOnAction(e -> valueFieldAction(valueField)); // Update value on enter pressed
				valueField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
					if (newValue == false) valueFieldAction(valueField); // Update value on focus lost
				});
				optionGrid.add(valueField, 1, i);
			} else {
				Label valueLabel = new Label(option.getValue());
				optionGrid.add(valueLabel, 1, i);
			}
			
			// Option unit choice box
			if (option.hasUnit()) {
				if (option.getAvailableUnits().length == 1) {
					Label unitLabel = new Label(option.getAvailableUnits()[0]);
					optionGrid.add(unitLabel, 2, i);
				} else {
					ChoiceBox unitBox = new ChoiceBox(FXCollections.observableArrayList(option.getAvailableUnits()));
					unitBox.setStyle("-fx-background-color: #ffffff15; -fx-text-fill: white;");
					unitBox.getSelectionModel().select(option.getUnitId());
					unitBox.setPrefWidth(UNIT_WIDTH);
					
					unitBox.getSelectionModel().selectedItemProperty().addListener(e -> unitBoxAction(unitBox));
					optionGrid.add(unitBox, 2, i);
				}
			}
		}
	}
	
	public Node getNodeAt(int column, int row) {
		for (Node node : optionGrid.getChildren()) {
			if (GridPane.getColumnIndex(node) == column && GridPane.getRowIndex(node) == row) return node;
		}
		
		return null;
	}
	
	private void valueFieldAction(TextField source) {
		if (source == null) return;
		
		int index = GridPane.getRowIndex(source);
		if (index == -1) return;
		
		// The user entered a new value into the text field
		try {
			options.getOption(index).setValue(source.getText());
		} catch (NumberFormatException ex) {
			System.err.println("Wrong number format!");
			
			// Setting the text didn't work, let's set the node to its self text, this will invoke the value changed listener
			// and therefore set the text field to its old value.
			Option option = options.getOption(index);
			option.setValue(option.getValue());
		}
		Main.getRootPane().getBoardPane().getBoard().updateBoardDesignators();
		Platform.runLater(() -> Main.getRootPane().getBoardPane().getBoard().render());
		
		// Remove the focus from the option grid
		requestFocus();
	}
	
	private void unitBoxAction(ChoiceBox source) {
		if (source == null) return;
		
		int index = GridPane.getRowIndex(source);
		if (index == -1) return;
		
		int unitId = source.getSelectionModel().getSelectedIndex();
		if (unitId == -1) return;
		
		options.getOption(index).setUnit(unitId);
		Platform.runLater(() -> Main.getRootPane().getBoardPane().getBoard().render());
	}

	@Override
	public void optionAdded(List<Option> allOptions, Option addedOption, int addedOptionIndex) {
		// Update all the options
		updateOptionsDisplay();
	}

	@Override
	public void optionRemoved(List<Option> allOptions, Option removedOption) {
		// Update all the options
		updateOptionsDisplay();
	}

	@Override
	public void optionValueChanged(List<Option> allOptions, Option changedOption, int changedOptionIndex) {
		// Update the value
		Node node = getNodeAt(1, changedOptionIndex);
		if (node == null) return;
		
		if (node instanceof TextField) {
			Platform.runLater(() -> ((TextField) node).setText(changedOption.getValue()));
		} else if (node instanceof Label) {
			Platform.runLater(() -> ((Label) node).setText(changedOption.getValue()));
		}
	}

	@Override
	public void optionUnitChanged(List<Option> allOptions, Option changedOption, int changedOptionIndex) {
		// Update the unit
		Node node = getNodeAt(2, changedOptionIndex);
		if (node == null) return;
		
		if (node instanceof ChoiceBox) {
			Platform.runLater(() -> ((ChoiceBox) node).getSelectionModel().select(changedOption.getUnitId()));
		}
	}
}
