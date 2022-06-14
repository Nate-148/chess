package application;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// The chess board displayed in the GUI.
// Nate Hunter - 03/12/2022
public class Board {
	// The 64 squares on the chess board.
	private static Square[][] squares = new Square[8][8];
	
	// Sets up the chess board in the starting position.
	public static StackPane initialize() {
		// Add squares and pieces to the grid.
		GridPane grid = new GridPane();
	    for (int rank = 0; rank < 8; rank++)
	    	for (int file = 0; file < 8; file++) {
	    		Coordinate location = new Coordinate(rank, file);
	    		char piece = Position.current.getPiece(location);
	    		Square square = new Square(location, piece);
	    		squares[rank][file] = square;
	    		// Ranks are displayed from bottom to top when playing as white.
	    		int row = 7 - rank;
	    		int column = file;
	            grid.add(square.squareWithPiece, column, row);
	    	}

		// A light backdrop facilitates highlighting squares by adjusting their transparency.
		final int BOARD_PIXEL_WIDTH = Square.PIXEL_WIDTH * 8;
		Rectangle lightBackdrop = new Rectangle(BOARD_PIXEL_WIDTH, BOARD_PIXEL_WIDTH);
		lightBackdrop.setFill(Color.WHITE);
		StackPane displayBoard = new StackPane();
		displayBoard.getChildren().addAll(lightBackdrop, grid);
		return displayBoard;
	}
	
	// Updates the chess board with the current position.
	// This should be called whenever the position changes.
	public static void update() {
	    for (int rank = 0; rank < 8; rank++)
	    	for (int file = 0; file < 8; file++) {
	    		Coordinate location = new Coordinate(rank, file);
	    		char piece = Position.current.getPiece(location);
	    		squares[rank][file].drawPiece(piece);
	    	}
	}
	
	// Gets the square at the given coordinates.
	public static Square getSquare(Coordinate location) {
		return squares[location.rank][location.file];
	}
}
