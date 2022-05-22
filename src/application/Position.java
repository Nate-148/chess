package application;

import java.util.ArrayList;
import java.util.Arrays;

// A position on the chess board.
// Nate Hunter - 03/13/2022
public class Position {	
	// The current position active on the chess board.
	public static Position current = initialPosition();

	// An invalid coordinate, used for disabling moves, etc.
	public static final int INVALID_SQUARE = -1;
	
	// Whether the position is valid.
	// This can be set to false for any position resulting from an illegal move.
	public boolean valid;
	// The material value of the position.
	// Positive means white is up on material; negative means black is up on material.
	// Storing the material value with the position improves computational efficiency, as
	// evaluating the next position after a move does not require recounting every piece.
	public int materialValue;
	// Whether it is white's turn or black's turn.
	public boolean whiteToMove;
	
	// An 8x8 grid of the pieces on the board.
	private char[][] pieces = new char[8][8];

	// Tracking king positions is useful for quickly computing checks and invalid positions.
	// The rank of the white king.
	private int whiteKingRank;
	// The file of the white king.
	private int whiteKingFile;
	// The rank of the black king.
	private int blackKingRank;
	// The file of the black king.
	private int blackKingFile;
	// Tracking whether the kings and rooks have moved is necessary for castling rules.
	// Whether the white king has moved.
	private boolean whiteKingHasMoved;
	// Whether the white A-file rook has moved.
	private boolean whiteARookHasMoved;
	// Whether the white H-file rook has moved.
	private boolean whiteHRookHasMoved;
	// Whether the black king has moved.
	private boolean blackKingHasMoved;
	// Whether the black A-file rook has moved.
	private boolean blackARookHasMoved;
	// Whether the black H-file rook has moved.
	private boolean blackHRookHasMoved;
	// Whether the previous move was castling.
	private boolean justCastled;
	// The file in which an en passant move would be valid (after a pawn moves two squares).
	private int enPassantFile;
	
	// Returns the initial position of a standard chess game.
	public static Position initialPosition() {
		Position startPosition = new Position();
		// Note: the top row of capital letters represents the white pieces.
		final char[][] INITIAL_SETUP = {
			{'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'},
			{'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'},
			{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
			{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
			{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
			{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '},
			{'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'},
			{'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'}};
		startPosition.pieces = INITIAL_SETUP;
		startPosition.valid = true;
		// The white and black pieces are even, thus canceling out to 0.
		startPosition.materialValue = 0;
		startPosition.whiteToMove = true;
		startPosition.whiteKingRank = 0;
		startPosition.whiteKingFile = 4;
		startPosition.blackKingRank = 7;
		startPosition.blackKingFile = 4;
		startPosition.whiteKingHasMoved = false;
		startPosition.whiteARookHasMoved = false;
		startPosition.whiteHRookHasMoved = false;
		startPosition.blackKingHasMoved = false;
		startPosition.blackARookHasMoved = false;
		startPosition.blackHRookHasMoved = false;
		startPosition.justCastled = false;
		startPosition.enPassantFile = INVALID_SQUARE;
		return startPosition;
	}
	
	// Creates a deep copy of the position.
	public Position copy() {
		Position position = new Position();
		for (int rank = 0; rank < 8; rank++)
			position.pieces[rank] = Arrays.copyOf(pieces[rank], 8);
		position.valid = valid;
		position.materialValue = materialValue;
		position.whiteToMove = whiteToMove;
		position.whiteKingRank = whiteKingRank;
		position.whiteKingFile = whiteKingFile;
		position.blackKingRank = blackKingRank;
		position.blackKingFile = blackKingFile;
		position.whiteKingHasMoved = whiteKingHasMoved;
		position.whiteARookHasMoved = whiteARookHasMoved;
		position.whiteHRookHasMoved = whiteHRookHasMoved;
		position.blackKingHasMoved = blackKingHasMoved;
		position.blackARookHasMoved = blackARookHasMoved;
		position.blackHRookHasMoved = blackHRookHasMoved;
		position.justCastled = justCastled;
		position.enPassantFile = enPassantFile;
		return position;
	}
	
	// Gets the piece in the specified square.
	public char getPiece(int rank, int file) {
		return pieces[rank][file];
	}
	
	// Sets the specified square to the specified piece.
	public void setPiece(int rank, int file, char piece) {
		pieces[rank][file] = piece;
	}
	
	// Moves the piece to the target square and updates the position state.
	public void movePiece(Move move) {
		// Get the piece to move.
		char piece = getPiece(move.sourceRank, move.sourceFile);
		
		// Handle special cases, including castling, promotion, and en passant.
		switch (move.type) {
		case CASTLE:
			// Move the rook to the other side of the king, as the given move
			// coordinates apply to the king only.
			int rookSourceRank = move.sourceRank;
			boolean castlingKingside = (move.targetFile > move.sourceFile);
			int rookSourceFile = castlingKingside ? 7 : 0;
			int rookTargetRank = rookSourceRank;
			int rookTargetFile = castlingKingside ? 5 : 3;
			char rook = getPiece(rookSourceRank, rookSourceFile);
			setPiece(rookTargetRank, rookTargetFile, rook);
			setPiece(rookSourceRank, rookSourceFile, Piece.EMPTY);
			break;
		case PROMOTION:
			// All pawns promote to queens for simplicity.
			char pawn = piece;
			char queen = Piece.promoteToQueen(pawn);
			materialValue += Piece.materialValue(queen);
			materialValue -= Piece.materialValue(pawn);
			piece = queen;
			break;
		case EN_PASSANT:
			// Capture the opposing pawn en passant.
			int opposingPawnRank = move.sourceRank;
			int opposingPawnFile = enPassantFile;
			char opposingPawn = getPiece(opposingPawnRank, opposingPawnFile);
			materialValue -= Piece.materialValue(opposingPawn);
			setPiece(opposingPawnRank, opposingPawnFile, Piece.EMPTY);
			break;
		default: break;
		}
		
		// Move the piece to the target square and update the material value.
		char capturedPiece = getPiece(move.targetRank, move.targetFile);
		materialValue -= Piece.materialValue(capturedPiece);
		setPiece(move.targetRank, move.targetFile, piece);
		setPiece(move.sourceRank, move.sourceFile, Piece.EMPTY);
		
		// Update the position state.
		if (move.type == Move.Type.TWO_SQUARE_PAWN)
			enPassantFile = move.targetFile;
		else enPassantFile = INVALID_SQUARE;
		justCastled = (move.type == Move.Type.CASTLE);
		if (Piece.type(piece) == Piece.Type.KING) {
			if (whiteToMove) {
				whiteKingHasMoved = true;
				whiteKingRank = move.targetRank;
				whiteKingFile = move.targetFile;
			}
			else {
				blackKingHasMoved = true;
				blackKingRank = move.targetRank;
				blackKingFile = move.targetFile;
			}
		}
		else if (Piece.type(piece) == Piece.Type.ROOK) {
			if (move.sourceFile == 0) {
				if (whiteToMove)
					whiteARookHasMoved = true;
				else blackARookHasMoved = true;
			}
			else if (move.sourceFile == 7) {
				if (whiteToMove)
					whiteHRookHasMoved = true;
				else blackHRookHasMoved = true;
			}
		}
		whiteToMove = !whiteToMove;
	}
	
	// Creates a new position representing the game after playing the specified move.
	public Position nextPosition(Move move) {
		Position position = copy();
		position.movePiece(move);
		return position;
	}
	
	// Determines whether the active player is in check.
	// This requires computing an entire move list, so for efficiency at deeper levels of recursion,
	// clever implementation of computeMovesAndValidateSelf is recommended instead.
	public boolean inCheck() {
		// Create a new position where it is the opposing player's turn (i.e. pass the move).
		Position opposingPosition = copy();
		opposingPosition.whiteToMove = !whiteToMove;
		// Having passed the move, it is best to reset the position state to default values.
		// This ensures that the computed validity of the new position is correct.
		opposingPosition.justCastled = false;
		opposingPosition.enPassantFile = INVALID_SQUARE;
		
		// Determine whether the opponent can capture the active king.
		opposingPosition.computeMovesAndValidateSelf();
		// This position should only be invalid if any opponent moves capture the active king.
		boolean activeKingIsCapturable = !opposingPosition.valid;
		return activeKingIsCapturable;
	}
	
	// Computes a list of all possible moves for the position and validates the current position.
	// The position is considered invalid if any moves capture the opposing king.
	// If the opposing player just castled, then any moves targeting the opposing king's previous
	// square or passed-through square also invalidate the position.
	//
	// For maximum efficiency, this method does not account for checks from the opposing player
	// when computing the possible moves. Instead, it determines whether the current position
	// is valid. For example, suppose white wants to play a move to transition from position 1
	// to position 2, but the move leaves white's king in check. When this method is called on
	// position 1, the move is added and the check is not detected. When this method is called
	// on position 2, it detects a move from black that captures white's king, indicating that
	// position 2 is invalid. After this function returns, the calling code should check that
	// position 2 is invalid and remove the corresponding invalid move from position 1.
	//
	// The reason for this complex logic is that computing whether each move leaves a player in
	// check requires computing an entire move list for the opposing player, which is slow.
	// This alternative builds the check computations into the recursive structure of move
	// calculations, which could potentially give a bot time to compute an extra layer of depth.
	public ArrayList<Move> computeMovesAndValidateSelf() {
		// Compute the list of possible moves.
		ArrayList<Move> moves = new ArrayList<Move>();
		for (int rank = 0; rank < 8; rank++)
			for (int file = 0; file < 8; file++) {
				// Add moves if the source square contains an active piece.
				char piece = getPiece(rank, file);
				if (Piece.isActive(piece, whiteToMove)) {
					switch (Piece.type(piece)) {
					case KING:
						addShortRangeMoves(moves, Move.ROYALTY_MOVE_DIRECTIONS, rank, file);
						addCastlingMoves(moves);
						break;
					case QUEEN:
						addLongRangeMoves(moves, Move.ROYALTY_MOVE_DIRECTIONS, rank, file);
						break;
					case ROOK:
						addLongRangeMoves(moves, Move.ROOK_MOVE_DIRECTIONS, rank, file);
						break;
					case BISHOP:
						addLongRangeMoves(moves, Move.BISHOP_MOVE_DIRECTIONS, rank, file);
						break;
					case KNIGHT:
						addShortRangeMoves(moves, Move.KNIGHT_MOVE_DIRECTIONS, rank, file);
						break;
					case PAWN:
						addPawnMoves(moves, rank, file);
						break;
					default: break;
					}
				}
			}
		
		// Validate the position.
		int opposingKingRank, opposingKingFile;
		if (whiteToMove) {
			opposingKingRank = blackKingRank;
			opposingKingFile = blackKingFile;
		} else {
			opposingKingRank = whiteKingRank;
			opposingKingFile = whiteKingFile;
		}
		if (justCastled) {
			// If any moves target any squares between the enemy king's initial position and
			// the enemy king's current position, then the opposing player castled illegally.
			int opposingKingInitialFile = 4;
			int startFile = Math.min(opposingKingFile, opposingKingInitialFile);
			int endFile = Math.max(opposingKingFile, opposingKingInitialFile);
			for (int forbiddenFile = startFile; forbiddenFile <= endFile; forbiddenFile++)
				for (Move move : moves)
					if (move.matchesTarget(opposingKingRank, forbiddenFile)) {
						valid = false;
						return null;
					}
		} else {
			// The enemy king should not be capturable.
			for (Move move : moves)
				if (move.matchesTarget(opposingKingRank, opposingKingFile)) {
					valid = false;
					return null;
				}
		}
		return moves;
	}
	
	// Adds short-range moves to the list of possible moves. Used for kings and knights.
	private void addShortRangeMoves(ArrayList<Move> moves, int[][] moveDirections,
			int sourceRank, int sourceFile) {
		for (int[] direction : moveDirections) {
			// Compute the target square from the source square and the move direction.
			int targetRank = sourceRank + Move.rankDelta(direction);
			int targetFile = sourceFile + Move.fileDelta(direction);
			// Add the move if the square is on the board and not occupied by a same-color piece.
			if (Move.inbounds(targetRank, targetFile)) {
				char targetPiece = getPiece(targetRank, targetFile);
				if (!Piece.isActive(targetPiece, whiteToMove))
					moves.add(new Move(sourceRank, sourceFile, targetRank, targetFile));
			}
		}
	}
	
	// Adds long-range moves to the list of possible moves. Used for queens, rooks, and knights.
	private void addLongRangeMoves(ArrayList<Move> moves, int[][] moveDirections,
			int sourceRank, int sourceFile) {
		for (int[] direction : moveDirections) {
			boolean directionBlocked = false;
			for (int squareCount = 1; !directionBlocked; squareCount++) {
				directionBlocked = true;
				// Compute the target square from the source square and the move direction.
				int targetRank = sourceRank + Move.rankDelta(direction) * squareCount;
				int targetFile = sourceFile + Move.fileDelta(direction) * squareCount;
				// Add the move if the square is on the board and not occupied by a same-color piece.
				if (Move.inbounds(targetRank, targetFile)) {
					char targetPiece = getPiece(targetRank, targetFile);
					if (!Piece.isActive(targetPiece, whiteToMove)) {
						moves.add(new Move(sourceRank, sourceFile, targetRank, targetFile));
						// Proceed to the next square in the move direction if the square is empty.
						if (Piece.isEmpty(targetPiece))
							directionBlocked = false;
					}
				}
			}
		}
	}
	
	// Adds pawn moves to the list of possible moves.
	private void addPawnMoves(ArrayList<Move> moves, int sourceRank, int sourceFile) {		
		// Add straight non-capturing moves.
		int forwardRankDelta = whiteToMove ? 1 : -1;
		int targetRank = sourceRank + forwardRankDelta;
		// Determine whether the pawn is promoting.
		int promotionRank = whiteToMove ? 7 : 0;
		boolean promoting = (targetRank == promotionRank);
		Move.Type moveType = promoting ? Move.Type.PROMOTION : Move.Type.NORMAL;
		// Add the move if the square is empty.
		int targetFile = sourceFile;
		char targetPiece = getPiece(targetRank, targetFile);
		if (Piece.isEmpty(targetPiece)) {
			// Add the single-rank move.
			moves.add(new Move(sourceRank, sourceFile, targetRank, targetFile, moveType));
			// Add the initial double-rank move if applicable.
			int startingRank = whiteToMove ? 1 : 6;
			if (sourceRank == startingRank) {
				int longTargetRank = sourceRank + 2 * forwardRankDelta;
				targetPiece = getPiece(longTargetRank, targetFile);
				if (Piece.isEmpty(targetPiece))
					moves.add(new Move(sourceRank, sourceFile, longTargetRank, targetFile, Move.Type.TWO_SQUARE_PAWN));
			}
		}
		
		// Add diagonal capturing moves.
		final int[] CAPTURE_FILE_DELTAS = {-1, 1};
		for (int fileDelta : CAPTURE_FILE_DELTAS) {
			targetFile = sourceFile + fileDelta;
			// Add the capture if the square is on the board and occupied by an opposite-color piece.
			if (Move.inbounds(targetRank, targetFile)) {
				targetPiece = getPiece(targetRank, targetFile);
				if (Piece.isOpposing(targetPiece, whiteToMove))
					moves.add(new Move(sourceRank, sourceFile, targetRank, targetFile, moveType));
				// Add an en passant capture if the target square is correct (and the file is valid).
				else if (targetFile == enPassantFile) {
					int enPassantRank = whiteToMove ? 5 : 2;
					if (targetRank == enPassantRank)
						moves.add(new Move(sourceRank, sourceFile, targetRank, targetFile, Move.Type.EN_PASSANT));
				}
			}
		}
	}
	
	// Adds castling moves to the list of possible moves.
	private void addCastlingMoves(ArrayList<Move> moves) {
		if (whiteToMove) {
			if (!whiteKingHasMoved) {
				if (!whiteARookHasMoved)
					addCastlingMove(moves, false);
				if (!whiteHRookHasMoved)
					addCastlingMove(moves, true);
			}
		} else {
			if (!blackKingHasMoved) {
				if (!blackARookHasMoved)
					addCastlingMove(moves, false);
				if (!blackHRookHasMoved)
					addCastlingMove(moves, true);
			}
		}
	}
	// Adds the specified castling move to the list of possible moves if the path is clear.
	private void addCastlingMove(ArrayList<Move> moves, boolean castlingKingside) {
		// Determine whether the path is clear.
		// For efficiency, checks are ignored here; resulting invalid positions should be checked for.
		boolean pathIsClear = true;
		int backRank = whiteToMove ? 0 : 7;
		int startFile = castlingKingside ? 5 : 1;
		int endFile = castlingKingside ? 6 : 3;
		for (int file = startFile; file <= endFile; file++) {
			char piece = getPiece(backRank, file);
			if (!Piece.isEmpty(piece)) {
				pathIsClear = false;
				break;
			}
		}
		if (pathIsClear) {
			// Add the castling move.
			int sourceFile = 4;
			int targetFile = castlingKingside ? 6 : 2;
			moves.add(new Move(backRank, sourceFile, backRank, targetFile, Move.Type.CASTLE));
		}
	}
	
	// Prints the position out in the console.
	public void print() {print(true);}
	public void print(boolean whitePerspective) {
		for (int row = 0; row < 8; row++) {
			String lineToPrint = "|";
			// From white's perspective, the first rank is at the bottom, not the top.
			int rank = whitePerspective ? 7 - row : row;
			for (int column = 0; column < 8; column++) {
				// From black's perspective, the A file is on the right, not the left.
				int file = whitePerspective ? column : 7 - column;
				lineToPrint += "" + pieces[rank][file] + '|';
			}
			System.out.println(lineToPrint);
		}
	}
}
