package de.amonbenson.breadboard;

import java.util.ArrayList;
import java.util.List;

import de.amonbenson.breadboard.core.Part;
import de.amonbenson.breadboard.core.parts.DipIC;
import de.amonbenson.breadboard.core.parts.ElectrolyticCapacitor;
import de.amonbenson.breadboard.core.parts.LED;
import de.amonbenson.breadboard.core.parts.Resistor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class PartChooser extends VBox {

	public static final int GROUP_UNKNOWN = -1;
	public static final int GROUP_ACTIVE_COMPONENTS = 0;
	public static final int GROUP_PASSIVE_COMPONENTS = 1;
	public static final int GROUP_ICS = 2;
	
	public static final int DEFAULT_GROUP = GROUP_PASSIVE_COMPONENTS;

	private static final String[] groupNames = new String[] { "Active Components", "Passive Components", "ICs" };

	private Label titleLabel;
	private Accordion accordion;
	
	private ListView<Part> listViews[];
	private ObservableList<Part> parts;

	private List<PartChooserListener> listeners;

	public PartChooser() {
		// Create the listeners
		listeners = new ArrayList<PartChooserListener>();
		
		// Create title and accordion
		titleLabel = new Label("Components");
		titleLabel.setId("title-text");
		
		accordion = new Accordion();
		VBox.setVgrow(accordion, Priority.ALWAYS);

		// Create all the groups
		listViews = (ListView<Part>[]) new ListView[groupNames.length];

		for (int i = 0; i < groupNames.length; i++) {
			// Create a list view
			ListView<Part> listView = new ListView<Part>();
			listViews[i] = listView;

			// Set the cell factory and selection listener
			listView.setCellFactory((list) -> {
				return new PartCell();
			});

			// Clear the selection automatically
			listView.getSelectionModel().selectedItemProperty().addListener((observable, oldPart, newPart) -> {
				if (newPart == null) return; // Ignore calls when no selection
												// is made

				for (PartChooserListener listener : listeners)
					listener.partSelected(newPart);
				Platform.runLater(() -> listView.getSelectionModel().clearSelection());
			});

			// Add the list to a new group pane
			TitledPane groupPane = new TitledPane(groupNames[i], listView);
			accordion.getPanes().add(groupPane);
			if (i == DEFAULT_GROUP) accordion.setExpandedPane(groupPane);
		}

		// Extend the passive compnents and make sure to never completely
		// collapse the accordion
		/*accordion.expandedPaneProperty().addListener(
				(ObservableValue<? extends TitledPane> observable, TitledPane oldPane, TitledPane newPane) -> {
					Boolean expand = true;
					for (TitledPane pane : accordion.getPanes()) {
						if (pane.isExpanded()) {
							expand = false;
						}
					}
					if ((expand == true) && (oldPane != null)) {
						Platform.runLater(() -> {
							accordion.setExpandedPane(oldPane);
						});
					}
				});*/
		
		// Add everything
		getChildren().addAll(titleLabel, accordion);

		// Create and view the parts lists
		initPartsList();
		updatePartsList();
	}

	public void initPartsList() {
		parts = FXCollections.observableArrayList();

		// Add all the components
		parts.add(new LED("Red"));
		parts.add(new LED("Green"));
		
		parts.add(new Resistor());
		parts.add(new ElectrolyticCapacitor());

		parts.add(new DipIC(3, "Opto"));
		parts.add(new DipIC(4, "Op-Amp"));
		parts.add(new DipIC(5, "Stuff"));
		parts.add(new DipIC(6, "Stuff"));
		parts.add(new DipIC(7, "Quad Op-Amp"));
		parts.add(new DipIC(8, "Counter"));
		parts.add(new DipIC(9, "Binary"));
		parts.add(new DipIC(10, "NE555 Pro"));
		parts.add(new DipIC(11, "3N328Z"));
		parts.add(new DipIC(12, "7T344B"));
		parts.add(new DipIC(13, "Thirteen"));
		parts.add(new DipIC(14, "ATMega328P"));
	}

	public void updatePartsList() {
		ObservableList<Part>[] groupedParts = (ObservableList<Part>[]) new ObservableList[groupNames.length];
		for (int i = 0; i < groupedParts.length; i++)
			groupedParts[i] = FXCollections.observableArrayList();

		for (Part part : parts) {
			int id = part.getGeneralGroupId();
			if (id == -1) {
				System.out.println("Part " + part.getGeneralName() + " won't be added because group is unknown.");
				continue;
			}
			groupedParts[id].add(part);
		}

		for (int i = 0; i < groupedParts.length; i++) {
			listViews[i].setPlaceholder(new Label("this bitch's empty yikes"));
			listViews[i].setItems(groupedParts[i]);
		}
	}

	public Part newInstance(Part part) {
		try {
			return (Part) part.clone();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static String getGroupName(int index) {
		if (index == -1) return ""; // Unknown group
		return groupNames[index];
	}

	public void addPartChooserListener(PartChooserListener listener) {
		if (listener == null) throw new NullPointerException("Listener cannot be null");
		listeners.add(listener);
	}

	public void removePartChooserListener(PartChooserListener listener) {
		if (listener == null) throw new NullPointerException("Listener cannot be null");
		listeners.remove(listener);
	}

	static class PartCell extends ListCell<Part> {
		@Override
		public void updateItem(Part item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) return;

			// Set the text
			setText(item.getGeneralName());

			// Show a preview
			setGraphic(item.createPreview(84, 64));
		}
	}
}
