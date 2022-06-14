package application;

import java.util.*;

// A position on the chess board.
// Nate Hunter - 03/13/2022
public class Position {	
	// The current position active on the chess board.
	public static Position current = initialPosition();

	// The possible moves for the active player. Checks are ignored for efficiency (see computeAllMoves).
	public List<Move> moves;
	// The material value of the position.
	// Positive means white is up on material; negative means black is up on material.
	// Storing the material value with the position improves computational efficiency,
	// as evaluating the next position after a move does not require recounting every piece.
	public int materialValue;
	// The move number (increments whenever black's turn finishes).
	public int moveNumber;
	// Whether it is white's turn or black's turn.
	public boolean whiteToMove;
	
	// An 8x8 grid of the pieces on the board.
	private char[][] pieces = new char[8][8];
	// The pieces attacking/defending each square of the chess board. For example,
	// if attackers[3][1] = {'P', 'r'}, then b4 is attacked/defended by a white pawn and a black rook.
	// The term "attackers" was chosen since it is typically more accurate,
	// but these lists also include pieces defending other pieces of the same color.
	// Arrays of typed lists cannot be created in Java, so this workaround is used.
	@SuppressWarnings("unchecked")
	private List<Character>[][] attackers = (List<Character>[][]) new List[8][8];

	// Tracking king locations is useful for quickly computing checks and invalid positions.
	// The location of the white king.
	private Coordinate whiteKingLocation;
	// Tracking whether the kings and rooks have moved is necessary for castling rules.
	// Whether the white king has moved.
	private boolean whiteKingHasMoved;
	// Whether the white A-file rook has moved.
	private boolean whiteARookHasMoved;
	// Whether the white H-file rook has moved.
	private boolean whiteHRookHasMoved;
	
	// The location of the black king.
	private Coordinate blackKingLocation;
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
		// The white and black pieces are even, thus canceling out to 0.
		startPosition.materialValue = 0;
		startPosition.moveNumber = 1;
		startPosition.whiteToMove = true;
		startPosition.whiteKingLocation = new Coordinate(0, 4);
		startPosition.whiteKingHasMoved = false;
		startPosition.whiteARookHasMoved = false;
		startPosition.whiteHRookHasMoved = false;
		startPosition.blackKingLocation = new Coordinate(7, 4);
		startPosition.blackKingHasMoved = false;
		startPosition.blackARookHasMoved = false;
		startPosition.blackHRookHasMoved = false;
		startPosition.justCastled = false;
		startPosition.enPassantFile = Coordinate.INVALID;
		startPosition.clearAllMoves();
		return startPosition;
	}
	
	// Creates a deep copy of the position.
	// Moves and attackers are initialized empty since they should be recalculated for each position.
	public Position copy() {
		Position position = new Position();
		for (int rank = 0; rank < 8; rank++)
			for (int file = 0; file < 8; file++)
				position.pieces[rank][file] = pieces[rank][file];
		position.materialValue = materialValue;
		position.moveNumber = moveNumber;
		position.whiteToMove = whiteToMove;
		position.whiteKingLocation = whiteKingLocation;
		position.whiteKingHasMoved = whiteKingHasMoved;
		position.whiteARookHasMoved = whiteARookHasMoved;
		position.whiteHRookHasMoved = whiteHRookHasMoved;
		position.blackKingLocation = blackKingLocation;
		position.blackKingHasMoved = blackKingHasMoved;
		position.blackARookHasMoved = blackARookHasMoved;
		position.blackHRookHasMoved = blackHRookHasMoved;
		position.justCastled = justCastled;
		position.enPassantFile = enPassantFile;
		position.clearAllMoves();
		return position;
	}
	
	// Initializes/refreshes the moves and attackers to empty lists.
	private void clearAllMoves() {
		moves = new ArrayList<Move>();
		for (int rank = 0; rank < 8; rank++)
			for (int file = 0; file < 8; file++)
				attackers[rank][file] = new ArrayList<Character>();
	}
	
	// Gets the pieces attacking the specified square.
	public List<Character> getAttackers(Coordinate coordinate) {
		return attackers[coordinate.rank][coordinate.file];
	}
	
	// Gets the piece in the specified square.
	public char getPiece(Coordinate coordinate) {
		return pieces[coordinate.rank][coordinate.file];
	}
	
	// Sets the specified square to the specified piece.
	public void setPiece(Coordinate coordinate, char piece) {
		pieces[coordinate.rank][coordinate.file] = piece;
	}
	
	// Plays the move specified and updates the position.
	public void playMove(Move move) {
		// Get the piece to move.
		char piece = getPiece(move.source);
		
		// Handle special cases, including castling, promotion, and en passant.
		switch (move.type) {
		case CASTLE:
			// Move the rook to the other side of the king.
			// Moving the king itself is handled as the standard case.
			boolean castlingKingside = (move.target.file > move.source.file);
			int rookSourceFile = castlingKingside ? 7 : 0;
			Coordinate rookSource = new Coordinate(move.source.rank, rookSourceFile);
			char rook = getPiece(rookSource);
			int rookTargetFile = castlingKingside ? 5 : 3;
			Coordinate rookTarget = new Coordinate(move.target.rank, rookTargetFile);
			setPiece(rookTarget, rook);
			setPiece(rookSource, Piece.EMPTY);
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
			Coordinate opposingPawnLocation = new Coordinate(move.source.rank, enPassantFile);
			char opposingPawn = getPiece(opposingPawnLocation);
			materialValue -= Piece.materialValue(opposingPawn);
			setPiece(opposingPawnLocation, Piece.EMPTY);
			break;
		default: break;
		}
		
		// Move the piece to the target square and update the material value.
		char capturedPiece = getPiece(move.target);
		materialValue -= Piece.materialValue(capturedPiece);
		setPiece(move.target, piece);
		setPiece(move.source, Piece.EMPTY);
		
		// Update the position state.
		if (move.type == Move.Type.TWO_SQUARE_PAWN)
			enPassantFile = move.target.file;
		else enPassantFile = Coordinate.INVALID;
		justCastled = (move.type == Move.Type.CASTLE);
		if (Piece.type(piece) == Piece.Type.KING) {
			if (whiteToMove) {
				whiteKingHasMoved = true;
				whiteKingLocation = move.target;
			}
			else {
				blackKingHasMoved = true;
				blackKingLocation = move.target;
			}
		}
		else if (Piece.type(piece) == Piece.Type.ROOK) {
			if (move.source.file == 0) {
				if (whiteToMove)
					whiteARookHasMoved = true;
				else blackARookHasMoved = true;
			}
			else if (move.source.file == 7) {
				if (whiteToMove)
					whiteHRookHasMoved = true;
				else blackHRookHasMoved = true;
			}
		}
		whiteToMove = !whiteToMove;
		if (whiteToMove)
			moveNumber++;
		// The old moves and attackers are no longer valid.
		clearAllMoves();
	}
	
	// Creates a new position representing the game after playing the specified move.
	public Position nextPosition(Move move) {
		Position position = copy();
		position.playMove(move);
		return position;
	}
	
	// Computes the following:
	//		a) moves: A list of all possible moves for the position (ignoring checks).
	// 		b) attackers: Lists of all pieces that attack/defend each square.
	// This method serves as the computational core of the entire program.
	//
	// Attackers are computed alongside moves since both require similar calculations.
	// They are useful to identify check, evaluate moves, and choose moves to explore further.
	//
	// For maximum efficiency, this method does not account for checks from the opposing player
	// when computing the possible moves. Instead, it allows calling code to check whether
	// resulting positions are valid. For example, suppose white wants to play a move to transition
	// from position 1 to position 2, but the move leaves white's king in check.
	// When this method is called on position 1, the move is added and the check is not detected.
	// When this method is called on position 2, it computes that black is attacking white's king 
	// on black's turn, so position2.valid() will return false. Thus, the calling code should check
	// that position 2 is invalid and ignore the corresponding illegal move from position 1.
	//
	// The reason for requiring this extra step is that computing whether each move leaves a player
	// in check requires computing all the moves for the subsequent position, which is slow.
	// This alternative builds the check computations into the recursive structure of move
	// calculations, thus giving a bot time to compute an extra layer of depth.
	public void computeAllMoves() {
		for (int rank = 0; rank < 8; rank++)
			for (int file = 0; file < 8; file++) {
				Coordinate source = new Coordinate(rank, file);
				char piece = getPiece(source);
				switch (Piece.type(piece)) {
				case KING:
					computeShortRangeMoves(Coordinate.ROYALTY_MOVE_DIRECTIONS, source, piece);
					break;
				case QUEEN:
					computeLongRangeMoves(Coordinate.ROYALTY_MOVE_DIRECTIONS, source, piece);
					break;
				case ROOK:
					computeLongRangeMoves(Coordinate.ROOK_MOVE_DIRECTIONS, source, piece);
					break;
				case BISHOP:
					computeLongRangeMoves(Coordinate.BISHOP_MOVE_DIRECTIONS, source, piece);
					break;
				case KNIGHT:
					computeShortRangeMoves(Coordinate.KNIGHT_MOVE_DIRECTIONS, source, piece);
					break;
				case PAWN:
					computePawnMoves(source, piece);
					break;
				default: break;
				}
			}
		computeCastlingMoves();
	}
	
	// Computes short-range moves and attackers. Used for kings and knights.
	private void computeShortRangeMoves(Coordinate[] moveDirections, Coordinate source, char piece) {
		boolean sourceActive = Piece.isActive(piece, whiteToMove);
		for (Coordinate direction : moveDirections) {
			// Compute the target square from the source square and the move direction.
			Coordinate target = source.add(direction);
			
			// Add the attacker if the square is on the board.
			if (target.inbounds()) {
				addAttacker(target, piece);
				
				// Add the move for an active piece targeting an opposing piece or empty square.
				if (sourceActive && !Piece.isActive(getPiece(target), whiteToMove))
					addMove(source, target, Move.Type.NORMAL);
			}
		}
	}
	
	// Computes long-range moves and attackers. Used for queens, rooks, and knights.
	private void computeLongRangeMoves(Coordinate[] moveDirections, Coordinate source, char piece) {
		boolean sourceActive = Piece.isActive(piece, whiteToMove);
		// Iterate through target squares outward in each direction until the direction is blocked.
		for (Coordinate direction : moveDirections) {
			boolean directionOpen = true;
			for (int squareCount = 1; directionOpen; squareCount++) {
				directionOpen = false;
				
				// Compute the target square from the source square and the move direction.
				Coordinate target = source.add(direction.scale(squareCount));
				
				// Add the attacker if the square is on the board.
				if (target.inbounds()) {
					addAttacker(target, piece);
					
					// Add the move for an active piece targeting an opposing piece or empty square.
					char targetPiece = getPiece(target);
					if (sourceActive && !Piece.isActive(targetPiece, whiteToMove))
						addMove(source, target, Move.Type.NORMAL);
					
					// Proceed to the next square in the move direction if the square is empty.
					if (Piece.isEmpty(targetPiece))
						directionOpen = true;
				}
			}
		}
	}
	
	// Computes pawn moves and attackers.
	private void computePawnMoves(Coordinate source, char pawn) {
		// Add straight non-capturing moves.
		Coordinate nonCaptureDirection;
		boolean pawnWhite = Piece.isWhite(pawn);
		if (pawnWhite)
			nonCaptureDirection = Coordinate.WHITE_PAWN_NON_CAPTURE_DIRECTION;
		else nonCaptureDirection = Coordinate.BLACK_PAWN_NON_CAPTURE_DIRECTION;
		Coordinate target = source.add(nonCaptureDirection);
		
		// Determine whether the pawn is promoting.
		int promotionRank = pawnWhite ? 7 : 0;
		boolean promoting = (target.rank == promotionRank);
		Move.Type moveType = promoting ? Move.Type.PROMOTION : Move.Type.NORMAL;
		
		// Add the move for an active pawn targeting an empty square.
		boolean sourceActive = Piece.isActive(pawn, whiteToMove);
		if (sourceActive)
			if (Piece.isEmpty(getPiece(target))) {
				// Add the single-rank move.
				addMove(source, target, moveType);
				
				// Add the initial double-rank move if applicable.
				int startingRank = pawnWhite ? 1 : 6;
				if (source.rank == startingRank) {
					Coordinate longTarget = source.add(nonCaptureDirection.scale(2));
					if (Piece.isEmpty(getPiece(longTarget)))
						addMove(source, longTarget, Move.Type.TWO_SQUARE_PAWN);
				}
			}
		
		// Add diagonal capturing moves.
		Coordinate[] captureDirections;
		if (pawnWhite)
			captureDirections = Coordinate.WHITE_PAWN_CAPTURE_DIRECTIONS;
		else captureDirections = Coordinate.BLACK_PAWN_CAPTURE_DIRECTIONS;
		for (Coordinate direction : captureDirections) {
			target = source.add(direction);
			
			// Add the attacker if the square is on the board.
			if (target.inbounds()) {
				addAttacker(target, pawn);
				
				// Add the capturing move for an active pawn targeting an opposing piece.
				if (sourceActive) {
					if (Piece.isOpposing(getPiece(target), whiteToMove))
						addMove(source, target, moveType);
					
					// Add an en passant capture if the target square is correct (and the file is valid).
					int enPassantRank = pawnWhite ? 5 : 2;
					Coordinate enPassantTarget = new Coordinate(enPassantRank, enPassantFile);
					if (target.equals(enPassantTarget))
						addMove(source, target, Move.Type.EN_PASSANT);
				}
			}
		}
	}
	
	// Computes castling moves.
	private void computeCastlingMoves() {
		if (whiteToMove) {
			if (!whiteKingHasMoved) {
				if (!whiteARookHasMoved)
					addCastlingMove(false);
				if (!whiteHRookHasMoved)
					addCastlingMove(true);
			}
		} else {
			if (!blackKingHasMoved) {
				if (!blackARookHasMoved)
					addCastlingMove(false);
				if (!blackHRookHasMoved)
					addCastlingMove(true);
			}
		}
	}
	// Adds the specified castling move to the list of possible moves if the path is clear.
	private void addCastlingMove(boolean castlingKingside) {
		// Determine whether the path is clear (i.e. the squares between the king and rook).
		// For efficiency, checks are ignored here; resulting invalid positions should be checked for.
		boolean pathClear = true;
		int backRank = whiteToMove ? 0 : 7;
		int startFile = castlingKingside ? 5 : 1;
		int endFile = castlingKingside ? 6 : 3;
		for (int file = startFile; file <= endFile; file++) {
			Coordinate squareInPath = new Coordinate(backRank, file);
			if (!Piece.isEmpty(getPiece(squareInPath))) {
				pathClear = false;
				break;
			}
		}
		if (pathClear) {
			// Add the castling move.
			Coordinate activeKingLocation = whiteToMove ? whiteKingLocation : blackKingLocation;
			int targetFile = castlingKingside ? 6 : 2;
			Coordinate target = new Coordinate(backRank, targetFile);
			addMove(activeKingLocation, target, Move.Type.CASTLE);
		}
	}
	
	// Adds a move to the list of possible moves.
	private void addMove(Coordinate source, Coordinate target, Move.Type type) {
		moves.add(new Move(source, getPiece(source), target, getPiece(target), type));
	}
	
	// Adds an attacker for the specified target square.
	private void addAttacker(Coordinate target, char attackingPiece) {
		getAttackers(target).add(attackingPiece);
	}
	
	// Determines whether the specified player is attacking/defending the specified square.
	// This method assumes that attackers have already been computed.
	private boolean squareAttacked(Coordinate square, boolean activePlayer) {
		List<Character> attackers = getAttackers(square);
		for (char attacker : attackers)
			if (activePlayer && Piece.isActive(attacker, whiteToMove))
				return true;
			else if (!activePlayer && Piece.isOpposing(attacker, whiteToMove))
				return true;
		return false;
	}
	
	// Determines whether the active player is in check.
	// This method assumes that attackers have already been computed.
	public boolean inCheck() {
		Coordinate activeKingLocation = whiteToMove ? whiteKingLocation : blackKingLocation;
		boolean opposingPlayer = false;
		return squareAttacked(activeKingLocation, opposingPlayer);
	}
	
	// Determines whether the position is valid.
	// The position is considered invalid if the opposing king is capturable.
	// If the opposing player just castled, then any moves targeting the opposing king's
	// initial square or passed-through square also invalidate the position.
	// This method assumes that attackers have already been computed.
	public boolean valid() {
		// The enemy king should not be capturable.
		Coordinate opposingKingLocation = whiteToMove ? blackKingLocation : whiteKingLocation;
		boolean activePlayer = true;
		if (squareAttacked(opposingKingLocation, activePlayer))
			return false;
		
		// The enemy king should not have castled out of or through check.
		if (justCastled) {
			int opposingKingInitialFile = 4;
			Coordinate opposingKingInitialLocation = new Coordinate(opposingKingLocation.rank, opposingKingInitialFile);
			if (squareAttacked(opposingKingInitialLocation, activePlayer))
				return false;
			int opposingKingPassThroughFile = (opposingKingInitialFile + opposingKingLocation.file) / 2;
			Coordinate opposingKingPassThroughLocation = new Coordinate(opposingKingLocation.rank, opposingKingPassThroughFile);
			if (squareAttacked(opposingKingPassThroughLocation, activePlayer))
				return false;
		}
		return true;
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
