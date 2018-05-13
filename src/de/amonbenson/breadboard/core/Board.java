package de.amonbenson.breadboard.core;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.amonbenson.breadboard.BoardPane;
import de.amonbenson.breadboard.Colors;
import de.amonbenson.breadboard.Main;
import de.amonbenson.breadboard.core.parts.Trace;
import de.amonbenson.breadboard.options.Option;
import de.amonbenson.breadboard.options.Options;
import de.amonbenson.breadboard.options.OptionsListener;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public class Board extends Canvas implements OptionsListener {

	public static final double SOLDER_JOINT_SIZE = 0.65;
	public static final double SOLDER_JOINT_HOLE_SIZE = 0.2;

	private Image IMG_SOLDER_JOINT;
	
	private Options options;
	private Affine boardTransform;

	private Layer displayedLayer;

	private List<Part> parts;

	// Floating
	private boolean floatingEnabled, selectionEnabled;
	private double floatingDX, floatingDY, floatingSX, floatingSY, selectionX, selectionY, selectionW, selectionH;

	// Trace
	private boolean traceEnabled;
	private int traceSX, traceSY, traceEX, traceEY;
	
	// Listeners
	private List<Part> selectedParts;

	public Board(int pinWidth, int pinHeight) {
		super(100, 100);
		
		IMG_SOLDER_JOINT = new Image("resources/image/solderJoint.png");
		
		options = new Options("Board");
		options.addOption("pinWidth", "Width", "" + pinWidth, true, true, "pins");
		options.addOption("pinHeight", "Height", "" + pinHeight, true, true, "pins");
		options.addOptionsListener(this);

		boardTransform = new Affine();

		displayedLayer = Layer.TOP;

		parts = new ArrayList<Part>();

		// Mouse and key events
		setOnMousePressed(e -> mousePressed(e));
		setOnMouseReleased(e -> mouseReleased(e));
		setOnMouseDragged(e -> mouseDragged(e));
		Platform.runLater(() -> getScene().setOnKeyPressed(e -> keyPressed(e)));

		// Set the default font
		getGraphicsContext2D().setFont(new Font("Sans Serif", 1));

		// Initial render
		Platform.runLater(() -> {
			updateOptionPane();
			render();
		});
	}

	public void render() {
		if (getPinWidth() <= 0) options.setValue("pinWidth", "1");
		if (getPinHeight() <= 0) options.setValue("pinheight", "1");
		
		GraphicsContext g = getGraphicsContext2D();
		g.setFill(Colors.COLOR_BB_YELLOW);
		g.fillRect(0, 0, getWidth(), getHeight());

		// We will now scale the canvas, so that one unit corresponds to one pin
		// size
		g.save();

		// Size of a single pin unit
		double pinSize = getWidth() / getPinWidth();
		boardTransform = new Affine();
		boardTransform.appendScale(pinSize, pinSize);
		boardTransform.appendTranslation(0.5, 0.5);
		g.setTransform(boardTransform);

		// Draw the background grid
		for (int x = 0; x < getPinWidth(); x++) {
			for (int y = 0; y < getPinHeight(); y++) {
				if (displayedLayer == Layer.BOTTOM) {
					g.setFill(Colors.COLOR_BB_GOLD);
					g.fillOval(x - SOLDER_JOINT_SIZE / 2, y - SOLDER_JOINT_SIZE / 2, SOLDER_JOINT_SIZE,
							SOLDER_JOINT_SIZE);
				}
				g.setFill(Colors.COLOR_BB_HOLES);
				g.fillOval(x - SOLDER_JOINT_HOLE_SIZE / 2, y - SOLDER_JOINT_HOLE_SIZE / 2, SOLDER_JOINT_HOLE_SIZE,
						SOLDER_JOINT_HOLE_SIZE);
			}
		}

		// Render the trace drawing indicator
		g.setLineWidth(0.1);
		g.setStroke(Color.RED);
		if (traceEnabled) {
			g.strokeLine(traceSX, traceSY, traceEX, traceEY);
		}

		// Render all parts
		g.setLineWidth(0.1);
		for (Part part : parts) {
			g.save();

			// Translate to the part's position
			g.translate(part.xPos, part.yPos);

			// Translate to the floating part position
			if (floatingEnabled && part.selected) {
				g.translate(floatingDX, floatingDY);
			}

			// Rotate our part
			g.save();
			g.translate(part.x, part.y);
			if (part.getRotation() == 1 || part.getRotation() == 2) g.translate(part.getWidth(), 0);
			if (part.getRotation() == 2 || part.getRotation() == 3) g.translate(0, part.getHeight());
			g.rotate(part.getRotation() * 90);
			g.translate(-part.x, -part.y);

			// Render our part
			part.render(g, displayedLayer);
			g.restore();

			// Render the pins of our part
			if (displayedLayer == Layer.BOTTOM) {
				g.setFill(Colors.COLOR_METAL);
				g.setLineWidth(0.4);
				for (Pin pin : part.getPins()) {
					g.setFill(new ImagePattern(IMG_SOLDER_JOINT));
					g.fillOval(pin.x - SOLDER_JOINT_SIZE / 2, pin.y - SOLDER_JOINT_SIZE / 2, SOLDER_JOINT_SIZE,
							SOLDER_JOINT_SIZE);
				}
			}
			
			// Render the board designator if no trace
			if (!(part instanceof Trace)) {
				double dx = part.getX() + 0.2;
				double dy = part.getY() - 0.2;
				
				if (part.getAbsoluteX() < 1 && part.getAbsoluteX() + part.getWidth() + 1 < getPinWidth()) {
					dx += part.getWidth();
					dy += 1;
				} else if (part.getAbsoluteY() < 0.5) {
					dy += part.getHeight() + 1;
				}
				
				g.setFill(Color.BLACK);
				g.fillText(part.getBoardDesignatorString(), dx, dy);
			}

			// Render the green selection mask arround the part
			if (part.selected) {
				g.setStroke(Colors.COLOR_BB_SELECTION);
				g.setLineWidth(0.1);

				if (part instanceof Trace) {
					Trace trace = (Trace) part;
					g.strokeLine(trace.getStartPin().x, trace.getStartPin().y, trace.getEndPin().x,
							trace.getEndPin().y);
				} else {
					g.strokeRect(part.getX(), part.getY(), part.getWidth(), part.getHeight());
				}
			}

			// Reset the transform
			g.restore();
		}

		// Render the selection rect
		if (selectionEnabled) {
			final double sx = selectionW > 0 ? selectionX : selectionX + selectionW;
			final double sy = selectionH > 0 ? selectionY : selectionY + selectionH;
			final double sw = Math.abs(selectionW);
			final double sh = Math.abs(selectionH);

			g.setStroke(Colors.COLOR_BB_SELECTION);
			g.setFill(((Color) g.getStroke()).deriveColor(0, 1.0, 1.0, 0.2));

			g.fillRect(sx, sy, sw, sh);
			g.strokeRect(sx, sy, sw, sh);
		}

		g.restore();
	}

	private void updateAllPartsState(boolean selected) {
		for (Part part : parts) {
			part.selected = selected;
		}
	}

	private void updateSelectedPartsBySelectionRect() {
		final double sx = selectionW > 0 ? selectionX : selectionX + selectionW;
		final double sy = selectionH > 0 ? selectionY : selectionY + selectionH;
		final double sw = Math.abs(selectionW);
		final double sh = Math.abs(selectionH);
		final Rectangle2D selectionRect = new Rectangle2D.Double(sx, sy, sw, sh);

		for (Part part : parts) {
			// If the part isn't a trace, check if the bounds contain that part
			part.selected = part.intersects(selectionRect);
		}
	}
	
	private void updateOptionPane() {
		Part selPart = null;
		for (Part part : parts) {
			if (part instanceof Trace) continue;
			
			if (part.selected) {
				if (selPart != null) {
					// We have more than one selected part, so we display no options
					Main.getRootPane().getOptionPane().setOptions(null);
					return;
				} else selPart = part;
			}
		}
		
		// We have no part selected at all. Display the board's options
		if (selPart == null) {
			Main.getRootPane().getOptionPane().setOptions(options);
			return;
		}
		
		// We finally have one part selected. Display the options
		Main.getRootPane().getOptionPane().setOptions(selPart.getOptions());
	}

	public Part getPartAt(double x, double y) {
		final Point2D point = new Point2D.Double(x, y);

		for (int i = parts.size() - 1; i >= 0; i--) {
			// Check if the part contains the point
			Part part = parts.get(i);
			if (part.contains(point)) return part;
		}

		return null;
	}

	private void mousePressed(MouseEvent e) {
		// Request the focus
		requestFocus();
		
		// Determine pin position
		Point2D pinPos = getPinPosition(e.getX(), e.getY());
		if (pinPos == null) return;
		final double pinX = pinPos.getX(), pinY = pinPos.getY();

		if (e.getButton() == MouseButton.PRIMARY) {

			// Reset floating values
			floatingDX = 0;
			floatingDY = 0;

			// Select the currently pressed part
			Part part = getPartAt(pinX, pinY);
			if (part != null) {
				e.consume();
				// If the part was not selected, deselect all other parts and
				// select only this part. If shift key is down, select this
				// part, but don't deselect others
				if (!part.selected) {
					if (!e.isShiftDown()) {
						updateAllPartsState(false);
					}
					part.selected = true;
				}
			} else {
				// Deselect all if clicked on no part
				updateAllPartsState(false);
			}
		}

		if (e.getButton() == MouseButton.SECONDARY) {
			traceEnabled = true;
			traceSX = (int) Math.round(pinX);
			traceSY = (int) Math.round(pinY);
			traceEX = (int) Math.round(pinX);
			traceEY = (int) Math.round(pinY);
		}

		updateOptionPane();
		render();
	}

	private void mouseReleased(MouseEvent e) {
		// Determine pin position
		Point2D pinPos = getPinPosition(e.getX(), e.getY());
		if (pinPos == null) return;
		final double pinX = pinPos.getX(), pinY = pinPos.getY();

		if (e.getButton() == MouseButton.PRIMARY) {
			// Stop selection
			selectionEnabled = false;

			// Stop moving and apply the transformation to all selected parts
			if (floatingEnabled) {
				floatingEnabled = false;
				for (Part part : parts) {
					if (part.selected) {
						snapTo(part, floatingDX, floatingDY);
					}
				}
			}
		}

		if (e.getButton() == MouseButton.SECONDARY) {
			traceEnabled = false;
			// Make a trace (use the currently displayed layer for the trace
			// layer)
			addPart(new Trace(traceSX, traceSY, traceEX, traceEY, displayedLayer));
		}

		updateOptionPane();
		render();
	}

	private void mouseDragged(MouseEvent e) {
		// Determine pin position
		Point2D pinPos = getPinPosition(e.getX(), e.getY());
		if (pinPos == null) return;
		final double pinX = pinPos.getX(), pinY = pinPos.getY();

		if (e.getButton() == MouseButton.PRIMARY) {
			Part part = getPartAt(pinX, pinY);

			// Start the selection rect
			if (part == null && !selectionEnabled && !floatingEnabled) {
				selectionEnabled = true;
				selectionX = pinX;
				selectionY = pinY;
			}
			if (selectionEnabled) {
				selectionW = pinX - selectionX;
				selectionH = pinY - selectionY;
				updateSelectedPartsBySelectionRect();
			}

			// Start moving the parts (floating)
			if (!floatingEnabled && !selectionEnabled) {
				floatingSX = pinX;
				floatingSY = pinY;
				floatingDX = 0;
				floatingDY = 0;
				floatingEnabled = true;
			} else {
				floatingDX = pinX - floatingSX;
				floatingDY = pinY - floatingSY;
			}
		}

		if (e.getButton() == MouseButton.SECONDARY) {
			traceEX = (int) Math.round(pinX);
			traceEY = (int) Math.round(pinY);
		}

		// Don't update the selected part here, this would cause unnecessary lag
		render();
	}

	private void keyPressed(KeyEvent e) {
		if (e.getCode() == KeyCode.DELETE) {
			// Delete currently selected parts
			Iterator<Part> it = parts.iterator();
			while (it.hasNext()) {
				Part part = it.next();
				if (part.selected) Platform.runLater(() -> {
					removePart(part);
					updateOptionPane();
				});
			}
		}
		
		if (e.getCode() == KeyCode.R) {
			// Rotate the part by 90 degrees
			for (Part part : parts) {
				if (part.selected) {
					part.rotate90();
				}
			}
		}

		if (e.getCode() == KeyCode.F) {
			// Flip the board
			if (displayedLayer == Layer.TOP) displayedLayer = Layer.BOTTOM;
			else if (displayedLayer == Layer.BOTTOM) displayedLayer = Layer.TOP;

			render();
		}

		// Re-Render
		Platform.runLater(() -> render());
	}

	public void snapTo(Part part, double xp, double yp) {
		// Set the new position
		part.xPos += (int) Math.round(xp);
		part.yPos += (int) Math.round(yp);

		// Keep in bounds
		if (part.xPos < -part.getY()) part.xPos = (int) -part.getX();
		if (part.yPos < -part.getY()) part.yPos = (int) -part.getY();
		if (part.xPos > getPinWidth() - part.getX() - part.getWidth() - 0.5) part.xPos = (int) (getPinWidth() - part.getX() - part.getWidth() - 0.5);
		if (part.yPos > getPinHeight() - part.getY() - part.getHeight() - 0.5) part.yPos = (int) (getPinHeight() - part.getY() - part.getHeight() - 0.5);
	}

	public void updateBoardDesignators() {
		List<String> prefixes = new ArrayList<String>();
		List<Integer> currentDesignators = new ArrayList<Integer>();
		
		for (Part part : parts) {
			if (prefixes.contains(part.getGeneralBoardDesignatorPrefix())) {
				int index = prefixes.indexOf(part.getGeneralBoardDesignatorPrefix());
				int newDesignator = currentDesignators.get(index) + 1;
				
				currentDesignators.set(index, newDesignator);
				part.setBoardDesignator(newDesignator);
				
			} else {
				// Start a new designator
				prefixes.add(part.getGeneralBoardDesignatorPrefix());
				
				currentDesignators.add(1);
				part.setBoardDesignator(1);
			}
		}
	}

	private Point2D getPinPosition(double x, double y) {
		try {
			javafx.geometry.Point2D p2d = boardTransform.inverseTransform(x, y);
			return new Point2D.Double(p2d.getX(), p2d.getY());
		} catch (NonInvertibleTransformException ex) {
			System.err.println("Pin position could not be determined!");
		}

		return null;
	}

	public void addParts(List<Part> parts) {
		this.parts.addAll(parts);
		updateBoardDesignators();
	}

	public void addParts(Part... parts) {
		for (Part part : parts) {
			this.parts.add(part);
		}
		updateBoardDesignators();
	}

	public void addPart(Part part) {
		if (part instanceof Trace) parts.add(0, part);
		else parts.add(parts.size(), part);
		updateBoardDesignators();
	}

	public void removePart(Part part) {
		parts.remove(part);
		updateBoardDesignators();
	}

	public int getPinWidth() {
		return (int) Double.parseDouble(options.getValue("pinWidth"));
	}

	public int getPinHeight() {
		return (int) Double.parseDouble(options.getValue("pinHeight"));
	}

	@Override
	public void optionAdded(List<Option> allOptions, Option addedOption, int addedOptionIndex) {
	}

	@Override
	public void optionRemoved(List<Option> allOptions, Option removedOption) {
	}

	@Override
	public void optionValueChanged(List<Option> allOptions, Option changedOption, int changedOptionIndex) {
		// Resizing stuff
		((BoardPane) getParent()).setPrefSize(getPinWidth() * BoardPane.BOARD_RESOLUTION, getPinHeight() * BoardPane.BOARD_RESOLUTION);
		
		// Snap all parts to their new positions
		for (Part part : parts) {
			if (part.xPos > getPinWidth() - part.getX() - part.getWidth() - 0.5) part.xPos = (int) (getPinWidth() - part.getX() - part.getWidth() - 0.5);
			if (part.yPos > getPinHeight() - part.getY() - part.getHeight() - 0.5) part.yPos = (int) (getPinHeight() - part.getY() - part.getHeight() - 0.5);
		}
		
		// Re-Render
		render();
	}

	@Override
	public void optionUnitChanged(List<Option> allOptions, Option changedOption, int changedOptionIndex) {
	}
}
