package de.amonbenson.breadboard;

import de.amonbenson.breadboard.core.Part;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class RootPane extends BorderPane {

	public static final int SIDE_PANEL_PADDING = 4;
	
	public static final int PART_CHOOSER_PREF_SIZE = 320;
	public static final int OPTION_PANE_PREF_SIZE = 320;
	
	private PartChooser partChooser;
	private BoardPane boardPane;
	private OptionPane optionPane;
	
	public RootPane() {
		// Create the part chooser
		partChooser = new PartChooser();
		partChooser.addPartChooserListener(part -> partChooserSelected(part));
		partChooser.setPrefWidth(PART_CHOOSER_PREF_SIZE);
		partChooser.setPadding(new Insets(SIDE_PANEL_PADDING));
		setLeft(partChooser);
		
		// Create the breadboard pane
		boardPane = new BoardPane();
		ZoomableScrollPane boardPaneScroller = new ZoomableScrollPane(boardPane);
		boardPaneScroller.setZoomIntensity(0.05);
		boardPaneScroller.setStyle("-fx-background-color: #00000020;");
		setCenter(boardPaneScroller);
		
		// Create the option pane
		optionPane = new OptionPane();
		optionPane.setPrefWidth(OPTION_PANE_PREF_SIZE);
		optionPane.setPadding(new Insets(SIDE_PANEL_PADDING));
		setRight(optionPane);
		
		// Render for the first time!
		boardPane.getBoard().render();
	}

	private void partChooserSelected(Part selPart) {
		// Create a new instance of that part (to manipulate it individually)
		Part newPart = partChooser.newInstance(selPart);
		if (newPart == null) return;
		
		// Add our new part
		boardPane.getBoard().addPart(newPart);
		boardPane.getBoard().snapTo(newPart, 0, 0);
		boardPane.getBoard().render();
	}

	public BoardPane getBoardPane() {
		return boardPane;
	}

	public OptionPane getOptionPane() {
		return optionPane;
	}
}
