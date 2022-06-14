package application;

import java.util.Collection;

// Manages the flow of the chess game.
// Nate Hunter - 03/13/2022
public class Arbiter {
	// The source square for a pending move.
	private static Coordinate source = Coordinate.getInvalid();
	
	// Responds to the click of a square by selecting the square, moving a piece,
	// or doing nothing (depending on the square and game state).
	public static void processClick(Coordinate location) {
		boolean pieceAlreadySelected = source.inbounds();
		if (pieceAlreadySelected) {
			// The click corresponds to the intended target of a move.
			Coordinate target = location;
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
				PositionTree.Status gameStatus = positionTree.futureBranches.get(move).status;
				switch (gameStatus) {
				case CHECKMATE:
					Log.appendToHistory(Position.current.whiteToMove ? "1-0" : "0-1");
					break;
				case STALEMATE:
					Log.appendToHistory("0.5-0.5");
					break;
				default: break;
				}
				
				// Play the move.
				// Playing the move switches whose turn it is, so it is done after logging the move.
				Position.current.playMove(move);
				Board.update();
			}
			
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
}
