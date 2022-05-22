package application;

import java.util.ArrayList;

// Manages the flow of the chess game.
// Nate Hunter - 03/13/2022
public class Arbiter {
	// Whether or not a piece is selected.
	private static boolean pieceSelected = false;
	
	// The rank of the source square of a move.
	private static int sourceRank = Position.INVALID_SQUARE;
	// The file of the source square of a move.
	private static int sourceFile = Position.INVALID_SQUARE;
	
	// Responds to the click of a square by selecting the square, moving a piece,
	// or doing nothing (depending on the square and game state).
	public static void processClick(int rank, int file) {
		if (pieceSelected) {
			// Move the piece if the target square is a valid move.
			ArrayList<Move> validMoves = computeValidMoves();
			Move move = getMove(validMoves, sourceRank, sourceFile, rank, file);
			boolean moveValid = (move != null);
			if (moveValid)
				movePiece(move);
			else
				deselectSquare();
		}
		else {
			// Select the square clicked if it holds a piece of the active color.
			char pieceClicked = Position.current.getPiece(rank, file);
			if (Piece.isActive(pieceClicked, Position.current.whiteToMove))
				selectSquare(rank, file);
		}
	}
	
	// Computes the valid moves for the position.
	private static ArrayList<Move> computeValidMoves() {
		// Get the possible moves for the position.
		ArrayList<Move> validMoves = Position.current.computeMovesAndValidateSelf();
		
		// Remove any moves that are illegal (e.g. those that do not escape check).
		ArrayList<Move> invalidMoves = new ArrayList<Move>();
		for (Move move : validMoves) {
			Position nextPosition = Position.current.nextPosition(move);
			nextPosition.computeMovesAndValidateSelf();
			if (!nextPosition.valid)
				invalidMoves.add(move);
		}
		validMoves.removeAll(invalidMoves);
		return validMoves;
	}
	
	// Gets the specified move from a list of moves if it exists (returns null otherwise).
	private static Move getMove(ArrayList<Move> moves, int sourceRank, int sourceFile,
			int targetRank, int targetFile) {
		for (Move move : moves)
			if (move.matches(sourceRank, sourceFile, targetRank, targetFile))
				return move;
		return null;
	}
	
	// Move a piece.
	private static void movePiece(Move move) {
		Position.current.movePiece(move);
		Board.update();
		pieceSelected = false;
		sourceRank = Position.INVALID_SQUARE;
		sourceFile = Position.INVALID_SQUARE;
	}
	
	// Selects the specified square.
	// This indicates that the user intends to move the piece in that square.
	private static void selectSquare(int rank, int file) {
		Board.getSquare(rank, file).select();
		pieceSelected = true;
		sourceRank = rank;
		sourceFile = file;
	}
	
	// Deselects the selected square.
	private static void deselectSquare() {
		Board.getSquare(sourceRank, sourceFile).deselect();
		pieceSelected = false;
		sourceRank = Position.INVALID_SQUARE;
		sourceFile = Position.INVALID_SQUARE;
	}
}
