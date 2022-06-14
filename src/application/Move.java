package application;

import java.util.Collection;

// A single move of a chess piece.
// Nate Hunter - 04/03/2022
public class Move {
	// Move types to account for particular special cases.
	public static enum Type {
		NORMAL,
		CASTLE,
		PROMOTION,
		EN_PASSANT,
		TWO_SQUARE_PAWN
	}
	
	// The coordinates from which and to which the piece is moving.
	public Coordinate source, target;
	// The move type (for special cases).
	public Type type;
	// The notation of the move.
	public String notation;
	
	// Creates a move.
	public Move(Coordinate source, char sourcePiece, Coordinate target, char targetPiece, Type type) {
		this.source = source.copy();
		this.target = target.copy();
		this.type = type;
		notate(sourcePiece, targetPiece);
	}
	
	// Determines whether a move matches the coordinates supplied.
	public boolean matches(Coordinate source, Coordinate target) {
		return this.source.equals(source) && this.target.equals(target);
	}
	// Determines whether a move matches the coordinates of the move supplied.
	public boolean matches(Move move) {
		return move.source.equals(source) && move.target.equals(target);
	}
	
	// Notates a move.
	// Information on check, checkmate, and whether different moves have the same notation
	// is typically unavailable when this method is called; it must be added in later.
	private void notate(char sourcePiece, char targetPiece) {
		notation = "";
		
		// Castling is a special case for notation.
		if (type == Type.CASTLE) {
			boolean kingside = (target.file > source.file);
			notation += kingside ? "O-O" : "O-O-O";
		}
		else {
			// Notate the source piece if applicable.
			boolean pawnMove = (Piece.type(sourcePiece) == Piece.Type.PAWN);
			if (!pawnMove)
				notation += Character.toUpperCase(sourcePiece);
					
			// Notate the capture if applicable.
			boolean capture = (!Piece.isEmpty(targetPiece) || type == Type.EN_PASSANT);
			if (capture) {
				if (pawnMove)
					notation += notateFile(source.file);
				notation += 'x';
			}
			
			// Notate the target square.
			notation += notateFile(target.file);
			notation += notateRank(target.rank);
			
			// Notate the promotion if applicable.
			if (type == Type.PROMOTION)
				// All pawns promote to queen for simplicity.
				notation += "=Q";
		}
	}
	
	// Notates a rank.
	// Program rank 0 = notation rank 1; program rank 7 = notation rank 8.
	private static char notateRank(int rank) {
		return (char)('1' + rank);
	}
	// Notates a file.
	// Program file 0 = notation file 'a'; program file 7 = notation file 'h'.
	private static char notateFile(int file) {
		return (char)('a' + file);
	}
	
	// Updates the notation if needed to distinguish the move from others with the same notation.
	// This is done separately from regular notation because other moves and more computation are needed.
	public void distinguishNotation(Collection<Move> allMoves) {
		// Determine whether other moves exist with the same notation (and source rank or file).
		boolean notationUnique = true;
		boolean sourceFileUniqueForNotation = true;
		boolean sourceRankUniqueForNotation = true;
		for (Move move : allMoves)
			if (notation.equals(move.notation)) {
				boolean sameMoveAsSelf = matches(move);
				if (!sameMoveAsSelf) {
					notationUnique = false;
					if (source.file == move.source.file)
						sourceFileUniqueForNotation = false;
					if (source.rank == move.source.rank)
						sourceRankUniqueForNotation = false;
				}
			}
		
		// Update the notation as needed.
		if (!notationUnique) {
			String sourceNotation = "";
			if (sourceFileUniqueForNotation)
				sourceNotation += notateFile(source.file);
			else {
				if (sourceRankUniqueForNotation)
					sourceNotation += notateRank(source.rank);
				else {
					sourceNotation += notateFile(source.file);
					sourceNotation += notateRank(source.rank);
				}
			}
			int sourceNotationIndex = 1;
			notation = notation.substring(0, sourceNotationIndex) + sourceNotation + notation.substring(sourceNotationIndex);
		}
	}
}
