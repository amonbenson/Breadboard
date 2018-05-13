package de.amonbenson.breadboard;

import java.net.URL;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

	public static final String SOFTWARE_TITLE = "Breadboard";
	public static final String VERSION = "Alpha 1.0";
	
	public static final String RESOURCE_FOLDER = "resources";
	
	private static final Logger log = Logger.getLogger(Main.class.getName());
	
	private Stage primaryStage;
	private Scene scene;
	private static RootPane root;
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		primaryStage.setTitle(SOFTWARE_TITLE);
		
		// Water mark
		Label watermark = new Label("Breadboard - by Amon Benson - " + VERSION);
		watermark.setStyle("-fx-text-color: #ffffff80; -fx-padding: 0.3em 0.6em;");
		watermark.setPickOnBounds(false);
		StackPane.setAlignment(watermark, Pos.BOTTOM_RIGHT);
		
		// Link everything together
		root = new RootPane();
		scene = new Scene(new StackPane(root, watermark));
		primaryStage.setScene(scene);
		
		// Set the style
		URL stylesheetPath = getClass().getClassLoader().getResource(RESOURCE_FOLDER + "/style/tanger.css");
		if (stylesheetPath == null) log.warning("Could not load tanger stylesheet. Using the default style instead.");
		else scene.getStylesheets().add(stylesheetPath.toExternalForm());
		
		// Show the stage
		primaryStage.setWidth(1200);
		primaryStage.setHeight(800);
		//primaryStage.setMaximized(true);
		primaryStage.show();
	}
	
	public static RootPane getRootPane() {
		return root;
	}

	public static void main(String[] args) {
		launch(args);
	}

}
