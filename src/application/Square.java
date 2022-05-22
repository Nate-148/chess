package application;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

// A square on the chess board GUI and the piece it contains.
// Nate Hunter - 03/12/2022
public class Square {
	// The side length of each square.
	public static final int PIXEL_WIDTH = 50;
	// The color of the light squares.
	private static final Color LIGHT_SQUARE = Color.DARKGOLDENROD;
	// The color of the dark squares.
	private static final Color DARK_SQUARE = Color.SADDLEBROWN;
	// The color of the white pieces.
	private static final Color WHITE_PIECE = Color.BISQUE;
	// The color of the black pieces.
	private static final Color BLACK_PIECE = Color.BLACK;
	// The piece font.
	private static final Font PIECE_FONT = new Font("Arial", 40);

	// The pane containing the square and the piece.
	public StackPane squareWithPiece;
	// The square displayed in the GUI.
	private Rectangle square;
	// The piece displayed in the GUI.
	private Text piece;
	// The rank of the square from 0 to 7 (chess-notation rank minus one).
	private int rank;
	// The file of the square from 0 (A) to 7 (H).
	private int file;
	// Whether the square is currently selected.
	private boolean selected = false;
	
	// Initializes a square with a piece.
	public Square(int rank, int file, char pieceLetter) {
		square = new Rectangle(PIXEL_WIDTH, PIXEL_WIDTH);
		this.rank = rank;
		this.file = file;
		boolean darkSquare = rank % 2 == file % 2;
        if (darkSquare)
        	square.setFill(DARK_SQUARE);
        else
        	square.setFill(LIGHT_SQUARE);
        
		piece = new Text();
        drawPiece(pieceLetter);
        
        squareWithPiece = new StackPane();
        squareWithPiece.getChildren().addAll(square, piece);
        setEventHandlers();
	}
	
	// Updates the square with the new piece and deselects it.
	// This should be called every time the position changes.
	public void update(char pieceLetter) {
		deselect();
		drawPiece(pieceLetter);
	}
	
	// Selects the square.
	public void select() {
		highlight();
		selected = true;
	}
	// Deselects the square.
	public void deselect() {
		unhighlight();
		selected = false;
	}
	// Highlights the square. This works because a lighter background is set behind the squares.
	private void highlight() {square.setOpacity(0.8);}
	// Unhighlights the square.
	private void unhighlight() {square.setOpacity(1);}
	
	// Updates the GUI representation of the piece.
	private void drawPiece(char pieceLetter) {
		piece.setFont(PIECE_FONT);
		String unicodePieceType = Piece.unicodeType(pieceLetter);
		piece.setText(unicodePieceType);
		if (Piece.isWhite(pieceLetter))
			piece.setFill(WHITE_PIECE);
		else if (Piece.isBlack(pieceLetter))
			piece.setFill(BLACK_PIECE);
	}
	
	// Sets the events to occur when the mouse hovers over and clicks the square or piece.
	private void setEventHandlers() {
        squareWithPiece.setOnMouseClicked(new EventHandler<MouseEvent>() {
        	@Override
            public void handle(MouseEvent t) {
        		Arbiter.processClick(rank, file);
            }
        });
        squareWithPiece.setOnMouseEntered(new EventHandler<MouseEvent>() {
        	@Override
            public void handle(MouseEvent t) {
        		if (!selected)
        			highlight();
            }
        });
        squareWithPiece.setOnMouseExited(new EventHandler<MouseEvent>() {
        	@Override
            public void handle(MouseEvent t) {
        		if (!selected)
        			unhighlight();
            }
        });
	}
}
