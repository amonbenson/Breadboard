package de.amonbenson.breadboard;

import de.amonbenson.breadboard.core.Board;
import de.amonbenson.breadboard.core.parts.ElectrolyticCapacitor;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class BoardPane extends Pane {

	public static double BOARD_RESOLUTION = 32;
	
	private Point2D dragStart;
	
	private Board board;
	
	public BoardPane() {
		// Graphic settings
		setPadding(new Insets(10, 10, 10, 10));
		
		// Create a board and add it
		board = new Board(32, 24);
		getChildren().add(board);
		
		ElectrolyticCapacitor c = new ElectrolyticCapacitor();
		c.xPos = 10;
		c.yPos = 10;
		board.addPart(c);
		
		// Set the preferred size
		setPrefSize(board.getPinWidth() * BOARD_RESOLUTION, board.getPinHeight() * BOARD_RESOLUTION);
	}

	@Override
    protected void layoutChildren() {
        super.layoutChildren();
        
        snapBoard();
    }
    
    public void snapBoard() {
    	// Calculate the available space on this pane
        final double x = snappedLeftInset();
        final double y = snappedTopInset();
        final double w = snapSize(getWidth()) - x - snappedRightInset();
        final double h = snapSize(getHeight()) - y - snappedBottomInset();
        
    	// Resize the board, so that it fits nicely into this pane.
        if (w / board.getWidth() > h / board.getHeight()) {
        	board.setWidth(h / board.getPinHeight() * board.getPinWidth());
        	board.setHeight(h);
        	board.setLayoutX(x + (w - board.getWidth()) / 2);
        	board.setLayoutY(y);
        } else {
        	board.setWidth(w);
        	board.setHeight(w / board.getPinWidth() * board.getPinHeight());
        	board.setLayoutX(x);
        	board.setLayoutY(y + (h - board.getHeight()) / 2);
        }
        
        // Repaint our board
        board.render();
    }

	public Board getBoard() {
		return board;
	}
}
