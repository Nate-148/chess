package application;

import java.util.Collection;

import javafx.application.Platform;

// Manages the flow of the chess game.
// Nate Hunter - 03/13/2022
public class Arbiter {
	// Whether white is a human player or a bot.
	private static final boolean WHITE_HUMAN = true;
	// Whether black is a human player or a bot.
	private static final boolean BLACK_HUMAN = false;
	
	// The source square for a pending move.
	private static Coordinate source = Coordinate.getInvalid();
	
	// Responds to the click of a square by selecting the square, moving a piece,
	// or doing nothing (depending on the square and game state).
	public static void processClick(Coordinate location) {
		// Ensure it is a human's turn.
		if (!humanToMove())
			return;
		
		boolean pieceAlreadySelected = source.inbounds();
		if (pieceAlreadySelected) {
			// The click corresponds to the intended target of a move.
			playMoveIfLegal(source, location);
			
			// Deselect the selected square.
			Board.getSquare(source).deselect();
			source = Coordinate.getInvalid();
		}
		else {
			// Select the square clicked if it holds a piece of the active color.
			char pieceClicked = Position.current.getPiece(location);
			if (Piece.isActive(pieceClicked, Position.current.whiteToMove)) {
				Board.getSquare(location).select();
				source = location;
			}
		}
	}
	
	// Plays a move if it is legal.
	private static void playMoveIfLegal(Move move) {
		playMoveIfLegal(move.source, move.target);
	}
	private static void playMoveIfLegal(Coordinate source, Coordinate target) {
		// Determine whether the move is legal.
		Move move = null;
		// A depth of 2 is necessary to evaluate checkmate/stalemate for each move.
		PositionTree positionTree = PositionTree.grow(Position.current, 2);
		Collection<Move> legalMoves = positionTree.getLegalMoves();
		for (Move legalMove : legalMoves)
			if (legalMove.matches(source, target)) {
				move = legalMove;
				break;
			}
		boolean moveLegal = (move != null);
		if (moveLegal) {
			// Log the move.
			if (Position.current.whiteToMove)
				Log.appendToHistory("" + Position.current.moveNumber + '.');
			move.distinguishNotation(legalMoves);
			Log.appendToHistory(move.notation);
			// Check if the game has ended.
			boolean gameOver = false;
			PositionTree.Status gameStatus = positionTree.futureBranches.get(move).status;
			switch (gameStatus) {
			case CHECKMATE:
				Log.appendToHistory(Position.current.whiteToMove ? "1-0" : "0-1");
				gameOver = true;
				break;
			case STALEMATE:
				Log.appendToHistory("0.5-0.5");
				gameOver = true;
				break;
			default: break;
			}
			
			// Play the move.
			// Playing the move switches whose turn it is, so it is done after logging the move.
			Position.current.playMove(move);
			Board.update();
			
			// Instruct the bot to play a move if appropriate.
			if (!gameOver)
				playBotMoveIfAppropriate();
		}
	}
	
	// Plays a bot move if appropriate.
	public static void playBotMoveIfAppropriate() {
		if (!humanToMove())
			// A separate thread is created so the program does not freeze while the bot thinks.
			Platform.runLater(new Runnable() {
				@Override
				public void run () {
					Move botMove = new Bot().move();
					playMoveIfLegal(botMove);
				}
			});
	}
	
	// Determines whether it is a human or a bot to play next.
	private static boolean humanToMove() {
		return Position.current.whiteToMove ? WHITE_HUMAN : BLACK_HUMAN;
	}
}
